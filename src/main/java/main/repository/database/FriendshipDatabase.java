package main.repository.database;

import main.domain.Friendship;
import main.domain.Tuple;
import main.domain.validators.Validator;
import main.repository.Repository;
import main.repository.paging.Page;
import main.repository.paging.Pageable;
import main.repository.paging.Paginator;
import main.repository.paging.PagingRepository;
import main.service.serviceExceptions.AddException;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.util.Set;

public class FriendshipDatabase implements PagingRepository<Tuple<Long,Long>, Friendship> {
    private String url;
    private String username;
    private String password;
    private Validator<Friendship> validator;
    /**
     * Constructor for the Repo
     *
     * @param validator Validator that represents the validator for the entities in the repo
     */
    public FriendshipDatabase(String url, String username, String password,Validator<Friendship> validator) {
        this.validator = validator;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Friendship save(Friendship entity) {
        String sql = "insert into friendships (buddy1, buddy2 ,date) values (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, entity.getBuddy1());
            ps.setLong(2, entity.getBuddy2());
            ps.setDate(3,Date.valueOf(entity.getDate()));           //verify if friendship buddy1->buddy2 exists

            ps.executeUpdate();

        } catch (SQLException e) {
            return entity;
        }
        return null;
    }

    @Override
    public Friendship delete(Tuple<Long,Long> id) {
        String sql = "delete from friendships where buddy1= ? and buddy2=?";

        Friendship removedFriendship = findOne(id);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1,id.getE1());
            ps.setLong(2,id.getE2());

            ps.executeUpdate();
        } catch (SQLException e) {
            return null;
        }
        return removedFriendship;
    }

    @Override
    public Friendship update(Friendship entity) {
        return null;
    }

    @Override
    public Iterable<Friendship> findAll() {
        List<Friendship> friends = new ArrayList<>();
        try(Connection connection = DriverManager.getConnection(url,username,password);
            PreparedStatement statement = connection.prepareStatement("SELECT * from friendships");
            ResultSet resultSet = statement.executeQuery()){

            while(resultSet.next()){
                Long buddy1 = resultSet.getLong("buddy1");
                Long buddy2 = resultSet.getLong("buddy2");
                LocalDate date = resultSet.getDate("date").toLocalDate();

                Friendship friendship = new Friendship(buddy1,buddy2);
                friendship.setDate(date);
                friends.add(friendship);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    @Override
    public Friendship findOne(Tuple<Long, Long> id) {

        Friendship friendship = null;

        String sql = "SELECT * from friendships where buddy1 = ? and buddy2 = ?";

        try(Connection connection = DriverManager.getConnection(url,username,password);
            PreparedStatement ps = connection.prepareStatement(sql)){

            ps.setLong(1,id.getE1());
            ps.setLong(2,id.getE2());

            ResultSet resultSet = ps.executeQuery();
            if(resultSet.next()){
                Long buddy1 = resultSet.getLong("buddy1");
                Long buddy2 = resultSet.getLong("buddy2");
                LocalDate date = resultSet.getDate("date").toLocalDate();

                friendship = new Friendship(buddy1,buddy2);
                friendship.setDate(date);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendship;
    }

    @Override
    public Page<Friendship> findAll(Pageable pageable) {
        Paginator<Friendship> paginator = new Paginator<Friendship>(pageable, this.findAll());
        return paginator.paginate();
    }
}
