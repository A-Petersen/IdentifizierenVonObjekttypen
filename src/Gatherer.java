import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jzy3d.maths.Coord3d;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

public class Gatherer {
    public static void main(String[] args) throws Exception {
        boolean xyz = true;
        Gatherer g = new Gatherer(xyz);
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

    public List<Coord3d> getCoordsA() {
        return coordsA;
    }

    public List<Coord3d> getCoordsB() {
        return coordsB;
    }

    private static int objectMatrixSize = 20;
    public static int numRows_static = 2000; // 4943
    public static int numColumns_static = 2000; // 3000

    Gatherer(boolean xyz) throws IOException {
        xyValueA = getXYValues('A');
        xyValueB = getXYValues('B');
        getZValues();
//        meanA = mean(zValueA);
//        meanB = mean(zValueB);
        coordsA = getCoords('A', numRows_static,numColumns_static,1,1, true);
        coordsB = getCoords('B', numRows_static,numColumns_static,1,1, true);

        double canyonA = objectsA.stream().filter(x -> x.getMax() > 10.0).count();
        double numFlatA = objectsA.stream().filter(Object::isFlat).count();
        double numIsMonotonicA = objectsA.stream().filter(Object::issMonotonic).count();
        double symA = objectsA.stream().filter(Object::isSymetric).count();

        double canyonB = objectsB.stream().filter(x -> x.getMax() > 10.0).count();
        double numFlatB = objectsB.stream().filter(Object::isFlat).count();
        double numIsMonotonicB = objectsB.stream().filter(Object::issMonotonic).count();
        double symB = objectsB.stream().filter(Object::isSymetric).count();

        double PA = objectsA.size() / (double)(objectsA.size() + objectsB.size());
        double PB = objectsB.size() / (double)(objectsA.size() + objectsB.size());
        System.out.println("\n\nP(A) = " + PA + "\tP(B) = " + PB + "\t of " + (objectsA.size() + objectsB.size()) + " Objects");

        double PMonoA = numIsMonotonicA / objectsA.size();
        double PFlatA = numFlatA / objectsA.size();
        double PCanyonA = canyonA / objectsA.size();
        double SymA = symA / objectsB.size();
        System.out.println("P(Mono|A) = " + PMonoA + "\nP(Flat|A) = " + PFlatA + "\nP(Canyon|A) = " + PCanyonA + "\nP(Sym|A) = " + SymA);

        double PMonoB = numIsMonotonicB / objectsB.size();
        double PFlatB = numFlatB / objectsB.size();
        double PCanyonB = canyonB / objectsB.size();
        double SymB = symB / objectsB.size();
        System.out.println("P(Mono|B) = " + PMonoB + "\nP(Flat|B) = " + PFlatB + "\nP(Canyon|B) = " + PCanyonB + "\nP(Sym|B) = " + SymB);

        objectsA.stream().forEach(x -> x.calculateType(0.16846986089644514, 0.4311111111111111, 0.8, 0.06666666666666667, 0.7419724770642202, 0.2580275229357798));
        System.out.println("\nA_right: "
                + objectsA.stream().filter(x -> x.calcRight()).count()
                + " \tB_right: "
                + objectsB.stream().filter(x -> x.calcRight()).count()
                + " \tA_false: "
                + objectsA.stream().filter(x -> !x.calcRight()).count()
                + " \tB_false: "
                + objectsB.stream().filter(x -> !x.calcRight()).count()
        );

        System.out.println("AvgHightA: " + objectsA.stream().mapToDouble(Object::getHight).average());
        System.out.println("AvgHightB: " + objectsB.stream().mapToDouble(Object::getHight).average());
    }

    Gatherer() throws IOException {
        xyValueA = getXYValues('A');
        xyValueB = getXYValues('B');
        getZValues();
    }

    private void getZValues() throws IOException {
        int count = 1;
        Reader data = new FileReader("Data/data.csv");
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);

        for (CSVRecord row : rows) {
            if (xyValueA.containsKey(count)) {
                zValueA.add(Integer.parseInt(row.get(xyValueA.get(count) -1).replace(".", "")));
            }
            if (xyValueB.containsKey(count)) {
                zValueB.add(Integer.parseInt(row.get(xyValueB.get(count) -1).replace(".", "")));
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

    public List<Coord3d> getCoords(char type, int xSize, int ySize, int xStart, int yStart, boolean createObjects) throws IOException {
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
                Coord3d coord = new Coord3d(entry.getKey() - xStart, entry.getValue() - yStart, object.get(count));
                list.add(coord);
                System.out.println("\n" + type + ": " + entry.getKey() + "-" + entry.getValue() + "-" + object.get(count) + " Index[" + (list.size() - 1) + "]");
                if (type == 'A' && createObjects) {
                    objectsA.add(new Object(coord, objectMatrixSize, 'A'));
                } else if (createObjects) {
                    objectsB.add(new Object(coord, objectMatrixSize, 'B'));
                }
            }
            count++;
        }

        return list;
    }

    public static List<List<Integer>> getMatrix(int xStart, int xSize, int yStart, int ySize) throws IOException {
        int count = 0;
        List<List<Integer>> dataList = new LinkedList<>();

        xSize = secureOutOfBound(xSize,0, 4943);
        xStart = secureOutOfBound(xStart,0, 4943);
        ySize = secureOutOfBound(ySize,0, 3000);
        yStart = secureOutOfBound(yStart,0, 3000);

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

    public static int secureOutOfBound(int x, int min, int max) {
        int res = x < min ? min : x;
        res = res > max ? max : res;
        return res;
    }
}
