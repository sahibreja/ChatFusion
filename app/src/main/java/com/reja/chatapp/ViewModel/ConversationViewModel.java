package com.reja.chatapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.reja.chatapp.Model.Conversation;
import com.reja.chatapp.Repository.ConversationRepository;

import java.util.List;

public class ConversationViewModel extends AndroidViewModel {
    private final ConversationRepository conversationRepository;

    public ConversationViewModel(@NonNull Application application) {
        super(application);
        conversationRepository = new ConversationRepository(application);
    }

    public void getAllConversation(String userId) {
        conversationRepository.getAllConversation(userId);
    }

    public MutableLiveData<String> getUserName() {
        return conversationRepository.getUserName();
    }

    public void setOnlineStatus(String userId, boolean status) {
        conversationRepository.setOnlineStatus(userId, status);
    }

    public void getConversationsByUserName(String userId, String queryUserName) {
        conversationRepository.getConversationsByUserName(userId, queryUserName);
    }

    public MutableLiveData<List<Conversation>> getConversations() {
        return conversationRepository.getMutableListOfChatsLiveData();
    }
}
