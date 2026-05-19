package org.example.client.fx.canvas;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.client.fx.i18n.I18n;
import org.example.data.City;
import org.example.data.Coordinates;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Координатная плоскость: оси X/Y, сетка, города — круги в точках (x, y).
 */
public class CityCanvasPane extends Pane {

    private static final double MIN_RADIUS = 5;
    private static final double MAX_RADIUS = 36;
    private static final int MAX_INDIVIDUAL_CIRCLES = 800;

    private static final Color GRID_COLOR = Color.rgb(200, 210, 220);
    private static final Color AXIS_COLOR = Color.rgb(60, 70, 90);
    private static final Color PLOT_BORDER_COLOR = Color.rgb(140, 150, 165);

    private final Map<Long, Circle> shapesById = new HashMap<>();
    private final Group plotLayer = new Group();
    private final Pane citiesLayer = new Pane();
    private List<City> cities = List.of();
    private Long selectedCityId;
    private Consumer<City> onCitySelected;

    public CityCanvasPane() {
        setMinSize(320, 240);
        setStyle("-fx-background-color: #f8f9fb;");
        getChildren().addAll(plotLayer, citiesLayer);
        widthProperty().addListener((o, a, b) -> redraw());
        heightProperty().addListener((o, a, b) -> redraw());
    }

    public void setOnCitySelected(Consumer<City> handler) {
        this.onCitySelected = handler;
    }

    public void setSelectedCityId(Long cityId) {
        this.selectedCityId = cityId;
        updateSelectionStroke();
    }

    public void setCities(List<City> newCities) {
        setCities(newCities, false);
    }

    /** Обновить список городов на плоскости и перерисовать. */
    public void setCities(List<City> newCities, boolean animateAppear) {
        this.cities = List.copyOf(newCities);
        redraw();
        if (animateAppear) {
            for (Circle circle : shapesById.values()) {
                FadeTransition fade = new FadeTransition(Duration.millis(300), circle);
                fade.setFromValue(0);
                fade.setToValue(1);
                fade.play();
            }
        }
    }

    private void redraw() {
        plotLayer.getChildren().clear();
        citiesLayer.getChildren().clear();
        citiesLayer.setClip(null);
        shapesById.clear();
        if (getWidth() < 10 || getHeight() < 10) {
            return;
        }

        CoordinatePlaneTransform plane = CoordinatePlaneTransform.forPane(getWidth(), getHeight());
        drawPlotFrame(plane);
        drawGrid(plane);
        drawAxes(plane);
        drawAxisLabels(plane);

        if (cities.isEmpty()) {
            return;
        }

        double maxArea = cities.stream()
                .mapToDouble(City::getArea)
                .max()
                .orElse(1);
        if (maxArea < 1e-9) {
            maxArea = 1;
        }
        final double areaScaleMax = maxArea;

        Map<String, List<City>> byCoordinate = groupByCoordinate(cities);
        applyPlotClip(plane);

        int drawnIndividuals = 0;
        for (List<City> cluster : byCoordinate.values()) {
            if (cluster.isEmpty()) {
                continue;
            }
            Coordinates coords = cluster.get(0).getCoordinates();
            if (coords == null) {
                continue;
            }
            if (cluster.size() > CoordinateClusterLayout.AGGREGATE_THRESHOLD) {
                drawClusterMarker(cluster, plane, areaScaleMax);
                continue;
            }
            for (City city : cluster) {
                if (drawnIndividuals >= MAX_INDIVIDUAL_CIRCLES) {
                    break;
                }
                drawCityCircle(city, cluster, plane, areaScaleMax);
                drawnIndividuals++;
            }
        }
    }

    private void applyPlotClip(CoordinatePlaneTransform plane) {
        double left = plane.getOriginScreenX();
        double bottom = plane.getOriginScreenY();
        double right = plane.plotRightX();
        double top = plane.plotTopY();
        double w = Math.max(1, right - left);
        double h = Math.max(1, bottom - top);
        citiesLayer.setClip(new Rectangle(left, top, w, h));
    }

    private void drawClusterMarker(List<City> cluster, CoordinatePlaneTransform plane, double areaScaleMax) {
        City rep = cluster.get(0);
        Coordinates coords = rep.getCoordinates();
        double cx = plane.toScreenX(coords.getX());
        double cy = plane.toScreenY(coords.getY());
        int n = cluster.size();
        double radius = Math.min(32, 14 + Math.log10(n) * 6);

        Circle circle = new Circle(cx, cy, radius);
        circle.setFill(OwnerColors.forOwner(rep.getOwnerLogin()));
        boolean repSelected = selectedCityId != null && cluster.stream()
                .anyMatch(c -> selectedCityId.equals(c.getId()));
        if (repSelected) {
            circle.setStrokeWidth(3);
        } else {
            circle.setStrokeWidth(1);
        }
        circle.setStroke(OwnerColors.forOwner(rep.getOwnerLogin()).darker());
        circle.setCursor(Cursor.HAND);

        String countText = "×" + n;
        Text countLabel = new Text(countText);
        countLabel.setFill(Color.web("#1a1a2e"));
        countLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        countLabel.setX(cx - countText.length() * 3.5);
        countLabel.setY(cy + 4);
        countLabel.setMouseTransparent(true);

        circle.setOnMouseClicked(e -> {
            if (onCitySelected != null) {
                onCitySelected.accept(rep);
            }
            showClusterInfo(cluster);
            e.consume();
        });

        citiesLayer.getChildren().addAll(circle, countLabel);
        for (City c : cluster) {
            if (c.getId() != null) {
                shapesById.put(c.getId(), circle);
            }
        }
    }

    private void drawCityCircle(City city, List<City> cluster, CoordinatePlaneTransform plane, double areaScaleMax) {
        Coordinates coords = city.getCoordinates();
        if (coords == null || city.getId() == null) {
            return;
        }
        int clusterSize = cluster.size();
        int indexInCluster = cluster.indexOf(city);

        double baseX = plane.toScreenX(coords.getX());
        double baseY = plane.toScreenY(coords.getY());
        double[] offset = CoordinateClusterLayout.screenOffset(indexInCluster, clusterSize);
        double cx = baseX + offset[0];
        double cy = baseY + offset[1];

        double radius = CoordinateClusterLayout.adjustRadius(
                radiusForArea(city.getArea(), areaScaleMax),
                clusterSize
        );

        Circle circle = new Circle(cx, cy, radius);
        circle.setFill(OwnerColors.forOwner(city.getOwnerLogin()));
        if (isSelected(city.getId())) {
            circle.setStrokeWidth(3);
        } else {
            circle.setStrokeWidth(0);
        }
        circle.setStroke(OwnerColors.forOwner(city.getOwnerLogin()).darker());
        circle.setCursor(Cursor.HAND);

        City bound = city;
        circle.setOnMouseClicked(e -> {
            if (onCitySelected != null) {
                onCitySelected.accept(bound);
            }
            showCityInfo(bound);
            e.consume();
        });

        citiesLayer.getChildren().add(circle);
        shapesById.put(city.getId(), circle);
    }

    private void showClusterInfo(List<City> cluster) {
        City rep = cluster.get(0);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.get("city.cluster.title"));
        alert.setHeaderText(I18n.format("city.cluster.header", cluster.size()));
        alert.setContentText(formatCityDetails(rep) + "\n\n" + I18n.get("city.cluster.hint"));
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }

    private void drawPlotFrame(CoordinatePlaneTransform plane) {
        double left = plane.getOriginScreenX();
        double bottom = plane.getOriginScreenY();
        double right = plane.plotRightX();
        double top = plane.plotTopY();
        double w = Math.max(1, right - left);
        double h = Math.max(1, bottom - top);
        Rectangle frame = new Rectangle(left, top, w, h);
        frame.setFill(Color.WHITE);
        frame.setStroke(PLOT_BORDER_COLOR);
        frame.setStrokeWidth(1);
        frame.setMouseTransparent(true);
        plotLayer.getChildren().add(frame);
    }

    private void drawGrid(CoordinatePlaneTransform plane) {
        double step = plane.getGridStep();
        double left = plane.getOriginScreenX();
        double right = plane.plotRightX();
        double bottom = plane.getOriginScreenY();
        double top = plane.plotTopY();

        for (double wx = step; wx < CoordinatePlaneTransform.WORLD_MAX_X; wx += step) {
            double sx = plane.toScreenX(wx);
            Line line = new Line(sx, bottom, sx, top);
            line.setStroke(GRID_COLOR);
            line.setMouseTransparent(true);
            plotLayer.getChildren().add(line);
        }
        for (double wy = step; wy < CoordinatePlaneTransform.WORLD_MAX_Y; wy += step) {
            double sy = plane.toScreenY(wy);
            Line line = new Line(left, sy, right, sy);
            line.setStroke(GRID_COLOR);
            line.setMouseTransparent(true);
            plotLayer.getChildren().add(line);
        }
    }

    private void drawAxes(CoordinatePlaneTransform plane) {
        double ox = plane.getOriginScreenX();
        double oy = plane.getOriginScreenY();
        double right = plane.plotRightX();
        double top = plane.plotTopY();

        Line xAxis = new Line(ox, oy, right, oy);
        xAxis.setStroke(AXIS_COLOR);
        xAxis.setStrokeWidth(1.5);
        xAxis.setMouseTransparent(true);

        Line yAxis = new Line(ox, oy, ox, top);
        yAxis.setStroke(AXIS_COLOR);
        yAxis.setStrokeWidth(1.5);
        yAxis.setMouseTransparent(true);

        plotLayer.getChildren().addAll(xAxis, yAxis);
    }

    private void drawAxisLabels(CoordinatePlaneTransform plane) {
        double step = plane.getGridStep();
        double ox = plane.getOriginScreenX();
        double oy = plane.getOriginScreenY();
        double right = plane.plotRightX();
        double top = plane.plotTopY();

        Text originLabel = tickLabel("0");
        placeLeftOfAxis(originLabel, ox, oy);
        plotLayer.getChildren().add(originLabel);

        for (double wx = step; wx <= CoordinatePlaneTransform.WORLD_MAX_X; wx += step) {
            double sx = plane.toScreenX(wx);
            Text label = tickLabel(formatTick(wx));
            placeBelowAxis(label, sx, oy);
            plotLayer.getChildren().add(label);
        }
        for (double wy = step; wy <= CoordinatePlaneTransform.WORLD_MAX_Y; wy += step) {
            double sy = plane.toScreenY(wy);
            Text label = tickLabel(formatTick(wy));
            placeLeftOfAxis(label, ox, sy);
            plotLayer.getChildren().add(label);
        }

        Text xCaption = new Text(I18n.get("canvas.axis.x"));
        xCaption.setFill(AXIS_COLOR);
        xCaption.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        xCaption.setMouseTransparent(true);
        placeBelowAxis(xCaption, right, oy + 10);
        plotLayer.getChildren().add(xCaption);

        Text yCaption = new Text(I18n.get("canvas.axis.y"));
        yCaption.setFill(AXIS_COLOR);
        yCaption.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        yCaption.setMouseTransparent(true);
        placeLeftOfAxis(yCaption, ox, top);
        plotLayer.getChildren().add(yCaption);
    }

    private static Text tickLabel(String text) {
        Text t = new Text(text);
        t.setFill(Color.rgb(90, 100, 115));
        t.setStyle("-fx-font-size: 10px;");
        t.setMouseTransparent(true);
        return t;
    }

    /** Подпись деления по оси X — по центру под линией сетки. */
    private static void placeBelowAxis(Text t, double tickX, double axisY) {
        double w = t.getLayoutBounds().getWidth();
        double h = t.getLayoutBounds().getHeight();
        t.setX(tickX - w / 2);
        t.setY(axisY + h + 2);
    }

    /** Подпись деления по оси Y — справа от левого поля, по вертикали у линии сетки. */
    private static void placeLeftOfAxis(Text t, double axisX, double tickY) {
        double w = t.getLayoutBounds().getWidth();
        double h = t.getLayoutBounds().getHeight();
        t.setX(axisX - w - 6);
        t.setY(tickY + h * 0.35);
    }

    private static String formatTick(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return I18n.formatNumber(value);
    }

    private static Map<String, List<City>> groupByCoordinate(List<City> cities) {
        Map<String, List<City>> groups = new LinkedHashMap<>();
        for (City city : cities) {
            Coordinates c = city.getCoordinates();
            if (c == null || city.getId() == null) {
                continue;
            }
            String key = CoordinateClusterLayout.coordKey(c.getX(), c.getY());
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(city);
        }
        for (List<City> group : groups.values()) {
            group.sort(Comparator.comparing(City::getId, Comparator.nullsLast(Long::compareTo)));
        }
        return groups;
    }

    private static double radiusForArea(double area, double maxArea) {
        double t = Math.sqrt(Math.max(area, 0) / maxArea);
        return MIN_RADIUS + t * (MAX_RADIUS - MIN_RADIUS);
    }

    private boolean isSelected(Long cityId) {
        if (selectedCityId == null || cityId == null) {
            return false;
        }
        return selectedCityId.equals(cityId);
    }

    private void updateSelectionStroke() {
        for (Map.Entry<Long, Circle> e : shapesById.entrySet()) {
            if (isSelected(e.getKey())) {
                e.getValue().setStrokeWidth(3);
            } else {
                e.getValue().setStrokeWidth(0);
            }
        }
    }

    private void showCityInfo(City city) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(I18n.get("city.info.title"));
        alert.setHeaderText(city.getName());
        alert.setContentText(formatCityDetails(city));
        alert.getButtonTypes().setAll(ButtonType.OK);
        alert.showAndWait();
    }

    private String formatCityDetails(City city) {
        Coordinates c = city.getCoordinates();
        String coords;
        if (c == null) {
            coords = "—";
        } else {
            coords = c.toString();
        }
        String owner;
        if (city.getOwnerLogin() == null) {
            owner = "—";
        } else {
            owner = city.getOwnerLogin();
        }
        return I18n.format("city.info.body",
                String.valueOf(city.getId()),
                coords,
                I18n.formatNumber(city.getArea()),
                I18n.formatNumber(city.getPopulation()),
                owner,
                I18n.formatDate(city.getCreationDate()));
    }
    
}
