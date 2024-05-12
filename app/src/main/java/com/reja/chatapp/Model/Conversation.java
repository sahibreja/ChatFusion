package com.reja.chatapp.Model;

public class Conversation {
    private String conversationId;
    private String senderId;
    private String recipientId;
    private String recipientProfilePicture;
    private String recipientName;
    private Message lastMessage;
    private int unreadMessageCount;

    public Conversation() {
    }

    public Conversation(String conversationId, String senderId, String recipientId, String recipientProfilePicture, String recipientName, Message lastMessage, int unreadMessageCount) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.recipientProfilePicture = recipientProfilePicture;
        this.recipientName = recipientName;
        this.lastMessage = lastMessage;
        this.unreadMessageCount = unreadMessageCount;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientProfilePicture() {
        return recipientProfilePicture;
    }

    public void setRecipientProfilePicture(String recipientProfilePicture) {
        this.recipientProfilePicture = recipientProfilePicture;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }


}
