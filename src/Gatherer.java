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
    public static int numRows_static = 1000; // 4943
    public static int numColumns_static = 1000; // 3000

    Gatherer(boolean xyz) throws IOException {
        xyValueA = getXYValues('A');
        xyValueB = getXYValues('B');
        getZValues();
//        meanA = mean(zValueA);
//        meanB = mean(zValueB);
        coordsA = getCoords('A', numRows_static,numColumns_static,1,1, true);
        coordsB = getCoords('B', numRows_static,numColumns_static,1,1, true);

//        System.out.println("\nMax A: " + objectsA.stream().max(Comparator.comparing(Object::getMax)).get().getMax());
//        System.out.println("Min A: " + objectsA.stream().max(Comparator.comparing(Object::getMin)).get().getMin());
//        System.out.println("Avg A: " + objectsA.stream().mapToDouble(Object::getMax).average().getAsDouble());
        double canyonA = objectsA.stream().filter(x -> x.getMax() > 10.0).count();
//        System.out.println("Filter A - max < 10:\t" + canyonA + " out of " + objectsA.size());
        double numFlatA = objectsA.stream().filter(x -> x.isFlat()).count();
//        System.out.println("Filter A - Flat:\t" + numFlatA + " out of " + objectsA.size());
        double numIsMonotonicA = objectsA.stream().filter(x -> x.issMonotonic()).count();
        double symA = objectsA.stream().filter(x -> x.isSymetric()).count();
//        System.out.println("Filter A - monotonic:\t" + numIsMonotonicA + " out of " + objectsA.size());
//        System.out.println("Filter A - monotonic && max < 10 && not Flat:\t" + objectsA.stream()
//                .filter(x -> x.issMonotonic())
//                .filter(x -> x.getMax() < 5.0)
//                .filter(x -> !x.isFlat())
//                .count() + " out of " + objectsA.size());

//        System.out.println("Max B: " + objectsB.stream().max(Comparator.comparing(Object::getMax)).get().getMax());
//        System.out.println("Min B: " + objectsB.stream().max(Comparator.comparing(Object::getMin)).get().getMin());
//        System.out.println("Avg B: " + objectsB.stream().mapToDouble(Object::getMax).average().getAsDouble());
        double canyonB = objectsB.stream().filter(x -> x.getMax() > 10.0).count();
//        System.out.println("Filter B - max < 10:\t" + canyonB + " out of " + objectsB.size());
        double numFlatB = objectsB.stream().filter(x -> x.isFlat()).count();
//        System.out.println("Filter B - Flat:\t" + numFlatB + " out of " + objectsB.size());
        double numIsMonotonicB = objectsB.stream().filter(x -> x.issMonotonic()).count();
        double symB = objectsB.stream().filter(x -> x.isSymetric()).count();
//        System.out.println("Filter B - monotonic:\t" + numIsMonotonicB + " out of " + objectsB.size());

        double PA = objectsA.size() / (double)(objectsA.size() + objectsB.size());
        double PB = objectsB.size() / (double)(objectsA.size() + objectsB.size());
        System.out.println("P(A) = " + PA + "\tP(B) = " + PB + "\t of " + objectsA.size() + " Objects");

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
//        System.out.println("Filter B - monotonic && max < 10 && not Flat:\t" + objectsB.stream()
//                .filter(x -> x.issMonotonic())
//                .filter(x -> x.getMax() < 5.0)
//                .filter(x -> !x.isFlat())
//                .count() + " out of " + objectsB.size());
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
