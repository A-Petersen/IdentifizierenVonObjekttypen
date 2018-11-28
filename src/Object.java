import org.jzy3d.maths.Coord3d;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.Math.log;

public class Object {
    private char type;
    private char calculatedType;
    private Coord3d position;
    private int size;
    List<List<Integer>> matrixList = new LinkedList<>();
    List<Double> gradientsInXpos = new LinkedList<>();
    List<Double> gradientsInXneg = new LinkedList<>();
    List<Double> gradientsInYneg = new LinkedList<>();
    List<Double> gradientsInYpos = new LinkedList<>();
    private double[] minMax;
    private boolean sMonotonic = true;
    private List<Double> symetricInX = new LinkedList<>();
    private List<Double> symetricInY = new LinkedList<>();
    private boolean symetricWeak = false;
    private boolean symetricStrong = false;
    private boolean flat = false;
//    private int flatGradients = 0;
    private boolean canyon = false;
    private double hight;

    private int[] monotonicMatrix = {0,0,0,0};

    public double getMin() {
        return minMax[1];
    }

    public double getMax() {
        return minMax[0];
    }

    public Object(Coord3d position, int ab, char type) throws IOException {
        this.type = type;
        this.position = position;
        this.size = ab;
        gatherGradients();
        flat();
        symetric();
        fastSharp();
        minMax = maxGradientChange();
        hight = getMax(matrixList).z - getMin(matrixList).z;
        System.out.println("Gradient differences Max: [" + minMax[0] + "] \t Min: [" + minMax[1] + "]\nFlat: [" + flat + "]");
        calculateType(0.32821229050279327, 0.6127659574468085, 0.09078212290502793, 0.2425531914893617, 0.3784916201117318, 0.1276595744680851, 0.04888268156424581, 0.01702127659574468, 0.7528916929547844, 0.24710830704521555);
        System.out.println("Correct Type: [" + calcRight() + "]" +
                "\n------------------------------------------------------------------------------------------\n");
    }

    public void gatherGradients() throws IOException {

        matrixList = Gatherer.getMatrix((int)position.x - size, (int)position.x + size, (int)position.y - size, (int)position.y + size);
        Coord3d maxZ = getMax(matrixList);
        position.x = position.x - (matrixList.size()/2) + maxZ.x;
        position.y = position.y - (matrixList.get(0).size()/2) + maxZ.y;
        position.z = maxZ.z;
        System.out.println("New Max Coord_pos: [" + position + "]");
        matrixList = Gatherer.getMatrix((int)position.x - size, (int)position.x + size, (int)position.y - size, (int)position.y + size);

        fillGradients();
        strictlyMonotonic();
//        System.out.println("strictly monotonic: [" + sMonotonic + "]");
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
//        gradientsInYneg = gradientsInYneg.stream().map(x -> x < 0 ? x * -1 : x).collect(Collectors.toList());
        System.out.println("Gradienten in Y -> (down): \t" + gradientsInYneg);
        System.out.println("Gradienten in <- Y (up): \t" + gradientsInYpos);

    }

    private double getGradient(List<List<Integer>> mList, Point a, char direction) {
        if (direction == 'x') {
//            System.out.println("Z zZ.:" + matrixList.get(a.x).get(a.y));
//            System.out.println(mList.get(a.x + 1).get(a.y) + ") - (" + mList.get(a.x).get(a.y));
            return (mList.get(a.x + 1).get(a.y) - mList.get(a.x).get(a.y));
        }
//        System.out.println("Z zZ.:" + matrixList.get(a.x).get(a.y));
//        System.out.println("(" + mList.get(a.x).get(a.y + 1) + ") - (" + mList.get(a.x).get(a.y) + ") = " + (mList.get(a.x).get(a.y + 1) - mList.get(a.x).get(a.y)));
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
//        for (int i = 1; i < values.size() -3; i++) {
////            if ((i < size - 2) && values.get(i) * 1.25 < values.get(i+1))
////                sMonotonic = false;
////            if ((i >= size) && values.get(i) * 0.75 < values.get(i+1))
////                sMonotonic = false;
//            if ((i < size - 2) && values.get(i) > values.get(i+1)) break;
//            if ((i < size - 2) && values.get(i) * 0.75 < values.get(i+1)) sMonotonic = false;
//            if ((i >= size) && values.get(i) > values.get(i+1)) break;
//            if ((i >= size) && values.get(i) * 1.25 < values.get(i+1)) sMonotonic = false;
//        }

        // x ->
        for (int i = 0; i < gradientsInXpos.size() -1; i++) {
            if (gradientsInXpos.get(i) < gradientsInXpos.get(i+1)) break;
//            if (gradientsInXpos.get(i) * 1.2 < gradientsInXpos.get(i+1)) sMonotonic = false;
            monotonicMatrix[0]++;
        }
        // <- x
        for (int i = 0; i < gradientsInXneg.size() -1; i++) {
            if (gradientsInXneg.get(i) < gradientsInXneg.get(i + 1)) break;
//            if (gradientsInXneg.get(i) * 1.2 < gradientsInXneg.get(i + 1)) sMonotonic = false;
            monotonicMatrix[1]++;
        }
        // y ->
        for (int i = 0; i < gradientsInYpos.size() -1; i++) {
            if (gradientsInYpos.get(i) < gradientsInYpos.get(i + 1)) break;
//            if (gradientsInYpos.get(i) * 1.2 < gradientsInYpos.get(i + 1)) sMonotonic = false;
            monotonicMatrix[2]++;
        }
        // <- y
        for (int i = 0; i < gradientsInYneg.size() -1; i++) {
            if (gradientsInYneg.get(i) < gradientsInYneg.get(i + 1)) break;
//            if (gradientsInYneg.get(i) * 1.2 < gradientsInYneg.get(i + 1)) sMonotonic = false;
            monotonicMatrix[3]++;
        }

        System.out.println("Monoton direction until failure: [x -> " + monotonicMatrix[0] + " | <- x " + monotonicMatrix[1] + " | <- y (up) " + monotonicMatrix[2] + " | y -> (down) " + monotonicMatrix[3] + "]");
    }

//    public int getFlatGradients() {
//        return flatGradients;
//    }

    private void flat() {
        //TODO: magicvalues
//        int whatsFlat = (int)(0.02 * (getMax(matrixList).z - getMin(matrixList).z));
//        if (gradientsInXpos.size() < 4
//            ||gradientsInXneg.size() < 4
//            ||gradientsInYneg.size() < 4
//            ||gradientsInYpos.size() < 4) return;
//        if (
//                // x ->
//                (((gradientsInXpos.get(3) + gradientsInXpos.get(2) + gradientsInXpos.get(1) ) * -1 < whatsFlat)
//                ||  // <- x
//                ((gradientsInXneg.get(3) + gradientsInXneg.get(2) + gradientsInXneg.get(1) ) * -1 < whatsFlat))
//            ||
//                // <- y
//                (((gradientsInYneg.get(3) + gradientsInYneg.get(2) + gradientsInYneg.get(1) ) * -1 < whatsFlat)
//                ||  // y ->
//                ((gradientsInYpos.get(3) + gradientsInYpos.get(2) + gradientsInYpos.get(1) ) * -1 < whatsFlat)))
//        {
//            flat = true;
//        }
        // x ->
        int whatsFlat = (int)(0.01 * (getMax(matrixList).z - getMin(matrixList).z));
        int maxGradChange = 3;
//        int counter = 0;
//        int watched = 0;
        double erg = 0;
        AtomicInteger counterA = new AtomicInteger(0);
        AtomicInteger watchedA = new AtomicInteger(0);
        //TODO: i = 1 !!!! direkte erste nachbarn nicht beachten.
//        for (int i = 1; i < gradientsInXpos.size() - 1; i++) {
//            if (gradientsInXpos.get(i + 1) / gradientsInXpos.get(i) > maxGradChange) break;
//            if (gradientsInXpos.get(i) *-1 < whatsFlat)
//            {
//                counter++;
//            }
//            watched++;
//        }
//        // <- x
//        for (int i = 1; i < gradientsInXneg.size() -1; i++) {
//            if (gradientsInXneg.get(i + 1) / gradientsInXneg.get(i) > maxGradChange) break;
//            if (gradientsInXneg.get(i) *-1 < whatsFlat)
//            {
//                counter++;
//            }
//            watched++;
//        }
//        // y ->
//        for (int i = 1; i < gradientsInYpos.size() -1; i++) {
//            if (gradientsInYpos.get(i + 1) / gradientsInYpos.get(i) > maxGradChange) break;
//            if (gradientsInYpos.get(i) *-1 < whatsFlat)
//            {
//                counter++;
//            }
//            watched++;
//        }
//        // <- y
//        for (int i = 1; i < gradientsInYneg.size() -1; i++) {
//            if (gradientsInYneg.get(i + 1) / gradientsInYneg.get(i) > maxGradChange) break;
//            if (gradientsInYneg.get(i) *-1 < whatsFlat)
//            {
//                counter++;
//            }
//            watched++;
//        }

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
//        System.out.println("New Max Coord: " + max);
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
//        System.out.println("New Max Coord: " + max);
        return min;
    }

    public boolean issMonotonic() {
        return sMonotonic;
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

    public void calculateType(double pCanyonA, double pCanyonB, double pFlatA, double pFlatB, double pSymA, double pSymB, double pSymAs, double pSymBs, double pA, double pB) {

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

    public boolean calcRight() {
        return calculatedType == type;
    }

    public double getHight() {
        return hight;
    }

    private void normalize() {
        // MIT INTEGER NICHT I.O.
        int row = 0;
        int column = 0;
        int normValue = 10000;
        int minZ = (int)getMin(matrixList).z;
        double maxZ = getMax(matrixList).z - minZ;
        for (List<Integer> list : matrixList) {
            for (int z : list) {
//                System.out.println("TEstXY "+ (z - minZ) + " / " + maxZ + " = " + (z - minZ)/maxZ);
                list.set(column, (int)(((z - minZ)/maxZ) * normValue));
                column++;
            }
            column = 0;
            row++;
        }
//        matrixList.stream().forEach(x -> x.stream().forEach(y-> System.out.println(y)));
        //Gradienten in X: (incl. negativ)[1204.0, 901.0, 602.0, 336.0, 127.0, -93.0, -563.0, -1099.0, -1738.0, -2179.0]
    }

    public Coord3d getPosition() {
        return position;
    }
}
