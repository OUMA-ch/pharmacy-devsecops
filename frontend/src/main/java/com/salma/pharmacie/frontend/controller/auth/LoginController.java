package com.salma.pharmacie.frontend.controller.auth;

import com.salma.pharmacie.frontend.MainApp;
import com.salma.pharmacie.frontend.model.UserResponse;
import com.salma.pharmacie.frontend.utils.ApiClient;
import com.salma.pharmacie.frontend.utils.ApiErrors;
import com.salma.pharmacie.frontend.utils.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        try {
            String email = emailField.getText();
            String password = passwordField.getText();

            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                errorLabel.setText("Veuillez remplir email et mot de passe.");
                return;
            }

            Map<String, String> loginData = new HashMap<>();
            loginData.put("email", email.trim());
            loginData.put("password", password);

            UserResponse user = ApiClient.post("/auth/login", loginData, UserResponse.class);

            if (user == null || user.getRole() == null) {
                errorLabel.setText("Email ou mot de passe incorrect.");
                return;
            }

            Session.setUser(user);
            MainApp.showDashboard(user);

        } catch (Exception e) {
            errorLabel.setText(ApiErrors.extractMessage(e));
        }
    }

    @FXML
    private void openRegister() {
        try {
            MainApp.showRegisterView();
        } catch (Exception e) {
            errorLabel.setText("Erreur ouverture inscription: " + e.getMessage());
        }
    }
}
