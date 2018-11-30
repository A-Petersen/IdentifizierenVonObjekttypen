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
    private List<Double> symetricInX = new LinkedList<>();
    private List<Double> symetricInY = new LinkedList<>();
    private boolean symetricWeak = false;
    private boolean symetricStrong = false;
    private boolean flat = false;
    private boolean canyon = false;
    private double hight;
    private double inHightRange;
    private boolean positivVolume = false;

    private int[] monotonicMatrix = {0,0,0,0};

    public Object(Coord3d position, int ab, char type) throws IOException {
        this.type = type;
        this.position = position;
        this.size = ab;
        this.inHightRange = 0;
        gatherGradients();
        flat();
        symetric();
        fastSharp();
        minMax = maxGradientChange();
        volume();
        hight = getMax(matrixList).z - getMin(matrixList).z;
        System.out.println("Gradient differences Max: [" + minMax[0] + "] \t Min: [" + minMax[1] + "]\nFlat: [" + flat + "]");
        calculateType( 0.7765363128, 0.48085106382,0.32821229050279327, 0.6127659574468085, 0.09078212290502793, 0.2425531914893617, 0.3784916201117318, 0.1276595744680851, 0.04888268156424581, 0.01702127659574468, 0.7528916929547844, 0.24710830704521555);
        System.out.println("Correct Type: [" + calcRight() + "]" +
                "\n------------------------------------------------------------------------------------------\n");
    }

    public void gatherGradients() throws IOException {

        matrixList = Gatherer.getMatrix((int)position.x - size, (int)position.x + size, (int)position.y - size, (int)position.y + size, "Data/data.csv");
        Coord3d maxZ = getMax(matrixList);
        position.x = position.x - (matrixList.size()/2) + maxZ.x;
        position.y = position.y - (matrixList.get(0).size()/2) + maxZ.y;
        position.z = maxZ.z;
        System.out.println("New Max Coord_pos: [" + position + "]");
        matrixList = Gatherer.getMatrix((int)position.x - size, (int)position.x + size, (int)position.y - size, (int)position.y + size, "Data/data.csv");
        fillGradients();
        strictlyMonotonic();
    }

    private void fillGradients() {
        List<Double> dummy = new LinkedList<>();
        Point startPointX = new Point((matrixList.size()/2), 0);

        for (int i = 0; i < matrixList.get(0).size() - 1; i++) {
            dummy.add(getGradient(matrixList, startPointX, 'y'));
            startPointX.moveOneY();
        }
        gradientsInXneg.addAll(dummy.subList(0, (int)getMax(matrixList).y ));
        Collections.reverse(gradientsInXneg);
        gradientsInXneg = gradientsInXneg.stream().map(x -> x * -1 ).collect(Collectors.toList());
        gradientsInXpos.addAll(dummy.subList((int)getMax(matrixList).y , dummy.size()));
        System.out.println("Gradienten in X ->: \t\t" + gradientsInXpos);
        System.out.println("Gradienten in <- X: \t\t" + gradientsInXneg);


        dummy.clear();
        Point startPointY = new Point(0, (matrixList.get(0).size()/2));
        for (int i = 0; i < matrixList.size() - 1; i++) {
            dummy.add(getGradient(matrixList, startPointY, 'x'));
            startPointY.moveOneX();
        }
        gradientsInYpos.addAll(dummy.subList(0, (int)getMax(matrixList).x ));
        Collections.reverse(gradientsInYpos);
        gradientsInYpos = gradientsInYpos.stream().map(x -> x * -1 ).collect(Collectors.toList());
        gradientsInYneg.addAll(dummy.subList((int)getMax(matrixList).x , dummy.size()));
        System.out.println("Gradienten in Y -> (down): \t" + gradientsInYneg);
        System.out.println("Gradienten in <- Y (up): \t" + gradientsInYpos);

    }

    private double getGradient(List<List<Integer>> mList, Point a, char direction) {
        if (direction == 'x') {
            return (mList.get(a.x + 1).get(a.y) - mList.get(a.x).get(a.y));
        }
        return (mList.get(a.x).get(a.y + 1) - mList.get(a.x).get(a.y));
    }

    private double[] maxGradientChange() {
        double minMax[] = {0,0};
        List<Double> values = new LinkedList<>();
        // TODO: Rechnet derzeit erst ab xy + 1, um eine platte Anfangsfäche zu berücksichtigen.
        // x ->
        for (int i = 1; i < monotonicMatrix[0]; i++) {
            values.add((gradientsInXpos.get(i + 1) / gradientsInXpos.get(i)));
        }
        // <- x
        for (int i = 1; i < monotonicMatrix[1]; i++) {
            values.add((gradientsInXneg.get(i + 1) / gradientsInXneg.get(i)));
        }
        // y ->
        for (int i = 1; i < monotonicMatrix[3]; i++) {
            values.add((gradientsInYneg.get(i + 1) / gradientsInYneg.get(i)));
        }
        // <- y
        for (int i = 1; i < monotonicMatrix[2]; i++) {
            values.add((gradientsInYpos.get(i + 1) / gradientsInYpos.get(i)));
        }
        minMax[0] = values.isEmpty() ? 0 : values.stream().max(Comparator.comparing(Double::valueOf)).get();
        minMax[1] = values.isEmpty() ? 0 : values.stream().min(Comparator.comparing(Double::valueOf)).get();
        if (minMax[0] > 6.0) canyon = true;
        System.out.println("Canyon: [" + canyon + "]");
        return minMax;
    }

    private void fastSharp() {
        double whatsSharp = 5000;
//                (0.25 * (getMax(matrixList).z - getMin(matrixList).z));
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

    private void strictlyMonotonic() {
        // TODO: muss eigentlich i = 0 und  -2 zwei sein !!! schränkt im moment den kreis für monoton ein. 1.25 streng monoton gilt auch für 25% nicht monoton

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

    private void flat() {
        //TODO: magicvalues
        int whatsFlat = (int)(0.01 * (getMax(matrixList).z - getMin(matrixList).z));
        int maxGradChange = 3;
        double erg = 0;
        AtomicInteger counterA = new AtomicInteger(0);
        AtomicInteger watchedA = new AtomicInteger(0);
        //TODO: i = 1 !!!! direkte erste nachbarn nicht beachten.

        Consumer<List<Double>> iterateGradients = (x) -> {
            for (int i = 1; i < x.size() -1; i++) {
                if (x.get(i + 1) / x.get(i) > maxGradChange) break;
                if (x.get(i) *-1 < whatsFlat)
                {
                    counterA.incrementAndGet();
                }
                watchedA.incrementAndGet();
            }
        };
        iterateGradients.accept(gradientsInYneg);
        iterateGradients.accept(gradientsInXneg);
        iterateGradients.accept(gradientsInYpos);
        iterateGradients.accept(gradientsInXpos);
        if (watchedA.get() != 0) {
            erg = counterA.get() / (double)watchedA.get();
        }

        if (erg > 0.4) flat = true;
        System.out.println("Flat in %: [" + erg + "]");
    }

    private void symetric() {
        int sizeX = gradientsInXpos.size() > gradientsInXneg.size() ? gradientsInXneg.size() : gradientsInXpos.size();
        for (int i = 1; i < sizeX; i++) {
            symetricInX.add(gradientsInXneg.get(i) / gradientsInXpos.get(i));
        }
        System.out.println("Amount of Symetric Gradients in X: [" + symetricInX.stream().filter(x -> 0.7 < x && x < 1.3).count() + "]");
        boolean inX = symetricInX.stream().filter(x -> 0.7 < x && x < 1.3).count() > 5 ? true : false;

        int sizeY = gradientsInYpos.size() > gradientsInYneg.size() ? gradientsInYneg.size() : gradientsInYpos.size();
        for (int i = 1; i < sizeY; i++) {
            symetricInY.add(gradientsInYneg.get(i) / gradientsInYpos.get(i));
        }
        System.out.println("Amount of Symetric Gradients in Y: [" + symetricInY.stream().filter(x -> 0.7 < x && x < 1.3).count() + "]");
        boolean inY = symetricInY.stream().filter(x -> 0.7 < x && x < 1.3).count() > 5 ? true : false;

        symetricWeak = inX || inY;
        symetricStrong = inX && inY;

        System.out.println("Weak Symetric: [" + symetricWeak + "]" + "\nStrong Symetric: [" + symetricStrong + "]");
    }

    private Coord3d getMax(List<List<Integer>> matrixList) {
        int x = 0;
        int y = 0;
        int maxSize = 10;
        int overSizeX = (matrixList.size() - maxSize) / 2;
        int overSizeY = (matrixList.get(0).size() - maxSize) / 2;
        Coord3d max = new Coord3d(0,0,0);
        for (List<Integer> row : matrixList) {
            if (x >= overSizeX && x < maxSize + overSizeX)
            for (Integer zValue : row) {
                if (max.z < zValue
                        && y >= overSizeY
                        && y < maxSize + overSizeY)
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

    public void calculateType(double pVolA, double pVolB, double pCanyonA, double pCanyonB, double pFlatA, double pFlatB, double pSymA, double pSymB, double pSymAs, double pSymBs, double pA, double pB) {

        double PAattr = ( ( canyon ? -log(pCanyonA)
                                : -log(1 - pCanyonA) )
                        +
                        (symetricWeak ? -log(pSymA)
                                : -log(1 - pSymA) )
                        +
                        ( -log(pA) ) );
        double PAattrM = ( ( canyon ? (pCanyonA)
                : (1.0 - pCanyonA) )
                *
                (symetricWeak ? (pSymA)
                        : (1.0 - pSymA) )
                *
                (symetricStrong ? (pSymAs)
                        : (1.0 - pSymAs) )
                *
                (flat ? (pFlatA)
                        : (1.0 - pFlatA) )
//                *
//                (positivVolume ? (pVolA)
//                        : (1.0 - pVolA) )
//                *
//                ( (pA) )
                );

        double PBattr = ( ( canyon ? -log(pCanyonB)
                        : -log(1 - pCanyonB) )
                        +
                        (symetricWeak ? -log(pSymB)
                                : -log(1 - pSymB) )
                        +
                        ( -log(pB) ) );
        double PBattrM = ( ( canyon ? (pCanyonB)
                : (1.0 - pCanyonB) )
                *
                (symetricWeak ? (pSymB)
                        : (1.0 - pSymB) )
                *
                (symetricStrong ? (pSymBs)
                        : (1.0 - pSymBs) )
                *
                (flat ? (pFlatB)
                        : (1.0 - pFlatB) )
//                *
//                (positivVolume ? (pVolB)
//                        : (1.0 - pVolB) )
//                *
//                ( (pB) )
                );

        double Q = PAattr / PBattr;
        double QM = PAattrM / PBattrM;

        if (QM > 1) {
            calculatedType = 'A';
        } else {
            calculatedType = 'B';
        }
    }

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
        inHightRange = counter / (double)amountPoints;
        positivVolume = inHightRange > 0.5;
        System.out.println("Pseudo Volume: [" + inHightRange + "] by[" + zMinHigh + "]");
    }

    public boolean calcRight() {
        return calculatedType == type;
    }

    public boolean isPositivVolume() {
        return positivVolume;
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
        return symetricWeak;
    }

    public boolean isStrongSymetric() {
        return symetricStrong;
    }

    public double getInHightRange() {
        return inHightRange;
    }
}
