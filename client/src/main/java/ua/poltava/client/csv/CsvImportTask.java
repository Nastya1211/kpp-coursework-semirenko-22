package ua.poltava.client.csv;

import com.google.gson.reflect.TypeToken;
import javafx.concurrent.Task;
import ua.poltava.client.api.ApiClient;
import ua.poltava.client.api.ApiException;
import ua.poltava.client.model.Car;
import ua.poltava.client.model.Client;

import java.lang.reflect.Type;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CsvImportTask extends Task<ImportResult> {
    private static final Logger LOG = Logger.getLogger("client");
    private static final Type CLIENTS = new TypeToken<List<Client>>() {}.getType();

    private final Path file;
    private final ApiClient api;

    public CsvImportTask(Path file, ApiClient api) {
        this.file = file;
        this.api = api;
    }

    @Override
    protected ImportResult call() throws Exception {
        LOG.info("Початок CSV-імпорту: " + file.getFileName());
        List<String> lines;
        try {
            lines = new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
        } catch (MalformedInputException e) {
            throw new IllegalArgumentException("Файл має бути в кодуванні UTF-8");
        }
        if (lines.size() < 2) {
            throw new IllegalArgumentException("У файлі немає даних (потрібен заголовок і хоча б один рядок)");
        }
        int total = lines.size() - 1; // перший рядок це заголовок

        // власників шукаємо за телефоном; відсутніх створюємо
        Map<String, Client> owners = new HashMap<>();
        List<Client> existing = api.request("GET", "/clients", null, CLIENTS);
        for (Client c : existing) {
            owners.put(c.phone(), c);
        }

        int imported = 0;
        List<String> errors = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            if (isCancelled()) {
                break;
            }
            updateProgress(i, total);
            updateMessage("Рядок " + i + " з " + total);
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                CarCsvParser.Row row = CarCsvParser.parse(line);
                Client owner = owners.get(row.ownerPhone());
                if (owner == null) {
                    owner = api.request("POST", "/clients",
                            new Client(0, row.ownerName(), row.ownerPhone()), Client.class);
                    owners.put(owner.phone(), owner);
                }
                api.request("POST", "/cars",
                        new Car(0, row.vin(), row.brand(), row.model(), row.year(), owner.id()), Car.class);
                imported++;
            } catch (IllegalArgumentException | ApiException e) {
                errors.add("Рядок " + (i + 1) + ": " + e.getMessage());
                LOG.warning("CSV, рядок " + (i + 1) + ": " + e.getMessage());
            }
            Thread.sleep(200);
        }
        LOG.info("CSV-імпорт завершено: імпортовано " + imported + ", помилок " + errors.size());
        return new ImportResult(imported, errors);
    }
}
