package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/bus_reservation";
            String user = "root";
            String password = "Ganji1423";

            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Error while connecting to the database.");
            e.printStackTrace();
            return null;
        }
    }
}
