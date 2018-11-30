import org.jzy3d.analysis.AnalysisLauncher;

public class main {
    public static void main(String[] args) throws Exception {
        AnalysisLauncher.open(new Test(new View(0, 20, 'A'), true));
        AnalysisLauncher.open(new Test(new View(0, 20, 'B'), true));
        AnalysisLauncher.open(new Test(new View(400, 580, 200, 380), false));


        AttributeValues maxByMax = new AttributeValues(979, 405, 0.7073699421965318, 0.29263005780346824,
                0.09397344228804903, 0.2839632277834525, 0.4198161389172625, 0.056179775280898875, 0.0,
                0.2691358024691358, 0.5456790123456791, 0.15555555555555556, 0.019753086419753086, 0.0);

        Gatherer gatherer = new Gatherer(true, "Data/A0.csv", "Data/B0.csv", "Data/data.csv");
        gatherer.createAttributes();
        gatherer.calculateObjects(gatherer.getAttributeValues());

        gatherer.calculateObjects(maxByMax);
    }
}
