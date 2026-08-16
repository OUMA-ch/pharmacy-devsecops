package com.salma.pharmacie.frontend.controller.responsable;

import com.google.gson.reflect.TypeToken;
import com.salma.pharmacie.frontend.model.Pharmacien;
import com.salma.pharmacie.frontend.utils.ApiClient;
import com.salma.pharmacie.frontend.utils.ApiErrors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class PharmacienCrudControllerFX {

    @FXML private TableView<Pharmacien> table;
    @FXML private TableColumn<Pharmacien, Integer> idCol;
    @FXML private TableColumn<Pharmacien, String> nomCol;
    @FXML private TableColumn<Pharmacien, String> emailCol;
    @FXML private TableColumn<Pharmacien, String> telCol;
    @FXML private TableColumn<Pharmacien, String> roleCol;
    @FXML private TableColumn<Pharmacien, Void> actionsCol;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nomUser"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        telCol.setCellValueFactory(new PropertyValueFactory<>("tele"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button delBtn = new Button("Supprimer");
            private final ToolBar bar = new ToolBar(editBtn, delBtn);

            {
                editBtn.setOnAction(e -> handleEdit(getCurrent()));
                delBtn.setOnAction(e -> handleDelete(getCurrent()));
            }

            private Pharmacien getCurrent() {
                return getTableView().getItems().get(getIndex());
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : bar);
            }
        });

        Platform.runLater(this::loadPharmaciens);
    }

    @FXML
    public void loadPharmaciens() {
        try {
            Type type = new TypeToken<List<Pharmacien>>(){}.getType();
            List<Pharmacien> list = ApiClient.get("/pharmaciens", type);
            table.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showError("Erreur", ApiErrors.extractMessage(e));
        }
    }

    @FXML
    public void handleAdd() {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Ajouter Pharmacien");

        ButtonType ok = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField nomField = new TextField();
        TextField emailField = new TextField();
        PasswordField passField = new PasswordField();
        TextField telField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Nom:"), nomField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("Password:"), passField);
        grid.addRow(3, new Label("Téléphone:"), telField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ok) return null;
            return Map.of(
                    "nomUser", nomField.getText().trim(),
                    "email", emailField.getText().trim(),
                    "password", passField.getText(),
                    "tele", telField.getText().trim()
            );
        });

        dialog.showAndWait().ifPresent(data -> {
            try {
                ApiClient.post("/pharmaciens", data, Object.class);
                loadPharmaciens();
            } catch (Exception e) {
                showError("Erreur création", ApiErrors.extractMessage(e));
            }
        });
    }

    private void handleEdit(Pharmacien p) {
        if (p == null) return;

        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Modifier Pharmacien");

        ButtonType ok = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        TextField nomField = new TextField(p.getNomUser());
        TextField emailField = new TextField(p.getEmail());
        PasswordField passField = new PasswordField(); // vide = ne pas changer
        TextField telField = new TextField(p.getTele());

        passField.setPromptText("Laisser vide pour ne pas changer");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Nom:"), nomField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("Nouveau password:"), passField);
        grid.addRow(3, new Label("Téléphone:"), telField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ok) return null;
            return Map.of(
                    "nomUser", nomField.getText().trim(),
                    "email", emailField.getText().trim(),
                    "password", passField.getText(),   // peut être vide
                    "tele", telField.getText().trim()
            );
        });

        dialog.showAndWait().ifPresent(data -> {
            try {
                ApiClient.put("/pharmaciens/" + p.getIdUser(), data, Object.class);
                loadPharmaciens();
            } catch (Exception e) {
                showError("Erreur modification", ApiErrors.extractMessage(e));
            }
        });
    }

    private void handleDelete(Pharmacien p) {
        if (p == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce pharmacien ?");
        confirm.setContentText(p.getNomUser() + " (" + p.getEmail() + ")");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    ApiClient.delete("/pharmaciens/" + p.getIdUser());
                    loadPharmaciens();
                } catch (Exception e) {
                    showError("Erreur suppression", ApiErrors.extractMessage(e));
                }
            }
        });
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
