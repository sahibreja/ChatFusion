package com.reja.chatapp.Model;

import java.util.List;

public class ProfileDetails {
    private String userId;
    private String userName;
    private String userBio;
    private String userProfilePicture;
    private List<User> listOfMutualFriend;
    private boolean isFriend;
    private boolean isIsent;
    private boolean isIReceived;

    public ProfileDetails() {
    }

    public ProfileDetails(String userId, String userName, String userProfilePicture, List<User> listOfMutualFriend, boolean isFriend, boolean isIsent, boolean isIReceived) {
        this.userId = userId;
        this.userName = userName;
        this.userProfilePicture = userProfilePicture;
        this.listOfMutualFriend = listOfMutualFriend;
        this.isFriend = isFriend;
        this.isIsent = isIsent;
        this.isIReceived = isIReceived;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserProfilePicture() {
        return userProfilePicture;
    }

    public void setUserProfilePicture(String userProfilePicture) {
        this.userProfilePicture = userProfilePicture;
    }

    public List<User> getListOfMutualFriend() {
        return listOfMutualFriend;
    }

    public void setListOfMutualFriend(List<User> listOfMutualFriend) {
        this.listOfMutualFriend = listOfMutualFriend;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public boolean isIsent() {
        return isIsent;
    }

    public void setIsent(boolean isent) {
        isIsent = isent;
    }

    public boolean isIReceived() {
        return isIReceived;
    }

    public void setIReceived(boolean IReceived) {
        isIReceived = IReceived;
    }

    public String getUserBio() {
        return userBio;
    }

    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }
}
