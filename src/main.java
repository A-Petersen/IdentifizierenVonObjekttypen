import org.jzy3d.analysis.AnalysisLauncher;

public class main {
    public static void main(String[] args) throws Exception {
        AnalysisLauncher.open(new Test(new View(0, 20, 'A'), true));
        AnalysisLauncher.open(new Test(new View(0, 20, 'B'), true));
        AnalysisLauncher.open(new Test(new View(400, 580, 200, 380), false));
    }
}
