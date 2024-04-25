package com.reja.chatapp.Model;

public class Friend {
    private String friendId;
    private String timeStamp;

    public Friend() {
    }

    public Friend(String friendId, String timeStamp) {
        this.friendId = friendId;
        this.timeStamp = timeStamp;
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
