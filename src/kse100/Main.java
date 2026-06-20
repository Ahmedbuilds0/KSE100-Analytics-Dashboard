package com.kse100;

import com.kse100.ui.LoginView;
import com.kse100.ui.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Main application launcher.
 * Handles scene switching between the Login screen and the main Dashboard.
 */
public class Main extends Application {
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        primaryStage.setTitle("KSE-100 Analytics Dashboard & Speed Analyzer");
        showLogin();
        primaryStage.show();
    }

    private void showLogin() {
        LoginView loginView = new LoginView(this::showDashboard);
        Scene loginScene = new Scene(loginView.getView(), 520, 560);

        try {
            loginScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("CSS WARNING: Could not load stylesheet — " + e.getMessage());
        }

        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.centerOnScreen();
    }

    private void showDashboard(String username) {
        DashboardView dashboardView = new DashboardView(username, this::showLogin);
        // 1400x840 gives charts plenty of room
        Scene dashboardScene = new Scene(dashboardView.getView(), 1400, 840);

        try {
            dashboardScene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("CSS WARNING: Could not load stylesheet — " + e.getMessage());
        }

        stage.setResizable(true);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(dashboardScene);
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
