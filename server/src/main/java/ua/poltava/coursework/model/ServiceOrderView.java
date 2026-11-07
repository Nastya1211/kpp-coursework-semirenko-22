package ua.poltava.coursework.model;

import java.time.LocalDate;

public record ServiceOrderView(int id, int carId, String description, OrderStatus status,
                               int safetyLevel, int mobilityLevel, LocalDate createdAt, double index) {
    public static ServiceOrderView of(ServiceOrder o, double index) {
        return new ServiceOrderView(o.id(), o.carId(), o.description(), o.status(),
                o.safetyLevel(), o.mobilityLevel(), o.createdAt(), index);
    }
}
