package com.salma.pharmacie.frontend;

import com.salma.pharmacie.frontend.model.UserResponse;
import com.salma.pharmacie.frontend.utils.Session;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class MainApp extends Application {

    private static Stage primaryStage;

    // ✅ Racine unique de tous tes FXML
    private static final String FXML_ROOT = "/com/salma/pharmacie/frontend/fxml/";

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        showLoginView();
    }

    public static void showLoginView() throws IOException {
        Session.clear();
        Parent root = loadFXML("auth/login.fxml");
        primaryStage.setTitle("Pharmacie - Connexion");
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showRegisterView() throws IOException {
        Parent root = loadFXML("auth/register.fxml");
        primaryStage.setTitle("Pharmacie - Inscription Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showDashboard(UserResponse user) throws IOException {
        if (user == null || user.getRole() == null) {
            showLoginView();
            return;
        }

        String role = user.getRole().toUpperCase();

        switch (role) {
            case "PHARMACIEN" -> showPharmacienDashboard();
            case "RESPONSABLE" -> showResponsableDashboard();
            case "CLIENT" -> showClientDashboard();
            default -> showLoginView();
        }
    }

    public static void showPharmacienDashboard() throws IOException {
        Parent root = loadFXML("pharmacien/dashboard.fxml");
        primaryStage.setTitle("Dashboard Pharmacien");
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showResponsableDashboard() throws IOException {
        // ⚠️ IMPORTANT: dossier doit être "responsable" (pas responsaple)
        Parent root = loadFXML("responsable/dashboard.fxml");
        primaryStage.setTitle("Dashboard Responsable");
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void showClientDashboard() throws IOException {
        Parent root = loadFXML("client/dashboard.fxml");
        primaryStage.setTitle("Dashboard Client");
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private static Parent loadFXML(String relativePath) throws IOException {
        String fullPath = FXML_ROOT + relativePath;
        URL url = MainApp.class.getResource(fullPath);

        Objects.requireNonNull(url,
                "FXML introuvable : " + fullPath + "\n" +
                        "Vérifie que le fichier est bien dans src/main/resources\n" +
                        "Exemple attendu: src/main/resources" + fullPath
        );

        return FXMLLoader.load(url);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
