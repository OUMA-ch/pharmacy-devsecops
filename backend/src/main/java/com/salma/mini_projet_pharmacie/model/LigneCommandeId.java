package com.salma.mini_projet_pharmacie.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class LigneCommandeId implements Serializable {

    private Integer numCmd;
    private Integer idProduit;

    public LigneCommandeId() {
    }

    public Integer getNumCmd() {
        return numCmd;
    }

    public void setNumCmd(Integer numCmd) {
        this.numCmd = numCmd;
    }

    public Integer getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(Integer idProduit) {
        this.idProduit = idProduit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LigneCommandeId)) return false;

        LigneCommandeId that = (LigneCommandeId) o;

        return Objects.equals(numCmd, that.numCmd)
                && Objects.equals(idProduit, that.idProduit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numCmd, idProduit);
    }
}