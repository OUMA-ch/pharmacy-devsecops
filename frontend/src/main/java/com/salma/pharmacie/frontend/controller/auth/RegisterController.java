package com.salma.pharmacie.frontend.controller.auth;

import com.salma.pharmacie.frontend.MainApp;
import com.salma.pharmacie.frontend.utils.ApiClient;
import com.salma.pharmacie.frontend.utils.ApiErrors;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class RegisterController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label infoLabel;


    @FXML
    private void handleRegister() {

        String nom = nomField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        // Vérification des champs
        if (nom == null || nom.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {

            infoLabel.setTextFill(Color.RED);
            infoLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        infoLabel.setTextFill(Color.BLACK);
        infoLabel.setText("Inscription en cours...");

        try {

            Map<String, String> data = new HashMap<>();

            data.put("nomUser", nom.trim());
            data.put("email", email.trim());
            data.put("password", password);

            // Appel backend
            ApiClient.post("/users/register", data, Object.class);

            infoLabel.setTextFill(Color.GREEN);
            infoLabel.setText("Compte créé avec succès !");

            // Retour vers connexion
            MainApp.showLoginView();

        } catch (Exception e) {

            e.printStackTrace();

            infoLabel.setTextFill(Color.RED);
            infoLabel.setText(ApiErrors.extractMessage(e));
        }
    }


    @FXML
    private void goToLogin() {

        try {
            MainApp.showLoginView();

        } catch (Exception e) {

            e.printStackTrace();

            infoLabel.setTextFill(Color.RED);
            infoLabel.setText("Erreur : " + e.getMessage());
        }
    }
}