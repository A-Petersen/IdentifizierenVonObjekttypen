public class Math {

    private double xp;
    private double yp;
    private double xq;
    private double yq;
    private double m;

    public Math(double xp, double yp, double xq, double yq) {
        this.xp = xp;
        this.yp = yp;
        this.xq = xq;
        this.yq = yq;
        this.m = (yq-yp)/(xq-xp);
    }

    public double getGradient () {
        return m;
    }

}
