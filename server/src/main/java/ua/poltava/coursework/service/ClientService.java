package ua.poltava.coursework.service;

import ua.poltava.coursework.dao.ClientDao;
import ua.poltava.coursework.model.Client;

import java.sql.SQLException;
import java.util.List;

public class ClientService {
    private final ClientDao dao = new ClientDao();

    public List<Client> findAll() throws SQLException {
        return dao.findAll();
    }

    public Client create(Client client) throws SQLException {
        validate(client);
        return dao.create(client);
    }

    public boolean update(Client client) throws SQLException {
        validate(client);
        return dao.update(client);
    }

    public boolean delete(int id) throws SQLException {
        return dao.delete(id);
    }

    public static void validate(Client client) {
        if (client.fullName() == null || client.fullName().isBlank()) {
            throw new ValidationException("ПІБ клієнта не може бути порожнім");
        }
        if (client.phone() == null || !client.phone().matches("\\+?[0-9]{10,13}")) {
            throw new ValidationException("Телефон має містити 10-13 цифр");
        }
    }
}
