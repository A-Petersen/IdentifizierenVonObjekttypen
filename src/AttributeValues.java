public class AttributeValues {

    private int As;
    private int Bs;

    private double PA;
    private double PB;

    private double PFlatA;
    private double PCanyonA;
    private double PSymAw;
    private double PSymAs;
    private double PVolA;

    private double PFlatB;
    private double PCanyonB;
    private double PSymBw;
    private double PSymBs;
    private double PVolB;

    public AttributeValues(int As, int Bs, double PA, double PB,
                           double PFlatA, double PCanyonA, double PSymAw, double PSymAs, double PVolA,
                           double PFlatB, double PCanyonB, double PSymBw, double PSymBs, double PVolB) {
        this.As = As;
        this.Bs = Bs;
        this.PA = PA;
        this.PB = PB;
        this.PFlatA = PFlatA;
        this.PCanyonA = PCanyonA;
        this.PSymAw = PSymAw;
        this.PSymAs = PSymAs;
        this.PVolA = PVolA;
        this.PFlatB = PFlatB;
        this.PCanyonB = PCanyonB;
        this.PSymBw = PSymBw;
        this.PSymBs = PSymBs;
        this.PVolB = PVolB;
    }

    public void printAttrValues() {
        System.out.println("\n\nP(A) = " + PA + "\tP(B) = " + PB + "\t of " + (As + Bs) + " Objects [A=" + As + "] B[" + Bs + "]");
        System.out.println("P(Volume|A) = " + PVolA + "\nP(Flat|A) = " + PFlatA + "\nP(Canyon|A) = " + PCanyonA + "\nP(SymWeak|A) = " + PSymAw + "\nP(SymStrong|A) = " + PSymAs);
        System.out.println("P(Volume|B) = " + PVolB + "\nP(Flat|B) = " + PFlatB + "\nP(Canyon|B) = " + PCanyonB + "\nP(SymWeak|B) = " + PSymBw + "\nP(SymStrong|B) = " + PSymBs);
    }

    public double getPA() {
        return PA;
    }

    public double getPB() {
        return PB;
    }

    public double getPFlatA() {
        return PFlatA;
    }

    public double getPCanyonA() {
        return PCanyonA;
    }

    public double getPSymAw() {
        return PSymAw;
    }

    public double getPSymAs() {
        return PSymAs;
    }

    public double getPVolA() {
        return PVolA;
    }

    public double getPFlatB() {
        return PFlatB;
    }

    public double getPCanyonB() {
        return PCanyonB;
    }

    public double getPSymBw() {
        return PSymBw;
    }

    public double getPSymBs() {
        return PSymBs;
    }

    public double getPVolB() {
        return PVolB;
    }
}
