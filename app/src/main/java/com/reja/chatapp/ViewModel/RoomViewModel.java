package com.reja.chatapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.reja.chatapp.Model.Friend;
import com.reja.chatapp.Model.Room;
import com.reja.chatapp.Model.RoomMessage;
import com.reja.chatapp.Model.User;
import com.reja.chatapp.Repository.RoomRepository;

import java.util.List;

public class RoomViewModel extends AndroidViewModel {
    private RoomRepository roomRepository;
    public RoomViewModel(@NonNull Application application) {
        super(application);
        roomRepository = new RoomRepository(application);
    }

    public MutableLiveData<String> getUserActionLiveData(){
        return roomRepository.getUserActionLiveData();
    }

    public MutableLiveData<User> getUserDetails(){
        return roomRepository.getUserDetails();
    }

    public void createRoom(String videoUrl){
        roomRepository.createRoom(videoUrl);
    }

    public void joinRoom(String userId,String userName,String userProfilePicture, String roomId){
        roomRepository.joinRoom(userId,userName,userProfilePicture,roomId);
    }

    public void updatePlayState(String roomId, boolean isPlaying){
        roomRepository.updatePlayState(roomId,isPlaying);
    }

    public void updateCurrentPosition(String roomId, long position){
        roomRepository.updateCurrentPosition(roomId,position);
    }
    public MutableLiveData<Room> getRoomData(String roomId){
        return roomRepository.getRoomData(roomId);
    }

    public void sendRoomMessage(RoomMessage roomMessage){
        roomRepository.sendRoomMessage(roomMessage);
    }

    public MutableLiveData<List<RoomMessage>> getRoomMessages(String roomId){
        return roomRepository.getRoomMessages(roomId);
    }

    public void updateMediaUrl(String roomId, String mediaUrl){
        roomRepository.updateMediaUrl(roomId,mediaUrl);
    }

    public MutableLiveData<Boolean> isPlaying(String roomId){
        return roomRepository.isPlaying(roomId);
    }

    public MutableLiveData<String> getMediaUrl(String roomId){
        return roomRepository.getMediaUrl(roomId);

    }

    public MutableLiveData<Long> getCurrentPosition(String roomId){
        return roomRepository.getCurrentPosition(roomId);
    }

    public void sendRoomRequest(String roomId,String senderId, String friendId){
        roomRepository.sendRoomRequest(roomId,senderId,friendId);

    }

    public MutableLiveData<List<Friend>> getFriendList(String userId, String roomId){
        return roomRepository.getFriendList(userId,roomId);
    }

    public MutableLiveData<Boolean> getUserSentActionLiveData(){
        return roomRepository.getUserSentActionLiveData();
    }
}
