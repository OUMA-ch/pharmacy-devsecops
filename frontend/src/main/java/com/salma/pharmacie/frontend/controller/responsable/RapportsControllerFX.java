// src/main/java/com/salma/pharmacie/frontend/controller/responsable/RapportsControllerFX.java
package com.salma.pharmacie.frontend.controller.responsable;

import com.google.gson.reflect.TypeToken;
import com.salma.pharmacie.frontend.model.ChartPoint;
import com.salma.pharmacie.frontend.utils.ApiClient;
import com.salma.pharmacie.frontend.utils.ApiErrors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class RapportsControllerFX {

    @FXML private ChoiceBox<String> typeBox;
    @FXML private ChoiceBox<String> metricBox;
    @FXML private DatePicker fromPicker;
    @FXML private DatePicker toPicker;
    @FXML private TextField topField;
    @FXML private TextField seuilField;

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML
    public void initialize() {
        typeBox.getItems().addAll("VENTES_PRODUIT", "STOCK_PRODUIT", "STOCK_FAIBLE");
        typeBox.setValue("VENTES_PRODUIT");

        metricBox.getItems().addAll("QTE", "CA", "STOCK");
        metricBox.setValue("CA");

        topField.setText("10");
        seuilField.setText("10");

        typeBox.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> refreshUIForType(n));

        Platform.runLater(() -> {
            refreshUIForType(typeBox.getValue());
            loadChart();
        });
        Platform.runLater(() -> {
            xAxis.setTickLabelRotation(-45);
            xAxis.setTickLabelGap(10);
        });
    }

    private void refreshUIForType(String type) {
        boolean ventes = "VENTES_PRODUIT".equals(type);
        boolean stockFaible = "STOCK_FAIBLE".equals(type);

        fromPicker.setDisable(!ventes);
        toPicker.setDisable(!ventes);

        seuilField.setDisable(!stockFaible);

        if (ventes) {
            metricBox.setValue("CA");
        } else if ("STOCK_PRODUIT".equals(type)) {
            metricBox.setValue("STOCK");
        } else if (stockFaible) {
            metricBox.setValue("STOCK");
        }
    }

    @FXML
    private void handleAfficher() {
        loadChart();
    }

    private void loadChart() {
        try {
            String type = typeBox.getValue();
            String metric = metricBox.getValue();

            Integer top = parseIntOrNull(topField.getText());
            Integer seuil = parseIntOrNull(seuilField.getText());
            LocalDate from = fromPicker.getValue();
            LocalDate to = toPicker.getValue();

            String url = buildUrl(type, metric, from, to, top, seuil);

            Type listType = new TypeToken<List<ChartPoint>>() {}.getType();
            List<ChartPoint> data = ApiClient.get(url, listType);

            renderBarChart(type, metric, data);

        } catch (Exception e) {
            showError("Erreur rapports", ApiErrors.extractMessage(e));
        }
    }

    private String buildUrl(String type, String metric, LocalDate from, LocalDate to, Integer top, Integer seuil) {
        StringBuilder sb = new StringBuilder("/reports/bar");
        sb.append("?type=").append(enc(type));

        if (metric != null && !metric.isBlank()) sb.append("&metric=").append(enc(metric));
        if (from != null) sb.append("&from=").append(enc(from.toString()));
        if (to != null) sb.append("&to=").append(enc(to.toString()));
        if (top != null) sb.append("&top=").append(top);
        if (seuil != null) sb.append("&seuil=").append(seuil);

        return sb.toString();
    }

    private void renderBarChart(String type, String metric, List<ChartPoint> data) {
        barChart.getData().clear();

        String seriesName = type + (metric != null ? (" / " + metric) : "");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

        if (data != null) {
            for (ChartPoint p : data) {
                if (p == null) continue;
                String label = (p.getLabel() == null) ? "" : p.getLabel();
                Number val = (p.getValue() == null) ? 0 : p.getValue();
                series.getData().add(new XYChart.Data<>(label, val));
            }
        }

        barChart.getData().add(series);
    }

    private Integer parseIntOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (Exception e) { return null; }
    }

    private String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
