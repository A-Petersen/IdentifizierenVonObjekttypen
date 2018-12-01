/**
 *  AttributeValues contains the values for the calculation of the object types with the Naive Bayes classifier.
 */
public class AttributeValues {

    /**
     * Amount of A objects.
     */
    private int As;
    /**
     * Amount of B objects.
     */
    private int Bs;
    /**
     * P(A)
     */
    private double PA;
    /**
     * P(B)
     */
    private double PB;
    /**
     * P(Flat|A)
     */
    private double PFlatA;
    /**
     * P(Canyon|A)
     */
    private double PCanyonA;
    /**
     * P(SymWeak|A)
     */
    private double PSymAw;
    /**
     * P(SymStrong|A)
     */
    private double PSymAs;
    /**
     * P(Volume|A)
     */
    private double PVolA;
    /**
     * P(Flat|B)
     */
    private double PFlatB;
    /**
     * P(Canyon|B)
     */
    private double PCanyonB;
    /**
     * P(SymWeak|B)
     */
    private double PSymBw;
    /**
     * P(SymStrong|B)
     */
    private double PSymBs;
    /**
     * P(Volume|B)
     */
    private double PVolB;

    /**
     * Constructor for AttributeValues.
     * @param As    Amount of A objects
     * @param Bs    Amount of B objects
     * @param PA    P(A)
     * @param PB    P(B)
     * @param PFlatA    P(Flat|A)
     * @param PCanyonA  P(Canyon|A)
     * @param PSymAw    P(SymWeak|A)
     * @param PSymAs    P(SymStrong|A)
     * @param PVolA     P(Volume|A)
     * @param PFlatB    P(Flat|B)
     * @param PCanyonB  P(Canyon|B)
     * @param PSymBw    P(SymWeak|B)
     * @param PSymBs    P(SymStrong|B)
     * @param PVolB     P(Volume|B)
     */
    AttributeValues(int As, int Bs, double PA, double PB,
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


    /**
     * Output of the AttributeValues. Similar to toString.
     */
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
