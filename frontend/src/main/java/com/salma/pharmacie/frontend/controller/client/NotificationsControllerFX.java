package com.salma.pharmacie.frontend.controller.client;

import com.google.gson.reflect.TypeToken;
import com.salma.pharmacie.frontend.model.NotificationFX;
import com.salma.pharmacie.frontend.utils.ApiClient;
import com.salma.pharmacie.frontend.utils.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.lang.reflect.Type;
import java.util.List;

public class NotificationsControllerFX {

    @FXML private TextField searchField;
    @FXML private TableView<NotificationFX> table;
    @FXML private TableColumn<NotificationFX, String> titreCol;
    @FXML private TableColumn<NotificationFX, String> messageCol;
    @FXML private TableColumn<NotificationFX, String> dateCol;
    @FXML private Label countLabel;
    @FXML private CheckBox unreadOnlyCheck;

    private List<NotificationFX> allNotifications;

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        titreCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitre()));
        messageCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMessage()));
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDateEnvoi()));

        loadNotifications();
    }

    @FXML
    private void loadNotifications() {
        try {
            int clientId = Session.getUser().getId();
            Type type = new TypeToken<List<NotificationFX>>(){}.getType();
            allNotifications = ApiClient.get("/notifications/client/" + clientId, type);

            applyFilter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void search() {
        try {
            int clientId = Session.getUser().getId();
            String q = searchField.getText();

            Type type = new TypeToken<List<NotificationFX>>(){}.getType();
            allNotifications = ApiClient.get("/notifications/client/" + clientId + "/search?q=" + q, type);

            applyFilter();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleUnreadFilter() {
        applyFilter();
    }

    private void applyFilter() {
        if (allNotifications == null) return;

        List<NotificationFX> filtered = allNotifications;
        if (unreadOnlyCheck.isSelected()) {
            filtered = allNotifications.stream()
                    .filter(n -> !n.isLu())
                    .toList();
        }

        table.setItems(FXCollections.observableArrayList(filtered));
        updateCount();
    }

    private void updateCount() {
        if (allNotifications == null) return;
        long total = allNotifications.size();
        long unread = allNotifications.stream().filter(n -> !n.isLu()).count();
        countLabel.setText("Total: " + total + " • Non lues: " + unread);
    }
}