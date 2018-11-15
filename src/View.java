import org.jzy3d.maths.Coord3d;

import java.io.IOException;

public class View {
    private int xSize;
    private int xStart;
    private int ySize;
    private int yStart;
    private Gatherer points = new Gatherer();

    View (int xSize, int xStart, int ySize, int yStart) throws IOException {
        this.xSize = xSize;
        this.xStart = xStart;
        this.ySize = ySize;
        this.yStart = yStart;
    }

    View (int index, int ab, char type) throws IOException {
        Coord3d coord = points.getCoords(type == 'A' ? 'A' : 'B', 4000, 3000, 0, 0).get(index);
        xSize = secureOutOfBound((int)coord.x + ab,0, 4000);
        xStart = secureOutOfBound((int)coord.x - ab,0, 4000);
        ySize = secureOutOfBound((int)coord.y + ab,0, 3000);
        yStart = secureOutOfBound((int)coord.y - ab,0, 3000);
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
