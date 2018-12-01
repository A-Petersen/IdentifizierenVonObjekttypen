import org.jzy3d.maths.Coord3d;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.Math.*;

public class Object {
    private char type;
    private char calculatedType;
    private Coord3d position;
    private int size;
    private List<List<Integer>> matrixList = new LinkedList<>();
    private List<Double> gradientsInXpos = new LinkedList<>();
    private List<Double> gradientsInXneg = new LinkedList<>();
    private List<Double> gradientsInYneg = new LinkedList<>();
    private List<Double> gradientsInYpos = new LinkedList<>();
    private double[] minMax;
    private List<Double> symmetricInX = new LinkedList<>();
    private List<Double> symmetricInY = new LinkedList<>();
    private boolean symmetricWeak = false;
    private boolean symmetricStrong = false;
    private boolean flat = false;
    private boolean canyon = false;
    private double height;
    private double inHeightRange;
    private boolean positiveVolume = false;

    private int[] monotonicMatrix = {0,0,0,0};

    /**
     * Constructor for an object.
     * Most methods from object will take action inside the constructor, to gather the needed data.
     * @param position  Coord3d - centre reference for the on´bject matrix
     * @param objectMatrixSize  Sets the size of X/2-Y/2
     * @param type  A or B object
     * @throws IOException
     */
    public Object(Coord3d position, int objectMatrixSize, char type) throws IOException {
        this.type = type;
        this.position = position;
        this.size = objectMatrixSize;
        this.inHeightRange = 0;
        buildMatrix();
        fillGradients();
        strictlyMonotonic();
        flat(0.01, 0.4, 3);
        symmetric(5, 0.3);
        fastSharp(5000);
        this.minMax = maxGradientChange(6.0);
        volume();
        this.height = getMax(matrixList).z - getMin(matrixList).z;
        System.out.println("Gradient differences Max: [" + minMax[0] + "] \t Min: [" + minMax[1] + "]\nFlat: [" + flat + "]");
        System.out.println("Canyon: [" + canyon + "]");
        System.out.println("Flat in %: [" + flat + "]");
        System.out.println("Weak Symetric: [" + symmetricWeak + "]" + "\nStrong Symetric: [" + symmetricStrong + "]");
        System.out.println("Correct Type: [" + calcRight() + "]" +
                "\n------------------------------------------------------------------------------------------\n");
    }

    /**
     * Builds the Matrix for the given object.
     * In detail, builds the matrix two times. First around the given object point, then searches for the highest point
     * inside this matrix (max 10 by 10 around the old centre point). Second builds a new matrix around the new centre point.
     * @throws IOException
     */
    public void buildMatrix() throws IOException {

        matrixList = Gatherer.getMatrix((int)position.x - size, (int)position.x + size, (int)position.y - size, (int)position.y + size, "Data/data.csv");
        Coord3d maxZ = getMax(matrixList);
        position.x = position.x - (matrixList.size()/2) + maxZ.x;
        position.y = position.y - (matrixList.get(0).size()/2) + maxZ.y;
        position.z = maxZ.z;
        System.out.println("New Max Coord_pos: [" + position + "]");
        matrixList = Gatherer.getMatrix((int)position.x - size, (int)position.x + size, (int)position.y - size, (int)position.y + size, "Data/data.csv");
    }

    /**
     * Fills the gradient lists (xPos, xNeg, yPos, yNeg) and formats them, so that every list is build from centre
     * to edge.
     */
    private void fillGradients() {
        List<Double> dummy = new LinkedList<>();
        Point startPointX = new Point((matrixList.size()/2), 0); // Start at P(mid X | 0) in the given matrix.

        for (int i = 0; i < matrixList.get(0).size() - 1; i++) {    // Iterate from P(mid X | 0) to P(mid X | max Y) in the given matrix.
            dummy.add(getGradient(matrixList, startPointX, 'y'));   // Calculate the gradient and add him.
            startPointX.moveOneY();
        }
        gradientsInXneg.addAll(dummy.subList(0, (int)getMax(matrixList).y ));       // Get the sublist P(mid X | 0) to P(mid X | mid Y)
        Collections.reverse(gradientsInXneg);                                         // Reverse the list, to P(mid X | mid Y) to P(mid X | 0)
        gradientsInXneg = gradientsInXneg.stream().map(x -> x * -1 ).collect(Collectors.toList());  // Change the sign.
        gradientsInXpos.addAll(dummy.subList((int)getMax(matrixList).y , dummy.size()));    // Get the sublist P(mid X | mid Y) to P(mid X | max Y)
        System.out.println("Gradienten in X ->: \t\t" + gradientsInXpos);
        System.out.println("Gradienten in <- X: \t\t" + gradientsInXneg);


        dummy.clear();
        Point startPointY = new Point(0, (matrixList.get(0).size()/2));
        for (int i = 0; i < matrixList.size() - 1; i++) {   // Iterate from P(0 | mid Y) to P(max X | mid Y) in the given matrix.
            dummy.add(getGradient(matrixList, startPointY, 'x'));   // Calculate the gradient and add him.
            startPointY.moveOneX();
        }
        gradientsInYpos.addAll(dummy.subList(0, (int)getMax(matrixList).x ));   // Get the sublist P(0 | mid Y) to P(mid X | mid Y)
        Collections.reverse(gradientsInYpos);                                     // Reverse the list, to P(mid X | mid Y) to P(0 | mid Y)
        gradientsInYpos = gradientsInYpos.stream().map(x -> x * -1 ).collect(Collectors.toList());  // Change the sign.
        gradientsInYneg.addAll(dummy.subList((int)getMax(matrixList).x , dummy.size()));    // Get the sublist P(mid X | mid Y) to P(max X | mid Y)
        System.out.println("Gradienten in Y -> (down): \t" + gradientsInYneg);
        System.out.println("Gradienten in <- Y (up): \t" + gradientsInYpos);

    }

    /**
     * Calculates the gradient from a given point in a given matrix to a parameterized direction.
     * @param mList Matrix area
     * @param a Point in matrix
     * @param direction Direction in the matrix
     * @return The gradient between the two points as double
     */
    private double getGradient(List<List<Integer>> mList, Point a, char direction) {
        if (direction == 'x') {
            return (mList.get(a.x + 1).get(a.y) - mList.get(a.x).get(a.y));
        }
        return (mList.get(a.x).get(a.y + 1) - mList.get(a.x).get(a.y));
    }

    /**
     * Counts the number of gradients, which behave strictly monotonic for each direction.
     * Saves the numbers into a reserved part of an Array [int[4] monotonicMatrix"].
     *
     * The direction order as follows:
     * monotonicMatrix[xPos][xNeg][yPos][yNeg]
     */
    private void strictlyMonotonic() {
        // x ->
        for (int i = 0; i < gradientsInXpos.size() -1; i++) {
            if (gradientsInXpos.get(i) < gradientsInXpos.get(i+1)) break;
            monotonicMatrix[0]++;
        }
        // <- x
        for (int i = 0; i < gradientsInXneg.size() -1; i++) {
            if (gradientsInXneg.get(i) < gradientsInXneg.get(i + 1)) break;
            monotonicMatrix[1]++;
        }
        // y ->
        for (int i = 0; i < gradientsInYpos.size() -1; i++) {
            if (gradientsInYpos.get(i) < gradientsInYpos.get(i + 1)) break;
            monotonicMatrix[2]++;
        }
        // <- y
        for (int i = 0; i < gradientsInYneg.size() -1; i++) {
            if (gradientsInYneg.get(i) < gradientsInYneg.get(i + 1)) break;
            monotonicMatrix[3]++;
        }

        System.out.println("Monoton direction until failure: [x -> " + monotonicMatrix[0] + " | <- x " + monotonicMatrix[1] + " | <- y (up) " + monotonicMatrix[2] + " | y -> (down) " + monotonicMatrix[3] + "]");
    }

    /**
     * Gives a double[] with the minimum and maximum change in between two followed gradients and sets the [boolean
     * canyon] true, if the parameter [double maxChange] is exceeded. Iterates through xPos, xNeg, yPos and yNeg.
     * Calculates the change between the gradients (change = Gm/Gn, where n = m+1) until the list stops behaving
     * strictly monotonic.
     * The calculation starts with index 1 of each list, to compensate a very slow increase of the gradients.
     * @param maxChange allowed maximum change of followed gradients
     * @return  double[0] = maximum gradient change, double[1] = minimum gradient change
     */
    private double[] maxGradientChange(double maxChange) {
        double minMax[] = {0,0};
        List<Double> values = new LinkedList<>();
        // x ->
        for (int i = 1; i < monotonicMatrix[0]; i++) {  // Iterate until the list stops behaving strictly monotonic.
            values.add((gradientsInXpos.get(i + 1) / gradientsInXpos.get(i)));
        }
        // <- x
        for (int i = 1; i < monotonicMatrix[1]; i++) {  // Iterate until the list stops behaving strictly monotonic.
            values.add((gradientsInXneg.get(i + 1) / gradientsInXneg.get(i)));
        }
        // y ->
        for (int i = 1; i < monotonicMatrix[3]; i++) {  // Iterate until the list stops behaving strictly monotonic.
            values.add((gradientsInYneg.get(i + 1) / gradientsInYneg.get(i)));
        }
        // <- y
        for (int i = 1; i < monotonicMatrix[2]; i++) {  // Iterate until the list stops behaving strictly monotonic.
            values.add((gradientsInYpos.get(i + 1) / gradientsInYpos.get(i)));
        }
        minMax[0] = values.isEmpty() ? 0 : values.stream().max(Comparator.comparing(Double::valueOf)).get();
        minMax[1] = values.isEmpty() ? 0 : values.stream().min(Comparator.comparing(Double::valueOf)).get();
        if (minMax[0] > maxChange) canyon = true;
        return minMax;
    }

    /**
     * Adds the first two gradients for each list xPos, xNeg, yPos, yNeg and tests if one of them exceeds
     * [double whatsSharp]. If it does, canyon will be set true.
     * @param whatsSharp    maximum gradient (G0 + G1)
     */
    private void fastSharp(double whatsSharp) {
        if (
                (gradientsInXpos.get(0) + gradientsInXpos.get(1)) *-1 > whatsSharp
                        ||
                        (gradientsInXneg.get(0) + gradientsInXneg.get(1)) *-1 > whatsSharp
                        ||
                        (gradientsInYpos.get(0) + gradientsInYpos.get(1)) *-1 > whatsSharp
                        ||
                        (gradientsInYneg.get(0) + gradientsInYneg.get(1)) *-1 > whatsSharp
        ) {
            canyon = true;
        }
    }

    /**
     * Counts the "flat" gradients in xPos, xNeg, yPos and yNeg until a parameterized [int maxGradChange] for each list.
     * If the amount of "flat" gradients exceeds [double amountOfFlats], [boolean flat] will be set true.
     * A "flat" gradient can be specified with the parameter [double whatsFlat].
     * The computation starts with index 1 of each list, to compensate a very slow increase of the gradients.
     * @param whatsFlat specify a "flat" gradient [% in decimal], references to the highest and lowest Z-Value in the given matrix
     * @param amountOfFlats allowed "flat" gradients [% in decimal]
     * @param maxGradChange Sets the maximum multiplier for a gradient change
     */
    private void flat(double whatsFlat, double amountOfFlats, int maxGradChange) {
        int wF = (int)(whatsFlat * (getMax(matrixList).z - getMin(matrixList).z));  // Calculates the boundary in reference to the highest and lowest Z-Value in the given matrix.
        double erg = 0;
        AtomicInteger counterA = new AtomicInteger(0);  // Atomic Integer needed to avoid duplicated Code.
        AtomicInteger watchedA = new AtomicInteger(0);

        // Iterate through the given list and count the amount of "flat" gradients until a gradient change exceeds [int maxGradChange].
        Consumer<List<Double>> iterateGradients = (x) -> {
            for (int i = 1; i < x.size() -1; i++) {
                if (x.get(i + 1) / x.get(i) > maxGradChange) break;
                if (x.get(i) *-1 < wF)
                {
                    counterA.incrementAndGet(); // Amount of "flat" gradients.
                }
                watchedA.incrementAndGet(); // Amount of gradients considered.
            }
        };
        // Do the necessary steps for each list.
        iterateGradients.accept(gradientsInYneg);
        iterateGradients.accept(gradientsInXneg);
        iterateGradients.accept(gradientsInYpos);
        iterateGradients.accept(gradientsInXpos);

        if (watchedA.get() != 0) {
            erg = counterA.get() / (double)watchedA.get();  // ( Amount gradients / Amount of "flat" gradients )
        }

        if (erg > amountOfFlats) flat = true; // ( Amount gradients / Amount of "flat" gradients ) > allowed "flat" gradients [% in decimal]
    }

    /**
     * Calculates whether the object is strong, weak or not symmetric.
     * Weak represents symmetry in X or Y. [boolean symmetricStrong]
     * Strong represents symmetry in X and Y. [boolean symmetricWeak]
     *
     * The calculation starts with index 1 of each list, to compensate a very slow increase of the gradients.
     * @param amountOfSymmetricsNeeded  Needed symmetric gradients, to consider X or Y symmetric
     * @param whatsSymmetric Specifies the symmetry [% in decimal]
     */
    private void symmetric(int amountOfSymmetricsNeeded, double whatsSymmetric) {
        int sizeX = gradientsInXpos.size() > gradientsInXneg.size() ? gradientsInXneg.size() : gradientsInXpos.size();  // Get the shortest of both lists.
        for (int i = 1; i < sizeX; i++) {   // Calculate the symmetry. (xPos[i] / xNeg[i])
            symmetricInX.add(gradientsInXneg.get(i) / gradientsInXpos.get(i));
        }
        int amountOfSymmetrics = (int) symmetricInX.stream().filter(x -> 1.0 - whatsSymmetric < x && x < 1.0 + whatsSymmetric).count();
        System.out.println("Amount of Symetric Gradients in X: [" + amountOfSymmetrics + "]");
        boolean inX = amountOfSymmetrics > amountOfSymmetricsNeeded;    // Is X symmetric ?

        int sizeY = gradientsInYpos.size() > gradientsInYneg.size() ? gradientsInYneg.size() : gradientsInYpos.size();  // Get the shortest of both lists.
        for (int i = 1; i < sizeY; i++) {   // Calculate the symmetry. (yPos[i] / yNeg[i])
            symmetricInY.add(gradientsInYneg.get(i) / gradientsInYpos.get(i));
        }
        amountOfSymmetrics = (int) symmetricInY.stream().filter(x -> 1.0 - whatsSymmetric < x && x < 1.0 + whatsSymmetric).count();
        System.out.println("Amount of Symetric Gradients in Y: [" + amountOfSymmetrics + "]");
        boolean inY = amountOfSymmetrics > amountOfSymmetricsNeeded;    // Is Y symmetric ?

        symmetricWeak = inX || inY;
        symmetricStrong = inX && inY;
    }

    /**
     * Get the Coord3d of the highest point (Z-Value) in the given matrix. Only in a range of 10 points in each X or Y direction
     * around the centre of the matrix.
     * @param matrixList    investigated matrix area
     * @return  The new Coord3d with the highest Z-Value
     */
    private Coord3d getMax(List<List<Integer>> matrixList) {
        int x = 0;
        int y = 0;
        int maxSize = 10; // Range of 10 points in each X or Y direction
        int outsizeX = (matrixList.size() - maxSize) / 2;
        int outsizeY = (matrixList.get(0).size() - maxSize) / 2;
        Coord3d max = new Coord3d(0,0,0);
        for (List<Integer> row : matrixList) {
            if (x >= outsizeX && x < maxSize + outsizeX)
            for (Integer zValue : row) {
                if (max.z < zValue
                        && y >= outsizeY
                        && y < maxSize + outsizeY)
                {
                    max.z = zValue;
                    max.x = x;
                    max.y = y;
                }
                y++;
            }
            y = 0;
            x++;
        }
        return max;
    }

    /**
     * Get the Coord3d of the lowest point (Z-Value) in the given matrix.
     * @param matrixList    investigated matrix area
     * @return  The new Coord3d with the lowest Z-Value
     */
    private Coord3d getMin(List<List<Integer>> matrixList) {
        int x = 0;
        int y = 0;
        int z = 0;
        Coord3d min = new Coord3d(Float.MAX_VALUE,Float.MAX_VALUE,Float.MAX_VALUE);
        for (List<Integer> row : matrixList) {
            for (Integer zValue : row) {
                if (min.z > zValue) {
                    min.z = zValue;
                    min.x = x;
                    min.y = y;
                }
                y++;
            }
            y = 0;
            x++;
        }
        return min;
    }

    /**
     * Calculation of the object type with the Naive Bayes classifier.
     * Only [boolean canyon], [boolean symmetricWeak], [boolean symmetricStrong] and [boolean flat] are taken into
     * calculation in the current implementation. More (of our implemented) attributes did not benefit the result.
     * @param aV    Class AttributeValues - Contains the necessary data
     */
    public void calculateType(AttributeValues aV) {

        double PAattr = ( ( canyon ? log(aV.getPCanyonA())
                                : log(1 - aV.getPCanyonA()) )
                        +
                        (symmetricWeak ? log(aV.getPSymAw())
                                : log(1 - aV.getPSymAw()) )
                        +
                        (symmetricStrong ? log(aV.getPSymAs())
                                : log(1 - aV.getPSymAs()) )
                        +
                        (flat ? log(aV.getPFlatA())
                                : log(1 - aV.getPFlatA()) )
                        );


        double PBattr = ( ( canyon ? log(aV.getPCanyonB())
                : log(1 - aV.getPCanyonB()) )
                +
                (symmetricWeak ? log(aV.getPSymBw())
                        : log(1 - aV.getPSymBw()) )
                +
                (symmetricStrong ? log(aV.getPSymBs())
                        : log(1 - aV.getPSymBs()) )
                +
                (flat ? log(aV.getPFlatB())
                        : log(1 - aV.getPFlatB()) )
        );

        if (PAattr > PBattr) {
            calculatedType = 'A';
        } else {
            calculatedType = 'B';
        }
    }

    /**
     * ATTENTION: This method is not used for any calculation. It did not benefit our analyses.
     * The idea is to count the longest strictly monotonic list [int max] out of xPos, xNeg, yPos and yNeg.
     * Get the lowest Z-Value over all lists [int zMinHigh] and test how many points in the given matrix, limited by a
     * radius [int max] from centre, exceed [int zMinHigh].
     * The amount of points exceeding [int zMinHigh] divided by the amount overall points inside the radius will give an
     * idea of a healthy volume.
     * The code inside this method is not explained since he is not used.
     */
    public void volume() {
        if (matrixList.size() < size*2 || matrixList.get(0).size() < size*2) return;

        Coord3d point = getMax(matrixList);
        int xPos = (int)point.x;
        int yPos = (int)point.y;

        // [x -> " + monotonicMatrix[0] + " | <- x " + monotonicMatrix[1] + " | <- y (up) " + monotonicMatrix[2] + " | y -> (down) " + monotonicMatrix[3]
        int max = 0;
        for (int x = 0; x < 4; x++) {
            if (monotonicMatrix[x] > max) {
                max = monotonicMatrix[x] + 1;
                max = max > size - 1 ? size -1 : max;
            }
        }

        int zMinHigh;
        double test0 = gradientsInXpos.subList(0, max).stream().reduce((x,y)-> x + y).get();
        double test1 = gradientsInXneg.subList(0, max).stream().reduce((x,y)-> x + y).get();
        double test2 = gradientsInYpos.subList(0, max).stream().reduce((x,y)-> x + y).get();
        double test3 = gradientsInYneg.subList(0, max).stream().reduce((x,y)-> x + y).get();
        zMinHigh = (int)(point.z + max(max(test0, test1), max(test2, test3)));

        int amountPoints = 0;
        int counter = 0;

        for (int i = xPos - max; i < xPos + max; i++) {
            for (int j = yPos - max; j < yPos + max; j++) {

                if ( sqrt((xPos-i)*(xPos-i) + (yPos-j)*(yPos-j))  <  max) {
                    amountPoints++;
                    if (matrixList.get(i).get(j) > zMinHigh) counter++;

                }
            }
        }
        inHeightRange = counter / (double)amountPoints;
        positiveVolume = inHeightRange > 0.5;
//        System.out.println("Pseudo Volume: [" + inHeightRange + "] by[" + zMinHigh + "]");
    }


    public boolean calcRight() {
        return calculatedType == type;
    }

    public boolean isPositiveVolume() {
        return positiveVolume;
    }

    public double getMin() {
        return minMax[1];
    }

    public double getMax() {
        return minMax[0];
    }

    public boolean isFlat() {
        return flat;
    }

    public boolean isWeakSymetric() {
        return symmetricWeak;
    }

    public boolean isStrongSymetric() {
        return symmetricStrong;
    }

    public double getInHeightRange() {
        return inHeightRange;
    }
}
