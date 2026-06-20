package com.kse100.ui;

import com.kse100.model.DataRecord;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import java.util.List;

/**
 * Manages synchronized crosshairs across two LineCharts.
 * When the user hovers anywhere on either chart, a vertical dashed line and floating label
 * tooltip appear on both charts at the corresponding x-position (date/timeline index).
 */
public class ChartCrosshair {
    private final LineChart<Number, Number> chart1;
    private final LineChart<Number, Number> chart2;
    private final Pane overlay1;
    private final Pane overlay2;
    private List<DataRecord> filteredRecords;
    private List<DataRecord> smaRecords;
    private List<DataRecord> emaRecords;
    private List<DataRecord> trendRecords;

    // Crosshair nodes for chart 1
    private final Line line1;
    private final Label tooltip1;

    // Crosshair nodes for chart 2
    private final Line line2;
    private final Label tooltip2;

    public ChartCrosshair(LineChart<Number, Number> chart1, Pane overlay1,
                          LineChart<Number, Number> chart2, Pane overlay2) {
        this.chart1 = chart1;
        this.chart2 = chart2;
        this.overlay1 = overlay1;
        this.overlay2 = overlay2;

        // Ensure overlays do not block mouse clicks / interactions on the chart itself
        overlay1.setMouseTransparent(true);
        overlay2.setMouseTransparent(true);

        // Initialize elements for chart 1 overlay
        this.line1 = new Line();
        this.line1.getStyleClass().add("crosshair-line");
        this.line1.setVisible(false);

        this.tooltip1 = new Label();
        this.tooltip1.getStyleClass().add("crosshair-tooltip");
        this.tooltip1.setVisible(false);

        overlay1.getChildren().addAll(line1, tooltip1);

        // Initialize elements for chart 2 overlay
        this.line2 = new Line();
        this.line2.getStyleClass().add("crosshair-line");
        this.line2.setVisible(false);

        this.tooltip2 = new Label();
        this.tooltip2.getStyleClass().add("crosshair-tooltip");
        this.tooltip2.setVisible(false);

        overlay2.getChildren().addAll(line2, tooltip2);

        // Setup mouse listeners on both charts
        setupHoverHandlers(chart1, (NumberAxis) chart1.getXAxis());
        setupHoverHandlers(chart2, (NumberAxis) chart2.getXAxis());
    }

    /**
     * Updates references to current datasets/record lists.
     */
    public void updateData(List<DataRecord> filteredRecords, 
                           List<DataRecord> smaRecords, 
                           List<DataRecord> emaRecords,
                           List<DataRecord> trendRecords) {
        this.filteredRecords = filteredRecords;
        this.smaRecords = smaRecords;
        this.emaRecords = emaRecords;
        this.trendRecords = trendRecords;
        hideCrosshairs();
    }

    private void setupHoverHandlers(LineChart<Number, Number> chart, NumberAxis xAxis) {
        chart.setOnMouseMoved(e -> handleMouseMove(e, xAxis));
        chart.setOnMouseExited(e -> hideCrosshairs());
    }

    private void handleMouseMove(MouseEvent event, NumberAxis activeXAxis) {
        if (filteredRecords == null || filteredRecords.isEmpty()) {
            hideCrosshairs();
            return;
        }

        // 1. Get x-coordinate relative to the axis
        Point2D mouseInAxis = activeXAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
        double xVal = activeXAxis.getValueForDisplay(mouseInAxis.getX()).doubleValue();
        int idx = (int) Math.round(xVal);

        if (idx < 0 || idx >= filteredRecords.size()) {
            hideCrosshairs();
            return;
        }

        // 2. Update crosshair on both charts
        updateCrosshair(chart1, overlay1, line1, tooltip1, idx, true);
        updateCrosshair(chart2, overlay2, line2, tooltip2, idx, false);
    }

    private void updateCrosshair(LineChart<Number, Number> chart, Pane overlay, Line line, Label tooltip, int idx, boolean isOriginalChart) {
        Node plotBackground = chart.lookup(".chart-plot-background");
        if (plotBackground == null) {
            line.setVisible(false);
            tooltip.setVisible(false);
            return;
        }

        Bounds boundsInScene = plotBackground.localToScene(plotBackground.getBoundsInLocal());
        Bounds boundsInOverlay = overlay.sceneToLocal(boundsInScene);

        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        double displayX = xAxis.getDisplayPosition(idx);
        Point2D sceneX = xAxis.localToScene(displayX, 0);
        Point2D localXInOverlay = overlay.sceneToLocal(sceneX);

        double x = localXInOverlay.getX();

        // Check if the calculated X position is within the plot bounds
        if (x < boundsInOverlay.getMinX() || x > boundsInOverlay.getMaxX()) {
            line.setVisible(false);
            tooltip.setVisible(false);
            return;
        }

        // Position and display vertical crosshair line
        line.setStartX(x);
        line.setEndX(x);
        line.setStartY(boundsInOverlay.getMinY());
        line.setEndY(boundsInOverlay.getMaxY());
        line.setVisible(true);

        // Build tooltip content dynamically
        DataRecord record = filteredRecords.get(idx);
        StringBuilder text = new StringBuilder();
        text.append("Date: ").append(record.getDate());

        if (isOriginalChart) {
            text.append("\nOriginal: ").append(String.format("%,.2f", record.getPrice())).append(" pts");
            if (trendRecords != null && idx < trendRecords.size()) {
                text.append("\nTrend: ").append(String.format("%,.2f", trendRecords.get(idx).getPrice())).append(" pts");
            }
        } else {
            boolean hasData = false;
            if (smaRecords != null && idx < smaRecords.size()) {
                text.append("\nSMA: ").append(String.format("%,.2f", smaRecords.get(idx).getPrice())).append(" pts");
                hasData = true;
            }
            if (emaRecords != null && idx < emaRecords.size()) {
                text.append("\nEMA: ").append(String.format("%,.2f", emaRecords.get(idx).getPrice())).append(" pts");
                hasData = true;
            }
            if (!hasData) {
                text.append("\nOriginal: ").append(String.format("%,.2f", record.getPrice())).append(" pts");
            }
        }

        tooltip.setText(text.toString());
        tooltip.setVisible(true);

        // Position tooltip near the line but within bounds
        double tooltipWidth = tooltip.prefWidth(-1);
        double tooltipHeight = tooltip.prefHeight(-1);

        // Place to the right of line, or left if near the edge
        double tx = x + 15;
        if (tx + tooltipWidth > boundsInOverlay.getMaxX()) {
            tx = x - tooltipWidth - 15;
        }

        // Center vertically in the plot area
        double ty = boundsInOverlay.getMinY() + (boundsInOverlay.getHeight() - tooltipHeight) / 2.0;

        tooltip.setLayoutX(tx);
        tooltip.setLayoutY(ty);
    }

    /**
     * Hides both crosshair lines and tooltips.
     */
    public void hideCrosshairs() {
        if (line1 != null) line1.setVisible(false);
        if (tooltip1 != null) tooltip1.setVisible(false);
        if (line2 != null) line2.setVisible(false);
        if (tooltip2 != null) tooltip2.setVisible(false);
    }
}
