package ua.poltava.coursework.service;

import ua.poltava.coursework.dao.CarDao;
import ua.poltava.coursework.model.Car;

import java.sql.SQLException;
import java.time.Year;
import java.util.List;

public class CarService {
    private final CarDao dao = new CarDao();

    public List<Car> findAll() throws SQLException {
        return dao.findAll();
    }

    public Car create(Car car) throws SQLException {
        validate(car);
        return dao.create(car);
    }

    public boolean update(Car car) throws SQLException {
        validate(car);
        return dao.update(car);
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id);
    }

    public static void validate(Car car) {
        if (car.vin() == null || !car.vin().matches("[A-HJ-NPR-Z0-9]{17}")) {
            throw new ValidationException("VIN має містити 17 символів (латинські літери й цифри)");
        }
        if (car.brand() == null || car.brand().isBlank() || car.model() == null || car.model().isBlank()) {
            throw new ValidationException("Марка і модель обов'язкові");
        }
        if (car.year() < 1950 || car.year() > Year.now().getValue() + 1) {
            throw new ValidationException("Некоректний рік випуску");
        }
        if (car.clientId() <= 0) {
            throw new ValidationException("Потрібно вказати власника");
        }
    }
}
