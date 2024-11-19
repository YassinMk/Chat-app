package com.omar.chatapp.model;

import com.omar.chatapp.util.DatabaseUtil;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDB {

    private static List<String> activeClientIds = new ArrayList<>();
    private static Map<String, Socket> activeClientList = new HashMap<>();
    private static List<MessageGroup> groupList = new ArrayList<>();

    public static List<String> getActiveClientIds(){
        return activeClientIds;
    }

    public static Map<String, Socket> getActiveClientList(){
        return activeClientList;
    }

    public static List<MessageGroup> getGroupList() {
        return groupList;
    }

    private static List<User> userList = new ArrayList<>();
    public static List<User> getUserList() {
        userList = DatabaseUtil.getRegisteredUsers();
        return userList;
    }

    public static void addUserInFile(User user){
        DatabaseUtil.addUser(user);
        userList = DatabaseUtil.getRegisteredUsers();
    }

    public static void deleteAllRegisteredUser(){
        DatabaseUtil.deleteAllUserRecords();
        userList = DatabaseUtil.getRegisteredUsers();
    }

    public static boolean isIdenticalUser(User targetUser){
        userList = DatabaseUtil.getRegisteredUsers();
        for (User user : userList) {
            if (targetUser.getUsername().equals(user.getUsername())) {
                return true;
            }
        }
        return false;
    }

    public static User findUser(String username, String password) {
        userList = DatabaseUtil.getRegisteredUsers();
        for (User user : userList) {
            if (username.equals(user.getUsername()) && password.equals(user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    private static Map<String, List<MessageGroup>> userWiseGroupMap = new HashMap<>();
    private static Map<Pair<String, String>, VBox> userWiseConversationMap = new HashMap<>();

    public static void checkUserAndAddGroup(String userId, MessageGroup messageGroup) {
        if (userWiseGroupMap.containsKey(userId)){
            userWiseGroupMap.get(userId).add(messageGroup);
        }else {
            List<MessageGroup> list = new ArrayList<>();
            list.add(messageGroup);
            userWiseGroupMap.put(userId, list);
        }
    }

    public static Map<Pair<String, String>, VBox> getUserWiseConversationMap() {
        return userWiseConversationMap;
    }
}
