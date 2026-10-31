package ua.poltava.coursework.model;

import java.math.BigDecimal;

public record Work(int id, int orderId, String name, BigDecimal price, BigDecimal hours) {}

