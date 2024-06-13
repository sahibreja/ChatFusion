package com.reja.chatapp.Model;


import java.util.List;

public class User {
    private String userId;
    private String userName;
    private String lowerCaseUserName;
    private String userEmail;
    private String userPassword;
    private String userProfilePicture;
    private String userBio;
    private String userDeviceToken;
    private List<Friend> friends;

    private List<FriendRequest> friendRequest;

    public User() {
    }

    public User(String userName, String lowerCaseUserName, String userEmail, String userPassword, String userProfilePicture) {
        this.userName = userName;
        this.lowerCaseUserName = lowerCaseUserName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.userProfilePicture = userProfilePicture;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserProfilePicture() {
        return userProfilePicture;
    }

    public void setUserProfilePicture(String userProfilePicture) {
        this.userProfilePicture = userProfilePicture;
    }

    public List<Friend> getFriends() {
        return friends;
    }

    public void setFriends(List<Friend> friends) {
        this.friends = friends;
    }

    public List<FriendRequest> getFriendRequest() {
        return friendRequest;
    }

    public void setFriendRequest(List<FriendRequest> friendRequest) {
        this.friendRequest = friendRequest;
    }

    public String getLowerCaseUserName() {
        return lowerCaseUserName;
    }

    public void setLowerCaseUserName(String lowerCaseUserName) {
        this.lowerCaseUserName = lowerCaseUserName;
    }

    public String getUserDeviceToken() {
        return userDeviceToken;
    }

    public void setUserDeviceToken(String userDeviceToken) {
        this.userDeviceToken = userDeviceToken;
    }

    public String getUserBio() {
        return userBio;
    }

    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }
}
