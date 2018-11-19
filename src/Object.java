import org.jzy3d.maths.Coord3d;

import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Object {
    private Coord3d position;
    private int halfSize;
    List<List<Integer>> matrixList = new LinkedList<>();
    List<Double> gradientsInX = new LinkedList<>();
    List<Double> gradientsInY = new LinkedList<>();
//    private Map<Integer, Double> gradients = new HashMap<>();

    public Object(Coord3d position, int ab) throws IOException {
        this.position = position;
        this.halfSize = ab;
        gatherGradients();
        System.out.println("Max: " + maxGradientChange()[0] + " - Min: " + maxGradientChange()[1] );
    }

    public void gatherGradients() throws IOException {

        System.out.println(position);
        matrixList = Gatherer.getMatrix((int)position.x - halfSize, (int)position.x + halfSize, (int)position.y - halfSize, (int)position.y + halfSize);
        System.out.println("Martix[0][0] = " + matrixList.get(0).get(0));

//        List<Double> gradientsInX = new LinkedList<>();
        Point startPointX = new Point(halfSize, 0);
//        System.out.println("P(" + startPointX.x + "|" + startPointX.y + ")");
        for (int i = 0; i < halfSize *2; i++) {
            gradientsInX.add(getGradient(matrixList, startPointX, 'y'));
            startPointX.moveOneY();
        }
        System.out.println(gradientsInX);

//        List<Double> gradientsInY = new LinkedList<>();
        Point startPointY = new Point(0, halfSize);
//        System.out.println("P(" + startPointY.x + "|" + startPointY.y + ")");
        for (int i = 0; i < halfSize *2; i++) {
            gradientsInY.add(getGradient(matrixList, startPointY, 'x'));
            startPointY.moveOneX();
        }
        System.out.println(gradientsInY);
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
        for (int i = start, j = start-1; i < gradientsInX.size() - 1; i++, j--) {
            // x ->
            values.add(1 - (gradientsInX.get(i) / gradientsInX.get(i+1)));
            // <- x
            values.add(1 - (gradientsInX.get(j) / gradientsInX.get(j-1)));
            // y ->
            values.add(1 - (gradientsInY.get(i) / gradientsInY.get(i+1)));
            // <- y
            values.add(1 - (gradientsInY.get(j) / gradientsInY.get(j-1)));
        }
        System.out.println(values);
        minMax[0] = values.stream().max(Comparator.comparing(Double::valueOf)).get();
        minMax[1] = values.stream().min(Comparator.comparing(Double::valueOf)).get();
        return minMax;
    }

    public int getHalfSize() {
        return halfSize;
    }

    public void setHalfSize(int halfSize) {
        this.halfSize = halfSize;
    }
}
