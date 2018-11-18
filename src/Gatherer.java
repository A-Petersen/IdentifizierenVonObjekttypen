import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jzy3d.maths.Coord3d;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Gatherer {
    public static void main(String[] args) throws Exception {
        Gatherer g = new Gatherer();
    }

    // Koordinaten (Spalte,Zeile)

    private Map<Integer, Integer> xyValueA = new LinkedHashMap<>();
    private Map<Integer, Integer> xyValueB = new LinkedHashMap<>();
    private List<Integer> zValueA = new LinkedList<>();
    private List<Integer> zValueB = new LinkedList<>();
    private List<Coord3d> coordsA = new LinkedList<>();
    private List<Coord3d> coordsB = new LinkedList<>();
    private List<Object> objectsA = new LinkedList<>();
    private List<Object> objectsB = new LinkedList<>();
    private double meanA;
    private double meanB;
    private int numRows = 0;

    Gatherer() throws IOException {
        xyValueA = getXYValues('A');
        xyValueB = getXYValues('B');
        getZValues();
        meanA = mean(zValueA);
        meanB = mean(zValueB);
        coordsA = getCoords('A', numRows,3000,0,0);
        coordsB = getCoords('B', numRows,3000,0,0);

        Object testObj = new Object(coordsB.get(1), 2);
//        System.out.println("MeanA: " + meanA + " | MeanB: " + meanB);

//        objectAarr.forEach(x -> System.out.println(x[0] + " - " + x[1]));
//        objectBarr.forEach(x -> System.out.println(x[0] + " - " + x[1]));
    }

    private void getZValues() throws IOException {
        int count = 1;
        Reader data = new FileReader("Data/data.csv");
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);

        for (CSVRecord row : rows) {
            if (xyValueA.containsKey(count)) {
                zValueA.add(Integer.parseInt(row.get(xyValueA.get(count) - 1).replace(".", "")));
            }
            if (xyValueB.containsKey(count)) {
                zValueB.add(Integer.parseInt(row.get(xyValueB.get(count) - 1).replace(".", "")));
            }
            count++;
            numRows++;
        }

    }

    private LinkedHashMap getXYValues(char object) throws IOException {
        Reader data = new FileReader("Data/" + object + "0.csv");
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);
        Map<Integer, Integer> unsorted = new HashMap<>();
        Map<Integer, Integer> sorted = new LinkedHashMap<>();
        for (CSVRecord row : rows) {
            Integer rowValue = Integer.parseInt(row.get(1));
            Integer columnValue = Integer.parseInt(row.get(0));
            if (object == 'A' || object == 'B') {
                unsorted.put(rowValue, columnValue);
            } else {
                System.err.println("Methode mit fehlerhaften Parametern genutzt!");
            }
        }
        unsorted.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> sorted.put(x.getKey(), x.getValue()));
        return (LinkedHashMap) sorted;
    }

    private int mean(List<Integer> list) {
        int res = 0;
        for (int i : list) {
            res = res + i;
        }
        return res / list.size();
    }

    public List<Coord3d> getCoords(char type, int xSize, int ySize, int xStart, int yStart) {
        List<Coord3d> list = new LinkedList<>();
        List<Integer> object = new LinkedList<>();
        Map<Integer, Integer> map = new LinkedHashMap<>();
        if (type == 'A') {
            map = xyValueA;
            object = zValueA;
        } else {
            map = xyValueB;
            object = zValueB;
        }
        int count = 0;

        for (Map.Entry<Integer,Integer> entry : map.entrySet()) {
            if (entry.getKey() <= xSize &&
                entry.getKey() > xStart &&
                entry.getValue() > yStart &&
                entry.getValue() <= ySize)
            {
                list.add(new Coord3d(entry.getKey() - xStart, entry.getValue() - yStart, object.get(count)));
                System.out.println(type + ": " + entry.getKey() + "-" + entry.getValue() + "-" + object.get(count));
            }
            count++;
        }

        return list;
    }

    public static List<List<Integer>> getMatrix(int xStart, int xSize, int yStart, int ySize) throws IOException {
        int count = 0;
        List<List<Integer>> dataList = new LinkedList<>();

        Reader data = new FileReader("Data/data.csv");
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);
        for (CSVRecord row : rows) {
            List<Integer> dataListInner = new LinkedList<>();
            for (int i = yStart; i <= ySize; i++) {
                if (count >= xStart) dataListInner.add(Integer.parseInt(row.get(i).replace(".", "")));
            }
            if (count >= xStart) dataList.add(dataListInner);
            if (count == xSize) break;
            count++;
        }
        System.out.println(dataList.size() + " | " + dataList.get(0).size());
        return dataList;
    }

    public List<Coord3d> getCoordsA() {
        return coordsA;
    }

    public List<Coord3d> getCoordsB() {
        return coordsB;
    }
}
