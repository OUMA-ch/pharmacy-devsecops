// src/main/java/com/salma/mini_projet_pharmacie/dto/ChartPointDTO.java
package com.salma.mini_projet_pharmacie.dto;

public class ChartPointDTO {
    private String label;
    private Double value;

    public ChartPointDTO() {}

    public ChartPointDTO(String label, Double value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
