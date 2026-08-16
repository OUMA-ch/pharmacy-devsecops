package com.salma.pharmacie.frontend.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ApiErrors {

    // Prend une exception et retourne un message lisible.
    // Supporte:
    // - {"message":"Stock insuffisant"}
    // - "Stock insuffisant"
    public static String extractMessage(Exception e) {
        if (e == null || e.getMessage() == null) return "Erreur inconnue";

        String msg = e.getMessage().trim();
        if (msg.isEmpty()) return "Erreur inconnue";

        // Si c'est JSON -> extraire "message"
        try {
            if (msg.startsWith("{") && msg.endsWith("}")) {
                JsonObject obj = JsonParser.parseString(msg).getAsJsonObject();
                if (obj.has("message")) return obj.get("message").getAsString();
                if (obj.has("error")) return obj.get("error").getAsString();
            }
        } catch (Exception ignore) {}

        return msg;
    }
}
