package controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import main.domain.*;
import main.service.FriendRequestService;
import main.service.FriendshipService;
import main.service.MessageService;
import main.service.UserService;
import main.service.serviceExceptions.RemoveException;
import main.utils.Observer;
import main.utils.events.MessageEvent;
import sn.socialnetwork.MainApp;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.security.Key;
import java.util.List;
import java.util.Objects;

public class ChatController implements Observer<MessageEvent> {

    @FXML
    private BorderPane chatPane;

    @FXML
    private Button sendMessageButton;

    @FXML
    private TextArea textMessage;

    @FXML
    private TableView<Chat> chatsList;

    @FXML
    private Label chatNameLabel;

    @FXML
    private TableColumn<Chat, String> userChats;

    @FXML
    private ComboBox chatMenu;

    @FXML
    private VBox conversationPane;

    @FXML
    private ScrollPane scroller;

    @FXML
    private TextField textFieldGroupName;

    private Chat currentSelectedChat;
    private MessageService messageService;
    private UserService userService;
    private FriendshipService friendshipService;
    private User loggedUser;
    private LocalDateTime currentMessageDate;

    public void setServicesChat(MessageService messageService, UserService userService,User loggedUser,FriendshipService friendshipService){
        this.messageService = messageService;
        this.messageService.addObserver(this);
        this.friendshipService = friendshipService;
        this.userService = userService;
        this.loggedUser = loggedUser;
        currentMessageDate = null;
        scroller.setContent(conversationPane);
    }

    public void initChatView(){
        Image img = new Image("imgs/rosar.png");
        ImageView view = new ImageView(img);
        view.setFitHeight(50);
        view.setPreserveRatio(true);
        sendMessageButton.setGraphic(view);
        ObservableList<String> choicesList = FXCollections.observableArrayList("Change group picture","Change group name","Create a new group chat");
        chatMenu.setItems(choicesList);
        chatMenu.getSelectionModel().selectedItemProperty().addListener((x,y,z)-> handleClickChatMenu());
        textMessage.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ENTER){
                    sendMessageAction(new ActionEvent());
                }
            }
        });
        start();
    }

    public void start(){
        displayChatsForUser();
        currentSelectedChat = messageService.getChatsForUser(loggedUser.getId()).get(0);
        chatsList.getSelectionModel().selectFirst();            //in the beginning we select the first chat
        displayCurrentChat(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
                0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true,
                true, true, true, true, true, true, null));
    }

    private void displayChatsForUser(){
        chatsList.getColumns().clear();
        chatsList.setItems(FXCollections.observableArrayList(messageService.getChatsForUser(loggedUser.getId())));
        addChatImages();
    }

    private void addChatImages(){
        userChats.setCellFactory(new Callback<TableColumn<Chat, String>, TableCell<Chat, String>>() {
            @Override
            public TableCell<Chat, String> call(TableColumn<Chat, String> param) {
                ImageView imageView = new ImageView();
                Circle circle = new Circle(40);
                imageView.setFitWidth(80);
                imageView.setFitHeight(80);
                imageView.setPreserveRatio(true);
                TableCell<Chat, String> cell =  new TableCell<Chat, String>() {

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            Image image = new Image(item, false);
                            imageView.setImage(image);
                            circle.setFill(new ImagePattern(image));
                            setCursor(Cursor.HAND);
                            setGraphic(circle);
                            setOnMouseClicked((MouseEvent event) -> {
                                displayCurrentChat(event);
                            });
                            setAlignment(Pos.CENTER);
                        }
                        else{
                            setGraphic(null);
                        }
                    }
                };
                return cell;
            }
        });
        userChats.setCellValueFactory(new PropertyValueFactory<Chat, String>("url"));
        chatsList.getColumns().add(userChats);
    }

    private void handleClickChatMenu(){
        String option = chatMenu.getSelectionModel().getSelectedItem().toString();
        if(option.equals("Change group picture")){
            FileChooser fileChooser = new FileChooser();
            Stage stage =(Stage) chatPane.getScene().getWindow();
            File file = fileChooser.showOpenDialog(stage);
            if(file != null){
                Chat updatedChat = new Chat();
                updatedChat.setId(currentSelectedChat.getId());
                updatedChat.setUrl(file.getAbsolutePath());
                updatedChat.setName(currentSelectedChat.getName());
                messageService.updateChat(updatedChat);
                displayChatsForUser();
            }
        }
        else if(option.equals("Create a new group chat")) {
            FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/views/friends-groupchat.fxml"));
            Stage stage = new Stage();
            Scene scene = null;
            final Chat[] createdChat = {null};
            try {
                scene = new Scene(fxmlLoader.load());
                FriendsGroupChatController friendsGroupChatController = fxmlLoader.getController();
                friendsGroupChatController.setServices(userService,friendshipService,messageService,loggedUser);
                friendsGroupChatController.start();
                stage.setOnHidden(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent e) {
                        System.out.println("intru aiceeee");
                        createdChat[0] = friendsGroupChatController.getCreatedChat();
                        displayChat(createdChat[0]);
                    }
                });
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            stage.setTitle("Creating group chat");
            stage.setScene(scene);
            stage.getIcons().add(new Image("imgs/app_icon.png"));
            stage.show();

        }
        else{
            System.out.println("Change group name");
            textFieldGroupName.setVisible(true);
            textFieldGroupName.getStyleClass().add("change-group-name");
            textFieldGroupName.setOnKeyPressed((KeyEvent event) -> {
                if(event.getCode() == KeyCode.ENTER){
                    if(!textFieldGroupName.getText().equals("")){
                        Chat updatedChat = new Chat();
                        updatedChat.setId(currentSelectedChat.getId());
                        updatedChat.setUrl(currentSelectedChat.getUrl());
                        updatedChat.setName(textFieldGroupName.getText());
                        messageService.updateChat(updatedChat);
                        textFieldGroupName.setText("");
                        textFieldGroupName.setVisible(false);
                        displayChatsForUser();
                        displayChat(updatedChat);
                    }
                    else {
                        textFieldGroupName.setVisible(false);
                    }
                }
            });
        }
    }

    private void updatePicsForPrivateChats(){
        for (Chat c : chatsList.getItems()) {
            Tuple<String,String> privateChat = messageService.getPrivateChatData(loggedUser.getId(),c);
            if(privateChat!= null){
                c.setName(privateChat.getE1());
                c.setUrl(privateChat.getE2());
                messageService.updateChatForUser(c, loggedUser.getId());
            }
        }
    }

    private void displayCurrentChat(MouseEvent event){
        conversationPane.getChildren().clear();
        currentSelectedChat = chatsList.getSelectionModel().getSelectedItem();
        displayChat(chatsList.getSelectionModel().getSelectedItem());
    }

    private void displayChat(Chat chat){
        currentSelectedChat = chat;
        updatePicsForPrivateChats();
        chatNameLabel.setText(currentSelectedChat.getName());
        List<ChatDTO> chatMessages = messageService.getConversation(currentSelectedChat.getId());
        for(ChatDTO chatDTO: chatMessages){
            showMessages(chatDTO);
        }
    }

    private void showMessages(ChatDTO chatDTO){
        LocalDateTime localDate = chatDTO.getTimestamp().toLocalDateTime();
        Integer day = localDate.getDayOfMonth();
        String month = localDate.getMonth().toString();
        if(currentMessageDate == null || (day != currentMessageDate.getDayOfMonth())){
            currentMessageDate = chatDTO.getTimestamp().toLocalDateTime();
            Label date = new Label(currentMessageDate.getDayOfMonth() + " " + currentMessageDate.getMonth() + " " + currentMessageDate.getYear());
            VBox showDate = new VBox();
            showDate.getChildren().add(date);
            showDate.setAlignment(Pos.CENTER);
            conversationPane.getChildren().add(showDate);
        }

        User sender = userService.findUserById(chatDTO.getUserID());
        VBox photoAndHour = new VBox();
        Circle circle = new Circle();
        circle.setRadius(20);
        Image image = new Image(sender.getImageURL(), false);
        circle.setFill(new ImagePattern(image));
        LocalDateTime localDateTime = chatDTO.getTimestamp().toLocalDateTime();
        Label hour = new Label(localDateTime.getHour()+":"+ localDateTime.getMinute());
        photoAndHour.getChildren().addAll(circle, hour);

        HBox icons = new HBox();
        ImageView imageView1 = new ImageView();
        imageView1.setFitWidth(20);
        imageView1.setFitHeight(20);
        imageView1.setImage(new Image("/imgs/bin.png"));

        ImageView imageView2 = new ImageView();
        imageView2.setFitWidth(20);
        imageView2.setFitHeight(20);
        imageView2.setImage(new Image("/imgs/reply.png"));
        icons.getChildren().addAll(imageView1, imageView2);
        icons.setVisible(false);

        HBox convo = new HBox();
        Label label = new Label(chatDTO.getMessage());
        {
            label.setPrefWidth(250);
            label.setWrapText(true);
            label.setStyle("-fx-background-color: ddbea9; -fx-cursor: hand;");
            label.setOnMouseClicked((MouseEvent event) -> {
                icons.setVisible(true);
            });
        }
        Text text = new Text();
        text.setText(chatDTO.getMessage());
        if(text.getLayoutBounds().getWidth() < 250)
            label.setPrefWidth(text.getLayoutBounds().getWidth());
        if(Objects.equals(chatDTO.getUserID(), loggedUser.getId())) {
            label.setAlignment(Pos.CENTER_RIGHT);
            convo.setMargin(label, new Insets(0, 0, 0, 0));
            convo.setMargin(icons, new Insets(18, 0, 0, 0));
            convo.getChildren().addAll(icons, label, photoAndHour);
            convo.setAlignment(Pos.CENTER_RIGHT);
        }
        else{
            convo.setMargin(icons, new Insets(10, 0, 0, 0));
            convo.setMargin(label, new Insets(15, 0, 0, 0));
            convo.getChildren().addAll(photoAndHour, label, icons);
        }
        conversationPane.getChildren().add(convo);
    }

    public void sendMessageAction(ActionEvent actionEvent) {
        if(!textMessage.getText().isBlank()){
            messageService.sendMessage(loggedUser.getId(), textMessage.getText(),currentSelectedChat.getChatUsers());
            textMessage.setText(null);
        }
    }

    public void selectChatById(Long chatId) {
        for(Chat c : chatsList.getItems()){
            if(c.getId().equals(chatId)){
                displayChat(c);
            }
        }
    }

    @Override
    public void update(MessageEvent messageEvent) {
        displayChat(currentSelectedChat);
//        displayCurrentChat(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0,
//                0, 0, 0, MouseButton.PRIMARY, 1, true, true, true, true,
//                true, true, true, true, true, true, null));
    }
}