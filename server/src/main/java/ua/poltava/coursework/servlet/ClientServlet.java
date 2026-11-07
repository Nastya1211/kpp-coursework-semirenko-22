package ua.poltava.coursework.servlet;

import ua.poltava.coursework.model.Client;
import ua.poltava.coursework.service.ClientService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/clients", "/clients/*"})
public class ClientServlet extends ApiServlet {
    private final ClientService service = new ClientService();

    @Override
    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JsonUtil.write(resp, 200, service.findAll());
    }

    @Override
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Client created = service.create(JsonUtil.read(req, Client.class));
        JsonUtil.write(resp, 201, created);
    }

    @Override
    protected void put(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = requireId(req);
        Client in = JsonUtil.read(req, Client.class);
        Client client = new Client(id, in.fullName(), in.phone());
        if (service.update(client)) {
            JsonUtil.write(resp, 200, client);
        } else {
            JsonUtil.error(resp, 404, "Клієнта не знайдено");
        }
    }

    @Override
    protected void delete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (service.delete(requireId(req))) {
            resp.setStatus(204);
        } else {
            JsonUtil.error(resp, 404, "Клієнта не знайдено");
        }
    }
}