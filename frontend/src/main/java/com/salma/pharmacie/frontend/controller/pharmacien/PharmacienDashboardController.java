package com.salma.pharmacie.frontend.controller.pharmacien;

import com.salma.pharmacie.frontend.MainApp;
import com.salma.pharmacie.frontend.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.Objects;

public class PharmacienDashboardController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        // petit guard front (sécurité UI, pas Spring Security)
        if (!Session.hasRole("PHARMACIEN") && !Session.hasRole("RESPONSABLE")) {
            try { MainApp.showLoginView(); } catch (IOException ignored) {}
        }
    }

    @FXML private void showProduits() { loadView("fxml/shared/produits.fxml"); }
    @FXML private void showVentes() { loadView("fxml/shared/ventes.fxml"); }
    @FXML private void showCommandes() { loadView("fxml/shared/commandes.fxml"); }
    @FXML private void showFournisseurs() { loadView("fxml/shared/fournisseurs.fxml"); }

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
            showError("Erreur", "Impossible de charger : " + path + "\n" + e.getMessage());
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
