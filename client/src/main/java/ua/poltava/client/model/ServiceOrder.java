package ua.poltava.client.model;

public record ServiceOrder(int id, int carId, String description, String status,
                           int safetyLevel, int mobilityLevel, String createdAt, double index) {}
