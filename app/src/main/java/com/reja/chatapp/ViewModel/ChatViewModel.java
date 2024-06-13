package com.reja.chatapp.ViewModel;

import android.app.Application;
import android.net.Uri;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.reja.chatapp.Model.Message;
import com.reja.chatapp.Model.User;
import com.reja.chatapp.Repository.ChatRepository;
import com.reja.chatapp.Repository.RoomRepository;

import java.util.List;

public class ChatViewModel extends AndroidViewModel {
    private ChatRepository chatRepository;
    private RoomRepository roomRepository;
    public ChatViewModel(@NonNull Application application) {
        super(application);
        chatRepository = new ChatRepository(application);
        roomRepository = new RoomRepository(application);

    }
    public MutableLiveData<List<Message>> getListOfMessageLiveData(){
        return chatRepository.getListOfMessageLiveData();
    }

    public MutableLiveData<List<Message>> getListOfMessage(String senderId,String receiverId){
         return chatRepository.getListOfMessages(senderId,receiverId);
    }

    public void loadInitialMessages(String senderId, String receiverId, int limit){
         chatRepository.loadInitialMessages(senderId,receiverId,limit);
    }

    public void loadOlderMessages(String senderId, String receiverId, long oldestMessageTimestamp, int limit){
        chatRepository.loadOlderMessages(senderId,receiverId,oldestMessageTimestamp,limit);
    }
    public void loadNewerMessages(String senderId, String receiverId, long newestMessageTimestamp, int limit){
         chatRepository.loadNewerMessages(senderId,receiverId,newestMessageTimestamp,limit);
    }

    public void sendTextMessage(Message message,boolean isChattingWithMe){
        chatRepository.sendTextMessage(message,isChattingWithMe);
    }

    public void sendMediaMessage(Message message, Uri fileUri,boolean isChattingWithMe){
        chatRepository.sendMediaMessage(message,fileUri,isChattingWithMe);
    }

    public void deleteMessage(Message message,Message secondLastMessage,boolean isLastMessage){
        chatRepository.deleteMessage(message,secondLastMessage,isLastMessage);
    }

    public void markAllMessagesAsSeen(String senderId, String receiverId,long seenTime){
        chatRepository.markAllMessagesAsSeen(senderId,receiverId,seenTime);
    }

    public void setOnlineStatus(String userId,boolean status){
        chatRepository.setOnlineStatus(userId,status);
    }
    public MutableLiveData<Boolean> isOnline(String userId){
        return chatRepository.isOnline(userId);
    }

    public void setTypingStatus(String userId,String receiverId,boolean isTyping){
        chatRepository.setUserTypingStatus(userId, receiverId, isTyping);
    }
    public MutableLiveData<Boolean> getTypingStatus(String userId, String receiverId){
        return chatRepository.isUserTyping(userId,receiverId);
    }


    public void setChattingWith(String userId,String receiverId,boolean status){
        chatRepository.setChattingWith(userId, receiverId, status);
    }

    public LiveData<Pair<Boolean, Boolean>> getCombinedStatus(String userId, String receiverId) {
        LiveData<Boolean> typingStatus = getTypingStatus(userId, receiverId);
        LiveData<Boolean> onlineStatus = isOnline(receiverId);

        return Transformations.switchMap(typingStatus, typing ->
                Transformations.map(onlineStatus, online ->
                        new Pair<>(typing, online)
                )
        );
    }

    public MutableLiveData<Boolean> isChattingWith(String userId, String receiverId){
        return chatRepository.isChattingWith(userId,receiverId);
    }

    public MutableLiveData<String> getUserprofilePicture(String receiverId){
        return chatRepository.getUserprofilePicture(receiverId);
    }

    public LiveData<String> getReceiverDeviceToken(String receiverId){
        return chatRepository.getReceiverDeviceToken(receiverId);
    }

    public void joinRoom(String userId, String userName, String userProfilePicture, String roomId){
        roomRepository.joinRoom(userId,userName,userProfilePicture,roomId);

    }

    public MutableLiveData<User> getUserDetails(){
        return roomRepository.getUserDetails();
    }

    public MutableLiveData<String> getUserActionLiveData(){
        return roomRepository.getUserActionLiveData();
    }
}
