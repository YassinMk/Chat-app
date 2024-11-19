package com.omar.chatapp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class  SingletonConnexionDB {
    private static Connection connection;

    static
    {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/java_jdbc", "root", "rootroot");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static Connection getConnexion(){
        return connection;
    }
}


