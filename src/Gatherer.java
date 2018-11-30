import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jzy3d.maths.Coord3d;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Gatherer {
    public static void main(String[] args) throws Exception {
        boolean getObjects = true;
        Gatherer g = new Gatherer(getObjects);
    }

    private Map<Integer, List<Integer>> xyValueA = new LinkedHashMap<>();
    private Map<Integer, List<Integer>> xyValueB = new LinkedHashMap<>();
    private List<Integer> zValueA = new LinkedList<>();
    private List<Integer> zValueB = new LinkedList<>();
    private List<Coord3d> coordsA = new LinkedList<>();
    private List<Coord3d> coordsB = new LinkedList<>();
    private List<Object> objectsA = new LinkedList<>();
    private List<Object> objectsB = new LinkedList<>();

    private static int objectMatrixSize = 20;
    public static int numRows_static = 3000;        // 4943
    public static int numColumns_static = 3000;     // 3000

    public AttributeValues attributeValues;

    /**
     * Constructor to create and analyse the given objects.
     * @param createObjects True - create objects, False - do not create objects
     * @throws IOException
     */
    Gatherer(boolean createObjects) throws IOException {
        xyValueA = getXYValues("Data/A0.csv");
        xyValueB = getXYValues("Data/B0.csv");
        getZValues("Data/data.csv");
        coordsA = getCoords('A', numRows_static,numColumns_static,1,1, createObjects);
        coordsB = getCoords('B', numRows_static,numColumns_static,1,1, createObjects);

        createAttributes();
        attributeValues.printAttrValues();
        calculateObjects(attributeValues);
        calculateObjects(new AttributeValues(979, 405, 0.7073699421965318, 0.29263005780346824,
                0.09397344228804903, 0.2839632277834525, 0.4198161389172625, 0.056179775280898875, 0.0,
                0.2691358024691358, 0.5456790123456791, 0.15555555555555556, 0.019753086419753086, 0.0));
    }

    /**
     * Constructor to build a View without creating and analysing the objects.
     * @throws IOException
     */
    Gatherer() throws IOException {
        xyValueA = getXYValues("Data/A0.csv");
        xyValueB = getXYValues("Data/B0.csv");
        getZValues("Data/data.csv");
    }

    /**
     * Prints the attribute values and the amount of True/False-Positives given by the Gatherer.
     * Only if A and B objects were created.
     */
    public void createAttributes() {
        if (objectsA.isEmpty() || objectsB.isEmpty()) return;
        double canyonA = objectsA.stream().filter(x -> x.getMax() > 6.0).count();
        double numFlatA = objectsA.stream().filter(Object::isFlat).count();
        double symAw = objectsA.stream().filter(Object::isWeakSymetric).count();
        double symAs = objectsA.stream().filter(Object::isStrongSymetric).count();

        double canyonB = objectsB.stream().filter(x -> x.getMax() > 6.0).count();
        double numFlatB = objectsB.stream().filter(Object::isFlat).count();
        double symBw = objectsB.stream().filter(Object::isWeakSymetric).count();
        double symBs = objectsB.stream().filter(Object::isStrongSymetric).count();

        double PA = objectsA.size() / (double)(objectsA.size() + objectsB.size());
        double PB = objectsB.size() / (double)(objectsA.size() + objectsB.size());

        double PFlatA = numFlatA / objectsA.size();
        double PCanyonA = canyonA / objectsA.size();
        double PSymAw = symAw / objectsA.size();
        double PSymAs = symAs / objectsA.size();
        double PVolA =  objectsA.stream().filter(x -> x.getInHightRange() > 0.55).count() / (double)objectsA.size();

        double PFlatB = numFlatB / objectsB.size();
        double PCanyonB = canyonB / objectsB.size();
        double PSymBw = symBw / objectsB.size();
        double PSymBs = symBs / objectsB.size();
        double PVolB =  objectsB.stream().filter(x -> x.getInHightRange() > 0.55).count() / (double)objectsB.size();

        attributeValues = new AttributeValues(
                objectsA.size(), objectsB.size(), PA, PB,
                PFlatA, PCanyonA, PSymAw, PSymAs, PVolA,
                PFlatB, PCanyonB, PSymBw, PSymBs, PVolB
        );
    }

    public void calculateObjects(AttributeValues attributeValues) {
        objectsA.stream().forEach(x -> x.calculateType(attributeValues));
        objectsB.stream().forEach(x -> x.calculateType(attributeValues));
        System.out.println(
                "\nA_right: "
                        + objectsA.stream().filter(x -> x.calcRight()).count()
                        + " \tA_false: "
                        + objectsA.stream().filter(x -> !x.calcRight()).count() + " FP[" + (objectsA.stream().filter(x -> !x.calcRight()).count() / (double)objectsA.size()) + "]"
                        + " \nB_right: "
                        + objectsB.stream().filter(x -> x.calcRight()).count() + " TP[" + (objectsB.stream().filter(x -> x.calcRight()).count() / (double)objectsB.size()) + "]"
                        + " \tB_false: "
                        + objectsB.stream().filter(x -> !x.calcRight()).count()
        );
    }

    /**
     * Creates a Map [Map<Integer, List<Integer>>] with the given X-Y pairs by the CSV-File.
     * CSV-File need the following format:
     * Column, row\nColumn, row\n...
     * Example data:
     * 3,2\n4,2\n1,3\n
     * creates -> {2={3,4},3={1}}
     * @param dataPath  Path of the CSV-File
     * @return Map<Integer, List<Integer>> with X<List<Y>>
     * @throws IOException
     */
    private LinkedHashMap getXYValues(String dataPath) throws IOException {
        Reader data = new FileReader(dataPath);
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);
        Map<Integer, List<Integer>> unsorted = new HashMap<>();
        Map<Integer, List<Integer>> sorted = new LinkedHashMap<>();
        for (CSVRecord row : rows) {    // Iterates through the CSV-File row by row (does not read the whole file into memory)
            Integer rowValue = Integer.parseInt(row.get(1));
            Integer columnValue = Integer.parseInt(row.get(0));
                if (!unsorted.containsKey(rowValue)) {  // If X-Value is not already considered create a new list for Y-Values and add it.
                    List<Integer> list = new LinkedList<>();
                    list.add(columnValue);
                    unsorted.put(rowValue, list);
                } else {    // Add the Y-Value to the desired X-Value
                    unsorted.get(rowValue).add(columnValue);
                }
        }
        // Sort the created map by the X-Values.
        unsorted.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(x -> sorted.put(x.getKey(), x.getValue()));
        return (LinkedHashMap) sorted;
    }

    /**
     * Fills the Gatherer lists [List<Integer> zValueA] and [List<Integer> zValueB] with their desired values
     * given the CSV-File.
     * Needs the X-Y coordinates out of [Map<Integer, List<Integer>> xyValueA] and [Map<Integer, List<Integer>> xyValueB]
     * @param dataPath  Path of the CSV-File
     * @throws IOException
     */
    private void getZValues(String dataPath) throws IOException {
        int count = 1;
        Reader data = new FileReader(dataPath);
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);

        for (CSVRecord row : rows) {
            if (xyValueA.containsKey(count)) {
                xyValueA.get(count).stream().forEach(x -> zValueA.add(Integer.parseInt(row.get(x - 1).replace(".", ""))) );
            }
            if (xyValueB.containsKey(count)) {
                xyValueB.get(count).stream().forEach(x -> zValueB.add(Integer.parseInt(row.get(x - 1).replace(".", ""))) );
            }
            count++;
        }
    }

    /**
     * Creates a List of coordinates in a specific field of the X-Y-Z matrix. Only X and Y can be parameterized.
     * Merges the data of [Map<Integer, List<Integer>> xyValue] and [List<Integer> zValue] to create the coordinates.
     * @param type      Type of the object as char ('A' or 'B')
     * @param xStart    Startpoint in X
     * @param xSize     Size of X
     * @param yStart    Startpoint in Y
     * @param ySize     Size of Y
     * @param createObjects    Should the object list be filled ? (True/False) High run-time!
     * @return  List<Coord3d> the list of the parameterized coordinates
     * @throws IOException
     */
    public List<Coord3d> getCoords(char type, int xSize, int ySize, int xStart, int yStart, boolean createObjects) throws IOException {
        int count = 0;
        List<Coord3d> list = new LinkedList<>();
        List<Integer> object = new LinkedList<>();
        Map<Integer, List<Integer>> map = new LinkedHashMap<>();
        if (type == 'A') {  // Decides which lists (A or B) are necessary for further computation.
            map = xyValueA;
            object = zValueA;
        } else {
            map = xyValueB;
            object = zValueB;
        }

        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {    // Iterate X
            if (entry.getKey() <= xSize &&              // Check if X has to be considered.
                entry.getKey() >= xStart)
            {
                for (Integer y : entry.getValue()) {    // Iterate Y in current X
                    if( y >= yStart &&                  // Check if Y has to be considered.
                            y <= ySize) {
                        Coord3d coord = new Coord3d(entry.getKey() - xStart, y - yStart, object.get(count));
                        list.add(coord);

                        System.out.println("\n------------------------------------------------------------------------------------------\n"
                                + type + ": " + entry.getKey() + "-" + y + "-" + object.get(count) + " Index[" + (list.size() - 1) + "]");

                        if (type == 'A' && createObjects) { // Create the object lists.
                            objectsA.add(new Object(coord, objectMatrixSize, 'A'));
                        } else if (createObjects) {
                            objectsB.add(new Object(coord, objectMatrixSize, 'B'));
                        }
                    }
                    count++;
                }
            } else {    // If coordinate is not in our desired matrix, count the necessary steps.
                count = count + entry.getValue().size();
            }

        }
        return list;
    }

    /**
     * Get thr trained attribute values out of the Gatherer.
     * @return Class - AttributeValues
     */
    public AttributeValues getAttributeValues() {
        return attributeValues;
    }

    /**
     * Builds a X-Y-Z matrix out of an CSV-File within the given field.
     * Does only support a matrix of 4943 by 3000 without failure. These parameters can be changed inside the method.
     * @param xStart    Startpoint in X
     * @param xStop     Size of X
     * @param yStart    Startpoint in Y
     * @param yStop     Size of Y
     * @param dataPath  Path of the CSV-File
     * @return  List<List<Integer>> where X<Y<Z>>
     * @throws IOException
     */
    public static List<List<Integer>> getMatrix(int xStart, int xStop, int yStart, int yStop, String dataPath) throws IOException {
        // Secure parameters to be correct, if they are incorrect, correct them.
        xStop = secureOutOfBound(xStop, 1, 4943);
        xStart = secureOutOfBound(xStart, 1, 4943);
        yStop = secureOutOfBound(yStop, 1, 3000);
        yStart = secureOutOfBound(yStart, 1, 3000);

        int count = 0;
        List<List<Integer>> dataList = new LinkedList<>();             // Represents the matrix. (X-Y-Z)
        Reader data = new FileReader(dataPath);                        // Main .csv data origin.
        Iterable<CSVRecord> rows = CSVFormat.EXCEL.parse(data);
        for (CSVRecord row : rows) {                                   // Iterates through the CSV-File row by row (does not read the whole file into memory)
            List<Integer> dataListInner = new LinkedList<>();
            for (int i = yStart -1; i < yStop; i++) {                  // Iterates through Y (row) checks if the Y coordinate has to be considered.
                //TODO: ??? war ohne -1, 41 | 40  (41|41 hat das ergebnis leicht verschlechtert, warum?)
                if (count >= xStart) dataListInner.add(Integer.parseInt(row.get(i).replace(".", "")));
            }
            if (count >= xStart) dataList.add(dataListInner);          // Checks if X has to be considered, if then add the list.
            if (count == xStop) break;
            count++;
        }
        System.out.println("MatrixSize X-Y: [" + dataList.size() + " | " + dataList.get(0).size() + "]");
        return dataList;
    }

    /**
     * Secures an out of bound case.
     * @param x     Number to be secured
     * @param min   Minimum number to be returned
     * @param max   Maximum number to be returned
     * @return  Number
     */
    public static int secureOutOfBound(int x, int min, int max) {
        int res = x < min ? min : x;
        res = res > max ? max : res;
        return res;
    }
}
