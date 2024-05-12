package com.reja.chatapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.reja.chatapp.Model.Conversation;
import com.reja.chatapp.Repository.ConversationRepository;

import java.util.List;

public class ConversationViewModel extends AndroidViewModel {
    private ConversationRepository conversationRepository;
    public ConversationViewModel(@NonNull Application application) {
        super(application);
        conversationRepository = new ConversationRepository(application);
    }

    public MutableLiveData<List<Conversation>> getListOfConversation(String userId){
        return conversationRepository.getAllConversation(userId);
    }

    public MutableLiveData<String> getUserName(){
        return conversationRepository.getUserName();
    }

    public void setOnlineStatus(String userId,boolean status){
        conversationRepository.setOnlineStatus(userId,status);
    }
}
