import org.jzy3d.maths.Coord3d;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Object {
    private Coord3d position;
    private int halfSize;
    List<List<Integer>> matrixList = new LinkedList<>();
//    private Map<Integer, Double> gradients = new HashMap<>();

    public Object(Coord3d position, int ab) throws IOException {
        this.position = position;
        this.halfSize = ab;
        gatherGradients();
    }

    public void gatherGradients() throws IOException {

        System.out.println("Orginal Z: " + position.z + " Orginal X: " + position.x + " Orginal Y: " + position.y);
        matrixList = Gatherer.getMatrix((int)position.x -1 - halfSize, (int)position.x -1 + halfSize, (int)position.y -1 - halfSize, (int)position.y -1 + halfSize);
        System.out.println("Martix[0][0]:" + matrixList.get(0).get(0));

        List<Double> dummy = new LinkedList<>();
        Point startPoint = new Point(halfSize, 0);
        for (int i = 0; i < halfSize *2; i++) {
            dummy.add(getGradient(matrixList, startPoint, 'y'));
            startPoint.moveOneY();
            System.out.println(dummy);
        }
    }

    private double getGradient(List<List<Integer>> mList, Point a, char direction) {
        if (direction == 'x') {
            System.out.println("Z zZ.:" + matrixList.get(a.x).get(a.y));
            System.out.println(mList.get(a.x + 1).get(a.y) + ") - (" + mList.get(a.x).get(a.y));
            return (mList.get(a.x + 1).get(a.y) - mList.get(a.x).get(a.y));
        }
        System.out.println("Z zZ.:" + matrixList.get(a.x).get(a.y));
        System.out.println("(" + mList.get(a.x).get(a.y + 1) + ") - (" + mList.get(a.x).get(a.y) + ")");
        return (mList.get(a.x).get(a.y + 1) - mList.get(a.x).get(a.y));
    }

    public int getHalfSize() {
        return halfSize;
    }

    public void setHalfSize(int halfSize) {
        this.halfSize = halfSize;
    }
}
