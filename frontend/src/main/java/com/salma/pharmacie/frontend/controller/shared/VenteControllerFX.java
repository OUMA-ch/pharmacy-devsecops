package com.salma.pharmacie.frontend.controller.shared;

import com.google.gson.reflect.TypeToken;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.salma.pharmacie.frontend.model.*;
import com.salma.pharmacie.frontend.utils.ApiClient;
import com.salma.pharmacie.frontend.utils.ApiErrors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class VenteControllerFX {

    @FXML private TextField clientIdField;

    @FXML private TableView<Vente> venteTable;
    @FXML private TableColumn<Vente, Integer> idCol;
    @FXML private TableColumn<Vente, String> dateCol;
    @FXML private TableColumn<Vente, Integer> produitCol;
    @FXML private TableColumn<Vente, Integer> quantiteCol;
    @FXML private TableColumn<Vente, Double> prixTotalCol;
    @FXML private TableColumn<Vente, Integer> ordonnanceCol;

    @FXML private TableColumn<Vente, String> ordMedecinCol;
    @FXML private TableColumn<Vente, String> ordDateCol;
    @FXML private TableColumn<Vente, String> ordDescCol;

    @FXML private TableColumn<Vente, Void> actionsCol;

    @FXML
    public void initialize() {

        // ----- binding colonnes -----
        idCol.setCellValueFactory(new PropertyValueFactory<>("idVente"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateVenteAffiche"));
        produitCol.setCellValueFactory(new PropertyValueFactory<>("produitId"));
        quantiteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        prixTotalCol.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));
        ordonnanceCol.setCellValueFactory(new PropertyValueFactory<>("ordonnanceId"));

        ordMedecinCol.setCellValueFactory(new PropertyValueFactory<>("ordonnanceNomMedecin"));
        ordDateCol.setCellValueFactory(new PropertyValueFactory<>("ordonnanceDateEmission"));
        ordDescCol.setCellValueFactory(new PropertyValueFactory<>("ordonnanceDescription"));

        // ----- boutons actions -----
        actionsCol.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final Button pdfBtn = new Button("PDF");

            private final ToolBar bar = new ToolBar(editBtn, deleteBtn, pdfBtn);

            {
                editBtn.setOnAction(e -> {
                    Vente v = getTableView().getItems().get(getIndex());
                    handleEditVente(v);
                });

                deleteBtn.setOnAction(e -> {
                    Vente v = getTableView().getItems().get(getIndex());
                    handleDeleteVente(v);
                });

                pdfBtn.setOnAction(e -> {
                    Vente v = getTableView().getItems().get(getIndex());
                    exportFacturePDF(v);
                });
            }


            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : bar);
            }
        });

        //  AUTO LOAD au démarrage (après rendu UI)
        Platform.runLater(this::loadVentes);
    }

    // ---------------- LOAD LIST ----------------
    @FXML
    public void loadVentes() {
        try {
            Integer clientId = parseIntOrNull(clientIdField.getText());

            Type type = new TypeToken<List<Vente>>() {}.getType();
            List<Vente> ventes;

            if (clientId == null) {
                ventes = ApiClient.get("/ventes", type); // toutes les ventes
            } else {
                ventes = ApiClient.get("/ventes/client/" + clientId, type); // ventes du client
            }

            venteTable.setItems(FXCollections.observableArrayList(ventes));

        } catch (Exception e) {
            showError("Erreur chargement ventes", ApiErrors.extractMessage(e));
        }
    }

    // ---------------- ADD (avec ordonnance optionnelle) ----------------
    @FXML
    public void handleAddVente() {
        Integer currentClient = parseIntOrNull(clientIdField.getText());

        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Vente");

        ButtonType okType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField clientField = new TextField();
        TextField produitField = new TextField();
        TextField quantiteField = new TextField();
        CheckBox ordonnanceCheck = new CheckBox("Avec ordonnance ?");

        clientField.setPromptText("Client ID");
        produitField.setPromptText("Produit ID");
        quantiteField.setPromptText("Quantité");

        if (currentClient != null) clientField.setText(String.valueOf(currentClient));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Client ID:"), clientField);
        grid.addRow(1, new Label("Produit ID:"), produitField);
        grid.addRow(2, new Label("Quantité:"), quantiteField);
        grid.add(ordonnanceCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(okType).disableProperty().bind(
                clientField.textProperty().isEmpty()
                        .or(produitField.textProperty().isEmpty())
                        .or(quantiteField.textProperty().isEmpty())
        );

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            Integer clientId = parseIntOrNull(clientField.getText());
            Integer produitId = parseIntOrNull(produitField.getText());
            Integer qte = parseIntOrNull(quantiteField.getText());

            if (clientId == null || produitId == null || qte == null || qte <= 0) return null;

            return Map.of(
                    "clientId", clientId,
                    "produitId", produitId,
                    "quantite", qte,
                    "avecOrdonnance", ordonnanceCheck.isSelected()
            );
        });

        dialog.showAndWait().ifPresent(data -> {
            try {
                Integer clientId = (Integer) data.get("clientId");
                Integer produitId = (Integer) data.get("produitId");
                Integer quantite = (Integer) data.get("quantite");
                boolean avecOrd = (Boolean) data.get("avecOrdonnance");

                Integer ordonnanceId = null;

                if (avecOrd) {
                    OrdonnanceRequest ordReq = showOrdonnanceDialog(clientId);
                    if (ordReq == null) return;

                    OrdonnanceResponse ordRes = ApiClient.post("/ordonnances", ordReq, OrdonnanceResponse.class);

                    if (ordRes == null || ordRes.getIdOrdonnance() == null) {
                        showError("Erreur", "Création ordonnance échouée (réponse serveur vide).");
                        return;
                    }

                    ordonnanceId = ordRes.getIdOrdonnance().intValue();
                }

                VenteRequest req = new VenteRequest();
                req.setClientId(clientId);
                req.setProduitId(produitId);
                req.setQuantite(quantite);
                req.setOrdonnanceId(ordonnanceId);

                ApiClient.post("/ventes", req, Object.class);

                clientIdField.setText(String.valueOf(clientId));
                loadVentes();

            } catch (Exception e) {
                showError("Erreur ajout vente", ApiErrors.extractMessage(e));
            }
        });
    }

    // ---------------- EDIT : modifier quantité ----------------
    private void handleEditVente(Vente v) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Modifier Vente (Quantité)");

        ButtonType okType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField quantiteField = new TextField(String.valueOf(v.getQuantite()));
        quantiteField.setPromptText("Nouvelle quantité");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Quantité:"), quantiteField);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;
            Integer q = parseIntOrNull(quantiteField.getText());
            return (q != null && q > 0) ? q : null;
        });

        dialog.showAndWait().ifPresent(newQ -> {
            try {
                VenteRequest req = new VenteRequest();
                req.setClientId(v.getClientId());
                req.setProduitId(v.getProduitId());
                req.setQuantite(newQ);
                req.setOrdonnanceId(v.getOrdonnanceId());

                ApiClient.put("/ventes/" + v.getIdVente(), req, Object.class);
                loadVentes();

            } catch (Exception e) {
                showError("Erreur modification vente", ApiErrors.extractMessage(e));
            }
        });
    }

    // ---------------- DELETE ----------------
    private void handleDeleteVente(Vente v) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la vente ?");
        confirm.setContentText("Vente ID: " + v.getIdVente());

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    ApiClient.delete("/ventes/" + v.getIdVente());
                    loadVentes();
                } catch (Exception e) {
                    showError("Erreur suppression vente", ApiErrors.extractMessage(e));
                }
            }
        });
    }

    // ---------------- Ordonnance dialog ----------------
    private OrdonnanceRequest showOrdonnanceDialog(Integer clientId) {
        Dialog<OrdonnanceRequest> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Ordonnance");

        ButtonType okType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField medecinField = new TextField();
        medecinField.setPromptText("Nom du médecin");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Date d'émission");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(4);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Médecin:"), medecinField);
        grid.addRow(1, new Label("Date émission:"), datePicker);
        grid.addRow(2, new Label("Description:"), descArea);

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().lookupButton(okType).disableProperty().bind(
                medecinField.textProperty().isEmpty()
                        .or(descArea.textProperty().isEmpty())
                        .or(datePicker.valueProperty().isNull())
        );

        dialog.setResultConverter(btn -> {
            if (btn != okType) return null;

            OrdonnanceRequest req = new OrdonnanceRequest();
            req.setClientId(clientId);
            req.setNomMedecin(medecinField.getText().trim());
            req.setDescription(descArea.getText().trim());
            req.setDateEmission(datePicker.getValue().toString()); // yyyy-MM-dd
            return req;
        });

        return dialog.showAndWait().orElse(null);
    }
    // ===================== PDF =====================
    private void exportFacturePDF(Vente v) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exporter facture");
            chooser.setInitialFileName("facture_vente_" + v.getIdVente() + ".pdf");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf")
            );

            File file = chooser.showSaveDialog(venteTable.getScene().getWindow());
            if (file == null) return;

            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            doc.add(new Paragraph("PHARMACIE")
                    .setBold().setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(new Paragraph("Facture de vente\n\n"));

            doc.add(new Paragraph("Vente ID : " + v.getIdVente()));
            doc.add(new Paragraph("Date : " + v.getDateVenteAffiche()));
            doc.add(new Paragraph("Client ID : " + v.getClientId()));
            doc.add(new Paragraph("\n"));

            Table table = new Table(4);
            table.addHeaderCell("Produit ID");
            table.addHeaderCell("Quantité");
            table.addHeaderCell("Prix Unitaire");
            table.addHeaderCell("Total");

            table.addCell(String.valueOf(v.getProduitId()));
            table.addCell(String.valueOf(v.getQuantite()));
            table.addCell(String.valueOf(v.getPrixTotal() / v.getQuantite()));
            table.addCell(String.valueOf(v.getPrixTotal()));

            doc.add(table);

            doc.add(new Paragraph("\nPrix total : " + v.getPrixTotal() + " DH")
                    .setBold());

            if (v.getOrdonnanceId() != null) {
                doc.add(new Paragraph("\nOrdonnance")
                        .setBold());
                doc.add(new Paragraph("Médecin : " + v.getOrdonnanceNomMedecin()));
                doc.add(new Paragraph("Date émission : " + v.getOrdonnanceDateEmission()));
                doc.add(new Paragraph("Description : " + v.getOrdonnanceDescription()));
            }

            doc.add(new Paragraph("\nMerci pour votre confiance.")
                    .setTextAlignment(TextAlignment.CENTER));

            doc.close();

            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Succès");
            a.setHeaderText("Facture générée");
            a.setContentText(file.getAbsolutePath());
            a.showAndWait();

        } catch (Exception e) {
            showError("Erreur PDF", e.getMessage());
        }}
    // ---------------- Helpers ----------------
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
