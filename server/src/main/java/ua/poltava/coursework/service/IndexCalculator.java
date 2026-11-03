package ua.poltava.coursework.service;

public final class IndexCalculator {
    private static final double MAX_WAIT_DAYS = 14.0;

    private IndexCalculator() {}

    public static double normalizeLevel(int level) {
        if (level < 1 || level > 5) {
            throw new ValidationException("Рівень має бути від 1 до 5");
        }
        return (level - 1) * 25.0;
    }

    public static double normalizeWait(long days) {
        if (days <= 0) return 0.0;
        return Math.min(days / MAX_WAIT_DAYS, 1.0) * 100.0;
    }

    public static double integral(double f1, double f2, double f3) {
        double value = 0.40 * f1 + 0.35 * f2 + 0.25 * f3;
        return Math.round(value * 100.0) / 100.0;
    }
}
