package ua.poltava.coursework.servlet;

import ua.poltava.coursework.model.ServiceOrder;
import ua.poltava.coursework.model.ServiceOrderView;
import ua.poltava.coursework.service.ServiceOrderService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

@WebServlet(urlPatterns = {"/orders", "/orders/*"})
public class ServiceOrderServlet extends ApiServlet {
    private final ServiceOrderService service = new ServiceOrderService();

    private static ServiceOrderView view(ServiceOrder order) {
        return ServiceOrderView.of(order, ServiceOrderService.calculateIndex(order, LocalDate.now()));
    }

    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Integer id = idFromPath(req);
        if (id == null) {
            List<ServiceOrderView> all = service.findAll().stream().map(ServiceOrderServlet::view).toList();
            JsonUtil.write(resp, 200, all);
            return;
        }
        var order = service.findById(id);
        if (order.isPresent()) {
            JsonUtil.write(resp, 200, view(order.get()));
        } else {
            JsonUtil.error(resp, 404, "Замовлення не знайдено");
        }
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String path = req.getPathInfo();
        if (path != null && path.endsWith("/advance")) {
            ServiceOrder updated = service.advance(requireId(req));
            JsonUtil.write(resp, 200, view(updated));
            return;
        }
        ServiceOrder created = service.create(JsonUtil.read(req, ServiceOrder.class));
        JsonUtil.write(resp, 201, view(created));
    }

    @Override
    protected void put(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = requireId(req);
        ServiceOrder in = JsonUtil.read(req, ServiceOrder.class);
        var existing = service.findById(id);
        if (existing.isEmpty()) {
            JsonUtil.error(resp, 404, "Замовлення не знайдено");
            return;
        }
        // стан і дату змінювати не можна: стан міняється лише через /advance
        ServiceOrder order = new ServiceOrder(id, in.carId(), in.description(), existing.get().status(),
                in.safetyLevel(), in.mobilityLevel(), existing.get().createdAt());
        service.update(order);
        JsonUtil.write(resp, 200, view(order));
    }

    @Override
    protected void delete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (service.delete(requireId(req))) {
            resp.setStatus(204);
        } else {
            JsonUtil.error(resp, 404, "Замовлення не знайдено");
        }
    }
}
