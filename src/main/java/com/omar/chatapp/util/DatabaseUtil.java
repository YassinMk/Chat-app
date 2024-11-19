package com.omar.chatapp.util;

import com.omar.chatapp.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {

    private static Connection connection;
    static {
        connection = SingletonConnexionDB.getConnexion();

    }

    public static void createUserTable() throws SQLException {
        PreparedStatement pstm = connection.prepareStatement(
        "CREATE TABLE IF NOT EXISTS user (" +
                "userId INT NOT NULL AUTO_INCREMENT," +
                "username VARCHAR(50) NOT NULL," +
                "password VARCHAR(100) NOT NULL," +
                "PRIMARY KEY (userId)" +
                ");");
        pstm.executeUpdate();
    }

    public static int getNumberOfRegisteredUser(){
        int numberOfRegisteredUser = 0;
        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT COUNT(*) FROM user");
            ResultSet rst = pstm.executeQuery();
            rst.next();
            numberOfRegisteredUser = rst.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return numberOfRegisteredUser;
    }
    public static List<User> getRegisteredUsers(){
        List<User> userList = new ArrayList<>();
        try {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM user");
            ResultSet rst = pstm.executeQuery();
            while (rst.next()) {
                User user = new User();
                user.setUsername(rst.getString("username"));
                user.setUserId(rst.getInt("userId"));
                user.setPassword(rst.getString("password"));
                userList.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return userList;
    }
    public static void addUser(User user){
        try {
            if(!user.getUsername().isEmpty() && !user.getPassword().isEmpty()){
                String insertQuery = "INSERT INTO user (username, password) VALUES (?, ?)";
                PreparedStatement pstm = connection.prepareStatement(insertQuery);
                pstm.setString(1, user.getUsername());
                pstm.setString(2, user.getPassword());
                pstm.executeUpdate();
            }else{
                System.out.println("User not added.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void deleteAllUserRecords(){
        try {
            PreparedStatement pstm = connection.prepareStatement("TRUNCATE user");
            pstm.executeUpdate();
            System.out.println("All users deleted.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
