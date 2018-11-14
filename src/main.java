import org.jzy3d.analysis.AnalysisLauncher;

public class main {
    public static void main(String[] args) throws Exception {
        AnalysisLauncher.open(new Test(new View(9, 20, 'A')));
        AnalysisLauncher.open(new Test(new View(10, 20, 'A')));
        AnalysisLauncher.open(new Test(new View(11, 20, 'A')));
        AnalysisLauncher.open(new Test(new View(9, 20, 'B')));
        AnalysisLauncher.open(new Test(new View(10, 20, 'B')));
        AnalysisLauncher.open(new Test(new View(11, 20, 'B')));
    }
}
