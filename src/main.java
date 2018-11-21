import org.jzy3d.analysis.AnalysisLauncher;

public class main {
    public static void main(String[] args) throws Exception {
        AnalysisLauncher.open(new Test(new View(12, 8, 'A'), true));
        AnalysisLauncher.open(new Test(new View(33, 8, 'A'), true));
        AnalysisLauncher.open(new Test(new View(64, 8, 'A'), true));
        AnalysisLauncher.open(new Test(new View(26, 8, 'B'), true));
        AnalysisLauncher.open(new Test(new View(28, 8, 'B'), true));
        AnalysisLauncher.open(new Test(new View(29, 8, 'B'), true));
//        AnalysisLauncher.open(new Test(new View(24, 8, 'B'), true));
//        AnalysisLauncher.open(new Test(new View(1840, 1820, 1130, 1090)));
//        AnalysisLauncher.open(new Test(new View(1000, 0, 1000, 0), false));
    }
}
