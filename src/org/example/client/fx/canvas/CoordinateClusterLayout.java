package org.example.client.fx.canvas;

/**
 * Раскладка нескольких городов с одинаковыми координатами: кольца вокруг точки на экране.
 */
final class CoordinateClusterLayout {

    /** Больше этого числа в одной точке — один маркер «×N», без сетки из тысяч кругов. */
    static final int AGGREGATE_THRESHOLD = 20;

    private static final double CLUSTER_BASE_PX = 10;

    private CoordinateClusterLayout() {}

    static String coordKey(float x, double y) {
        return x + "\u0000" + y;
    }

    /**
     * @param index порядковый номер города в группе (0 … count-1)
     * @param count размер группы
     * @return [offsetX, offsetY] в пикселях экрана
     */
    static double[] screenOffset(int index, int count) {
        if (count <= 1) {
            return new double[]{0, 0};
        }
        if (count > AGGREGATE_THRESHOLD) {
            return new double[]{0, 0};
        }
        int ring = 0;
        int idx = index;
        int slots = 6;
        while (idx >= slots) {
            idx -= slots;
            ring++;
            slots = 6 + ring * 4;
        }
        double ringRadius = CLUSTER_BASE_PX * (1.2 + ring * 1.1);
        double angle = 2 * Math.PI * idx / slots - Math.PI / 2;
        return new double[]{
                ringRadius * Math.cos(angle),
                -ringRadius * Math.sin(angle)
        };
    }

    /** Уменьшает радиус круга, если в одной точке много городов. */
    static double adjustRadius(double radius, int count) {
        if (count <= 1) {
            return radius;
        }
        double factor = Math.max(0.35, 1.0 / Math.sqrt(count));
        return Math.max(3, radius * factor);
    }
}
