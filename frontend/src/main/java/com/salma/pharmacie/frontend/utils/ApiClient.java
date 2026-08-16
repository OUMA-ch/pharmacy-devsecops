package com.salma.pharmacie.frontend.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080";
    private static final Gson gson = new GsonBuilder().create();

    private static String readBody(CloseableHttpResponse response) throws IOException {
        return response.getEntity() != null
                ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8)
                : "";
    }

    private static void ensureSuccess(CloseableHttpResponse response, String body) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status < 200 || status >= 300) {
            // throw body brut, puis on va l'extraire proprement dans le controller
            throw new IOException(body == null || body.isBlank()
                    ? ("HTTP " + status)
                    : body);
        }
    }

    // ----------------------------- GET ------------------------------
    public static <T> T get(String endpoint, Type type) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(BASE_URL + endpoint);

            try (CloseableHttpResponse response = client.execute(request)) {
                String body = readBody(response);
                ensureSuccess(response, body);
                return gson.fromJson(body, type);
            }
        }
    }

    // ----------------------------- POST -----------------------------
    public static <T> T post(String endpoint, Object bodyObj, Type type) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost request = new HttpPost(BASE_URL + endpoint);
            request.setHeader("Content-Type", "application/json");

            String json = gson.toJson(bodyObj);
            request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(request)) {
                String body = readBody(response);
                ensureSuccess(response, body);

                if (type == Object.class || body == null || body.isBlank()) return null;
                return gson.fromJson(body, type);
            }
        }
    }

    public static <T> T post(String endpoint, Object body, Class<T> clazz) throws IOException {
        return post(endpoint, body, (Type) clazz);
    }

    // ----------------------------- PUT ------------------------------
    public static <T> T put(String endpoint, Object bodyObj, Type type) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPut request = new HttpPut(BASE_URL + endpoint);
            request.setHeader("Content-Type", "application/json");

            String json = gson.toJson(bodyObj);
            request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(request)) {
                String body = readBody(response);
                ensureSuccess(response, body);

                if (type == Object.class || body == null || body.isBlank()) return null;
                return gson.fromJson(body, type);
            }
        }
    }

    // ---------------------------- DELETE ----------------------------
    public static void delete(String endpoint) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpDelete request = new HttpDelete(BASE_URL + endpoint);

            try (CloseableHttpResponse response = client.execute(request)) {
                String body = readBody(response);
                ensureSuccess(response, body);
            }
        }
    }
}
