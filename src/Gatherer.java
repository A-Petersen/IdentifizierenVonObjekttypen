import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Gatherer {

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
        System.out.println(A + "\n" + B);
        createObjectLists();
        System.out.println(objectA + "\n"  + objectB);
        meanA = mean(objectA);
        meanB = mean(objectB);
        objectAarr.forEach(x -> System.out.println(x[0] + " - " + x[1]));
        objectBarr.forEach(x -> System.out.println(x[0] + " - " + x[1]));
        System.out.println("MeanA: " + meanA + " | MeanB: " + meanB);
    }

    public void createObjectLists() throws IOException {
        int count = 1;
        Reader data = new FileReader("Data/data.csv");
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);
        for (CSVRecord row : rows) {
            if (count == 3) System.out.println("TEST [2][0]: " + row.get(0));
            if (A.containsKey(count)) {
                objectA.add(Integer.parseInt(row.get(A.get(count) - 1).replace(".", "")));
                objectAarr.add(row.get(A.get(count) - 1).split("\\."));
//                System.out.println((row.get(A.get(count) - 1).split("\\.")));
            }
            if (B.containsKey(count)) {
                objectB.add(Integer.parseInt(row.get(B.get(count) - 1).replace(".", "")));
                objectBarr.add(row.get(B.get(count) - 1).split("\\."));
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

}
