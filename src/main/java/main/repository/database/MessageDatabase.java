package main.repository.database;

import main.domain.Message;
import main.domain.validators.Validator;
import main.repository.paging.Page;
import main.repository.paging.Pageable;
import main.repository.paging.Paginator;
import main.repository.paging.PagingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDatabase implements PagingRepository<Long, Message> {

    private String url;
    private String username;
    private String password;
    private Validator validator;

    public MessageDatabase(String url, String username, String password, Validator<Message> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Message findOne(Long aLong) {
        Message entity = null;
        String sql = "SELECT * from messages where message_id = ?";
        try(Connection connection = DriverManager.getConnection(url,username,password);
            PreparedStatement ps = connection.prepareStatement(sql)){

            ps.setLong(1, aLong);

            ResultSet resultSet = ps.executeQuery();
            if(resultSet.next()){
                Long messageId = resultSet.getLong("message_id");
                Long fromId = resultSet.getLong("from_user_id");
                String message = resultSet.getString("message");
                Timestamp dateTime = resultSet.getTimestamp("date_time");
                Long replyId = resultSet.getLong("reply_id");
                Long chatId = resultSet.getLong("chat_id");

                entity = new Message(fromId, message, dateTime, replyId,chatId);
                entity.setId(messageId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return entity;
    }

    @Override
    public Iterable<Message> findAll() {
        List<Message> messages = new ArrayList<>();

        try(Connection connection = DriverManager.getConnection(url,username,password);
            PreparedStatement statement = connection.prepareStatement("SELECT * from messages");
            ResultSet resultSet = statement.executeQuery()){

            while(resultSet.next()){
                Long messageId = resultSet.getLong("message_id");
                Long fromId = resultSet.getLong("from_user_id");
                String message = resultSet.getString("message");
                Timestamp dateTime = resultSet.getTimestamp("date_time");
                Long replyId = resultSet.getLong("reply_id");
                Long chatId = resultSet.getLong("chat_id");

                Message entity = new Message(fromId, message, dateTime, replyId,chatId);
                entity.setId(messageId);
                messages.add(entity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public Message save(Message entity) {
        String sql = "insert into messages (from_user_id, message, date_time, chat_id, reply_id) values (?, ?, ?, ?, ?)";
        validator.validate(entity);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, entity.getUser());
            ps.setString(2, entity.getMessage());
            ps.setTimestamp(3, entity.getTimeOfMessage());
            ps.setLong(4,entity.getChatID());
            ps.setLong(5, entity.getReplyId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return entity;
        }
        return null;
    }

    @Override
    public Message delete(Long aLong) {
        String sql = "delete from messages where message_id = ?";

        Message removedMessage = findOne(aLong);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, aLong);

            ps.executeUpdate();
        } catch (SQLException e) {
            return null;
        }
        return removedMessage;
    }

    @Override
    public Message update(Message entity) {
        return null;
    }

    @Override
    public Page<Message> findAll(Pageable pageable) {
        Paginator<Message> paginator = new Paginator<Message>(pageable, this.findAll());
        return paginator.paginate();
    }
}
