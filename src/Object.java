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
        minMax = maxGradientChange();
        System.out.println("Max: " + minMax[0] + " \t Min: " + minMax[1] );
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
//        System.out.println("Martix[0][0] = " + matrixList.get(0).get(0));

//        List<Double> gradientsInX = new LinkedList<>();
        Point startPointX = new Point(size, 0);
//        System.out.println("P(" + startPointX.x + "|" + startPointX.y + ")");
        for (int i = 0; i < size *2; i++) {
            gradientsInX.add(getGradient(matrixList, startPointX, 'y'));
            startPointX.moveOneY();
        }
        System.out.println("Gradienten in X: (incl. negativ)" + gradientsInX);
        strictlyMonotonic(gradientsInX);
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
        strictlyMonotonic(gradientsInY);
        gradientsInY = gradientsInY.stream().map(x -> x < 0 ? x * -1 : x).collect(Collectors.toList());
//        System.out.println("Gradienten in Y: " + gradientsInY);
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

    private void strictlyMonotonic(List<Double> values) {
        // TODO: muss eigentlich i = 0 und  -2 zwei sein !!! schränkt im moment den kreis für monoton ein. 1.25 streng monoton gilt auch für 25% nicht monoton
        for (int i = 1; i < values.size() -3; i++) {
            if ((i < size - 2) && values.get(i) * 1.25 < values.get(i+1))
                sMonotonic = false;
            if ((i >= size) && values.get(i) * 0.75 < values.get(i+1))
                sMonotonic = false;
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


    public boolean issMonotonic() {
        return sMonotonic;
    }
}
