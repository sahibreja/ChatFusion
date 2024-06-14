package com.reja.chatapp.Model;

public class MutualFriend {

    private String friendId;
    private String name;
    private String profilePicture;

    public MutualFriend() {
    }

    public MutualFriend(String friendId, String name, String profilePicture) {
        this.friendId = friendId;
        this.name = name;
        this.profilePicture = profilePicture;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
