package com.reja.chatapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.reja.chatapp.Model.FriendRequest;
import com.reja.chatapp.Model.MutualFriend;
import com.reja.chatapp.Model.ProfileDetails;
import com.reja.chatapp.Repository.AddFriendRepository;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {
    private AddFriendRepository repository;
    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository =  new AddFriendRepository(application);
    }

    public MutableLiveData<ProfileDetails> getProfileDetails(String userId,String receiverId){
        return repository.getProfileDetails(userId,receiverId);
    }

    public void sendFriendRequest(FriendRequest friendRequest){
        repository.sendFriendRequest(friendRequest);
    }
    public void cancelFriendRequest(FriendRequest friendRequest){
        repository.cancelFriendRequest(friendRequest);
    }

    public void acceptFriendRequest(FriendRequest friendRequest){
        repository.acceptFriendRequest(friendRequest);
    }

    public void rejectFriendRequest(FriendRequest friendRequest){
        repository.rejectFriendRequest(friendRequest);
    }

    public MutableLiveData<List<MutualFriend>> getMutualFriends(String userId, String receiverId){
        return repository.getMutualFriends(userId,receiverId);
    }


}
