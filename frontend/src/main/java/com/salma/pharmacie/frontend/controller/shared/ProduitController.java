package com.salma.pharmacie.frontend.controller.shared;

import com.google.gson.reflect.TypeToken;
import com.salma.pharmacie.frontend.model.ApiResponse;
import com.salma.pharmacie.frontend.model.Produit;
import com.salma.pharmacie.frontend.utils.ApiClient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

public class ProduitController {

    @FXML private TableView<Produit> produitTable;

    @FXML private TableColumn<Produit, Integer> idCol;
    @FXML private TableColumn<Produit, String> nomCol;
    @FXML private TableColumn<Produit, String> descriptionCol;
    @FXML private TableColumn<Produit, Double> prixCol;
    @FXML private TableColumn<Produit, String> datePeremptionCol;
    @FXML private TableColumn<Produit, Integer> stockCol;

    @FXML private TableColumn<Produit, Void> actionsCol;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        stockCol.setCellValueFactory(new PropertyValueFactory<>("quantiteStock"));

        //  colonne date (utilise getDatePeremptionAffiche())
        datePeremptionCol.setCellValueFactory(new PropertyValueFactory<>("datePeremptionAffiche"));

        //  colonne actions (Modifier/Supprimer)
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final ToolBar toolBar = new ToolBar(editBtn, deleteBtn);

            {
                editBtn.setOnAction(e -> {
                    Produit p = getTableView().getItems().get(getIndex());
                    handleEditProduit(p);
                });

                deleteBtn.setOnAction(e -> {
                    Produit p = getTableView().getItems().get(getIndex());
                    handleDeleteProduit(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : toolBar);
            }
        });

        loadProduits();
    }

    // ------------------- LOAD -------------------
    @FXML
    public void loadProduits() {
        try {
            // Cas A : backend renvoie ApiResponse<List<Produit>>
            Type type = new TypeToken<ApiResponse<List<Produit>>>() {}.getType();
            ApiResponse<List<Produit>> res = ApiClient.get("/produits", type);

            List<Produit> produits = (res != null && res.getData() != null) ? res.getData() : List.of();
            produitTable.setItems(FXCollections.observableArrayList(produits));

        } catch (Exception ex) {
            // Cas B : backend renvoie List<Produit> direct
            try {
                Type typeList = new TypeToken<List<Produit>>() {}.getType();
                List<Produit> produits = ApiClient.get("/produits", typeList);
                produitTable.setItems(FXCollections.observableArrayList(produits));
            } catch (Exception e2) {
                showError("Erreur chargement produits", e2.getMessage());
            }
        }
    }

    // ------------------- ADD -------------------
    @FXML
    public void handleAddProduit() {
        Dialog<Produit> dialog = buildProduitDialog("Ajouter Produit", null);

        dialog.showAndWait().ifPresent(p -> {
            try {
                ApiClient.post("/produits", p, Object.class);
                loadProduits();
            } catch (Exception e) {
                showError("Erreur ajout produit", e.getMessage());
            }
        });
    }

    // ------------------- EDIT -------------------
    private void handleEditProduit(Produit existing) {
        Dialog<Produit> dialog = buildProduitDialog("Modifier Produit", existing);

        dialog.showAndWait().ifPresent(updated -> {
            try {
                ApiClient.put("/produits/" + existing.getId(), updated, Object.class);
                loadProduits();
            } catch (Exception e) {
                showError("Erreur modification produit", e.getMessage());
            }
        });
    }

    // ------------------- DELETE -------------------
    private void handleDeleteProduit(Produit p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le produit ?");
        confirm.setContentText("ID: " + p.getId() + " | " + p.getNom());

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    ApiClient.delete("/produits/" + p.getId());
                    loadProduits();
                } catch (Exception e) {
                    showError("Erreur suppression produit", e.getMessage());
                }
            }
        });
    }

    // ------------------- DIALOG (Ajout/Modif) -------------------
    private Dialog<Produit> buildProduitDialog(String title, Produit existing) {
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType okType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField nomField = new TextField();
        TextField descField = new TextField();
        TextField prixField = new TextField();
        TextField stockField = new TextField();
        DatePicker datePicker = new DatePicker();

        nomField.setPromptText("Nom");
        descField.setPromptText("Description");
        prixField.setPromptText("Prix");
        stockField.setPromptText("Stock");
        datePicker.setPromptText("Date péremption");

        // Pré-remplir si modification
        if (existing != null) {
            nomField.setText(existing.getNom());
            descField.setText(existing.getDescription());
            prixField.setText(existing.getPrix() != null ? existing.getPrix().toString() : "");
            stockField.setText(existing.getQuantiteStock() != null ? existing.getQuantiteStock().toString() : "");

            String d = existing.getDatePeremptionAffiche();
            if (d != null && !d.isBlank()) {
                try { datePicker.setValue(LocalDate.parse(d)); } catch (Exception ignored) {}
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Nom:"), nomField);
        grid.addRow(1, new Label("Description:"), descField);
        grid.addRow(2, new Label("Prix:"), prixField);
        grid.addRow(3, new Label("Stock:"), stockField);
        grid.addRow(4, new Label("Date péremption:"), datePicker);

        dialog.getDialogPane().setContent(grid);

        // Désactiver OK si nom vide
        dialog.getDialogPane().lookupButton(okType).disableProperty().bind(nomField.textProperty().isEmpty());

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            Produit p = new Produit();
            if (existing != null) p.setId(existing.getId());

            p.setNom(nomField.getText());
            p.setDescription(descField.getText());

            try { p.setPrix(Double.parseDouble(prixField.getText().trim())); }
            catch (Exception e) { p.setPrix(0.0); }

            try { p.setQuantiteStock(Integer.parseInt(stockField.getText().trim())); }
            catch (Exception e) { p.setQuantiteStock(0); }

            if (datePicker.getValue() != null) {
                p.setDatePeremption(datePicker.getValue().toString()); // yyyy-MM-dd
            } else {
                p.setDatePeremption(null);
            }

            return p;
        });

        return dialog;
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
