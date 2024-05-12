package com.reja.chatapp.Session;

import java.util.HashSet;
import java.util.Set;

public class ChatSessionManager {
    private static ChatSessionManager instance;
    private Set<String> activeChatSessions;

    private ChatSessionManager() {
        activeChatSessions = new HashSet<>();
    }

    public static synchronized ChatSessionManager getInstance() {
        if (instance == null) {
            instance = new ChatSessionManager();
        }
        return instance;
    }

    public void addActiveChatSession(String senderId, String receiverId) {
        String chatSessionId = getChatSessionId(senderId, receiverId);
        activeChatSessions.add(chatSessionId);
    }

    public void removeActiveChatSession(String senderId, String receiverId) {
        String chatSessionId = getChatSessionId(senderId, receiverId);
        activeChatSessions.remove(chatSessionId);
    }

    public boolean isChatSessionActive(String senderId, String receiverId) {
        String chatSessionId = getChatSessionId(senderId, receiverId);
        return activeChatSessions.contains(chatSessionId);
    }

    private String getChatSessionId(String senderId, String receiverId) {
        // Custom logic to generate a unique ID for the chat session
        return senderId.compareTo(receiverId) < 0 ? senderId + "-" + receiverId : receiverId + "-" + senderId;
    }
}

