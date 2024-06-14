package com.reja.chatapp.Model;

public class Friend {
    private String friendId;
    private String timeStamp;
    private String friendName;
    private String friendProfilePicture;
    private boolean isSent;


    public Friend() {
    }

    public Friend(String friendId, String timeStamp) {
        this.friendId = friendId;
        this.timeStamp = timeStamp;
    }

    public Friend(String friendId, String friendName, String friendProfilePicture, boolean isSent) {
        this.friendId = friendId;
        this.friendName = friendName;
        this.friendProfilePicture = friendProfilePicture;
        this.isSent = isSent;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getFriendProfilePicture() {
        return friendProfilePicture;
    }

    public void setFriendProfilePicture(String friendProfilePicture) {
        this.friendProfilePicture = friendProfilePicture;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
