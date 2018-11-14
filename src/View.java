import org.jzy3d.maths.Coord3d;

import java.io.IOException;

public class View {
    private int xSize;
    private int xStart;
    private int ySize;
    private int yStart;
    private Gatherer points = new Gatherer();

//    View (int xSize, int xStart, int ySize, int yStart) throws IOException {
//
//    }

    View (int index, int ab, char type) throws IOException {
        Coord3d coord = points.getCoords(type == 'A' ? 'A' : 'B', 1000, 1000, 0, 0).get(index);
        xSize = secureOutOfBound((int)coord.x + ab,0, 1000);
        xStart = secureOutOfBound((int)coord.x - ab,0, 1000);
        ySize = secureOutOfBound((int)coord.y + ab,0, 1000);
        yStart = secureOutOfBound((int)coord.y - ab,0, 1000);
        System.out.println(xSize + "-" + xStart + "-" + yStart + "-" + ySize);
    }

    public int getySize() {
        return ySize;
    }

    public int getxStart() {
        return xStart;
    }

    public int getxSize() {
        return xSize;
    }

    public int getyStart() {
        return yStart;
    }

    private int secureOutOfBound(int x, int min, int max) {
        int res = x < min ? min : x;
        res = res > max ? max : res;
        return res;
    }
}
