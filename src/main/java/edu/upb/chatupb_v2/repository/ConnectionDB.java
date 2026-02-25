package edu.upb.chatupb_v2.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionDB {

    private static final ConnectionDB connection = new ConnectionDB();

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/chatupb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String DB_USER = "root";

    private static final String DB_PASSWORD = "12345689";

    private ConnectionDB() {
    }

    public static ConnectionDB getInstance() {
        return connection;
    }

    public Connection getConection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Conexión MySQL exitosa.");
            return conn;

        } catch (SQLException e) {
            System.out.println("Error SQL al conectar a MySQL: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("No se encontró el driver de MySQL (mysql-connector-j).");
        }
        return null;
    }
}