package com.omar.chatapp.server;

import com.omar.chatapp.model.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ServerController implements Initializable {
    @FXML
    private Label labelInfo;
    @FXML
    private Pane titlePane;
    @FXML
    private ImageView btnClose;
    @FXML
    private ImageView btnMinimize;
    @FXML
    private Button btnLoadRegisteredUser;
    @FXML
    private VBox activeClientsBox;
    @FXML
    private VBox usersBox;
    @FXML
    private ScrollPane sp_main;
    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private double x, y;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            serverSocket = new ServerSocket(1234);
            labelInfo.setText("Server successfully created.");
            Thread t = new Thread(() -> {
                while (!serverSocket.isClosed()) {
                    try {
                        socket = serverSocket.accept();
                        System.out.println("A new client has joined !");
                        communicateWithClient(socket);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.out.println("Server-side message: Client is failed to join.");
                        closeSocket(socket);
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
            labelInfo.setText("Error creating Server ... ");
        }

        activeClientsBox.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue);
            }
        });
        Platform.runLater(() -> {
                btnLoadRegisteredUser.fire();
        });
    }


    private void closeSocket(Socket socket) {
        try {
            if (socket != null) {
                socket.close();
                System.out.println(socket + "Socket is closed successfully.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void communicateWithClient(Socket socket) {
        Thread communicateWithClientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String clientId = null;
                try {
                    while (socket != null && socket.isConnected()) {
                        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                        Message message = (Message) objectInputStream.readObject();
                        if (message.getMessageType().equals(MessageType.CONNECTION)) {
                            clientId = message.getSenderId();
                            UserDB.getActiveClientList().put(message.getSenderId(), socket);
                            UserDB.getActiveClientIds().add(message.getSenderId());
                            System.out.println("Active Client Number: " + UserDB.getActiveClientList().size());
                            HBox hBox = new HBox(20);
                            Text text = new Text(message.getSenderId());
                            Button button = new Button();
                            ImageView iv = new ImageView(new Image(this.getClass().getResourceAsStream("/com/omar/chatapp/Images/logout-box-r-fill.png")));
                            iv.setFitHeight(15);
                            iv.setFitWidth(15);
                            button.setGraphic(iv);
                            hBox.getChildren().addAll(text, button);

                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    labelInfo.setText("A new client is joined !");
                                    activeClientsBox.getChildren().add(hBox);
                                    button.setOnAction(actionEvent -> {
                                        String deletedId = message.getSenderId();
                                        try {
                                            Message clientLogoutMsg = new Message();
                                            clientLogoutMsg.setMessageType(MessageType.LOGOUT_CLIENT);
                                            clientLogoutMsg.setSenderId("SERVER");
                                            clientLogoutMsg.setReceiverId(deletedId);
                                            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                                            objectOutputStream.writeObject(clientLogoutMsg);
                                            objectOutputStream.flush();

                                            UserDB.getActiveClientList().remove(deletedId);
                                            UserDB.getActiveClientIds().remove(deletedId);
                                            updateActiveClientListAfterRemovingClient(deletedId);
                                            socket.close();
                                            activeClientsBox.getChildren().remove(hBox);
                                            System.out.println("Shutdown : " + socket.toString());
                                            labelInfo.setText("Shutdown !");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            labelInfo.setText("Failed: Shutdown instruction sent to client!");
                                        }
                                    });
                                }
                            });

                            Message activeClientMessage = new Message();
                            activeClientMessage.setMessageType(MessageType.ACTIVE_CLIENTS);
                            activeClientMessage.setSenderId("SERVER");
                            activeClientMessage.setReceiverId(message.getSenderId());
                            activeClientMessage.setDataObject(UserDB.getActiveClientIds());
                            dispatchMessageToAllClients(activeClientMessage);
                        } else if (message.getMessageType().equals(MessageType.PLAIN) && isValidMessage(message)) {
                            if (UserDB.getActiveClientList().containsKey(message.getReceiverId())) {
                                Socket skt = UserDB.getActiveClientList().get(message.getReceiverId());
                                objectOutputStream = new ObjectOutputStream(skt.getOutputStream());
                                objectOutputStream.writeObject(message);
                                objectOutputStream.flush();
                            }
                        } else if (message.getMessageType().equals(MessageType.GROUP_CREATION)) {
                            UserDB.getGroupList().add((MessageGroup) message.getDataObject());
                            sendToGroupMember(message);
                        } else if (message.getMessageType().equals(MessageType.GROUP_MESSAGE) && isValidMessage(message)) {
                            sendToGroupMember(message);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    closeSocket(socket);
                    if (e instanceof SocketException || e instanceof EOFException){
                        if (clientId != null){
                            UserDB.getActiveClientList().remove(clientId);
                            UserDB.getActiveClientIds().remove(clientId);
                            for (Node n : activeClientsBox.getChildren()) {
                                HBox hBox = (HBox) n;
                                Text text = (Text) hBox.getChildren().get(0);
                                if (text.getText().equals(clientId)){
                                    Platform.runLater(() -> {
                                        activeClientsBox.getChildren().remove(n);
                                    });
                                    break;
                                }
                            }
                            updateActiveClientListAfterRemovingClient(clientId);
                        }
                    }
                    e.printStackTrace();
                    System.out.println("Error receiving message from the Client!");
                }
            }
        });
        communicateWithClientThread.setDaemon(true);
        communicateWithClientThread.start();
    }

    private void updateActiveClientListAfterRemovingClient(String removedClientId) {
        Message removeClientMessage = new Message();
        removeClientMessage.setMessageType(MessageType.REMOVE_CLIENT);
        removeClientMessage.setSenderId("SERVER");
        removeClientMessage.setReceiverId("");
        removeClientMessage.setDataObject(removedClientId);
        dispatchMessageToAllClients(removeClientMessage);
    }

    private void dispatchMessageToAllClients(Message message) {
        try {
            for (var activeClient : UserDB.getActiveClientList().entrySet()) {
                Socket s = activeClient.getValue();
                objectOutputStream = new ObjectOutputStream(s.getOutputStream());
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
            }
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private boolean isValidMessage(Message message) {
        if (message.getSenderId() != null && !message.getSenderId().isEmpty()
                && message.getReceiverId() != null && !message.getReceiverId().isEmpty()
                && message.getMsg() != null && !message.getMsg().isEmpty()
                && message.getSentDateTime() != null && !message.getSenderId().equals(message.getReceiverId())) {
            return true;
        } else {
            return false;
        }
    }

    private void sendToGroupMember(Message message) throws IOException {
        MessageGroup messageGroupDetails = (MessageGroup) message.getDataObject();
        List<String> participantIdList = messageGroupDetails.getParticipantIdList();
        for (String id : participantIdList) {
            if (!id.equals(message.getSenderId()) && UserDB.getActiveClientList().containsKey(id)){
                Socket skt = UserDB.getActiveClientList().get(id);
                objectOutputStream = new ObjectOutputStream(skt.getOutputStream());
                objectOutputStream.writeObject(message);
                objectOutputStream.flush();
            }
        }
    }

    private void activateTitleDragFunctionality(Stage stage){
        titlePane.setOnMousePressed(mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        });
        titlePane.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() - x);
            stage.setY(mouseEvent.getScreenY() - y);
        });
        btnClose.setOnMouseClicked(mouseEvent -> {
            Platform.exit();
        });
        btnMinimize.setOnMouseClicked(mouseEvent -> stage.setIconified(true));
    }
    @FXML
    void loadActiveUser(ActionEvent event) {
        usersBox.getChildren().clear();
        usersBox.setAlignment(Pos.CENTER_LEFT);
        Text t = new Text("Active Users");
        t.setFont(Font.font("Arial Rounded MT Bold", FontWeight.NORMAL, 13));
        usersBox.getChildren().add(t);
        int i = 1;
        for (String activeClientId : UserDB.getActiveClientIds()) {
            t = new Text(i++ +". " + activeClientId);
            t.setFont(Font.font("Arial Rounded MT Bold", FontWeight.NORMAL, 12));
            usersBox.getChildren().add(t);
        }
    }
    @FXML
    void loadRegisteredUser(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        activateTitleDragFunctionality(stage);
        usersBox.getChildren().clear();
        usersBox.setAlignment(Pos.CENTER_LEFT);
        Text t = new Text("Registered Users");
        t.setFont(Font.font("Arial Rounded MT Bold", FontWeight.NORMAL, 13));
        usersBox.getChildren().add(t);
        int i = 1;
        for (User user : UserDB.getUserList()) {
            t = new Text(i++ +". " + user.getUsername());
            t.setFont(Font.font("Arial Rounded MT Bold", FontWeight.NORMAL, 12));
            usersBox.getChildren().add(t);
        }
    }
    @FXML
    void deleteAllUser(ActionEvent event) {
        UserDB.deleteAllRegisteredUser();
    }

}