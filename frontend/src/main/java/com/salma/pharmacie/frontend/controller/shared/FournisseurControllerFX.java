package com.salma.pharmacie.frontend.controller.shared;

import com.google.gson.reflect.TypeToken;
import com.salma.pharmacie.frontend.model.*;
import com.salma.pharmacie.frontend.utils.ApiClient;
import com.salma.pharmacie.frontend.utils.ApiErrors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class FournisseurControllerFX {

    @FXML private TextField searchField;

    @FXML private TableView<Fournisseur> fournisseurTable;
    @FXML private TableColumn<Fournisseur, Integer> idCol;
    @FXML private TableColumn<Fournisseur, String> nomCol;
    @FXML private TableColumn<Fournisseur, String> telCol;
    @FXML private TableColumn<Fournisseur, Void> actionsCol;

    @FXML private TableView<Fourniture> fournitureTable;
    @FXML private TableColumn<Fourniture, Integer> prodIdCol;
    @FXML private TableColumn<Fourniture, String> prodNomCol;
    @FXML private TableColumn<Fourniture, Double> prixAchatCol;
    @FXML private TableColumn<Fourniture, Void> fActionsCol;

    private ObservableList<Fournisseur> allFournisseurs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // ---------- Fournisseurs table ----------
        idCol.setCellValueFactory(new PropertyValueFactory<>("idFournisseur"));
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nomFournisseur"));
        telCol.setCellValueFactory(new PropertyValueFactory<>("tel"));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button delBtn = new Button("Supprimer");
            private final ToolBar bar = new ToolBar(editBtn, delBtn);

            {
                editBtn.setOnAction(e -> {
                    Fournisseur f = getTableView().getItems().get(getIndex());
                    handleEditFournisseur(f);
                });
                delBtn.setOnAction(e -> {
                    Fournisseur f = getTableView().getItems().get(getIndex());
                    handleDeleteFournisseur(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : bar);
            }
        });

        // ---------- Fournitures table ----------
        prodIdCol.setCellValueFactory(new PropertyValueFactory<>("idProduit"));
        prodNomCol.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        prixAchatCol.setCellValueFactory(new PropertyValueFactory<>("prixAchat"));

        fActionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Prix");
            private final Button delBtn = new Button("Supprimer");
            private final ToolBar bar = new ToolBar(editBtn, delBtn);

            {
                editBtn.setOnAction(e -> {
                    Fourniture f = getTableView().getItems().get(getIndex());
                    handleEditFourniturePrix(f);
                });
                delBtn.setOnAction(e -> {
                    Fourniture f = getTableView().getItems().get(getIndex());
                    handleDeleteFourniture(f);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : bar);
            }
        });

        // sélection fournisseur -> charger fournitures
        fournisseurTable.getSelectionModel().selectedItemProperty().addListener((obs, oldF, newF) -> {
            if (newF != null) loadFournitures();
            else fournitureTable.setItems(FXCollections.observableArrayList());
        });

        // filtre recherche fournisseurs
        searchField.textProperty().addListener((obs, o, n) -> applyFilter(n));

        Platform.runLater(this::loadFournisseurs);
    }

    // ===================== FOURNISSEURS =====================
    @FXML
    public void loadFournisseurs() {
        try {
            Type type = new TypeToken<ApiResponse<List<Fournisseur>>>() {}.getType();
            ApiResponse<List<Fournisseur>> res = ApiClient.get("/fournisseurs", type);

            List<Fournisseur> list = (res != null && res.getData() != null) ? res.getData() : List.of();
            allFournisseurs = FXCollections.observableArrayList(list);
            fournisseurTable.setItems(allFournisseurs);

            applyFilter(searchField.getText());

            // sélectionner 1er fournisseur pour charger fournitures
            if (!allFournisseurs.isEmpty()) {
                fournisseurTable.getSelectionModel().select(0);
                loadFournitures();
            } else {
                fournitureTable.setItems(FXCollections.observableArrayList());
            }

        } catch (Exception e) {
            showError("Erreur chargement fournisseurs", ApiErrors.extractMessage(e));
        }
    }

    private void applyFilter(String query) {
        if (query == null || query.trim().isEmpty()) {
            fournisseurTable.setItems(allFournisseurs);
            return;
        }

        String q = query.trim().toLowerCase(Locale.ROOT);

        List<Fournisseur> filtered = allFournisseurs.stream()
                .filter(f ->
                        (f.getNomFournisseur() != null && f.getNomFournisseur().toLowerCase(Locale.ROOT).contains(q)) ||
                                (f.getTel() != null && f.getTel().toLowerCase(Locale.ROOT).contains(q))
                )
                .collect(Collectors.toList());

        fournisseurTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    public void handleAddFournisseur() {
        Optional<Fournisseur> res = showFournisseurDialog(null);
        res.ifPresent(f -> {
            try {
                Type t = new TypeToken<ApiResponse<Fournisseur>>() {}.getType();
                ApiClient.post("/fournisseurs", f, t);
                loadFournisseurs();
            } catch (Exception e) {
                showError("Erreur ajout fournisseur", ApiErrors.extractMessage(e));
            }
        });
    }

    private void handleEditFournisseur(Fournisseur existing) {
        Optional<Fournisseur> res = showFournisseurDialog(existing);
        res.ifPresent(updated -> {
            try {
                Type t = new TypeToken<ApiResponse<Fournisseur>>() {}.getType();
                ApiClient.put("/fournisseurs/" + existing.getIdFournisseur(), updated, t);
                loadFournisseurs();
            } catch (Exception e) {
                showError("Erreur modification fournisseur", ApiErrors.extractMessage(e));
            }
        });
    }

    private void handleDeleteFournisseur(Fournisseur f) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce fournisseur ?");
        confirm.setContentText("ID: " + f.getIdFournisseur() + " | " + f.getNomFournisseur());

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    ApiClient.delete("/fournisseurs/" + f.getIdFournisseur());
                    loadFournisseurs();
                } catch (Exception e) {
                    showError("Erreur suppression fournisseur", ApiErrors.extractMessage(e));
                }
            }
        });
    }

    private Optional<Fournisseur> showFournisseurDialog(Fournisseur existing) {

        Dialog<Fournisseur> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Ajouter Fournisseur" : "Modifier Fournisseur");

        ButtonType okType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField nomField = new TextField();
        nomField.setPromptText("Nom fournisseur");

        TextField telField = new TextField();
        telField.setPromptText("Téléphone");

        if (existing != null) {
            nomField.setText(existing.getNomFournisseur());
            telField.setText(existing.getTel());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Nom:"), nomField);
        grid.addRow(1, new Label("Téléphone:"), telField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            String nom = nomField.getText() != null ? nomField.getText().trim() : "";
            String tel = telField.getText() != null ? telField.getText().trim() : "";

            if (nom.isEmpty() || tel.isEmpty()) {
                showError("Erreur", "Nom et Téléphone sont obligatoires.");
                return null;
            }

            Fournisseur f = new Fournisseur();
            f.setNomFournisseur(nom);
            f.setTel(tel);
            return f;
        });

        return dialog.showAndWait();
    }

    // ===================== FOURNITURES =====================
    @FXML
    public void loadFournitures() {
        Fournisseur selected = fournisseurTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            fournitureTable.setItems(FXCollections.observableArrayList());
            return;
        }

        try {
            Integer idF = selected.getIdFournisseur();

            Type type = new TypeToken<ApiResponse<List<Fourniture>>>() {}.getType();
            ApiResponse<List<Fourniture>> res = ApiClient.get("/fournitures/fournisseur/" + idF, type);

            List<Fourniture> list = (res != null && res.getData() != null) ? res.getData() : List.of();
            fournitureTable.setItems(FXCollections.observableArrayList(list));

        } catch (Exception e) {
            showError("Erreur chargement fournitures", ApiErrors.extractMessage(e));
        }
    }

    @FXML
    public void handleAddFourniture() {
        Fournisseur selected = fournisseurTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Erreur", "Sélectionne un fournisseur d'abord.");
            return;
        }

        Dialog<double[]> dialog = new Dialog<>();
        dialog.setTitle("Ajouter Fourniture (Produit vendu)");

        ButtonType okType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField produitIdField = new TextField();
        produitIdField.setPromptText("Produit ID");

        TextField prixField = new TextField();
        prixField.setPromptText("Prix Achat");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Produit ID:"), produitIdField);
        grid.addRow(1, new Label("Prix achat:"), prixField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            Integer pid = parseIntOrNull(produitIdField.getText());
            Double prix = parseDoubleOrNull(prixField.getText());

            if (pid == null || prix == null || prix <= 0) {
                showError("Erreur", "Produit ID et Prix valides obligatoires.");
                return null;
            }

            return new double[]{pid, prix};
        });

        dialog.showAndWait().ifPresent(arr -> {
            try {
                int idProduit = (int) arr[0];
                double prixAchat = arr[1];

                Type t = new TypeToken<ApiResponse<Fourniture>>() {}.getType();
                ApiClient.post("/fournitures/" + idProduit + "/" + selected.getIdFournisseur() + "?prixAchat=" + prixAchat,
                        null, t);

                loadFournitures();
            } catch (Exception e) {
                showError("Erreur ajout fourniture", ApiErrors.extractMessage(e));
            }
        });
    }

    private void handleEditFourniturePrix(Fourniture f) {
        TextInputDialog dialog = new TextInputDialog(f.getPrixAchat() != null ? String.valueOf(f.getPrixAchat()) : "");
        dialog.setTitle("Modifier Prix Achat");
        dialog.setHeaderText("Produit ID: " + f.getIdProduit());
        dialog.setContentText("Nouveau prix achat:");

        dialog.showAndWait().ifPresent(val -> {
            try {
                Double prix = parseDoubleOrNull(val);
                if (prix == null || prix <= 0) {
                    showError("Erreur", "Prix invalide.");
                    return;
                }

                Type t = new TypeToken<ApiResponse<Fourniture>>() {}.getType();
                ApiClient.put("/fournitures/" + f.getIdProduit() + "/" + f.getIdFournisseur() + "?prixAchat=" + prix,
                        null, t);

                loadFournitures();
            } catch (Exception e) {
                showError("Erreur modification prix", ApiErrors.extractMessage(e));
            }
        });
    }

    private void handleDeleteFourniture(Fourniture f) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer ce lien produit-fournisseur ?");
        confirm.setContentText("Produit ID: " + f.getIdProduit());

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    ApiClient.delete("/fournitures/" + f.getIdProduit() + "/" + f.getIdFournisseur());
                    loadFournitures();
                } catch (Exception e) {
                    showError("Erreur suppression fourniture", ApiErrors.extractMessage(e));
                }
            }
        });
    }

    // ===================== Helpers =====================
    private Integer parseIntOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
