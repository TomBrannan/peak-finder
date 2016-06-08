
import java.awt.geom.Point2D;
import java.util.Comparator;

/**
 * Comparator for Point2D.Double objects.
 * X values take priority over Y values.
 * 
 * @author Tom
 */
public class CoordinateComparator implements Comparator<Point2D.Double> {

    @Override
    public int compare(Point2D.Double o1, Point2D.Double o2) {
        if (o1.getX() < o2.getX()) {
            return -1;
        }
        if (o1.getX() > o2.getX()) {
            return 1;
        }
        if (o1.getY() < o2.getY()) {
            return -1;
        }
        if (o1.getY() > o2.getY()) {
            return 1;
        }
        return 0;
    }
}
