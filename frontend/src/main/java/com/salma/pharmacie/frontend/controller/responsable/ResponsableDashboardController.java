package com.salma.pharmacie.frontend.controller.responsable;

import com.salma.pharmacie.frontend.MainApp;
import com.salma.pharmacie.frontend.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class ResponsableDashboardController {

    private static final String FXML_ROOT = "/com/salma/pharmacie/frontend/fxml/";

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        if (!Session.hasRole("RESPONSABLE")) {
            try { MainApp.showLoginView(); } catch (IOException ignored) {}
            return;
        }

        // optionnel: afficher automatiquement une page
        // showProduits();
    }

    @FXML private void showProduits() { loadView("shared/produits.fxml"); }
    @FXML private void showVentes() { loadView("shared/ventes.fxml"); }
    @FXML private void showCommandes() { loadView("shared/commandes.fxml"); }
    @FXML private void showFournisseurs() { loadView("shared/fournisseurs.fxml"); }

    @FXML private void showPharmaciens() { loadView("responsable/pharmaciens.fxml"); }
    @FXML private void showRapports() { loadView("responsable/rapports.fxml"); }

    @FXML
    private void handleLogout() throws IOException {
        Session.clear();
        MainApp.showLoginView();
    }

    private void loadView(String relativePath) {
        try {
            String fullPath = FXML_ROOT + relativePath;
            URL url = MainApp.class.getResource(fullPath);

            Objects.requireNonNull(url, "FXML introuvable : " + fullPath);

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);

        } catch (Exception e) {
            showError("Erreur", e.getMessage());
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
