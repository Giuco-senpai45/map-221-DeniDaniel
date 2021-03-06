package main.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * This class represents a User.
 * It extends the Entity class giving it an Id of the Long type
 */
public class User extends Entity<Long>{
    /**
     * String representing the users first name
     */
    private String firstName;
    /**
     *  String representingthe users second name
     */
    private String lastName;

    /**
     * List of Users representing the friends list of the current user
     */
    private List<User> friends;

    private String address;
    private String email;
    private String gender;
    private LocalDate birthDate;
    private String lastGraduatedSchool;
    private String relationshipStatus;
    private String funFact;
    private String imageURL;
    private String notificationSubscription;

    /**
     * @param firstName String representing the firstname of the user
     * @param lastName String representing the lastname of the user
     */
    public User(String firstName, String lastName, LocalDate birthDate, String address, String gender, String email, String lastGraduatedSchool, String relationshipStatus, String funFact, String imageURL, String notificationSubscription) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.email = email;
        this.gender = gender;
        this.birthDate = birthDate;
        this.relationshipStatus = relationshipStatus;
        this.imageURL = imageURL;
        this.funFact = funFact;
        this.lastGraduatedSchool = lastGraduatedSchool;
        this.notificationSubscription = notificationSubscription;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getLastGraduatedSchool() {
        return lastGraduatedSchool;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public String getFunFact() {
        return funFact;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getNotificationSubscription() {
        return notificationSubscription;
    }

    public void setNotificationSubscription(String notificationSubscription) {
        this.notificationSubscription = notificationSubscription;
    }

    /**
     * @return String representing the users firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName representing the new firstname the user will get
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return String representing the users last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName representing the new lastname the user will get
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return List of users that are friends with the curent user
     */
    public List<User> getFriends() {
        return friends;
    }

    /**
     * @param friends list representing a list of friends
     *  This function sets the userFriendList as the one given as a parameter
     */
    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    /**
     * @return integer representing the size number of friends this user has in his friends List
     */
    public int getFriendListSize() {
        if(friends != null)
            return this.friends.size();
        else
            return 0;
    }

    /**
     * @return String representing how we are going to print the user
     */
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    /**
     * 2 users are equal if they have the same id
     * @param o representing the other object
     * @return boolean
     *  -true  if the 2 users are equal
     *  -false if the 2 users are not equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User that = (User) o;
        return getId().equals(that.getId());
    }

    /**
     * @return integer representing the hashcode of the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getFriends());
    }
}