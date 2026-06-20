package com.kse100.ui;

import com.kse100.model.DataRecord;
import com.kse100.model.Dataset;
import com.kse100.service.DataLoader;
import com.kse100.service.SMACalculator;
import com.kse100.service.EMACalculator;
import com.kse100.service.TrendLineCalculator;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders the primary Split Dashboard layout.
 * Manages side-by-side charts, statistics metadata, control buttons,
 * the dual-performance timer displays, live parameter sliders,
 * speed history bar chart, data table popup, and synchronized crosshair overlay.
 */
public class DashboardView {
    private final BorderPane rootPane;
    private final DataLoader dataLoader;
    private final String loggedInUsername;
    private final Runnable onLogout;
    private Dataset currentDataset;
    private List<DataRecord> filteredRecords;

    // Line Charts
    private final LineChart<Number, Number> originalChart;
    private final LineChart<Number, Number> processedChart;

    // Crosshair overlay management
    private final ChartCrosshair crosshair;

    // Series caches
    private XYChart.Series<Number, Number> smaSeries;
    private XYChart.Series<Number, Number> emaSeries;
    private XYChart.Series<Number, Number> trendSeries;

    // Record list caches (for crosshair tooltip values)
    private List<DataRecord> smaRecords;
    private List<DataRecord> emaRecords;
    private List<DataRecord> trendRecords;

    // Metadata & Statistics Labels
    private final Label totalRecordsLabel;
    private final Label dateRangeLabel;
    private final Label repairedRecordsLabel;
    private final Label percentChangeLabel;
    private final Label minPriceLabel;
    private final Label maxPriceLabel;
    private final Label meanPriceLabel;
    private final Label stdDevPriceLabel;

    // Speed Labels
    private final Label smaSpeedLabel;
    private final Label emaSpeedLabel;

    // File error label (shown in UI if dataset fails to load)
    private final Label fileErrorLabel;

    // Date Range Dropdown
    private final ComboBox<String> dateRangeComboBox;

    // Sliders & Labels
    private Slider smaSlider;
    private Slider emaSlider;
    private Label smaSliderLabel;
    private Label emaSliderLabel;

    // Speed history tracking (only populated by explicit Run button presses)
    private final List<Long> smaHistory = new ArrayList<>();
    private final List<Long> emaHistory = new ArrayList<>();
    private int runCounter = 0; // persistent counter so labels never renumber
    private BarChart<String, Number> speedHistoryChart;

    // State trackers
    private boolean isSmaActive = false;
    private boolean isEmaActive = false;
    private boolean isTrendActive = false;
    private Button trendBtn;

    // Series stroke colors
    private static final String COLOR_ORIGINAL = "#1a2035";
    private static final String COLOR_SMA      = "#2563eb";
    private static final String COLOR_EMA      = "#e8480a";
    private static final String DASH_EMA       = "10, 6";

    public DashboardView(String loggedInUsername, Runnable onLogout) {
        this.loggedInUsername = loggedInUsername;
        this.onLogout = onLogout;
        this.rootPane = new BorderPane();
        this.dataLoader = new DataLoader();
        this.filteredRecords = new ArrayList<>();

        // ── TOP: Navy header bar ──
        HBox headerRow = new HBox(20);
        headerRow.getStyleClass().add("header-bar");
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleArea = new VBox(4);
        Label title = new Label("KSE-100 Comparative Analytics Dashboard");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Pakistan Stock Exchange (PSX) · Performance & Speed Analyzer");
        subtitle.getStyleClass().add("header-subtitle");
        titleArea.getChildren().addAll(title, subtitle);
        HBox.setHgrow(titleArea, Priority.ALWAYS);

        HBox userBox = new HBox(12);
        userBox.setAlignment(Pos.CENTER_RIGHT);
        Label userBadge = new Label("User: " + loggedInUsername);
        userBadge.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #60a5fa;");
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("btn-reset");
        logoutBtn.setOnAction(e -> { if (onLogout != null) onLogout.run(); });
        userBox.getChildren().addAll(userBadge, logoutBtn);
        headerRow.getChildren().addAll(titleArea, userBox);
        rootPane.setTop(headerRow);

        // ── BODY ──
        VBox bodyContainer = new VBox(14);
        bodyContainer.setPadding(new Insets(16, 20, 16, 20));

        // File error label (hidden by default)
        fileErrorLabel = new Label();
        fileErrorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 13px;");
        fileErrorLabel.setVisible(false);
        fileErrorLabel.setManaged(false);

        // Filter row
        HBox filterRow = new HBox(10);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        Label filterLabel = new Label("DATE RANGE:");
        filterLabel.getStyleClass().add("stats-title");
        dateRangeComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "All Data (3 Years)", "Year 2023", "Year 2024", "Year 2025", "Year 2026"
        ));
        dateRangeComboBox.setValue("All Data (3 Years)");
        dateRangeComboBox.setOnAction(e -> handleFilterChange());

        Button dataTableBtn = new Button("📋  View Data Table");
        dataTableBtn.getStyleClass().add("btn-action-grey");
        dataTableBtn.setOnAction(e -> showDataTable());

        Region fsr = new Region();
        HBox.setHgrow(fsr, Priority.ALWAYS);
        filterRow.getChildren().addAll(filterLabel, dateRangeComboBox, fsr, dataTableBtn);

        // Stats pills
        FlowPane statsPane = new FlowPane();
        statsPane.setHgap(12);
        statsPane.setVgap(8);

        totalRecordsLabel      = new Label("0");
        dateRangeLabel         = new Label("N/A");
        repairedRecordsLabel   = new Label("0");
        percentChangeLabel     = new Label("N/A");
        minPriceLabel          = new Label("N/A");
        maxPriceLabel          = new Label("N/A");
        meanPriceLabel         = new Label("N/A");
        stdDevPriceLabel       = new Label("N/A");

        statsPane.getChildren().addAll(
            createStatsPill("RECORDS IN VIEW",      totalRecordsLabel,    false),
            createStatsPill("DATE RANGE",            dateRangeLabel,       false),
            createStatsPill("REPAIRED (TRY-CATCH)", repairedRecordsLabel,  true),
            createStatsPill("OVERALL CHANGE",        percentChangeLabel,    false),
            createAnalyticsPill("MIN VALUE",         minPriceLabel,        "#ef4444"),
            createAnalyticsPill("MAX VALUE",         maxPriceLabel,        "#22c55e"),
            createAnalyticsPill("MEAN",              meanPriceLabel,       "#3b82f6"),
            createAnalyticsPill("STD DEV",           stdDevPriceLabel,     "#a855f7")
        );

        // Charts
        NumberAxis ox = new NumberAxis(); ox.setLabel("Timeline"); ox.setTickLabelsVisible(false); ox.setTickMarkVisible(false);
        NumberAxis oy = new NumberAxis(); oy.setLabel("Index Value (Points)");
        originalChart = new LineChart<>(ox, oy);
        originalChart.setTitle("KSE-100 Original Index Data");
        originalChart.setAnimated(false);
        originalChart.setCreateSymbols(false);
        originalChart.setMinHeight(300);
        HBox.setHgrow(originalChart, Priority.ALWAYS);

        NumberAxis px = new NumberAxis(); px.setLabel("Timeline"); px.setTickLabelsVisible(false); px.setTickMarkVisible(false);
        NumberAxis py = new NumberAxis(); py.setLabel("Filtered Value (Points)");
        processedChart = new LineChart<>(px, py);
        processedChart.setTitle("Smoothed Processed Output");
        processedChart.setAnimated(false);
        processedChart.setCreateSymbols(false);
        processedChart.setLegendVisible(true);
        processedChart.setMinHeight(300);
        HBox.setHgrow(processedChart, Priority.ALWAYS);

        setupXAxisLabels(ox);
        setupXAxisLabels(px);

        Pane overlay1 = new Pane(); overlay1.setMouseTransparent(true);
        Pane overlay2 = new Pane(); overlay2.setMouseTransparent(true);
        StackPane stack1 = new StackPane(originalChart, overlay1);
        StackPane stack2 = new StackPane(processedChart, overlay2);

        VBox leftCard  = new VBox(stack1); leftCard.getStyleClass().add("card-panel"); HBox.setHgrow(leftCard, Priority.ALWAYS);
        VBox rightCard = new VBox(stack2); rightCard.getStyleClass().add("card-panel"); HBox.setHgrow(rightCard, Priority.ALWAYS);

        HBox chartsRow = new HBox(16, leftCard, rightCard);
        chartsRow.setAlignment(Pos.CENTER);
        VBox.setVgrow(chartsRow, Priority.ALWAYS);

        crosshair = new ChartCrosshair(originalChart, overlay1, processedChart, overlay2);

        // Control buttons
        Button runSmaBtn  = new Button("▶  Run SMA");  runSmaBtn.getStyleClass().add("btn-run-sma");  runSmaBtn.setOnAction(e -> runSMA(true));
        Button runEmaBtn  = new Button("▶  Run EMA");  runEmaBtn.getStyleClass().add("btn-run-ema");  runEmaBtn.setOnAction(e -> runEMA(true));
        Button runBothBtn = new Button("⚡  Run Both"); runBothBtn.getStyleClass().add("btn-run-both"); runBothBtn.setOnAction(e -> runBoth());

        trendBtn = new Button("Trend Line: OFF");
        trendBtn.getStyleClass().add("btn-action-grey");
        trendBtn.setOnAction(e -> toggleTrendLine());

        Button resetBtn  = new Button("↺  Reset");       resetBtn.getStyleClass().add("btn-reset");        resetBtn.setOnAction(e -> resetCharts(true));
        Button reloadBtn = new Button("⟳  Reload File"); reloadBtn.getStyleClass().add("btn-action-grey"); reloadBtn.setOnAction(e -> loadDatasetFile());

        HBox controlBar = new HBox(12, runSmaBtn, runEmaBtn, runBothBtn, trendBtn, resetBtn, reloadBtn);
        controlBar.setAlignment(Pos.CENTER_LEFT);

        // Parameter sliders
        smaSliderLabel = new Label("SMA Window: 3 (Radius: 1)");
        smaSliderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #4a5270;");
        smaSlider = new Slider(3, 15, 3);
        smaSlider.setMajorTickUnit(2); smaSlider.setMinorTickCount(0);
        smaSlider.setSnapToTicks(true); smaSlider.setShowTickMarks(true); smaSlider.setShowTickLabels(true);
        smaSlider.setPrefWidth(210);
        // FIX #3: slider updates label + re-runs silently (no history recording)
        smaSlider.valueProperty().addListener((obs, o, n) -> {
            int w = n.intValue(); if (w % 2 == 0) w--;
            smaSliderLabel.setText("SMA Window: " + w + " (Radius: " + ((w - 1) / 2) + ")");
            if (isSmaActive) runSMA(false); // false = don't record to history
        });

        emaSliderLabel = new Label("EMA Alpha: 0.20");
        emaSliderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #4a5270;");
        emaSlider = new Slider(0.1, 0.5, 0.2);
        emaSlider.setMajorTickUnit(0.1); emaSlider.setMinorTickCount(1);
        emaSlider.setShowTickMarks(true); emaSlider.setShowTickLabels(true);
        emaSlider.setPrefWidth(210);
        emaSlider.valueProperty().addListener((obs, o, n) -> {
            emaSliderLabel.setText(String.format("EMA Alpha: %.2f", n.doubleValue()));
            if (isEmaActive) runEMA(false); // false = don't record to history
        });

        VBox smaSliderBox = new VBox(4, smaSliderLabel, smaSlider);
        VBox emaSliderBox = new VBox(4, emaSliderLabel, emaSlider);
        HBox paramsBar = new HBox(30, smaSliderBox, emaSliderBox);
        paramsBar.setAlignment(Pos.CENTER_LEFT);

        // Speed monitors
        smaSpeedLabel = new Label("SMA Speed: Not Executed");
        emaSpeedLabel = new Label("EMA Speed: Not Executed");
        VBox smaSpeedBox = createSpeedMonitorBox("SMA ALGORITHM SPEED  (System.nanoTime())", smaSpeedLabel, "speed-value-sma");
        VBox emaSpeedBox = createSpeedMonitorBox("EMA ALGORITHM SPEED  (System.nanoTime())", emaSpeedLabel, "speed-value-ema");

        // Speed history bar chart
        CategoryAxis hx = new CategoryAxis(); hx.setLabel("Run");
        NumberAxis hy = new NumberAxis(); hy.setLabel("Time (ns)");
        speedHistoryChart = new BarChart<>(hx, hy);
        speedHistoryChart.setTitle("Execution Speed History (Last 10 Explicit Runs)");
        speedHistoryChart.setAnimated(false);
        speedHistoryChart.setPrefSize(440, 150);
        speedHistoryChart.setMinHeight(140);

        HBox speedRow = new HBox(20, smaSpeedBox, emaSpeedBox, speedHistoryChart);
        speedRow.setAlignment(Pos.CENTER_LEFT);

        VBox bottomBar = new VBox(12, controlBar, paramsBar, speedRow);

        bodyContainer.getChildren().addAll(fileErrorLabel, filterRow, statsPane, chartsRow, bottomBar);

        ScrollPane scroll = new ScrollPane(bodyContainer);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        rootPane.setCenter(scroll);

        loadDatasetFile();
    }

    // ── DATA LOADING ──────────────────────────────────────────────

    private void loadDatasetFile() {
        String filePath = "kse100_project_dataset.txt";

        // FIX #5: show an in-UI error if the file doesn't exist
        File f = new File(filePath);
        if (!f.exists()) {
            fileErrorLabel.setText("⚠  Dataset file not found: \"" + filePath
                + "\". Make sure you run the application from the project root directory.");
            fileErrorLabel.setVisible(true);
            fileErrorLabel.setManaged(true);
            return;
        }
        fileErrorLabel.setVisible(false);
        fileErrorLabel.setManaged(false);

        currentDataset = dataLoader.loadData(filePath);
        filteredRecords = new ArrayList<>(currentDataset.getRecords());
        dateRangeComboBox.setValue("All Data (3 Years)");
        totalRecordsLabel.setText(String.format("%,d lines processed", currentDataset.getTotalCount()));
        repairedRecordsLabel.setText(String.format("%d corrupt values fixed", currentDataset.getRepairedCount()));
        updateAnalyticsAndCharts(false); // false = don't reset trend
    }

    private void handleFilterChange() {
        if (currentDataset == null) return;
        String sel = dateRangeComboBox.getValue();
        List<DataRecord> all = currentDataset.getRecords();
        if (sel != null && sel.startsWith("Year ")) {
            String year = sel.substring(5);
            filteredRecords = all.stream().filter(r -> r.getDate().startsWith(year)).collect(Collectors.toList());
        } else {
            filteredRecords = new ArrayList<>(all);
        }
        // FIX #2: pass keepTrend=true so the trend toggle state survives a filter change
        updateAnalyticsAndCharts(true);
    }

    /**
     * @param keepTrendState when true, the trend line is redrawn if currently active
     *                       instead of being silently killed by resetCharts().
     */
    private void updateAnalyticsAndCharts(boolean keepTrendState) {
        if (filteredRecords == null) return;
        int n = filteredRecords.size();

        // Descriptive statistics
        if (n > 0) {
            double min = Double.MAX_VALUE, max = -Double.MAX_VALUE, sum = 0;
            for (DataRecord r : filteredRecords) {
                if (r.getPrice() < min) min = r.getPrice();
                if (r.getPrice() > max) max = r.getPrice();
                sum += r.getPrice();
            }
            double mean = sum / n;
            double variance = 0;
            for (DataRecord r : filteredRecords) variance += Math.pow(r.getPrice() - mean, 2);
            double stddev = Math.sqrt(variance / n);

            totalRecordsLabel.setText(String.format("%,d", n));
            dateRangeLabel.setText(filteredRecords.get(0).getDate() + "  →  " + filteredRecords.get(n - 1).getDate());
            minPriceLabel.setText(String.format("%,.2f pts", min));
            maxPriceLabel.setText(String.format("%,.2f pts", max));
            meanPriceLabel.setText(String.format("%,.2f pts", mean));
            stdDevPriceLabel.setText(String.format("%,.2f pts", stddev));

            double change = ((filteredRecords.get(n - 1).getPrice() - filteredRecords.get(0).getPrice()) / filteredRecords.get(0).getPrice()) * 100.0;
            if (change > 0) {
                percentChangeLabel.setText(String.format("+%.2f%% ▲", change));
                percentChangeLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else if (change < 0) {
                percentChangeLabel.setText(String.format("%.2f%% ▼", change));
                percentChangeLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else {
                percentChangeLabel.setText("0.00%");
                percentChangeLabel.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
        } else {
            totalRecordsLabel.setText("0"); dateRangeLabel.setText("N/A");
            minPriceLabel.setText("N/A"); maxPriceLabel.setText("N/A");
            meanPriceLabel.setText("N/A"); stdDevPriceLabel.setText("N/A");
            percentChangeLabel.setText("N/A");
        }

        // Y-axis bounds
        setupYAxisBounds((NumberAxis) originalChart.getYAxis(), filteredRecords);
        setupYAxisBounds((NumberAxis) processedChart.getYAxis(), filteredRecords);

        // Replot original
        originalChart.getData().clear();
        XYChart.Series<Number, Number> orig = new XYChart.Series<>();
        orig.setName("KSE-100 Index");
        for (int i = 0; i < n; i++) orig.getData().add(new XYChart.Data<>(i, filteredRecords.get(i).getPrice()));
        originalChart.getData().add(orig);
        styleSeries(orig, COLOR_ORIGINAL, null);

        // FIX #2: redraw trend BEFORE resetCharts() if keepTrendState=true
        if (keepTrendState && isTrendActive) {
            updateTrendLine();
        }

        // FIX #2: use the no-trend variant of reset so we don't override what we just drew
        resetCharts(false);
    }

    // ── TREND LINE ────────────────────────────────────────────────

    private void toggleTrendLine() {
        isTrendActive = !isTrendActive;
        trendBtn.setText("Trend Line: " + (isTrendActive ? "ON" : "OFF"));
        trendBtn.setStyle(isTrendActive ? "-fx-border-color: #8b5cf6; -fx-text-fill: #8b5cf6;" : "");
        updateTrendLine();
    }

    private void updateTrendLine() {
        originalChart.getData().removeIf(s -> "Trend (Linear Regression)".equals(s.getName()));
        trendRecords = null;
        trendSeries  = null;

        if (isTrendActive && filteredRecords != null && !filteredRecords.isEmpty()) {
            TrendLineCalculator calc = new TrendLineCalculator();
            trendRecords = calc.process(filteredRecords);
            trendSeries  = new XYChart.Series<>();
            trendSeries.setName("Trend (Linear Regression)");
            for (int i = 0; i < trendRecords.size(); i++)
                trendSeries.getData().add(new XYChart.Data<>(i, trendRecords.get(i).getPrice()));
            originalChart.getData().add(trendSeries);
            styleSeries(trendSeries, "#8b5cf6", "4, 4");
        }
        updateCrosshairData();
    }

    // ── ALGORITHM RUNNERS ─────────────────────────────────────────

    /**
     * @param recordHistory true when called from an explicit button press;
     *                      false when called from a slider drag (no history pollution).
     */
    private void runSMA(boolean recordHistory) {
        if (filteredRecords == null || filteredRecords.isEmpty()) return;
        int w = (int) smaSlider.getValue(); if (w % 2 == 0) w--;
        SMACalculator calc = new SMACalculator((w - 1) / 2);
        smaRecords = calc.process(filteredRecords);
        isSmaActive = true;

        smaSeries = new XYChart.Series<>();
        smaSeries.setName("SMA (Window=" + w + ")");
        for (int i = 0; i < smaRecords.size(); i++)
            smaSeries.getData().add(new XYChart.Data<>(i, smaRecords.get(i).getPrice()));

        refreshProcessedChart();
        long ns = calc.getExecutionTimeNs();
        smaSpeedLabel.setText(String.format("SMA Speed: %,d ns", ns));

        // FIX #3: only record to speed history on explicit button presses
        if (recordHistory) recordSpeed(ns, null);
        updateCrosshairData();
    }

    private void runEMA(boolean recordHistory) {
        if (filteredRecords == null || filteredRecords.isEmpty()) return;
        double alpha = emaSlider.getValue();
        EMACalculator calc = new EMACalculator(alpha);
        emaRecords = calc.process(filteredRecords);
        isEmaActive = true;

        emaSeries = new XYChart.Series<>();
        emaSeries.setName(String.format("EMA (α=%.2f)", alpha));
        for (int i = 0; i < emaRecords.size(); i++)
            emaSeries.getData().add(new XYChart.Data<>(i, emaRecords.get(i).getPrice()));

        refreshProcessedChart();
        long ns = calc.getExecutionTimeNs();
        emaSpeedLabel.setText(String.format("EMA Speed: %,d ns", ns));

        if (recordHistory) recordSpeed(null, ns);
        updateCrosshairData();
    }

    private void runBoth() {
        if (filteredRecords == null || filteredRecords.isEmpty()) return;
        int w = (int) smaSlider.getValue(); if (w % 2 == 0) w--;
        SMACalculator smaCalc = new SMACalculator((w - 1) / 2);
        smaRecords = smaCalc.process(filteredRecords);
        isSmaActive = true;
        smaSeries = new XYChart.Series<>(); smaSeries.setName("SMA (Window=" + w + ")");
        for (int i = 0; i < smaRecords.size(); i++) smaSeries.getData().add(new XYChart.Data<>(i, smaRecords.get(i).getPrice()));
        long smaTime = smaCalc.getExecutionTimeNs();
        smaSpeedLabel.setText(String.format("SMA Speed: %,d ns", smaTime));

        double alpha = emaSlider.getValue();
        EMACalculator emaCalc = new EMACalculator(alpha);
        emaRecords = emaCalc.process(filteredRecords);
        isEmaActive = true;
        emaSeries = new XYChart.Series<>(); emaSeries.setName(String.format("EMA (α=%.2f)", alpha));
        for (int i = 0; i < emaRecords.size(); i++) emaSeries.getData().add(new XYChart.Data<>(i, emaRecords.get(i).getPrice()));
        long emaTime = emaCalc.getExecutionTimeNs();
        emaSpeedLabel.setText(String.format("EMA Speed: %,d ns", emaTime));

        refreshProcessedChart();
        // FIX #3: Run Both is always an explicit press — always records
        recordSpeed(smaTime, emaTime);
        updateCrosshairData();
    }

    private void refreshProcessedChart() {
        processedChart.getData().clear();
        if (smaSeries != null) { processedChart.getData().add(smaSeries); styleSeries(smaSeries, COLOR_SMA, null); }
        if (emaSeries != null) { processedChart.getData().add(emaSeries); styleSeries(emaSeries, COLOR_EMA, DASH_EMA); }
    }

    // ── DATA TABLE POPUP (Fix #4) ─────────────────────────────────

    @SuppressWarnings("unchecked")
    private void showDataTable() {
        if (filteredRecords == null || filteredRecords.isEmpty()) return;

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Data Table — " + filteredRecords.size() + " records in view");

        TableView<DataRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DataRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDate()));

        TableColumn<DataRecord, Number> priceCol = new TableColumn<>("Original (pts)");
        priceCol.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrice()));
        priceCol.setCellFactory(col -> new TableCell<DataRecord, Number>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("%,.2f", v.doubleValue()));
            }
        });

        TableColumn<DataRecord, Number> smaCol = new TableColumn<>("SMA (pts)");
        smaCol.setCellValueFactory(c -> {
            int idx = table.getItems().indexOf(c.getValue());
            return (smaRecords != null && idx >= 0 && idx < smaRecords.size())
                ? new SimpleDoubleProperty(smaRecords.get(idx).getPrice())
                : new SimpleDoubleProperty(Double.NaN);
        });
        smaCol.setCellFactory(col -> new TableCell<DataRecord, Number>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null || Double.isNaN(v.doubleValue()) ? "—" : String.format("%,.2f", v.doubleValue()));
            }
        });

        TableColumn<DataRecord, Number> emaCol = new TableColumn<>("EMA (pts)");
        emaCol.setCellValueFactory(c -> {
            int idx = table.getItems().indexOf(c.getValue());
            return (emaRecords != null && idx >= 0 && idx < emaRecords.size())
                ? new SimpleDoubleProperty(emaRecords.get(idx).getPrice())
                : new SimpleDoubleProperty(Double.NaN);
        });
        emaCol.setCellFactory(col -> new TableCell<DataRecord, Number>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null || Double.isNaN(v.doubleValue()) ? "—" : String.format("%,.2f", v.doubleValue()));
            }
        });

        TableColumn<DataRecord, Number> trendCol = new TableColumn<>("Trend (pts)");
        trendCol.setCellValueFactory(c -> {
            int idx = table.getItems().indexOf(c.getValue());
            return (trendRecords != null && idx >= 0 && idx < trendRecords.size())
                ? new SimpleDoubleProperty(trendRecords.get(idx).getPrice())
                : new SimpleDoubleProperty(Double.NaN);
        });
        trendCol.setCellFactory(col -> new TableCell<DataRecord, Number>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null || Double.isNaN(v.doubleValue()) ? "—" : String.format("%,.2f", v.doubleValue()));
            }
        });

        table.getColumns().addAll(dateCol, priceCol, smaCol, emaCol, trendCol);
        ObservableList<DataRecord> items = FXCollections.observableArrayList(filteredRecords);
        table.setItems(items);

        Label info = new Label("Showing " + filteredRecords.size() + " records  ·  SMA/EMA/Trend columns show '—' if that algorithm hasn't been run yet.");
        info.setStyle("-fx-font-size: 11px; -fx-text-fill: #7a84a0; -fx-padding: 6 4 4 4;");

        VBox layout = new VBox(8, info, table);
        layout.setPadding(new Insets(14));
        Scene scene = new Scene(layout, 700, 520);
        try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}
        popup.setScene(scene);
        popup.show();
    }

    // ── RESET ─────────────────────────────────────────────────────

    /**
     * @param resetTrend when true, also resets the trend line toggle state.
     *                   Pass false when called internally after a filter change
     *                   so the trend button state is preserved.
     */
    private void resetCharts(boolean resetTrend) {
        smaSeries = null; emaSeries = null;
        smaRecords = null; emaRecords = null;
        processedChart.getData().clear();
        smaSpeedLabel.setText("SMA Speed: Not Executed");
        emaSpeedLabel.setText("EMA Speed: Not Executed");
        isSmaActive = false; isEmaActive = false;

        if (resetTrend) {
            isTrendActive = false;
            if (trendBtn != null) { trendBtn.setText("Trend Line: OFF"); trendBtn.setStyle(""); }
            originalChart.getData().removeIf(s -> "Trend (Linear Regression)".equals(s.getName()));
            trendRecords = null; trendSeries = null;
        }
        updateCrosshairData();
    }

    // ── HELPERS ───────────────────────────────────────────────────

    private void setupXAxisLabels(NumberAxis xAxis) {
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override public String toString(Number n) {
                int idx = n.intValue();
                return (filteredRecords != null && idx >= 0 && idx < filteredRecords.size())
                    ? filteredRecords.get(idx).getDate() : "";
            }
            @Override public Number fromString(String s) { return 0; }
        });
    }

    private void setupYAxisBounds(NumberAxis yAxis, List<DataRecord> records) {
        if (records == null || records.isEmpty()) return;
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (DataRecord r : records) {
            if (r.getPrice() < min) min = r.getPrice();
            if (r.getPrice() > max) max = r.getPrice();
        }
        double diff = max - min;
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(Math.max(0, min - diff * 0.05));
        yAxis.setUpperBound(max + diff * 0.05);
        yAxis.setTickUnit(Math.max(1, Math.round(diff / 8.0)));
    }

    private void styleSeries(XYChart.Series<Number, Number> series, String color, String dash) {
        Platform.runLater(() -> {
            if (series.getNode() == null) return;
            String style = "-fx-stroke: " + color + "; -fx-stroke-width: 2.4px;"
                + (dash != null ? " -fx-stroke-dash-array: " + dash + ";" : "");
            series.getNode().setStyle(style);
        });
    }

    // FIX #3: persistent runCounter ensures history labels never renumber
    private void recordSpeed(Long smaTime, Long emaTime) {
        runCounter++;
        String label = "Run " + runCounter;
        if (smaTime != null) {
            if (smaHistory.size() >= 10) smaHistory.remove(0);
            smaHistory.add(smaTime);
        }
        if (emaTime != null) {
            if (emaHistory.size() >= 10) emaHistory.remove(0);
            emaHistory.add(emaTime);
        }
        updateSpeedHistoryChart();
    }

    private void updateSpeedHistoryChart() {
        speedHistoryChart.getData().clear();
        XYChart.Series<String, Number> smaSer = new XYChart.Series<>(); smaSer.setName("SMA (ns)");
        XYChart.Series<String, Number> emaSer = new XYChart.Series<>(); emaSer.setName("EMA (ns)");

        // Use a shared run index offset so labels match across both series
        int offset = runCounter - Math.max(smaHistory.size(), emaHistory.size());
        int maxLen = Math.max(smaHistory.size(), emaHistory.size());
        for (int i = 0; i < maxLen; i++) {
            String lbl = "Run " + (offset + i + 1);
            if (i < smaHistory.size()) smaSer.getData().add(new XYChart.Data<>(lbl, smaHistory.get(i)));
            if (i < emaHistory.size()) emaSer.getData().add(new XYChart.Data<>(lbl, emaHistory.get(i)));
        }
        speedHistoryChart.getData().addAll(smaSer, emaSer);
    }

    private void updateCrosshairData() {
        if (crosshair != null) crosshair.updateData(filteredRecords, smaRecords, emaRecords, trendRecords);
    }

    private VBox createStatsPill(String title, Label val, boolean warn) {
        VBox b = new VBox(3); b.getStyleClass().add("stats-pill");
        Label t = new Label(title); t.getStyleClass().add("stats-title");
        val.getStyleClass().add(warn ? "stats-value-warning" : "stats-value");
        b.getChildren().addAll(t, val); return b;
    }

    private VBox createAnalyticsPill(String title, Label val, String border) {
        VBox b = new VBox(3); b.getStyleClass().add("stats-pill");
        b.setStyle("-fx-border-color: " + border + "; -fx-border-width: 1.5px;");
        Label t = new Label(title); t.getStyleClass().add("stats-title");
        val.getStyleClass().add("stats-value");
        b.getChildren().addAll(t, val); return b;
    }

    private VBox createSpeedMonitorBox(String title, Label val, String styleClass) {
        VBox b = new VBox(4); b.getStyleClass().add("speed-pane"); b.setMinWidth(220);
        Label t = new Label(title); t.getStyleClass().add("speed-label-title");
        val.getStyleClass().add(styleClass);
        b.getChildren().addAll(t, val); return b;
    }

    public Parent getView() { return rootPane; }
}
