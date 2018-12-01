import org.jzy3d.maths.Coord3d;

import java.io.IOException;
import java.util.List;

public class View {
    private int xStop;
    private int xStart;
    private int yStop;
    private int yStart;
    private Coord3d coord;
    private char type;
    private boolean landscape;
    private Gatherer gatherer;

    View (int xStart, int xStop, int yStart, int yStop, String aCoordsData, String bCoordsData, String dataPath) throws IOException {
        this.gatherer = new Gatherer(aCoordsData, bCoordsData, dataPath);
        this.landscape = true;
        this.xStop = Gatherer.secureOutOfBound(xStop,1, gatherer.getDataXsize());
        this.xStart = Gatherer.secureOutOfBound(xStart,1, gatherer.getDataXsize());
        this.yStop = Gatherer.secureOutOfBound(yStop,1, gatherer.getDataYsize());
        this.yStart = Gatherer.secureOutOfBound(yStart,1, gatherer.getDataYsize());
        System.out.println(this.xStop + "-" + this.xStart + "-" + this.yStop + "-" + this.yStart);
    }

    View (int index, int martixSize, char type, String aCoordsData, String bCoordsData, String dataPath) throws IOException {
        this.landscape = false;
        this.gatherer = new Gatherer(aCoordsData, bCoordsData, dataPath);
        List<Coord3d> coords = gatherer.getCoords(type == 'A' ? 'A' : 'B', gatherer.getDataXsize(), gatherer.getDataYsize(), 1, 1, false);
        this.coord = coords.get(index);
        this.type = type;
        this.xStop = Gatherer.secureOutOfBound((int)coord.x + martixSize,1, gatherer.getDataXsize());
        this.xStart = Gatherer.secureOutOfBound((int)coord.x - martixSize,1, gatherer.getDataXsize());
        this.yStop = Gatherer.secureOutOfBound((int)coord.y + martixSize,1, gatherer.getDataYsize());
        this.yStart = Gatherer.secureOutOfBound((int)coord.y - martixSize,1, gatherer.getDataYsize());
        System.out.println(xStop + "-" + xStart + "-" + yStart + "-" + yStop);
    }

    public int getyStop() {
        return yStop;
    }

    public int getxStart() {
        return xStart;
    }

    public int getxStop() {
        return xStop;
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

    public boolean isLandscape() {
        return landscape;
    }

    public Gatherer getGatherer() {
        return gatherer;
    }
}
