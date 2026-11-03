package ua.poltava.coursework.service;

import ua.poltava.coursework.dao.WorkDao;
import ua.poltava.coursework.model.Work;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class WorkService {
    private final WorkDao dao = new WorkDao();

    public List<Work> findAll() throws SQLException {
        return dao.findAll();
    }

    public List<Work> findByOrderId(int orderId) throws SQLException {
        return dao.findByOrderId(orderId);
    }

    public Work create(Work work) throws SQLException {
        validate(work);
        return dao.create(work);
    }

    public boolean update(Work work) throws SQLException {
        validate(work);
        return dao.update(work);
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id);
    }

    public static void validate(Work work) {
        if (work.name() == null || work.name().isBlank()) {
            throw new ValidationException("Назва роботи не може бути порожньою");
        }
        if (work.price() == null || work.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Ціна не може бути від'ємною");
        }
        if (work.hours() == null || work.hours().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Кількість годин має бути більшою за 0");
        }
        if (work.orderId() <= 0) {
            throw new ValidationException("Потрібно вказати замовлення");
        }
    }
}
