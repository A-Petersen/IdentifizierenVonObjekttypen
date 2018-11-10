import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Test extends AbstractAnalysis {

    private static int xSize = 1880;
    private static int xStart = 1820;
    private static int ySize = 1200;
    private static int yStart = 960;

    public static void main(String[] args) throws Exception {
        AnalysisLauncher.open(new Test());
    }

    @Override
    public void init() throws IOException {

        Gatherer points = new Gatherer();
        List<Coord3d> coordsA = points.getCoords('A', xSize, ySize, xStart, yStart);
        List<Coord3d> coordsB = points.getCoords('B', xSize, ySize, xStart, yStart);


        List<List<Integer>> data = createObjectLists();

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
        surface.setWireframeDisplayed(true);
        surface.setWireframeColor(Color.GREEN);

        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
        chart.getScene().getGraph().add(surface);

        chart.getAxeLayout().setXAxeLabel( "x-test" );
        chart.getAxeLayout().setYAxeLabel( "y-test" );
        chart.getAxeLayout().setZAxeLabel( "z-test" );

//        chart.addLight(new Coord3d(100.0,100.0,100.0));

        coordsA.forEach(x  -> chart.addDrawable(new Point(x, Color.RED, 20)));
        coordsB.forEach(x  -> chart.addDrawable(new Point(x, Color.GREEN, 20)));
//        Point abc = new Point(new Coord3d(100.0,100.0,100.0), Color.RED, 5);
//        chart.addDrawable(abc);

//        // Define a function to plot
//        Mapper mapper = new Mapper() {
//            @Override
//            public double f(double x, double y) {
//                return x * Math.sin(x * y);
//            }
//        };
//
//        // Define range and precision for the function to plot
//        Range range = new Range(-3, 3);
//        int steps = 80;
//
//        // Create the object to represent the function over the given range.
//        final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
//        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
//        surface.setFaceDisplayed(true);
//        surface.setWireframeDisplayed(false);
//
//        // Create a chart
//        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
//        chart.getScene().getGraph().add(surface);
    }

    public List<List<Integer>> createObjectLists() throws IOException {
        int count = 0;
        List<List<Integer>> dataList = new LinkedList<>();

        Reader data = new FileReader("Data/data.csv");
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);
        for (CSVRecord row : rows) {
            List<Integer> dataListInner = new LinkedList<>();
//            for (int i = 0; i < row.size(); i++) {
            for (int i = yStart; i < ySize; i++) {
                if (count >= xStart) dataListInner.add(Integer.parseInt(row.get(i).replace(".", "")));
            }
            if (count >= xStart) dataList.add(dataListInner);
            if (count == xSize - 1) break;
            count++;
        }
        System.out.println(dataList.size() + " | " + dataList.get(0).size());
        return dataList;
    }
}
