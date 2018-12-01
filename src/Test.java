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

public class Test extends AbstractAnalysis {

    private Coord3d coord;
    private char type;
    private int xStop;
    private int xStart;
    private int yStop;
    private int yStart;
    private boolean grid;
    private View view;
    private List<Coord3d> coordsA;
    private List<Coord3d> coordsB;

    Test (View view, boolean grid) throws IOException {
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
        System.out.println("T1: " + this.xStart + "-" + this.xStop + "-" + this.yStart + "-" + this.yStop);
    }

    @Override
    public void init() throws IOException {
        System.out.println("T1: " + this.xStart + "-" + this.xStop + "-" + this.yStart + "-" + this.yStop);
        List<List<Integer>> data = Gatherer.getMatrix(xStart, xStop, yStart, yStop, "Data/data.csv");

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

        // Creates the 3d object
        final Shape surface = new Shape(polygons);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new org.jzy3d.colors.Color(1,1,1,1f)));
        surface.setWireframeDisplayed(grid);
        surface.setWireframeColor(Color.GREEN);

        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
        chart.getScene().getGraph().add(surface);

        chart.getAxeLayout().setXAxeLabel( "x-test" );
        chart.getAxeLayout().setYAxeLabel( "y-test" );
        chart.getAxeLayout().setZAxeLabel( "z-test" );

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
