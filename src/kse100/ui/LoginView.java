package com.kse100.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.io.*;

/**
 * Secure Login & Registration View.
 * Light theme — navy background with white card.
 */
public class LoginView {
    private final StackPane rootNode;
    private final VBox cardNode;
    private final java.util.function.Consumer<String> onLoginSuccess;
    private static final String CREDENTIALS_FILE = "users.txt";

    public LoginView(java.util.function.Consumer<String> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;

        // Navy background fills entire scene
        rootNode = new StackPane();
        rootNode.setStyle("-fx-background-color: #1a2035;");

        // Decorative accent strip (top-left corner)
        Rectangle accentRect = new Rectangle(6, 80);
        accentRect.setFill(Color.web("#2563eb"));
        accentRect.setArcWidth(6);
        accentRect.setArcHeight(6);
        StackPane.setAlignment(accentRect, Pos.TOP_LEFT);
        StackPane.setMargin(accentRect, new Insets(60, 0, 0, 60));

        cardNode = new VBox(14);
        cardNode.setAlignment(Pos.CENTER_LEFT);
        cardNode.getStyleClass().add("card-panel");
        cardNode.setMinWidth(400);
        cardNode.setMaxWidth(400);

        rootNode.getChildren().addAll(accentRect, cardNode);

        initializeCredentialsFile();
        showLoginForm();
    }

    private void initializeCredentialsFile() {
        File file = new File(CREDENTIALS_FILE);
        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("admin,admin123");
            } catch (IOException e) {
                System.err.println("Error creating credentials file: " + e.getMessage());
            }
        }
    }

    private void showLoginForm() {
        cardNode.getChildren().clear();

        // Brand header
        Label eyebrow = new Label("KSE-100 ANALYTICS");
        eyebrow.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2563eb; -fx-letter-spacing: 1.5px;");

        Label title = new Label("Sign in to Dashboard");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("Pakistan Stock Exchange — Performance & Speed Analyzer");
        subtitle.getStyleClass().add("subtitle-label");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e2e6f0; -fx-padding: 4 0 4 0;");

        Label userLabel = new Label("Username");
        userLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a5270;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a5270;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);

        Button loginBtn = new Button("Sign In  →");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-font-size: 14px; -fx-padding: 12px 22px;");

        Button signupModeBtn = new Button("Create an Account");
        signupModeBtn.getStyleClass().add("btn-action-grey");
        signupModeBtn.setMaxWidth(Double.MAX_VALUE);

        VBox userGroup = new VBox(4, userLabel, usernameField);
        VBox passGroup = new VBox(4, passLabel, passwordField);

        loginBtn.setOnAction(e -> {
            String u = usernameField.getText().trim();
            String p = passwordField.getText();
            if (u.isEmpty() || p.isEmpty()) {
                errorLabel.setText("Please enter both username and password.");
            } else if (validateLogin(u, p)) {
                errorLabel.setText("");
                onLoginSuccess.accept(u);
            } else {
                errorLabel.setText("Access denied — invalid credentials.");
            }
        });

        signupModeBtn.setOnAction(e -> showRegisterForm());
        passwordField.setOnAction(e -> loginBtn.fire());
        usernameField.setOnAction(e -> passwordField.requestFocus());

        cardNode.getChildren().addAll(eyebrow, title, subtitle, sep, userGroup, passGroup, errorLabel, loginBtn, signupModeBtn);
    }

    private void showRegisterForm() {
        cardNode.getChildren().clear();

        Label eyebrow = new Label("KSE-100 ANALYTICS");
        eyebrow.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

        Label title = new Label("Create Account");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label("Register new credentials to access the dashboard");
        subtitle.getStyleClass().add("subtitle-label");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e2e6f0;");

        Label userLabel = new Label("Username");
        userLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a5270;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");

        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a5270;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");

        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a5270;");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter password");

        Label errorLabel = new Label();
        errorLabel.setWrapText(true);

        Button registerBtn = new Button("Register Account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle("-fx-font-size: 14px; -fx-padding: 12px 22px;");

        Button loginModeBtn = new Button("← Back to Sign In");
        loginModeBtn.getStyleClass().add("btn-action-grey");
        loginModeBtn.setMaxWidth(Double.MAX_VALUE);

        VBox userGroup = new VBox(4, userLabel, usernameField);
        VBox passGroup = new VBox(4, passLabel, passwordField);
        VBox confirmGroup = new VBox(4, confirmLabel, confirmPasswordField);

        registerBtn.setOnAction(e -> {
            String u = usernameField.getText().trim();
            String p = passwordField.getText();
            String c = confirmPasswordField.getText();

            if (u.isEmpty() || p.isEmpty()) {
                errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                errorLabel.setText("Username and password cannot be empty.");
            } else if (!p.equals(c)) {
                errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                errorLabel.setText("Passwords do not match.");
            } else if (registerUser(u, p)) {
                errorLabel.setStyle("-fx-text-fill: #0d9488; -fx-font-weight: bold;");
                errorLabel.setText("Account created! Returning to sign in...");
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.4));
                pause.setOnFinished(ev -> showLoginForm());
                pause.play();
            } else {
                errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                errorLabel.setText("Username already exists.");
            }
        });

        loginModeBtn.setOnAction(e -> showLoginForm());

        cardNode.getChildren().addAll(eyebrow, title, subtitle, sep, userGroup, passGroup, confirmGroup, errorLabel, registerBtn, loginModeBtn);
    }

    private boolean validateLogin(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(CREDENTIALS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].trim().equals(username) && parts[1].trim().equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading credentials: " + e.getMessage());
        }
        return false;
    }

    private boolean registerUser(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(CREDENTIALS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].trim().equalsIgnoreCase(username)) return false;
            }
        } catch (IOException ignored) {}

        try (PrintWriter pw = new PrintWriter(new FileWriter(CREDENTIALS_FILE, true))) {
            pw.println(username + "," + password);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving credentials: " + e.getMessage());
            return false;
        }
    }

    public Parent getView() {
        return rootNode;
    }
}
