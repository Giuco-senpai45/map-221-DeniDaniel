package main.service;

import main.domain.*;
import main.domain.validators.ValidationException;
import main.repository.Repository;
import main.repository.paging.Page;
import main.repository.paging.Pageable;
import main.repository.paging.PageableImplementation;
import main.repository.paging.PagingRepository;
import main.service.serviceExceptions.AddException;
import main.service.serviceExceptions.FindException;
import main.service.serviceExceptions.RemoveException;
import main.service.serviceExceptions.UpdateException;
import main.utils.AES256;
import main.utils.Observable;
import main.utils.Observer;
import main.utils.events.ChangeEventType;
import main.utils.events.MessageEvent;
import main.utils.events.UserEvent;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.chrono.ChronoLocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The service for the User entities
 */
public class UserService implements Observable<UserEvent> {
    /**
     * Repository for user entities
     */
    private PagingRepository<Long, User> repoUsers;
    /**
     * Repository for friendship entities
     */
    private PagingRepository<Tuple<Long,Long>, Friendship> repoFriends;

    private PagingRepository<String, Login> repoLogin;

    /**
     * Represents an automated id.
     * This id will be equal to the maximum id present in the repo
     */
    private Long currentUserID = 0L;

    /**
     * Overloaded constructor
     * @param repoUsers repository for user entities
     * @param repoFriends repository for friendship entities
     */
    public UserService(PagingRepository<Long, User> repoUsers, PagingRepository<Tuple<Long,Long>, Friendship> repoFriends, PagingRepository<String, Login> repoLogin) {
        this.repoUsers = repoUsers;
        this.repoFriends = repoFriends;
        this.repoLogin = repoLogin;

        for(User user : repoUsers.findAll())
        {
            if(currentUserID < user.getId())
                currentUserID = user.getId();
        }
        currentUserID ++;

    }

    private Long maximUserID() {
        Long maxID = 0L;
        for(User user: repoUsers.findAll()){
            if(user.getId() > maxID)
                maxID = user.getId();
        }
        currentUserID = maxID + 1;
        return maxID;
    }

    /**
     * This function creates a User entity and adds it to the repo
     * @param firstName String representing the first name of the user
     * @param lastName String representing the last name of the user
     * @throws AddException that user already exists
     */
    public void addUser(String firstName, String lastName, String address, LocalDate birthDate, String gender, String email, String school, String relationship, String funFact, String image, String notificationSubscription){
        User user = new User(firstName, lastName, birthDate, address, gender, email, school, relationship, funFact, image, notificationSubscription);
        Long nextID = maximUserID() + 1;
        user.setId(nextID);
        User addedUser = repoUsers.save(user);
        if(addedUser != null){
            throw new AddException("User already exists!");
        }
        else{
            System.out.println("User added cu with success");
        }
    }

    public void loginUser(String username, String password, Long userID){
        AES256 passwordEncrypter = new AES256();
        String encryptedPassword = passwordEncrypter.encrypt(password);
        Login loginData = new Login(encryptedPassword, userID);
        loginData.setId(username);
        repoLogin.save(loginData);
    }

    public Long getCurrentUserID() {
        return currentUserID;
    }

    public Login findRegisteredUser(String username){
        return  repoLogin.findOne(username);
    }

    public Iterable<Login> allRegisteredUsers(){
        return  repoLogin.findAll();
    }

    /**
     * This function removes the user with the given id
     * @param id representing the id of a user
     * @throws RemoveException if there doesn't exist an entity with the given id
     */
    public void removeUser(Long id){
        User user = repoUsers.delete(id);
        if(user == null){
            throw new RemoveException("User does not exist!");
        }
        System.out.println(user + " deleted");
    }

    /**
     * This function updates an existing user
     * @param firstName String representing the new firstName
     * @param lastName String representing the new lastName
     */
    public void updateUser(Long id,String firstName, String lastName, String address, LocalDate birthDate, String gender, String email, String school, String relationship, String funFact, String image, String notificationSubscription){
        User updatedUser = new User(firstName, lastName, birthDate, address, gender, email, school, relationship, funFact, image, notificationSubscription);
        updatedUser.setId(id);
        User user = repoUsers.update(updatedUser);
        if(user != null){
            throw new UpdateException("This user doesn't exist");
        }
        else{
            System.out.println("User updated with success");
            notifyObservers(new UserEvent(ChangeEventType.UPDATE, user));
        }
    }

    public Login findUserByUsername(String username){
        Login login = repoLogin.findOne(username);
        if(login == null){
            throw new FindException("Couldn't find specified user");
        }
        return login;
    }

    public User findUserById(Long id){
        User user = repoUsers.findOne(id);
        if(user == null){
            throw new FindException("No user with the specified id exists");
        }
        return user;
    }

    public List<UserFriendshipsDTO> getUserFriendList(Long id, int pageNumber, int pageSize){
        Iterable<Friendship> friendships = repoFriends.findAll();
        ArrayList<Friendship> listFriendships = new ArrayList<>();
        friendships.forEach(listFriendships::add);

        User userExists = repoUsers.findOne(id);
        if(userExists == null){
            throw new FindException("No user with the specified id exists");
        }

        Predicate<Friendship> testIsFriendshipForUser = f -> f.getBuddy1().equals(id) || f.getBuddy2().equals(id);

        if(pageNumber > -1 )
            return listFriendships.stream()
                    .filter(testIsFriendshipForUser)
                    .map((friendship) -> {
                        User friend;
                        if(friendship.getBuddy1().equals(id)){
                            friend = repoUsers.findOne(friendship.getBuddy2());
                            return new UserFriendshipsDTO(friend.getFirstName(),friend.getLastName(),friendship.getDate(), friend.getId(), friend.getImageURL());
                        }
                        friend = repoUsers.findOne(friendship.getBuddy1());
                        return new UserFriendshipsDTO(friend.getFirstName(),friend.getLastName(),friendship.getDate(), friend.getId(), friend.getImageURL());
                    })
                    .skip(pageNumber * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());
        else
            return listFriendships.stream()
                    .filter(testIsFriendshipForUser)
                    .map((friendship) -> {
                        User friend;
                        if(friendship.getBuddy1().equals(id)){
                            friend = repoUsers.findOne(friendship.getBuddy2());
                            return new UserFriendshipsDTO(friend.getFirstName(),friend.getLastName(),friendship.getDate(), friend.getId(), friend.getImageURL());
                        }
                        friend = repoUsers.findOne(friendship.getBuddy1());
                        return new UserFriendshipsDTO(friend.getFirstName(),friend.getLastName(),friendship.getDate(), friend.getId(), friend.getImageURL());
                    })
                    .collect(Collectors.toList());
    }

    /**
     * This function returns all friends of a users made in a specific month
     * @param userID representing the id of a user
     * @param month the month to filter friends list by
     * @return
     */
    public List<UserFriendshipsDTO> getUserFriendListByMonth(Long userID, Integer month) {
        String errors = "";
        User userExists = repoUsers.findOne(userID);
        if(userExists == null)
            errors = errors + "No user with the specified id exists.\n";
        if(month < 1 || month > 12)
            errors = errors + "There is no such month.";
        if(!errors.equals(""))
            throw new FindException(errors);

        List<Friendship> friendships = new ArrayList<>();
        repoFriends.findAll().forEach(friendships::add);

        Predicate<Friendship> testID = x -> (Objects.equals(x.getBuddy1(), userID) ||
                Objects.equals(x.getBuddy2(), userID));
        Predicate<Friendship> testMonth = x -> x.getDate().getMonthValue() == month;
        Predicate<Friendship> testBoth = testID.and(testMonth);

        Function<Long, String> getFriendFirstName = x -> repoUsers.findOne(x).getFirstName();
        Function<Long, String> getFriendLastName = x -> repoUsers.findOne(x).getLastName();
        Function<Long, String> getImage = x -> repoUsers.findOne(x).getImageURL();

        return friendships.stream()
                .filter(testBoth)
                .map(u -> {
                    Long friendID;
                    if(Objects.equals(u.getBuddy1(), userID))
                        friendID = u.getBuddy2();
                    else
                        friendID = u.getBuddy1();
                    return new UserFriendshipsDTO(getFriendFirstName.apply(friendID),
                            getFriendLastName.apply(friendID), u.getDate(), friendID, getImage.apply(friendID));
                })
                .collect(Collectors.toList());
    }

    public List<Tuple<String, Long>> allUsersByCharacters(String searchString, int pageIndex) {
        List<User> users = new ArrayList<>();
        repoUsers.findAll().forEach(users::add);

        Predicate<User> testFirstName = x -> (x.getFirstName().toLowerCase().startsWith(searchString.toLowerCase(), 0));
        Predicate<User> testLastName = x -> (x.getLastName().toLowerCase().startsWith(searchString.toLowerCase(), 0));

        Function<User, Tuple<String, Long>> getFriendName = x -> {
            if (testFirstName.test(x)){
                return new Tuple<>(repoUsers.findOne(x.getId()).getFirstName() + " " +
                        repoUsers.findOne(x.getId()).getLastName(), x.getId());
            }
            else if (testLastName.test(x)){
                return new Tuple<>(repoUsers.findOne(x.getId()).getLastName() + " " +
                        repoUsers.findOne(x.getId()).getFirstName(), x.getId());
            }
            return null;
        };

        if(pageIndex > -1)
            return users.stream()
                    .filter(testFirstName.or(testLastName))
                    .map(getFriendName)
                    .skip(pageIndex)
                    .limit(2)
                    .collect(Collectors.toList());
        else
            return users.stream()
                    .filter(testFirstName.or(testLastName))
                    .map(getFriendName)
                    .collect(Collectors.toList());
    }

    public void updateLoginInformation(Login newLogin){
        Login updatedLogin = repoLogin.update(newLogin);
        if(updatedLogin != null){
            throw new UpdateException("Could not update login");
        }
    }

    public List<UserFriendshipsDTO> report11(Long loggedUser, LocalDateTime beginningDate, LocalDateTime endDate){
        List<UserFriendshipsDTO> friends = getUserFriendList(loggedUser, -1, -1);
        List<UserFriendshipsDTO> report = new ArrayList<>();
        for(UserFriendshipsDTO friendshipsDTO: friends){
            if((friendshipsDTO.getDate().compareTo(beginningDate.toLocalDate()))>0 && (friendshipsDTO.getDate().compareTo(endDate.toLocalDate()))<0)
                report.add(friendshipsDTO);
        }
        return report;
    }

    /**
     * This function returns all the users present in the repo
     * @return iterable representing all users
     */
    public Iterable<User> getUsers(){
        return repoUsers.findAll();
    }




    private int page = 0;
    private int size = 1;

    private Pageable pageable;

    public void setPageSize(int size) {
        this.size = size;
    }

//    public void setPageable(Pageable pageable) {
//        this.pageable = pageable;
//    }

    public Set<User> getNextUsers() {
//        Pageable pageable = new PageableImplementation(this.page, this.size);
//        Page<MessageTask> studentPage = repo.findAll(pageable);
//        this.page++;
//        return studentPage.getContent().collect(Collectors.toSet());
        this.page++;
        return getUsersOnPage(this.page);
    }

    public Set<User> getUsersOnPage(int page) {
        this.page = page;
        Pageable pageable = new PageableImplementation(page, this.size);
        Page<User> userPage = repoUsers.findAll(pageable);
        return userPage.getContent().collect(Collectors.toSet());
    }

    private List<Observer<UserEvent>> observers=new ArrayList<>();

    public int numberOfPagesForFriends(Long userID){
        int friendsNumber = getUserFriendList(userID, -1, -1).size();
        int pagesNumber = friendsNumber % 4 != 0 ? (friendsNumber/4 + 1) : friendsNumber/4;
        return pagesNumber;
    }

    @Override
    public void addObserver(Observer<UserEvent> e) {
        observers.add(e);
    }

    @Override
    public void removeObserver(Observer<UserEvent> e) {
        observers.remove(e);
    }

    @Override
    public void notifyObservers(UserEvent t) {
        observers.stream().forEach(x -> x.update(t));
    }
}
