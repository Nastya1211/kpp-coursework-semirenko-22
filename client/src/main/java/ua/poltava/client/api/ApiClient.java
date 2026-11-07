package ua.poltava.client.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ApiClient {
    private static final String BASE = System.getenv().getOrDefault("API_URL", "http://localhost:8080/api");

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    public <T> T request(String method, String path, Object body, Type type) throws ApiException {
        try {
            HttpRequest.BodyPublisher publisher = body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .method(method, publisher)
                    .build();
            HttpResponse<String> response = http.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                throw new ApiException(errorText(response.body(), response.statusCode()));
            }
            String json = response.body();
            if (type == null || json == null || json.isBlank()) {
                return null;
            }
            return gson.fromJson(json, type);
        } catch (IOException e) {
            throw new ApiException("Сервер недоступний. Перевірте, чи запущено server.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Запит перервано");
        }
    }

    private String errorText(String body, int status) {
        try {
            JsonObject obj = JsonParser.parseString(body).getAsJsonObject();
            if (obj.has("error")) {
                return obj.get("error").getAsString();
            }
        } catch (RuntimeException ignored) {
        }
        return "Помилка сервера (код " + status + ")";
    }
}
