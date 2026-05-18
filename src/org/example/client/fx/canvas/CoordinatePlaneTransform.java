package org.example.client.fx.canvas;

/**
 * Преобразование мировых координат (X вправо, Y вверх) в экранные (JavaFX: Y вниз).
 * Область построения: [0, {@link #WORLD_MAX_X}] × [0, {@link #WORLD_MAX_Y}].
 */
final class CoordinatePlaneTransform {

    static final float WORLD_MAX_X = 959f;
    static final double WORLD_MAX_Y = 613.0;

    /** Слева — подписи делений оси Y. */
    private static final double MARGIN_LEFT = 58;
    /** Снизу — подписи оси X и буква «X». */
    private static final double MARGIN_BOTTOM = 44;
    /** Сверху — подпись оси Y. */
    private static final double MARGIN_TOP = 22;
    private static final double MARGIN_RIGHT = 16;
    private static final double GRID_STEP = 100;

    private final double originScreenX;
    private final double originScreenY;
    private final double scale;

    private CoordinatePlaneTransform(double originScreenX, double originScreenY, double scale) {
        this.originScreenX = originScreenX;
        this.originScreenY = originScreenY;
        this.scale = scale;
    }

    static CoordinatePlaneTransform forPane(double width, double height) {
        double plotW = width - MARGIN_LEFT - MARGIN_RIGHT;
        double plotH = height - MARGIN_TOP - MARGIN_BOTTOM;
        double scale = Math.min(plotW / WORLD_MAX_X, plotH / WORLD_MAX_Y);
        if (scale <= 0) {
            scale = 1;
        }
        double originScreenX = MARGIN_LEFT;
        double originScreenY = height - MARGIN_BOTTOM;
        return new CoordinatePlaneTransform(originScreenX, originScreenY, scale);
    }

    double toScreenX(double worldX) {
        return originScreenX + worldX * scale;
    }

    double toScreenY(double worldY) {
        return originScreenY - worldY * scale;
    }

    double plotRightX() {
        return toScreenX(WORLD_MAX_X);
    }

    double plotTopY() {
        return toScreenY(WORLD_MAX_Y);
    }

    double getOriginScreenX() {
        return originScreenX;
    }

    double getOriginScreenY() {
        return originScreenY;
    }

    double getGridStep() {
        return GRID_STEP;
    }
}
