import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jzy3d.maths.Coord3d;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Gatherer {
    public static void main(String[] args) throws Exception {
        boolean xyz = true;
        Gatherer g = new Gatherer(xyz);
    }

    // Koordinaten (Spalte,Zeile)

    private Map<Integer, List<Integer>> xyValueA = new LinkedHashMap<>();
    private Map<Integer, List<Integer>> xyValueB = new LinkedHashMap<>();
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
    public static int numRows_static = 3000;        // 4943
    public static int numColumns_static = 3000;     // 3000

    Gatherer(boolean xyz) throws IOException {
        xyValueA = getXYValues('A');
        xyValueB = getXYValues('B');
        getZValues();
//        meanA = mean(zValueA);
//        meanB = mean(zValueB);
        coordsA = getCoords('A', numRows_static,numColumns_static,1,1, true);
        coordsB = getCoords('B', numRows_static,numColumns_static,1,1, true);

        double canyonA = objectsA.stream().filter(x -> x.getMax() > 6.0).count();
        double numFlatA = objectsA.stream().filter(Object::isFlat).count();
        double numIsMonotonicA = objectsA.stream().filter(Object::issMonotonic).count();
        double symAw = objectsA.stream().filter(Object::isWeakSymetric).count();
        double symAs = objectsA.stream().filter(Object::isStrongSymetric).count();

        double canyonB = objectsB.stream().filter(x -> x.getMax() > 6.0).count();
        double numFlatB = objectsB.stream().filter(Object::isFlat).count();
        double numIsMonotonicB = objectsB.stream().filter(Object::issMonotonic).count();
        double symBw = objectsB.stream().filter(Object::isWeakSymetric).count();
        double symBs = objectsB.stream().filter(Object::isStrongSymetric).count();

        double PA = objectsA.size() / (double)(objectsA.size() + objectsB.size());
        double PB = objectsB.size() / (double)(objectsA.size() + objectsB.size());
        System.out.println("\n\nP(A) = " + PA + "\tP(B) = " + PB + "\t of " + (objectsA.size() + objectsB.size()) + " Objects [A=" + objectsA.size() + "] B[" + objectsB.size() + "]");

        double PMonoA = numIsMonotonicA / objectsA.size();
        double PFlatA = numFlatA / objectsA.size();
        double PCanyonA = canyonA / objectsA.size();
        double SymAw = symAw / objectsA.size();
        double SymAs = symAs / objectsA.size();
        double VolA =  objectsA.stream().filter(x -> x.inHightRange > 0.55).count() / (double)objectsA.size();
        System.out.println("P(Volume|A) = " + VolA + "\nP(Flat|A) = " + PFlatA + "\nP(Canyon|A) = " + PCanyonA + "\nP(SymWeak|A) = " + SymAw + "\nP(SymStrong|A) = " + SymAs);

        double PMonoB = numIsMonotonicB / objectsB.size();
        double PFlatB = numFlatB / objectsB.size();
        double PCanyonB = canyonB / objectsB.size();
        double SymBw = symBw / objectsB.size();
        double SymBs = symBs / objectsB.size();
        double VolB =  objectsB.stream().filter(x -> x.inHightRange > 0.55).count() / (double)objectsB.size();
        System.out.println("P(Volume|B) = " + VolB + "\nP(Flat|B) = " + PFlatB + "\nP(Canyon|B) = " + PCanyonB + "\nP(SymWeak|B) = " + SymBw + "\nP(SymStrong|B) = " + SymBs);

        System.out.println(
                "\nA_right: "
                + objectsA.stream().filter(x -> x.calcRight()).count()
                + " \tB_right: "
                + objectsB.stream().filter(x -> x.calcRight()).count() + " TP[" + (objectsB.stream().filter(x -> x.calcRight()).count() / (double)objectsB.size()) + "]"
                + " \tA_false: "
                + objectsA.stream().filter(x -> !x.calcRight()).count() + " FP[" + (objectsA.stream().filter(x -> !x.calcRight()).count() / (double)objectsA.size()) + "]"
                + " \tB_false: "
                + objectsB.stream().filter(x -> !x.calcRight()).count()
        );

        System.out.println("AvgHightA: " + objectsA.stream().filter(x -> x.inHightRange > 0.5).count());
        System.out.println("AvgHightB: " + objectsB.stream().filter(x -> x.inHightRange > 0.5).count());
//
//        System.out.println("AvgFGA: " + objectsA.stream().mapToDouble(Object::getFlatGradients).average());
//        System.out.println("AvgFGB: " + objectsB.stream().mapToDouble(Object::getFlatGradients).average());

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
                xyValueA.get(count).stream().forEach(x -> zValueA.add(Integer.parseInt(row.get(x - 1).replace(".", ""))) );
//                zValueA.add(Integer.parseInt(row.get(xyValueA.get(count) -1).replace(".", "")));
            }
            if (xyValueB.containsKey(count)) {
                xyValueB.get(count).stream().forEach(x -> zValueB.add(Integer.parseInt(row.get(x - 1).replace(".", ""))) );
//                zValueB.add(Integer.parseInt(row.get(xyValueB.get(count) -1).replace(".", "")));
            }
            count++;
            numRows++;
        }
        System.out.println("ZValueA: " + zValueA.size());
        System.out.println("ZValueB: " + zValueB.size());
    }

    private LinkedHashMap getXYValues(char object) throws IOException {
        Reader data = new FileReader("Data/" + object + "0.csv");
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);
        Map<Integer, List<Integer>> unsorted = new HashMap<>();
        Map<Integer, List<Integer>> sorted = new LinkedHashMap<>();
        for (CSVRecord row : rows) {
            Integer rowValue = Integer.parseInt(row.get(1));
            Integer columnValue = Integer.parseInt(row.get(0));
            if (object == 'A' || object == 'B') {
                if (!unsorted.containsKey(rowValue)) {
                    List<Integer> list = new LinkedList<>();
                    list.add(columnValue);
                    unsorted.put(rowValue, list);
                } else {
                    unsorted.get(rowValue).add(columnValue);
                }

            } else {
                System.err.println("Methode mit fehlerhaften Parametern genutzt!");
            }
        }
        unsorted.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> sorted.put(x.getKey(), x.getValue()));
        System.out.println(object + " - " + sorted.size());
        System.out.println(sorted.values().stream().mapToInt(List::size).sum());
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
        Map<Integer, List<Integer>> map = new LinkedHashMap<>();
        if (type == 'A') {
            map = xyValueA;
            object = zValueA;
        } else {
            map = xyValueB;
            object = zValueB;
        }
        int count = 0;

        System.out.println("getCoordsSizeB: " + map.values().stream().mapToInt(List::size).sum());
        System.out.println(map);

        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            if (entry.getKey() <= xSize &&
                entry.getKey() >= xStart)
            {
                for (Integer y : entry.getValue()) {
                    if( y >= yStart &&
                            y <= ySize) {
                        Coord3d coord = new Coord3d(entry.getKey() - xStart, y - yStart, object.get(count));
                        list.add(coord);
                        System.out.println("\n------------------------------------------------------------------------------------------\n"
                                + type + ": " + entry.getKey() + "-" + entry.getValue() + "-" + object.get(count) + " Index[" + (list.size() - 1) + "]");
                        if (type == 'A' && createObjects) {
                            objectsA.add(new Object(coord, objectMatrixSize, 'A'));
                        } else if (createObjects) {
                            objectsB.add(new Object(coord, objectMatrixSize, 'B'));
                        }
                    }
                    count++;
                }
//                if( entry.getValue() > yStart &&
//                        entry.getValue() <= ySize)
//                {
//                    Coord3d coord = new Coord3d(entry.getKey() - xStart, entry.getValue() - yStart, object.get(count));
//                    list.add(coord);
//                    System.out.println("\n" + type + ": " + entry.getKey() + "-" + entry.getValue() + "-" + object.get(count) + " Index[" + (list.size() - 1) + "]");
//                    if (type == 'A' && createObjects) {
//                        objectsA.add(new Object(coord, objectMatrixSize, 'A'));
//                    } else if (createObjects) {
//                        objectsB.add(new Object(coord, objectMatrixSize, 'B'));
//                    }
//                }
//                count++;
            }

        }
        System.out.println("Count: " + count);
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
            for (int i = yStart; i < ySize; i++) {
                if (count >= xStart) dataListInner.add(Integer.parseInt(row.get(i).replace(".", "")));
            }
            if (count >= xStart) dataList.add(dataListInner);
            if (count == xSize) break;
            count++;
        }
        System.out.println("Matrix: [" + dataList.size() + " | " + dataList.get(0).size() + "]");
        return dataList;
    }

    public static int secureOutOfBound(int x, int min, int max) {
        int res = x < min ? min : x;
        res = res > max ? max : res;
        return res;
    }
}
