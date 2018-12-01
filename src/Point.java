/**
 * Class Point. Helper class to move a point through a given matrix.
 */
public class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void moveOneX() {
        x++;
    }

    public void moveOneY() {
        y++;
    }
}
