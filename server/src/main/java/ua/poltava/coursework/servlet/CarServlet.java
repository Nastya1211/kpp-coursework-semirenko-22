package ua.poltava.coursework.servlet;

import ua.poltava.coursework.model.Car;
import ua.poltava.coursework.service.CarService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/cars", "/cars/*"})
public class CarServlet extends ApiServlet {
    private final CarService service = new CarService();

    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JsonUtil.write(resp, 200, service.findAll());
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Car created = service.create(JsonUtil.read(req, Car.class));
        JsonUtil.write(resp, 201, created);
    }

    @Override
    protected void put(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = requireId(req);
        Car in = JsonUtil.read(req, Car.class);
        Car car = new Car(id, in.vin(), in.brand(), in.model(), in.year(), in.clientId());
        if (service.update(car)) {
            JsonUtil.write(resp, 200, car);
        } else {
            JsonUtil.error(resp, 404, "Автомобіль не знайдено");
        }
    }

    @Override
    protected void delete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (service.delete(requireId(req))) {
            resp.setStatus(204);
        } else {
            JsonUtil.error(resp, 404, "Автомобіль не знайдено");
        }
    }
}
