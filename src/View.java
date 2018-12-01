import org.jzy3d.maths.Coord3d;

import java.io.IOException;
import java.util.List;

/**
 * View prepares the data for DrawView to visualize the data.
 */
public class View {
    /**
     * Endpoint of X
     */
    private int xStop;
    /**
     * Startpoint in X
     */
    private int xStart;
    /**
     * Endpoint of Y
     */
    private int yStop;
    /**
     * Startpoint in Y
     */
    private int yStart;
    /**
     * Desired point to build a View
     */
    private Coord3d coord;
    /**
     * Object type A or B
     */
    private char type;
    /**
     * Landscape or specific object View
     */
    private boolean landscape;
    /**
     * Gatherer
     */
    private Gatherer gatherer;
    /**
     * Datapath to the main CSV-File
     */
    private String dataPath;

    /**
     * Creates a landscape View with the assistance of the parameterisation.
     * @param xStart    Startpoint in X
     * @param xStop     Endpoint of X
     * @param yStart    Startpoint in Y
     * @param yStop     Endpoint of Y
     * @param aCoordsData   Datapath to the CSV-File containing coordinates of A objects
     * @param bCoordsData   Datapath to the CSV-File containing coordinates of B objects
     * @param dataPath  Datapath to the main CSV-File
     * @throws IOException
     */
    View (int xStart, int xStop, int yStart, int yStop, String aCoordsData, String bCoordsData, String dataPath) throws IOException {
        this.dataPath = dataPath;
        this.gatherer = new Gatherer(aCoordsData, bCoordsData, dataPath, true);
        this.landscape = true;
        this.xStop = Gatherer.secureOutOfBound(xStop,1, gatherer.getDataXsize());
        this.xStart = Gatherer.secureOutOfBound(xStart,1, gatherer.getDataXsize());
        this.yStop = Gatherer.secureOutOfBound(yStop,1, gatherer.getDataYsize());
        this.yStart = Gatherer.secureOutOfBound(yStart,1, gatherer.getDataYsize());
        System.out.println(this.xStop + "-" + this.xStart + "-" + this.yStop + "-" + this.yStart);
    }

    /**
     * Creates an object View.
     * @param index Index of the desired object
     * @param martixSize    Matrixsize of the desired View
     * @param type  Object type
     * @param aCoordsData   Datapath to the CSV-File containing coordinates of A objects
     * @param bCoordsData   Datapath to the CSV-File containing coordinates of B objects
     * @param dataPath  Datapath to the main CSV-File
     * @throws IOException
     */
    View (int index, int martixSize, char type, String aCoordsData, String bCoordsData, String dataPath) throws IOException {
        this.dataPath = dataPath;
        this.landscape = false;
        this.gatherer = new Gatherer(aCoordsData, bCoordsData, dataPath, true);
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

    /**
     * Returns the Coord3d of investigated point inside the created matrix
     * @return Coord3d of investigated point
     */
    public Coord3d getCoord() {
        return new Coord3d(coord.x - xStart, coord.y - yStart, coord.z);
    }

    /**
     * Returns the object type.
     * @return Object type
     */
    public char getType() {
        return type;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public Gatherer getGatherer() {
        return gatherer;
    }

    public String getDataPath() {
        return dataPath;
    }
}
