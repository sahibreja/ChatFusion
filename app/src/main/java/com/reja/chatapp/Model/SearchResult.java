package com.reja.chatapp.Model;

public class SearchResult {
    private String userId;
    private String userName;
    private String userBio;
    private String userProfileImage;
    private boolean isFriend;
    private boolean isIRequested;
    private boolean isIReceived;

    public SearchResult() {
    }

    public SearchResult(String userId, String userName, String userBio, String userProfileImage, boolean isFriend, boolean isIRequested, boolean isIReceived) {
        this.userId = userId;
        this.userName = userName;
        this.userBio = userBio;
        this.userProfileImage = userProfileImage;
        this.isFriend = isFriend;
        this.isIRequested = isIRequested;
        this.isIReceived = isIReceived;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
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

    public String getUserBio() {
        return userBio;
    }

    public void setUserBio(String userBio) {
        this.userBio = userBio;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setFriend(boolean friend) {
        isFriend = friend;
    }

    public boolean isIRequested() {
        return isIRequested;
    }

    public void setIRequested(boolean IRequested) {
        isIRequested = IRequested;
    }

    public boolean isIReceived() {
        return isIReceived;
    }

    public void setIReceived(boolean IReceived) {
        isIReceived = IReceived;
    }
}
