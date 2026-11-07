package ua.poltava.coursework.servlet;

import com.google.gson.*;
import ua.poltava.coursework.service.ValidationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

public final class JsonUtil {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) -> LocalDate.parse(json.getAsString()))
            .create();

    private JsonUtil() {}

    public static void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(GSON.toJson(body));
    }

    public static void error(HttpServletResponse resp, int status, String message) throws IOException {
        write(resp, status, Map.of("error", message));
    }

    public static <T> T read(HttpServletRequest req, Class<T> type) throws IOException {
        T value = GSON.fromJson(req.getReader(), type);
        if (value == null) {
            throw new ValidationException("Порожнє тіло запиту");
        }
        return value;
    }
}
