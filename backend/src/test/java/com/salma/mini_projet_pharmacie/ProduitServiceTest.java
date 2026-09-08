package com.salma.mini_projet_pharmacie;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProduitServiceTest {

    @Test
    void testCalculStockApresVente() {
        int stockInitial = 50;
        int quantiteVendue = 10;
        int stockAttendu = 40;

        int stockReel = stockInitial - quantiteVendue;

        assertEquals(stockAttendu, stockReel);
    }

    @Test
    void testStockNePeutPasEtreNegatif() {
        int stock = 5;
        int quantiteDemandee = 10;

        assertFalse(quantiteDemandee <= stock, "La vente ne devrait pas être possible si le stock est insuffisant");
    }
}