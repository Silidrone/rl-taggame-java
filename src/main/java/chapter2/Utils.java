package chapter2;

import math.geom2d.Point2D;
import math.geom2d.Vector2D;

import java.security.SecureRandom;
import java.util.Random;

public class Utils {
    static Random rng = new SecureRandom();

    public static Point2D toPoint(Vector2D v) {
        return new Point2D(v.x(), v.y());
    }

    public static int[] toIntArray(Point2D point) {
        return new int[]{(int) point.x(), (int) point.y()};
    }

    public static int randomInt(int size) {
        return rng.nextInt(size);
    }
}
