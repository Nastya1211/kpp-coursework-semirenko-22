package ua.poltava.coursework.model;

import java.time.LocalDate;

public record ServiceOrder(int id, int carId, String description, OrderStatus status,
                           int safetyLevel, int mobilityLevel, LocalDate createdAt) {}