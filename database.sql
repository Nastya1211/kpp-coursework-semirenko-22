CREATE DATABASE IF NOT EXISTS autoservice
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE autoservice;

DROP TABLE IF EXISTS work;
DROP TABLE IF EXISTS service_order;
DROP TABLE IF EXISTS car;
DROP TABLE IF EXISTS client;

CREATE TABLE client (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        full_name VARCHAR(100) NOT NULL,
                        phone VARCHAR(20) NOT NULL
);

CREATE TABLE car (
                     id INT AUTO_INCREMENT PRIMARY KEY,
                     vin VARCHAR(17) NOT NULL UNIQUE,
                     brand VARCHAR(50) NOT NULL,
                     model VARCHAR(50) NOT NULL,
                     year INT NOT NULL,
                     client_id INT NOT NULL,
                     FOREIGN KEY (client_id) REFERENCES client(id)
);

CREATE TABLE service_order (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               car_id INT NOT NULL,
                               description VARCHAR(255) NOT NULL,
                               status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
                               safety_level INT NOT NULL,
                               mobility_level INT NOT NULL,
                               created_at DATE NOT NULL,
                               FOREIGN KEY (car_id) REFERENCES car(id)
);

CREATE TABLE work (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      order_id INT NOT NULL,
                      name VARCHAR(100) NOT NULL,
                      price DECIMAL(10,2) NOT NULL,
                      hours DECIMAL(5,2) NOT NULL,
                      FOREIGN KEY (order_id) REFERENCES service_order(id)
);

INSERT INTO client (full_name, phone) VALUES
                                          ('Іваненко Петро Сергійович', '+380501112233'),
                                          ('Коваль Олена Миколаївна', '+380672223344');

INSERT INTO car (vin, brand, model, year, client_id) VALUES
                                                         ('WVWZZZ1JZXW000001', 'Volkswagen', 'Golf', 2012, 1),
                                                         ('JTDKB20U093000002', 'Toyota', 'Yaris', 2015, 2);

INSERT INTO service_order (car_id, description, status, safety_level, mobility_level, created_at) VALUES
                                                                                                      (1, 'Не працюють гальма', 'CREATED', 5, 5, '2026-10-28'),
                                                                                                      (2, 'Заміна масла', 'DIAGNOSED', 1, 2, '2026-10-29');

INSERT INTO work (order_id, name, price, hours) VALUES
                                                    (1, 'Заміна гальмівних колодок', 1800.00, 2.0),
                                                    (2, 'Заміна масла і фільтра', 900.00, 1.0);