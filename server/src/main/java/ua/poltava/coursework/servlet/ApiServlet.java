package ua.poltava.coursework.servlet;

import com.google.gson.JsonParseException;
import ua.poltava.coursework.service.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ApiServlet extends HttpServlet {
    protected static final Logger LOG = Logger.getLogger("coursework");

    protected interface Action {
        void run() throws Exception;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        LOG.info(req.getMethod() + " " + req.getRequestURI());
        super.service(req, resp);
    }

    protected void get(HttpServletRequest req, HttpServletResponse resp) throws Exception { notAllowed(resp); }
    protected void post(HttpServletRequest req, HttpServletResponse resp) throws Exception { notAllowed(resp); }
    protected void put(HttpServletRequest req, HttpServletResponse resp) throws Exception { notAllowed(resp); }
    protected void delete(HttpServletRequest req, HttpServletResponse resp) throws Exception { notAllowed(resp); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> get(req, resp));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> post(req, resp));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> put(req, resp));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        run(resp, () -> delete(req, resp));
    }

    private void run(HttpServletResponse resp, Action action) throws IOException {
        try {
            action.run();
        } catch (ValidationException e) {
            LOG.warning("Валідація: " + e.getMessage());
            JsonUtil.error(resp, 400, e.getMessage());
        } catch (JsonParseException e) {
            LOG.warning("Некоректний JSON: " + e.getMessage());
            JsonUtil.error(resp, 400, "Некоректний JSON");
        } catch (SQLIntegrityConstraintViolationException e) {
            LOG.warning("Порушення цілісності: " + e.getMessage());
            JsonUtil.error(resp, 409, "Запис дублюється або пов'язаний з іншими даними");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка бази даних", e);
            JsonUtil.error(resp, 500, "Помилка бази даних");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Внутрішня помилка", e);
            JsonUtil.error(resp, 500, "Внутрішня помилка сервера");
        }
    }

    private void notAllowed(HttpServletResponse resp) throws IOException {
        JsonUtil.error(resp, 405, "Метод не підтримується");
    }

    protected static Integer idFromPath(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.equals("/")) {
            return null;
        }
        try {
            return Integer.parseInt(path.substring(1).split("/")[0]);
        } catch (NumberFormatException e) {
            throw new ValidationException("Некоректний id");
        }
    }

    protected static int requireId(HttpServletRequest req) {
        Integer id = idFromPath(req);
        if (id == null) {
            throw new ValidationException("Потрібно вказати id у шляху");
        }
        return id;
    }
}
