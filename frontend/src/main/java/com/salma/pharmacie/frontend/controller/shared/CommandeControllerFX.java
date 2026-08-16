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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.lang.reflect.Type;
import java.util.*;

public class CommandeControllerFX {

    @FXML private TableView<Commande> commandeTable;
    @FXML private TableColumn<Commande, Integer> idCol;
    @FXML private TableColumn<Commande, String> dateCol;
    @FXML private TableColumn<Commande, String> statutCol;
    @FXML private TableColumn<Commande, Integer> fournisseurCol;
    @FXML private TableColumn<Commande, Void> actionsCol;

    @FXML private TableView<LigneCommande> ligneTable;
    @FXML private TableColumn<LigneCommande, Integer> produitIdCol;
    @FXML private TableColumn<LigneCommande, Integer> qteCol;

    @FXML
    public void initialize() {

        idCol.setCellValueFactory(new PropertyValueFactory<>("idCommande"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        fournisseurCol.setCellValueFactory(new PropertyValueFactory<>("fournisseurId"));

        produitIdCol.setCellValueFactory(new PropertyValueFactory<>("produitId"));
        qteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final Button statutBtn = new Button("Statut");
            private final Button deleteBtn = new Button("Supprimer");
            private final ToolBar bar = new ToolBar(statutBtn, deleteBtn);

            {
                statutBtn.setOnAction(e -> {
                    Commande c = getTableView().getItems().get(getIndex());
                    handleChangeStatut(c);
                });

                deleteBtn.setOnAction(e -> {
                    Commande c = getTableView().getItems().get(getIndex());
                    handleDeleteCommande(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : bar);
            }
        });

        commandeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldC, newC) -> {
            if (newC == null || newC.getLignes() == null) {
                ligneTable.setItems(FXCollections.observableArrayList());
            } else {
                ligneTable.setItems(FXCollections.observableArrayList(newC.getLignes()));
            }
        });

        Platform.runLater(this::loadCommandes);
    }

    @FXML
    public void loadCommandes() {
        try {
            Type type = new TypeToken<List<Commande>>() {}.getType();
            List<Commande> commandes = ApiClient.get("/commandes", type);

            ObservableList<Commande> data = FXCollections.observableArrayList(commandes);
            commandeTable.setItems(data);

            // ✅ sélectionner la première commande qui a des lignes
            if (!data.isEmpty()) {

                int indexToSelect = 0;
                for (int i = 0; i < data.size(); i++) {
                    Commande c = data.get(i);
                    if (c.getLignes() != null && !c.getLignes().isEmpty()) {
                        indexToSelect = i;
                        break;
                    }
                }

                commandeTable.getSelectionModel().select(indexToSelect);

                Commande selected = commandeTable.getSelectionModel().getSelectedItem();
                if (selected != null && selected.getLignes() != null) {
                    ligneTable.setItems(FXCollections.observableArrayList(selected.getLignes()));
                } else {
                    ligneTable.setItems(FXCollections.observableArrayList());
                }

            } else {
                ligneTable.setItems(FXCollections.observableArrayList());
            }

        } catch (Exception e) {
            showError("Erreur chargement commandes", ApiErrors.extractMessage(e));
        }
    }



    @FXML
    public void handleAddCommande() {

        Dialog<CommandeRequest> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Commande");

        ButtonType okType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField fournisseurField = new TextField();
        fournisseurField.setPromptText("Fournisseur ID");

        ChoiceBox<String> statutBox = new ChoiceBox<>();
        statutBox.getItems().addAll("EN_ATTENTE", "LIVREE", "ANNULEE");
        statutBox.setValue("EN_ATTENTE");

        ObservableList<LigneCommandeRequest> lignesTemp = FXCollections.observableArrayList();

        TableView<LigneCommandeRequest> lignesTable = new TableView<>(lignesTemp);
        TableColumn<LigneCommandeRequest, Integer> pCol = new TableColumn<>("Produit ID");
        pCol.setCellValueFactory(new PropertyValueFactory<>("produitId"));
        TableColumn<LigneCommandeRequest, Integer> qCol = new TableColumn<>("Quantité");
        qCol.setCellValueFactory(new PropertyValueFactory<>("quantiteDemande"));
        lignesTable.getColumns().addAll(pCol, qCol);
        lignesTable.setPrefHeight(180);

        TextField produitField = new TextField();
        produitField.setPromptText("Produit ID");

        TextField qteField = new TextField();
        qteField.setPromptText("Quantité demandée");

        Button addLineBtn = new Button("Ajouter ligne");
        Button removeLineBtn = new Button("Supprimer ligne");

        addLineBtn.setOnAction(e -> {
            Integer pid = parseIntOrNull(produitField.getText());
            Integer qte = parseIntOrNull(qteField.getText());

            if (pid == null || qte == null || qte <= 0) {
                showError("Erreur", "Produit ID et Quantité valides obligatoires.");
                return;
            }

            LigneCommandeRequest lr = new LigneCommandeRequest();
            lr.setProduitId(pid);
            lr.setQuantiteDemande(qte);
            lignesTemp.add(lr);

            produitField.clear();
            qteField.clear();
        });

        removeLineBtn.setOnAction(e -> {
            LigneCommandeRequest selected = lignesTable.getSelectionModel().getSelectedItem();
            if (selected != null) lignesTemp.remove(selected);
        });

        HBox lineInputs = new HBox(10, produitField, qteField, addLineBtn, removeLineBtn);

        VBox content = new VBox(10,
                new Label("Fournisseur ID:"),
                fournisseurField,
                new Label("Statut:"),
                statutBox,
                new Label("Lignes:"),
                lignesTable,
                lineInputs
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            Integer fid = parseIntOrNull(fournisseurField.getText());
            if (fid == null) {
                showError("Erreur", "Fournisseur ID obligatoire.");
                return null;
            }
            if (lignesTemp.isEmpty()) {
                showError("Erreur", "Ajoute au moins une ligne.");
                return null;
            }

            CommandeRequest req = new CommandeRequest();
            req.setFournisseurId(fid);
            req.setStatut(statutBox.getValue());
            req.setLignes(new ArrayList<>(lignesTemp));
            return req;
        });

        dialog.showAndWait().ifPresent(req -> {
            try {
                ApiClient.post("/commandes", req, Object.class);
                loadCommandes();
            } catch (Exception e) {
                showError("Erreur création commande", ApiErrors.extractMessage(e));
            }
        });
    }

    private void handleChangeStatut(Commande c) {
        List<String> statuts = Arrays.asList("EN_ATTENTE", "LIVREE", "ANNULEE");

        ChoiceDialog<String> dialog = new ChoiceDialog<>(c.getStatut(), statuts);
        dialog.setTitle("Changer Statut");
        dialog.setHeaderText("Commande #" + c.getIdCommande());
        dialog.setContentText("Nouveau statut:");

        dialog.showAndWait().ifPresent(newStatut -> {
            try {
                CommandeRequest body = new CommandeRequest();
                body.setStatut(newStatut);

                ApiClient.put("/commandes/" + c.getIdCommande() + "/statut", body, Object.class);
                loadCommandes();
            } catch (Exception e) {
                showError("Erreur changement statut", ApiErrors.extractMessage(e));
            }
        });
    }

    private void handleDeleteCommande(Commande c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la commande ?");
        confirm.setContentText("Commande #" + c.getIdCommande());

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    ApiClient.delete("/commandes/" + c.getIdCommande());
                    loadCommandes();
                } catch (Exception e) {
                    showError("Erreur suppression", ApiErrors.extractMessage(e));
                }
            }
        });
    }

    private Integer parseIntOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
