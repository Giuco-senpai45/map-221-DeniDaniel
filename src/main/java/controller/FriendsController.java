package controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import main.domain.*;
import main.service.FriendRequestService;
import main.service.FriendshipService;
import main.service.UserService;
import main.service.serviceExceptions.RemoveException;
import main.utils.Observer;
import main.utils.events.FriendDeletionEvent;


public class FriendsController implements Observer<FriendDeletionEvent> {

    @FXML
    private TableView friendsList;

    @FXML
    private TableColumn<UserFriendshipsDTO, String> avatar;
    @FXML
    private TableColumn<UserFriendshipsDTO, String> fullName;

    @FXML
    private TableColumn<UserFriendshipsDTO, String> friendshipDate;

    @FXML
    private TableColumn<UserFriendshipsDTO, Void> action;

    private UserService userService;
    private FriendshipService friendshipService;
    private FriendRequestService friendRequestService;
    private User loggedUser;
    private ObservableList<UserFriendshipsDTO> model = FXCollections.observableArrayList();

    public void setController(UserService userService, FriendshipService friendshipService, FriendRequestService friendRequestService, User loggedUser){
        this.userService = userService;
        this.friendshipService = friendshipService;
        friendshipService.addObserver(this);
        this.friendRequestService = friendRequestService;
        this.loggedUser = loggedUser;
    }

    public void start(){
        //friendsList.getColumns().clear();
        model.setAll(userService.getUserFriendList(loggedUser.getId()));
//        friendsList.setItems(FXCollections.observableArrayList(userService.getUserFriendList(loggedUser.getId())));
        addImageToTable();
        fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        friendshipDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        //friendsList.getColumns().addAll(fullName, friendshipDate);
        addButtonToTable();
        friendsList.setItems(model);
    }

    private void addButtonToTable() {
        Callback<TableColumn<UserFriendshipsDTO, Void>, TableCell<UserFriendshipsDTO, Void>> cellFactory = new Callback<TableColumn<UserFriendshipsDTO, Void>, TableCell<UserFriendshipsDTO, Void>>() {
            @Override
            public TableCell<UserFriendshipsDTO, Void> call(final TableColumn<UserFriendshipsDTO, Void> param) {
                final TableCell<UserFriendshipsDTO, Void> cell = new TableCell<UserFriendshipsDTO, Void>() {

                    private final Button btn = new Button("Remove friend");

                    {
                        btn.getStyleClass().add("remove-friend-btn");
                        btn.setOnAction((ActionEvent event) -> {
                            UserFriendshipsDTO data = getTableView().getItems().get(getIndex());
                            try {
                                friendshipService.removeFriendship(new Tuple<>(loggedUser.getId(), data.getFriendID()));
                                FriendRequest friendRequest = friendRequestService.findFriendRequest(loggedUser.getId(), data.getFriendID());
                                friendRequestService.processRequest(friendRequest.getId(), "deleted");
                                friendsList.getItems().remove(data);
                            }
                            catch(RemoveException e){
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            btn.setAlignment(Pos.CENTER);
                            setGraphic(btn);
                        }
                    }
                };
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        };

        action.setCellFactory(cellFactory);
       // friendsList.getColumns().add(action);
    }

    private void addImageToTable(){
        avatar.setCellFactory(new Callback<TableColumn<UserFriendshipsDTO, String>, TableCell<UserFriendshipsDTO, String>>() {
            @Override
            public TableCell<UserFriendshipsDTO, String> call(TableColumn<UserFriendshipsDTO, String> param) {
                ImageView imageView = new ImageView();
                imageView.setFitWidth(30);
                imageView.setFitHeight(30);
                TableCell<UserFriendshipsDTO, String> cell = new TableCell<UserFriendshipsDTO, String>() {
                    public void updateItem(String item, boolean empty) {
                        if (item != null) {
                            Image image = new Image(item, false);
                            imageView.setImage(image);
                        }
                    }
                };
                cell.setGraphic(imageView);
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        });
        avatar.setCellValueFactory(new PropertyValueFactory<UserFriendshipsDTO, String>("imageURL"));
        //friendsList.getColumns().add(avatar);
    }

    @Override
    public void update(FriendDeletionEvent friendDeletionEvent) {
        start();
    }
}