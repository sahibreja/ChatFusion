package com.reja.chatapp.Model;

public class Message {
    private String messageId;
    private String senderId;
    private String recipientId;
    private String conversationId;
    private String content;
    private String contentCaptions;
    private long sendTimeStamp;
    private boolean isSeen;
    private long seenTimeStamp;
    private MessageType messageType;

    public Message() {
    }

    public Message(String messageId, String senderId, String recipientId, String conversationId, String content, long timestamp, MessageType messageType) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.conversationId = conversationId;
        this.content = content;
        this.sendTimeStamp = timestamp;
        this.messageType = messageType;
    }

    public Message(String senderId, String recipientId, String conversationId, String content, long sendTimeStamp, boolean isSeen, long seenTimeStamp, MessageType messageType) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.conversationId = conversationId;
        this.content = content;
        this.sendTimeStamp = sendTimeStamp;
        this.isSeen = isSeen;
        this.seenTimeStamp = seenTimeStamp;
        this.messageType = messageType;
    }

    public Message(String senderId, String recipientId, String conversationId, String content,String contentCaptions, long sendTimeStamp, boolean isSeen, long seenTimeStamp, MessageType messageType) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.conversationId = conversationId;
        this.content = content;
        this.contentCaptions = contentCaptions;
        this.sendTimeStamp = sendTimeStamp;
        this.isSeen = isSeen;
        this.seenTimeStamp = seenTimeStamp;
        this.messageType = messageType;
    }

    public long getSendTimeStamp() {
        return sendTimeStamp;
    }

    public void setSendTimeStamp(long sendTimeStamp) {
        this.sendTimeStamp = sendTimeStamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public long getSeenTimeStamp() {
        return seenTimeStamp;
    }

    public void setSeenTimeStamp(long seenTimeStamp) {
        this.seenTimeStamp = seenTimeStamp;
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

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentCaptions() {
        return contentCaptions;
    }

    public void setContentCaptions(String contentCaptions) {
        this.contentCaptions = contentCaptions;
    }

    public long getTimestamp() {
        return sendTimeStamp;
    }

    public void setTimestamp(long timestamp) {
        this.sendTimeStamp = timestamp;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
