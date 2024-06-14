package com.reja.chatapp.Model;

import java.util.Map;

public class Room {
    private String roomId;
    private String adminId;
    private String videoUrl;
    private Map<String, User> userList;
    private RoomMessage message;
    private boolean isPlaying;
    private long currentPosition;

    public Room() {
    }

    public Room(String roomId, String adminId, String videoUrl) {
        this.roomId = roomId;
        this.adminId = adminId;
        this.videoUrl = videoUrl;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Map<String, User> getUserList() {
        return userList;
    }

    public void setUserList(Map<String, User> userList) {
        this.userList = userList;
    }

    public RoomMessage getMessage() {
        return message;
    }

    public void setMessage(RoomMessage message) {
        this.message = message;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }
}
