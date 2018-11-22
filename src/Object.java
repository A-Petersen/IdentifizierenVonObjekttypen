import org.jzy3d.maths.Coord3d;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Object {
    private char type;
    private Coord3d position;
    private int size;
    List<List<Integer>> matrixList = new LinkedList<>();
    List<Double> gradientsInX = new LinkedList<>();
    List<Double> gradientsInY = new LinkedList<>();
    private double[] minMax;
    private boolean sMonotonic = true;
    private boolean symetric = true;
    private boolean flat = false;

//    private Map<Integer, Double> gradients = new HashMap<>();

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
        minMax = maxGradientChange();
        System.out.println("Max: " + minMax[0] + " \t Min: " + minMax[1] + "\nFlat[" + flat + "]");
    }

    public void gatherGradients() throws IOException {

        System.out.println(position);
        matrixList = Gatherer.getMatrix((int)position.x - size, (int)position.x + size, (int)position.y - size, (int)position.y + size);
        Coord3d maxZ = getMax(matrixList);
        position.x = position.x - size + maxZ.x;
        position.y = position.y - size + maxZ.y;
        position.z = maxZ.z;
        System.out.println("New Max Coord_pos: " + position);
        matrixList = Gatherer.getMatrix((int)position.x - size, (int)position.x + size, (int)position.y - size, (int)position.y + size);
//        normalize();
//        System.out.println("Martix[0][0] = " + matrixList.get(0).get(0));

//        List<Double> gradientsInX = new LinkedList<>();
        Point startPointX = new Point(size, 0);
//        System.out.println("P(" + startPointX.x + "|" + startPointX.y + ")");
        for (int i = 0; i < size *2; i++) {
            gradientsInX.add(getGradient(matrixList, startPointX, 'y'));
            startPointX.moveOneY();
        }
        System.out.println("Gradienten in X: (incl. negativ)" + gradientsInX);
//        strictlyMonotonic(gradientsInX);
        gradientsInX = gradientsInX.stream().map(x -> x < 0 ? x * -1 : x).collect(Collectors.toList());
//        System.out.println("Gradienten in X: " + gradientsInX);

//        List<Double> gradientsInY = new LinkedList<>();
        Point startPointY = new Point(0, size);
//        System.out.println("P(" + startPointY.x + "|" + startPointY.y + ")");
        for (int i = 0; i < size *2; i++) {
            gradientsInY.add(getGradient(matrixList, startPointY, 'x'));
            startPointY.moveOneX();
        }
        System.out.println("Gradienten in Y: (incl. negativ)" + gradientsInY);
//        strictlyMonotonic(gradientsInY);
        gradientsInY = gradientsInY.stream().map(x -> x < 0 ? x * -1 : x).collect(Collectors.toList());
//        System.out.println("Gradienten in Y: " + gradientsInY);
        strictlyMonotonic();
        System.out.println("strictly monotonic: [" + sMonotonic + "]");
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
        int start = gradientsInX.size() / 2;
        List<Double> values = new LinkedList<>();
        // TODO: Rechnet derzeit erst ab xy + 1, um eine platte Anfangsfäche zu berücksichtigen.
        for (int i = start+1, j = start-2; i < gradientsInX.size() - 1; i++, j--) {
            // x ->
            values.add((gradientsInX.get(i+1) / gradientsInX.get(i)));
            // <- x
            values.add((gradientsInX.get(j+1) / gradientsInX.get(j)));

            // y ->
            values.add((gradientsInY.get(i+1) / gradientsInY.get(i)));
            // <- y
            values.add((gradientsInY.get(j+1) / gradientsInY.get(j)));
        }
        System.out.println("Gradienten XY in %: " + values);
//        values = values.stream().map(x -> x < 0 ? x * -1 : x).collect(Collectors.toList());
//        System.out.println("Gradienten XY in %: " + values);
        minMax[0] = values.stream().max(Comparator.comparing(Double::valueOf)).get();
        minMax[1] = values.stream().min(Comparator.comparing(Double::valueOf)).get();
        return minMax;
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
        for (int i = size, j = size-1; i < size*2 - 1; i++, j--) {
            // x ->
            if (gradientsInX.get(i) < gradientsInX.get(i+1)) break;
            if (gradientsInX.get(i) * 2 < gradientsInX.get(i+1)) sMonotonic = false;
            // <- x
            if (gradientsInX.get(j) > gradientsInX.get(j-1)) break;
            if (gradientsInX.get(j) * 2 > gradientsInX.get(j-1)) sMonotonic = false;
            // y ->
            if (gradientsInY.get(i) < gradientsInY.get(i+1)) break;
            if (gradientsInY.get(i) * 2 < gradientsInY.get(i+1)) sMonotonic = false;
            // <- y
            if (gradientsInY.get(j) > gradientsInY.get(j-1)) break;
            if (gradientsInY.get(j) * 2 > gradientsInY.get(j-1)) sMonotonic = false;
        }
    }

//    private void symetric(double prozent){
//        int start = gradientsInX.size() / 2;
//        for (int i = start, j = start-1; i < gradientsInX.size(); i++, j--) {
//            // x ->
//            if (gradientsInX.get(i) / gradientsInX.get(j));
//            // <- x
//            values.add((gradientsInX.get(j+1) / gradientsInX.get(j)));
//
//            // y ->
//            values.add((gradientsInY.get(i+1) / gradientsInY.get(i)));
//            // <- y
//            values.add((gradientsInY.get(j+1) / gradientsInY.get(j)));
//        }
//
//    }

    private void flat() {
        //TODO: magicvalues
        int whatsFlat = (int)((200.0 / 10000.0) * (getMax(matrixList).z - getMin(matrixList).z));
        System.out.println(whatsFlat);
        if (// x ->
            ((gradientsInX.get(size +2) + gradientsInX.get(size +1) + gradientsInX.get(size) ) < whatsFlat)
            ||// <- x
            ((gradientsInX.get(size -3) + gradientsInX.get(size -2) + gradientsInX.get(size -1) ) < whatsFlat)

            ||// y ->
            ((gradientsInY.get(size +2) + gradientsInY.get(size +1) + gradientsInY.get(size) ) < whatsFlat)
            ||// <- y
            (( gradientsInY.get(size -3) + gradientsInY.get(size -2) + gradientsInY.get(size -1) ) < whatsFlat))
//        if (// x ->
//            ((gradientsInX.get(size +2) < gradientsInX.get(size+1)*1.5 ))
//            ||// <- x
//            ((gradientsInX.get(size -3) < gradientsInX.get(size -2)*1.5 ))
//
//            ||// y ->
//            ((gradientsInY.get(size +2) < gradientsInY.get(size+1)*1.5 ))
//            ||// <- y
//            ((gradientsInY.get(size -3) < gradientsInY.get(size -2)*1.5 )))
        {
            flat = true;
        }

    }

    private Coord3d getMax(List<List<Integer>> matrixList) {
        int x = 0;
        int y = 0;
        int z = 0;
        Coord3d max = new Coord3d(0,0,0);
        for (List<Integer> row : matrixList) {
            for (Integer zValue : row) {
                if (max.z < zValue) {
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
}
