package org.hugoandrade.mirrordrawing;

public class Point {

    public double X;
    public double Y;

    public Point(double x, double y) {
        X = x;
        Y = y;
    }

    public double getLength() {
        return Math.sqrt(X * X + Y * Y);
    }

    public static Point subtract(Point point, Point midPoint) {

        return new Point(
                point.X - midPoint.X,
                point.Y - midPoint.Y);

    }

    public static Point rotateBy(Point point, double angle) {
        return new Point(
                Math.cos(angle) * point.X - Math.sin(angle) * point.Y,
                Math.sin(angle) * point.X + Math.cos(angle) * point.Y);
    }

    public static Point normalize(Point point) {
        return new Point(
                point.X / point.getLength(),
                point.Y / point.getLength());
    }

    public static Point add(Point pointOne, Point pointTwo) {
        return new Point(
                pointOne.X + pointTwo.X,
                pointOne.Y + pointTwo.Y);
    }

    public static Point reflect(Point point, Point normVector) {
        return Point.subtract(Point.mul(2.0 * Point.dotProduct(point, normVector), normVector), point);
        //return Point.subtract(point, Point.mul(2.0 * Point.crossProduct(point, normVector), normVector));
        /*return new Point(
                2 * (point.X * normVector.X + point.Y * normVector.Y) * normVector.X - point.X,
                2 * (point.X * normVector.X + point.Y * normVector.Y) * normVector.Y - point.Y);/**/

    }

    private static Point mul(double scale, Point vector) {
        return new Point(
                scale * vector.X,
                scale * vector.Y);
    }

    private static double crossProduct(Point pointOne, Point pointTwo) {
        return pointOne.X * pointTwo.Y - pointOne.Y * pointTwo.X;
    }

    private static double dotProduct(Point pointOne, Point pointTwo) {
        return pointOne.X * pointTwo.X + pointOne.Y * pointTwo.Y;
    }

    @Override
    public String toString() {
        return "Point{" +
                "X=" + X +
                ", Y=" + Y +
                '}';
    }
}
