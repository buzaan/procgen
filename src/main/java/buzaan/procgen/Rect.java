
package buzaan.procgen;

/**
 *
 * @author buzaan
 */
public class Rect {
    public final int x1;
    public final int y1;
    public final int x2;
    public final int y2;

    Rect(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    static Rect enclosing(Rect r1, Rect r2) {
        int x1 = Math.min(r1.x1, r2.x1);
        int y1 = Math.min(r1.y1, r2.y1);
        int x2 = Math.max(r1.x2, r2.x2);
        int y2 = Math.max(r1.y2, r2.y2);
        return new Rect(x1, y1, x2, y2);
    }
}
