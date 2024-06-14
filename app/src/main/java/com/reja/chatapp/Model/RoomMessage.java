package com.reja.chatapp.Model;

public class RoomMessage {
    private String messageId;
    private String roomId;
    private String senderId;
    private String senderName;
    private String senderProfilePicture;
    private String message;
    private long timeStamp;

    public RoomMessage() {
    }

    public RoomMessage(String roomId, String senderId, String senderName, String senderProfilePicture, String message, long timeStamp) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfilePicture = senderProfilePicture;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderProfilePicture() {
        return senderProfilePicture;
    }

    public void setSenderProfilePicture(String senderProfilePicture) {
        this.senderProfilePicture = senderProfilePicture;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
