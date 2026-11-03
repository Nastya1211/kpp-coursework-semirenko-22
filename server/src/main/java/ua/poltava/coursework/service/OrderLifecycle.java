package ua.poltava.coursework.service;

import ua.poltava.coursework.model.OrderStatus;

import java.util.Optional;

public final class OrderLifecycle {
    private OrderLifecycle() {}

    public static Optional<OrderStatus> next(OrderStatus status) {
        return switch (status) {
            case CREATED -> Optional.of(OrderStatus.DIAGNOSED);
            case DIAGNOSED -> Optional.of(OrderStatus.REPAIR);
            case REPAIR -> Optional.of(OrderStatus.DONE);
            case DONE -> Optional.empty();
        };
    }

    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        return next(from).map(n -> n == to).orElse(false);
    }
}