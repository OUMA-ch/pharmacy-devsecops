package com.salma.mini_projet_pharmacie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.Date;

@Data
public class ProduitDTO {

    private Integer idProduit;

    @NotBlank(message = "Le nom du produit est obligatoire.")
    private String nomCommercial;

    private String composition;

    @NotNull(message = "Le prix est obligatoire.")
    @PositiveOrZero(message = "Le prix doit être positif ou nul.")
    private Double prixP;

    private String formPharmaceutique;
    private String dosage;
    private Date datePeremption;

    @NotNull(message = "Le stock est obligatoire.")
    @PositiveOrZero(message = "Le stock doit être positif ou nul.")
    private Integer quantiteStock;
}
