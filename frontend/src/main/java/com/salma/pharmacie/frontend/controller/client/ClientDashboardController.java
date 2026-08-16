package com.salma.pharmacie.frontend.controller.client;

import com.salma.pharmacie.frontend.MainApp;
import com.salma.pharmacie.frontend.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Objects;

public class ClientDashboardController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        if (!Session.hasRole("CLIENT")) {
            try { MainApp.showLoginView(); } catch (IOException ignored) {}
            return;
        }

        // Afficher les notifications dans le dashboard
        loadView("fxml/client/notifications.fxml");
    }






    @FXML
    private void handleLogout() throws IOException {
        Session.clear();
        MainApp.showLoginView();
    }

    private void loadView(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    MainApp.class.getResource(path),
                    "FXML introuvable : " + path
            ));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            cause.printStackTrace();
            showError("Erreur", "Impossible de charger : " + path + "\n"
                    + cause.getClass().getSimpleName() + " : " + cause.getMessage());
        }
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
