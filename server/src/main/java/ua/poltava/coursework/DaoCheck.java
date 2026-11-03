package ua.poltava.coursework;

import ua.poltava.coursework.dao.*;

public class DaoCheck {
    public static void main(String[] args) throws Exception {
        System.out.println(new ClientDao().findAll());
        System.out.println(new CarDao().findAll());
        System.out.println(new ServiceOrderDao().findAll());
        System.out.println(new WorkDao().findAll());
    }
}
