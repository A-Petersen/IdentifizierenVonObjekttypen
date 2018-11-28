import org.jzy3d.maths.Coord3d;

import java.io.IOException;
import java.util.List;

public class View {
    private int xSize;
    private int xStart;
    private int ySize;
    private int yStart;
    private Coord3d coord;
    private char type;
    private Gatherer points = new Gatherer();

    View (int xSize, int xStart, int ySize, int yStart) throws IOException {
        this.xSize = xSize;
        this.xStart = xStart;
        this.ySize = ySize;
        this.yStart = yStart;
    }

    View (int index, int martixSize, char type) throws IOException {
        List<Coord3d> coords = points.getCoords(type == 'A' ? 'A' : 'B', Gatherer.numRows_static, Gatherer.numColumns_static, 1, 1, false);
        coord = coords.get(index);
        this.type = type;
        xSize = Gatherer.secureOutOfBound((int)coord.x + martixSize,1, 4943);
        xStart = Gatherer.secureOutOfBound((int)coord.x - martixSize,1, 4943);
        ySize = Gatherer.secureOutOfBound((int)coord.y + martixSize,1, 3000);
        yStart = Gatherer.secureOutOfBound((int)coord.y - martixSize,1, 3000);
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

    public Coord3d getCoord() {
        return new Coord3d(coord.x - xStart, coord.y - yStart, coord.z);
    }

    public char getType() {
        return type;
    }

    //    private int secureOutOfBound(int x, int min, int max) {
//        int res = x < min ? min : x;
//        res = res > max ? max : res;
//        return res;
//    }
}
