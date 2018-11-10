import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jzy3d.maths.Coord3d;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Gatherer {
    public static void main(String[] args) throws Exception {
        Gatherer g = new Gatherer();
    }

    // Koordinaten (Spalte,Zeile)

    private Map<Integer, Integer> A = new LinkedHashMap<>();
    private Map<Integer, Integer> B = new LinkedHashMap<>();
    private List<Integer> objectA = new LinkedList<>();
    private List<String[]> objectAarr = new LinkedList<>();
    private List<Integer> objectB = new LinkedList<>();
    private List<String[]> objectBarr = new LinkedList<>();
    private double meanA;
    private double meanB;

    public Gatherer() throws IOException {
        A = createObjectCoord('A');
        B = createObjectCoord('B');
//        System.out.println(A + "\n" + B);
        createObjectLists();
//        System.out.println(objectA + "\n"  + objectB);
        meanA = mean(objectA);
        meanB = mean(objectB);
//        System.out.println("MeanA: " + meanA + " | MeanB: " + meanB);

//        objectAarr.forEach(x -> System.out.println(x[0] + " - " + x[1]));
//        objectBarr.forEach(x -> System.out.println(x[0] + " - " + x[1]));

    }

    public void createObjectLists() throws IOException {
        int count = 0;
        Reader data = new FileReader("Data/data.csv");
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);
        for (CSVRecord row : rows) {
//            if (count == 3) System.out.println("TEST [2][0]: " + row.get(0));
            if (A.containsKey(count)) {
                objectA.add(Integer.parseInt(row.get(A.get(count) - 1).replace(".", "")));
            }
            if (B.containsKey(count)) {
                objectB.add(Integer.parseInt(row.get(B.get(count) - 1).replace(".", "")));
            }
            count++;
        }
    }

    private LinkedHashMap createObjectCoord(char object) throws IOException {
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
            map = A;
            object = objectA;
        } else {
            map = B;
            object = objectB;
        }
        int count = 0;

        for (Map.Entry<Integer,Integer> entry : map.entrySet()) {
            if (entry.getKey() <= xSize &&
                entry.getKey() > xStart &&
                entry.getValue() > yStart &&
                entry.getValue() <= ySize)
            {
                list.add(new Coord3d(entry.getKey() - xStart, entry.getValue() - yStart, object.get(count) + 10000));
                System.out.println(type + ": " + entry.getKey() + "-" + entry.getValue() + "-" + object.get(count));
            }
            count++;
        }

        return list;
    }
}
