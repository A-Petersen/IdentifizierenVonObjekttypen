import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DrawView uses the library Jzy3d to visualize the objects or a specified landscape
 * of the given CSV-File.
 */
public class DrawView extends AbstractAnalysis {

    /**
     * Desired point to build a View
     */
    private Coord3d coord;
    /**
     * Object type A or B
     */
    private char type;
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
     * View has a grid
     */
    private boolean grid;
    /**
     * View Class
     */
    private View view;
    /**
     * List of coordinates of A objects
     */
    private List<Coord3d> coordsA;
    /**
     * List of coordinates of B objects
     */
    private List<Coord3d> coordsB;

    /**
     * Constructor for DrawView. Needed to parameterize the polygonView.
     * @param view  Class View
     * @param grid  Should the View draw a grid ?
     * @throws IOException
     */
    DrawView(View view, boolean grid) throws IOException {
        this.view = view;
        this.xStop = view.getxStop();
        this.xStart = view.getxStart();
        this.yStop = view.getyStop();
        this.yStart = view.getyStart();
        this.type = view.getType();
        if (!view.isLandscape()) {
            coord = view.getCoord();
        } else {
            Gatherer gatherer = view.getGatherer();
            coordsA = gatherer.getCoords('A', xStop, yStop, xStart, yStart, false);
            coordsB = gatherer.getCoords('B', xStop, yStop, xStart, yStart, false);
        }
        this.grid = grid;
    }

    /**
     * Build the [List<Polygon> polygons] and creates the 3d object.
     * @throws IOException
     */
    @Override
    public void init() throws IOException {
        List<List<Integer>> data = Gatherer.getMatrix(xStart, xStop, yStart, yStop, view.getDataPath());

        // Build a polygon list
        List<Polygon> polygons = new ArrayList<>();
        for(int i = 0; i < data.size() -1; i++){
            for(int j = 0; j < data.get(i).size() - 1; j++){
                Polygon polygon = new Polygon();
                polygon.add(new Point( new Coord3d(i, j, data.get(i).get(j)) ));
                polygon.add(new Point( new Coord3d(i, j+1, data.get(i).get(j+1)) ));
                polygon.add(new Point( new Coord3d(i+1, j+1, data.get(i+1).get(j+1))));
                polygon.add(new Point( new Coord3d(i+1, j, data.get(i+1).get(j) ) ));
                polygons.add(polygon);
            }
        }

        // Creates the 3d object [Build from examples of jzy3d]
        final Shape surface = new Shape(polygons);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new org.jzy3d.colors.Color(1,1,1,1f)));
        surface.setWireframeDisplayed(grid);
        surface.setWireframeColor(Color.GREEN);

        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
        chart.getScene().getGraph().add(surface);

        chart.getAxeLayout().setXAxeLabel( "X" );
        chart.getAxeLayout().setYAxeLabel( "Y" );
        chart.getAxeLayout().setZAxeLabel( "Z" );

        if (type == 'A' && !view.isLandscape()) {
            chart.addDrawable(new Point(coord, Color.RED, 20));
        }else if (!view.isLandscape()) {
            chart.addDrawable(new Point(coord, Color.GREEN, 20));
        }

        if (view.isLandscape()) {
            coordsA.stream().forEach(x -> chart.addDrawable(new Point(new Coord3d(x.x - 1, x.y - 1, x.z + 10000.0), Color.RED, 10)));
            coordsB.stream().forEach(x -> chart.addDrawable(new Point(new Coord3d(x.x - 1, x.y - 1, x.z + 10000.0), Color.GREEN, 10)));
        }

    }
}
