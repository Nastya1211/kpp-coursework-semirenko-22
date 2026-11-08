package ua.poltava.client.csv;

public final class CarCsvParser {
    public record Row(String vin, String brand, String model, int year,
                      String ownerName, String ownerPhone) {}

    private CarCsvParser() {}

    public static Row parse(String line) {
        String[] p = line.split(",", -1);
        if (p.length != 6) {
            throw new IllegalArgumentException("Очікується 6 полів, знайдено " + p.length);
        }
        for (int i = 0; i < p.length; i++) {
            p[i] = p[i].trim();
        }
        String vin = p[0].toUpperCase();
        if (!vin.matches("[A-HJ-NPR-Z0-9]{17}")) {
            throw new IllegalArgumentException("Некоректний VIN: " + p[0]);
        }
        if (p[1].isEmpty() || p[2].isEmpty()) {
            throw new IllegalArgumentException("Марка і модель обов'язкові");
        }
        int year;
        try {
            year = Integer.parseInt(p[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Рік має бути числом: " + p[3]);
        }
        if (p[4].isEmpty()) {
            throw new IllegalArgumentException("ПІБ власника обов'язкове");
        }
        if (!p[5].matches("\\+?[0-9]{10,13}")) {
            throw new IllegalArgumentException("Некоректний телефон: " + p[5]);
        }
        return new Row(vin, p[1], p[2], year, p[4], p[5]);
    }
}
