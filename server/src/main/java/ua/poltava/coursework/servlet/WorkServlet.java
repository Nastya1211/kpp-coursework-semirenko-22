package ua.poltava.coursework.servlet;

import ua.poltava.coursework.model.Work;
import ua.poltava.coursework.service.ValidationException;
import ua.poltava.coursework.service.WorkService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/works", "/works/*"})
public class WorkServlet extends ApiServlet {
    private final WorkService service = new WorkService();

    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String orderId = req.getParameter("orderId");
        if (orderId == null) {
            JsonUtil.write(resp, 200, service.findAll());
            return;
        }
        try {
            JsonUtil.write(resp, 200, service.findByOrderId(Integer.parseInt(orderId)));
        } catch (NumberFormatException e) {
            throw new ValidationException("Некоректний orderId");
        }
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Work created = service.create(JsonUtil.read(req, Work.class));
        JsonUtil.write(resp, 201, created);
    }

    @Override
    protected void put(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = requireId(req);
        Work in = JsonUtil.read(req, Work.class);
        Work work = new Work(id, in.orderId(), in.name(), in.price(), in.hours());
        if (service.update(work)) {
            JsonUtil.write(resp, 200, work);
        } else {
            JsonUtil.error(resp, 404, "Роботу не знайдено");
        }
    }

    @Override
    protected void delete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (service.delete(requireId(req))) {
            resp.setStatus(204);
        } else {
            JsonUtil.error(resp, 404, "Роботу не знайдено");
        }
    }
}
