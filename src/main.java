import org.jzy3d.analysis.AnalysisLauncher;

/**
 * Executable main.
 */
public class main {
    public static void main(String[] args) throws Exception {
        // Data Path of A0, B0, A1, B1 and data.
        // DataSize: [4943 x 3000]
        String a0 = "Data/A0.csv";
        String b0 = "Data/B0.csv";
        String a1 = "Data/A1.csv";
        String b1 = "Data/B1.csv";
        String data = "Data/data.csv";

        boolean showExamples = false;

        if (showExamples) {

        // Examples for a View (First two represent a View an object, last a specified landscape).
        AnalysisLauncher.open(new DrawView(new View(0, 20, 'A', a0, b0, data), true));
        AnalysisLauncher.open(new DrawView(new View(2, 20, 'B', a0, b0, data), true));
        AnalysisLauncher.open(new DrawView(new View(400, 580, 200, 380, a0, b0, data), false));


        // Gathered AttributeValues by [4943 x 3000] data.csv and A0 + B0
        AttributeValues dataMaxA0B0AttrHardCoded = new AttributeValues(979, 405, 0.7073699421965318, 0.29263005780346824,
                0.09397344228804903, 0.2839632277834525, 0.4198161389172625, 0.056179775280898875, 0.0,
                0.2691358024691358, 0.5456790123456791, 0.15555555555555556, 0.019753086419753086, 0.0);

        // Example for a specific Gatherer
        Gatherer gathererSpecific = new Gatherer(true, a1, b1, data,
                1, 1000,
                1, 1000,
                40,
                true);
        gathererSpecific.calculateObjects(dataMaxA0B0AttrHardCoded);

        } else {

            //-----------------------------------------------------------------------------------------
            // Task "IntelligenteSysteme - Identifizieren von Objekttypen":
            // Gatherer for data.csv with A0 + B0
            Gatherer dataMaxA0B0 = new Gatherer(true, a0, b0, data,
                    40,
                    true
            );
            dataMaxA0B0.createAttributes(); // Creates the AttributeValues
            AttributeValues dataMaxA0B0Attr = dataMaxA0B0.getAttributeValues();
            dataMaxA0B0Attr.printAttrValues();

            // Gatherer for data.csv with A1 + B1
            Gatherer dataMaxA1B1 = new Gatherer(true, a1, b1, data,
                    40,
                    true
            );
            // Calculate objects with given attributes
            dataMaxA1B1.calculateObjects(dataMaxA0B0Attr);

        }
    }
}
