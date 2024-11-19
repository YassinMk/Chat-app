package com.omar.chatapp.client;

import com.omar.chatapp.model.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.*;

public class ClientController implements Initializable {
    @FXML
    private BorderPane bp_main;

    @FXML
    private ScrollPane sp_main;

    @FXML
    private VBox vBoxMessages;

    @FXML
    private VBox landingInfoVbox;

    @FXML
    private VBox chatWindowVbox;

    @FXML
    private Label labelSelectedUsername;

    @FXML
    private ListView<String> activeClientListView;

    @FXML
    private ListView<MessageGroup> groupListView;

    @FXML
    private TextField tf_message;

    @FXML
    private Label labelClientName;

    @FXML
    private Label labelInfo;
    @FXML
    private ImageView btnMessage;

    @FXML
    private ImageView btnGroup;

    @FXML
    private Pane groupChatPane;

    @FXML
    private Pane individualChatPane;

    private Socket socket;
    private static String userId;
    private ObjectOutputStream objectOutputStream;
    private boolean isDataBackedUp = false;
    private User currentUser;

    public void initializeCurrentUser(User currentUser){
        this.currentUser = currentUser;
        userId = currentUser.getUsername();
        labelClientName.setText(userId);
        sendMessageToServer(getInitialMessage());
        communicateWithServer(socket);
        activeClientListView.getSelectionModel().selectedItemProperty().addListener( (observable, oldVal, newVal) -> {
            if(activeClientListView.getSelectionModel().getSelectedIndex() >= 0){
                landingInfoVbox.setVisible(false);
                chatWindowVbox.setVisible(true);
            }
            if (newVal != null){
                labelInfo.setText("Connected to Server  -  Chat with: " + activeClientListView.getSelectionModel().getSelectedItem());
            }else {
                labelInfo.setText("Connected to Server.");
            }
            if (oldVal != null){
                Pair<String, String> pairOld = new Pair<>(userId, oldVal);
                VBox oldMessages = new VBox();
                if (isDataBackedUp){
                    isDataBackedUp = false;
                }else {
                    oldMessages.getChildren().addAll(vBoxMessages.getChildren());
                    if (!UserDB.getUserWiseConversationMap().containsKey(pairOld)){
                        UserDB.getUserWiseConversationMap().put(pairOld, oldMessages);
                    }else {
                        UserDB.getUserWiseConversationMap().replace(pairOld, oldMessages);
                    }
                    vBoxMessages.getChildren().clear();
                }
            }
            if (newVal != null) {
                labelSelectedUsername.setText(newVal);
                if (groupListView.getSelectionModel().getSelectedItem() != null){
                    Pair<String, String> selectedGroupPair = new Pair<>(userId, groupListView.getSelectionModel().getSelectedItem().getGroupHash());
                    VBox vBox = new VBox();
                    vBox.getChildren().addAll(vBoxMessages.getChildren());
                    if (!UserDB.getUserWiseConversationMap().containsKey(selectedGroupPair)){
                        UserDB.getUserWiseConversationMap().put(selectedGroupPair, vBox);
                    }else {
                        UserDB.getUserWiseConversationMap().replace(selectedGroupPair, vBox);
                    }
                    vBoxMessages.getChildren().clear();
                    isDataBackedUp = true;
                }
                Pair<String, String> pairNew = new Pair<>(userId, newVal);
                if (!UserDB.getUserWiseConversationMap().containsKey(pairNew)) {
                    UserDB.getUserWiseConversationMap().put(pairNew, new VBox());
                } else {
                    vBoxMessages.getChildren().addAll(UserDB.getUserWiseConversationMap().get(pairNew));
                }
            }
            Platform.runLater(() -> {
                tf_message.clear();
                tf_message.requestFocus();
            });
        });

        groupListView.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) -> {
            if(groupListView.getSelectionModel().getSelectedIndex() >= 0){
                landingInfoVbox.setVisible(false);
                chatWindowVbox.setVisible(true);
            }
            if (newVal != null){
                labelInfo.setText("Connected to Server  -  Chat with group: " + groupListView.getSelectionModel().getSelectedItem().getGroupName());
            }else {
                labelInfo.setText("Connected to Server.");
            }
            if (oldVal != null){
                Pair<String, String> pairOld = new Pair<>(userId, oldVal.getGroupHash());
                VBox oldMessages = new VBox();
                if (isDataBackedUp){
                    isDataBackedUp = false;
                }else {
                    oldMessages.getChildren().addAll(vBoxMessages.getChildren());
                    if (!UserDB.getUserWiseConversationMap().containsKey(pairOld)){
                        UserDB.getUserWiseConversationMap().put(pairOld, oldMessages);
                    }else {
                        UserDB.getUserWiseConversationMap().replace(pairOld, oldMessages);
                    }
                    vBoxMessages.getChildren().clear();
                }
            }
            if (newVal != null){
                labelSelectedUsername.setText(newVal.getGroupName());
                if (activeClientListView.getSelectionModel().getSelectedItem() != null){
                    Pair<String, String> selectedClientPair = new Pair<>(userId, activeClientListView.getSelectionModel().getSelectedItem());
                    VBox vBox = new VBox();
                    vBox.getChildren().addAll(vBoxMessages.getChildren());
                    if (!UserDB.getUserWiseConversationMap().containsKey(selectedClientPair)){
                        UserDB.getUserWiseConversationMap().put(selectedClientPair, vBox);
                    }else {
                        UserDB.getUserWiseConversationMap().replace(selectedClientPair, vBox);
                    }
                    vBoxMessages.getChildren().clear();
                    isDataBackedUp = true;
                }
                Pair<String, String> pairNew = new Pair<>(userId, newVal.getGroupHash());
                if (!UserDB.getUserWiseConversationMap().containsKey(pairNew)){
                    UserDB.getUserWiseConversationMap().put(pairNew, new VBox());
                }else {
                    vBoxMessages.getChildren().addAll(UserDB.getUserWiseConversationMap().get(pairNew));
                }
            }
            Platform.runLater(() -> {
                tf_message.clear();
                tf_message.requestFocus();
            });
        });
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            socket = new Socket("localhost", 1234);
            labelInfo.setText("Connected to Server");
        } catch (IOException e){
            e.printStackTrace();
            labelInfo.setText("Error creating Client ... ");
            closeSocket(socket);
        }
        vBoxMessages.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                sp_main.setVvalue((Double) newValue);
            }
        });
        groupListView.focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal){
                activeClientListView.getSelectionModel().clearSelection();
            }
        });
        activeClientListView.focusedProperty().addListener((observable, oldVal, newVal) -> {
            if (newVal){
                groupListView.getSelectionModel().clearSelection();
            }
        });
        groupListView.setCellFactory(messageGroupListView -> new ListCell<MessageGroup>(){
            private InputStream imageStream = this.getClass().getResourceAsStream("/com/omar/chatapp/Images/team-fill.png");
            private Image groupIconImage = new Image(imageStream);
            private ImageView imageView = new ImageView();
            private Label labelGroupName = new Label();
            @Override
            protected void updateItem(MessageGroup messageGroup, boolean empty) {
                super.updateItem(messageGroup, empty);
                HBox hBox = new HBox(5);
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setPrefHeight(40);
                labelGroupName.setStyle("-fx-text-fill: #F4F5F6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;");
                labelGroupName.setPrefSize(180, 40);
                if (empty || messageGroup == null){
                    setGraphic(null);
                } else {
                    labelGroupName.setText(messageGroup.getGroupName());
                    imageView.setFitHeight(30);
                    imageView.setFitWidth(30);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(groupIconImage);
                    hBox.getChildren().addAll(imageView, labelGroupName);
                    setGraphic(hBox);
                }
            }
        });
        activeClientListView.setCellFactory(stringListView -> new ListCell<String>(){
            private InputStream imageStream = this.getClass().getResourceAsStream("/com/omar/chatapp/Images/user-fill.png");
            private Image userIconImage = new Image(imageStream);
            private ImageView imageView = new ImageView();
            private Label labelUsername = new Label();
            @Override
            protected void updateItem(String str, boolean empty) {
                super.updateItem(str, empty);
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setPrefHeight(50);
                labelUsername.setStyle("-fx-text-fill: #F4F5F6;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;");
                labelUsername.setPrefSize(180, 40);
                if (empty || str == null){
                    setGraphic(null);
                } else {
                    labelUsername.setText(str);
                    imageView.setFitHeight(30);
                    imageView.setFitWidth(30);
                    imageView.setPreserveRatio(true);
                    imageView.setImage(userIconImage);
                    hBox.getChildren().addAll(imageView, labelUsername);
                    setGraphic(hBox);
                }
            }
        });
    }

    private void communicateWithServer(Socket socket) {
        receiverMessageTread(socket);
    }

    private void sendMessageToServer(Message message) {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            labelInfo.setText("Client: Message sent to server failed ... ");
        }
    }

    private Message getInitialMessage(){
        Message message = new Message();
        message.setSenderId(userId);
        message.setMessageType(MessageType.CONNECTION);
        return message;
    }

    private void receiverMessageTread(Socket skt){
        Thread t = new Thread(() -> {
            while(skt != null && skt.isConnected()) {
                try {
                    ObjectInputStream objectInputStream = new ObjectInputStream(skt.getInputStream());
                    Message message = (Message) objectInputStream.readObject();
                    if (message.getMessageType().equals(MessageType.ACTIVE_CLIENTS)){
                        List<String> activeClientIds= (List<String>) message.getDataObject();
                        Platform.runLater(() -> {
                            if(message.getReceiverId().equals(userId)){
                                activeClientListView.getItems().addAll(activeClientIds);
                                activeClientListView.getItems().remove(activeClientListView.getItems().size() - 1);
                            }else {
                                activeClientListView.getItems().add(activeClientIds.get(activeClientIds.size() - 1));
                            }
                        });
                    } else if (message.getMessageType().equals(MessageType.LOGOUT_CLIENT)) {
                        Platform.runLater(() -> {
                            bp_main.getScene().getWindow().hide();
                            openLoginUI();
                        });
                    } else if (message.getMessageType().equals(MessageType.REMOVE_CLIENT)) {
                        Platform.runLater(() -> {
                            activeClientListView.getItems().remove((String) message.getDataObject());
                            activeClientListView.refresh();
                        });
                    } else if (message.getMessageType().equals(MessageType.PLAIN)) {
                        Pair<String, String> msgReceivedConversationPair = new Pair<>(userId, message.getSenderId());
                        if (activeClientListView.getSelectionModel().getSelectedItem() != null &&
                                activeClientListView.getSelectionModel().getSelectedItem().equals(message.getSenderId())){
                            updateMessagesUI(message, Pos.CENTER_LEFT, true);
                        }else {
                            if (!UserDB.getUserWiseConversationMap().containsKey(msgReceivedConversationPair)){
                                UserDB.getUserWiseConversationMap().put(msgReceivedConversationPair, new VBox());
                            }
                            updateMessagesUI(message, Pos.CENTER_LEFT, false);
                        }

                    } else if (message.getMessageType().equals(MessageType.GROUP_CREATION)) {
                        MessageGroup messageGroupDetails = (MessageGroup) message.getDataObject();
                        UserDB.checkUserAndAddGroup(userId, messageGroupDetails);
                        UserDB.getUserWiseConversationMap().put(new Pair<>(userId, messageGroupDetails.getGroupHash()), new VBox());
                        Platform.runLater(() -> {
                            groupListView.getItems().add(messageGroupDetails);
                            labelInfo.setText("New message group is created: " + messageGroupDetails.getGroupName());
                        });
                    } else if (message.getMessageType().equals(MessageType.GROUP_MESSAGE)) {
                        Pair<String, String> groupMsgReceivedPair = new Pair<>(userId, message.getConversationHash());
                        MessageGroup msgGroup = groupListView.getSelectionModel().getSelectedItem();
                        if (msgGroup != null && message.getConversationHash().equals(msgGroup.getGroupHash())){
                            updateMessagesUI(message, Pos.CENTER_LEFT, true);
                        } else {
                            if (!UserDB.getUserWiseConversationMap().containsKey(groupMsgReceivedPair)){
                                UserDB.getUserWiseConversationMap().put(groupMsgReceivedPair, new VBox());
                            }
                            updateMessagesUI(message, Pos.CENTER_LEFT, false);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        labelInfo.setText("Client side: Message receiving failed from server.");
                    });
                    closeSocket(skt);
                    break;
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void closeSocket(Socket socket) {
        try{
            if (socket != null) {
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void updateMessagesUI(Message message, Pos alignmentPosition, boolean isMainMessageUI){
        HBox hBox = new HBox();
        hBox.setAlignment(alignmentPosition);
        Text text = new Text();
        text.setFont(Font.font(16));
        TextFlow textFlow = new TextFlow(text);
        if(alignmentPosition.equals(Pos.CENTER_RIGHT)){
            text.setFill(Color.WHITE);
            text.setText(message.getMsg());
            hBox.setPadding(new Insets(5, 5, 5, 35));
            textFlow.setPadding(new Insets(5, 5, 5, 5));
            textFlow.setStyle(
                    "-fx-color: rgb(239, 242, 255);" +
                            "-fx-background-color: rgb(15, 125, 242);" +
                            "-fx-background-radius: 10px;");
        } else if (alignmentPosition.equals(Pos.CENTER_LEFT)) {
            text.setFill(Color.BLACK);
            text.setText(message.getMsg());
            hBox.setPadding(new Insets(5, 35, 5, 5));
            textFlow.setPadding(new Insets(5, 5, 5, 5));
            textFlow.setStyle(
                    "-fx-background-color: rgb(233, 233, 235);" +
                            "-fx-background-radius: 10px;");
        }

        hBox.getChildren().add(textFlow);
        if (isMainMessageUI){
            Platform.runLater(() -> {
                vBoxMessages.getChildren().add(hBox);
            });
        }else {
            Pair<String, String> msgReceivedConversationPair;
            if (message.getMessageType().equals(MessageType.PLAIN)){
                msgReceivedConversationPair = new Pair<>(userId, message.getSenderId());
                UserDB.getUserWiseConversationMap().get(msgReceivedConversationPair).getChildren().add(hBox);
            } else if (message.getMessageType().equals(MessageType.GROUP_MESSAGE)) {
                msgReceivedConversationPair = new Pair<>(userId, message.getConversationHash());
                UserDB.getUserWiseConversationMap().get(msgReceivedConversationPair).getChildren().add(hBox);
            }
        }
    }

    private void openLoginUI() {
        try {
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("/com/omar/chatapp/Images/Chatting-icon.png")));
            primaryStage.show();
        }catch (IOException ex){
            System.out.println("Client: Failed to open login ui.");
            ex.printStackTrace();
        }
    }
    @FXML
    void sendMessage(ActionEvent event) {
        if (activeClientListView.getSelectionModel().getSelectedItem() != null) {
            String receiverId = activeClientListView.getSelectionModel().getSelectedItem();
            if(!receiverId.equals(userId) && !tf_message.getText().isEmpty()){
                String msg = tf_message.getText();
                Message message = new Message(userId, receiverId, msg, new Date(), MessageType.PLAIN);
                sendMessageToServer(message);
                updateMessagesUI(message, Pos.CENTER_RIGHT, true);
                tf_message.clear();
            }
        } else if (groupListView.getSelectionModel().getSelectedItem() != null) {
            MessageGroup messageGroup = groupListView.getSelectionModel().getSelectedItem();
            if (!tf_message.getText().isEmpty()) {
                Message message = new Message(userId, "Group-Member", userId+" : "+tf_message.getText(), new Date(), MessageType.GROUP_MESSAGE);
                message.setConversationHash(messageGroup.getGroupHash());
                message.setDataObject(messageGroup);
                sendMessageToServer(message);
                updateMessagesUI(message, Pos.CENTER_RIGHT, true);
                tf_message.clear();
            }
        }
    }

    @FXML
    void createGroup(ActionEvent event) {
        Node node = (Node) event.getSource();
        Stage groupCreationStage = new Stage();
        groupCreationStage.initModality(Modality.WINDOW_MODAL);
        groupCreationStage.initOwner(node.getScene().getWindow());
        groupCreationStage.setTitle("Create new message group");

        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        container.setPrefSize(350, 500);
        ListView<String> listView = new ListView<>(activeClientListView.getItems());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        TextField groupNameTf = new TextField();
        groupNameTf.setAlignment(Pos.CENTER);
        Button btnCreateGroup = new Button("Create Group");
        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(actionEvent -> {
            groupCreationStage.close();
        });
        btnCreateGroup.setOnAction(actionEvent -> {
            if (groupNameTf.getText() != null && listView.getSelectionModel().getSelectedItems().size() > 1){
                String gName = groupNameTf.getText();
                List<String> groupMemberList = new ArrayList<>(listView.getSelectionModel().getSelectedItems());
                groupMemberList.add(userId);
                List<String> groupAdminList = new ArrayList<>();
                groupAdminList.add(userId);
                groupCreationStage.close();
                MessageGroup messageGroupDetails = new MessageGroup("gId-"+gName, gName, "gHash-"+gName, new Date(),
                        groupMemberList, groupAdminList);
                UserDB.checkUserAndAddGroup(userId, messageGroupDetails);
                UserDB.getUserWiseConversationMap().put(new Pair<>(userId, messageGroupDetails.getGroupHash()), new VBox());
                groupListView.getItems().add(messageGroupDetails);
                labelInfo.setText("New message group is created: " + gName);
                Message groupCreationMessage = new Message(userId, "SERVER", "New group creation",
                        new Date(), MessageType.GROUP_CREATION);
                groupCreationMessage.setDataObject(messageGroupDetails);
                sendMessageToServer(groupCreationMessage);
            }
        });
        HBox hBox = new HBox(100, btnCreateGroup, btnCancel);
        hBox.setAlignment(Pos.CENTER);
        container.getChildren().addAll(listView, groupNameTf, hBox);
        groupCreationStage.setScene(new Scene(container));
        groupCreationStage.show();
    }

    @FXML
    void btnExitOnClicked(MouseEvent event) {
        bp_main.getScene().getWindow().hide();
        openLoginUI();
        closeSocket(socket);
    }

    @FXML
    void btnGroupOnClicked(MouseEvent event) {
        resetLeftPanelButtonColorToGray();
        btnGroup.setImage(new Image(getClass().getResourceAsStream("/com/omar/chatapp/Images/team-fill-white.png")));
        individualChatPane.setVisible(false);
        groupChatPane.setVisible(true);
        groupListView.getSelectionModel().clearSelection();
        landingInfoVbox.setVisible(true);
        chatWindowVbox.setVisible(false);
    }

    @FXML
    void btnMessageOnClicked(MouseEvent event) {
        resetLeftPanelButtonColorToGray();
        btnMessage.setImage(new Image(getClass().getResourceAsStream("/com/omar/chatapp/Images/message-white.png")));
        groupChatPane.setVisible(false);
        individualChatPane.setVisible(true);
        activeClientListView.getSelectionModel().clearSelection();
        landingInfoVbox.setVisible(true);
        chatWindowVbox.setVisible(false);
    }


    private void resetLeftPanelButtonColorToGray() {
        btnMessage.setImage(new Image(getClass().getResourceAsStream("/com/omar/chatapp/Images/message.png")));
        btnGroup.setImage(new Image(getClass().getResourceAsStream("/com/omar/chatapp/Images/team-fill-gray.png")));
    }
}
