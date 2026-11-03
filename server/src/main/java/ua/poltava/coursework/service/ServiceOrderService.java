package ua.poltava.coursework.service;

import ua.poltava.coursework.dao.ServiceOrderDao;
import ua.poltava.coursework.model.OrderStatus;
import ua.poltava.coursework.model.ServiceOrder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class ServiceOrderService {
    private final ServiceOrderDao dao = new ServiceOrderDao();

    public List<ServiceOrder> findAll() throws SQLException {
        return dao.findAll();
    }

    public Optional<ServiceOrder> findById(int id) throws SQLException {
        return dao.findById(id);
    }

    public ServiceOrder create(ServiceOrder order) throws SQLException {
        validate(order);
        ServiceOrder toSave = new ServiceOrder(0, order.carId(), order.description().trim(),
                OrderStatus.CREATED, order.safetyLevel(), order.mobilityLevel(), LocalDate.now());
        return dao.create(toSave);
    }

    public boolean update(ServiceOrder order) throws SQLException {
        validate(order);
        return dao.update(order);
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id);
    }

    public ServiceOrder advance(int id) throws SQLException {
        ServiceOrder order = dao.findById(id)
                .orElseThrow(() -> new ValidationException("Замовлення не знайдено"));
        OrderStatus next = OrderLifecycle.next(order.status())
                .orElseThrow(() -> new ValidationException("Замовлення вже виконано"));
        dao.updateStatus(id, next);
        return new ServiceOrder(order.id(), order.carId(), order.description(), next,
                order.safetyLevel(), order.mobilityLevel(), order.createdAt());
    }

    public boolean changeStatus(int id, OrderStatus target) throws SQLException {
        ServiceOrder order = dao.findById(id)
                .orElseThrow(() -> new ValidationException("Замовлення не знайдено"));
        if (!OrderLifecycle.canTransition(order.status(), target)) {
            throw new ValidationException("Недопустимий перехід: " + order.status() + " -> " + target);
        }
        return dao.updateStatus(id, target);
    }

    public static double calculateIndex(ServiceOrder order, LocalDate today) {
        double f1 = IndexCalculator.normalizeLevel(order.safetyLevel());
        double f2 = IndexCalculator.normalizeLevel(order.mobilityLevel());
        long days = ChronoUnit.DAYS.between(order.createdAt(), today);
        double f3 = IndexCalculator.normalizeWait(days);
        return IndexCalculator.integral(f1, f2, f3);
    }

    public static void validate(ServiceOrder order) {
        if (order.description() == null || order.description().isBlank()) {
            throw new ValidationException("Опис замовлення не може бути порожнім");
        }
        if (order.carId() <= 0) {
            throw new ValidationException("Потрібно вказати автомобіль");
        }

        IndexCalculator.normalizeLevel(order.safetyLevel());
        IndexCalculator.normalizeLevel(order.mobilityLevel());
    }
}
