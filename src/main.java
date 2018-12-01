import org.jzy3d.analysis.AnalysisLauncher;

public class main {
    public static void main(String[] args) throws Exception {
        String a0 = "Data/A0.csv";
        String b0 = "Data/B0.csv";
        String a1 = "Data/A1.csv";
        String b1 = "Data/B1.csv";
        String data = "Data/data.csv";

        AnalysisLauncher.open(new Test(new View(0, 20, 'A', a0, b0, data), true));
        AnalysisLauncher.open(new Test(new View(0, 20, 'B', a0, b0, data), true));
        AnalysisLauncher.open(new Test(new View(400, 580, 200, 380, a0, b0, data), false));


        AttributeValues maxByMax = new AttributeValues(979, 405, 0.7073699421965318, 0.29263005780346824,
                0.09397344228804903, 0.2839632277834525, 0.4198161389172625, 0.056179775280898875, 0.0,
                0.2691358024691358, 0.5456790123456791, 0.15555555555555556, 0.019753086419753086, 0.0);

        // DataSize: [4943 x 3000]
        Gatherer gathererSpecified = new Gatherer(true, a0, b0, data,
                200, 700,
                200, 700,
                40,
                true
        );
        gathererSpecified.createAttributes();
        AttributeValues attrSpecified = gathererSpecified.getAttributeValues();
        attrSpecified.printAttrValues();
        gathererSpecified.calculateObjects(attrSpecified);

        Gatherer gatherer = new Gatherer(true, a0, b0, data,
                40,
                true
        );

        gatherer.createAttributes();
        AttributeValues attr = gatherer.getAttributeValues();
        attr.printAttrValues();
        gatherer.calculateObjects(attr);

        maxByMax.printAttrValues();
        gatherer.calculateObjects(maxByMax);
        gathererSpecified.calculateObjects(maxByMax);
    }
}
