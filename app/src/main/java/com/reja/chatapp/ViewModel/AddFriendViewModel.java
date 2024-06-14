package com.reja.chatapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.reja.chatapp.Model.FriendRequest;
import com.reja.chatapp.Model.SearchResult;
import com.reja.chatapp.Repository.AddFriendRepository;

import java.util.List;

public class AddFriendViewModel extends AndroidViewModel {

    private AddFriendRepository addFriendRepository;
    public AddFriendViewModel(@NonNull Application application) {
        super(application);
        addFriendRepository = new AddFriendRepository(application);
    }

    public MutableLiveData<List<SearchResult>> getFriendsList(){
        return addFriendRepository.getFriendsData();
    }
    public MutableLiveData<List<SearchResult>> getSearchResult(){
        return addFriendRepository.getSearchResultData();
    }
    public void perFormSearch(String query){
        addFriendRepository.performSearch(query);
    }

    public void getFriend(){
        addFriendRepository.getFriend();
    }

    public void getRandomUser(){
        addFriendRepository.getRandomUser();
    }

    public void getFriendAndRandomUser(){
        addFriendRepository.getFriendAndRandomUser();
    }

    public void sendFriendRequest(FriendRequest friendRequest){
        addFriendRepository.sendFriendRequest(friendRequest);
    }
    public void cancelFriendRequest(FriendRequest friendRequest){
        addFriendRepository.cancelFriendRequest(friendRequest);
    }

    public void acceptFriendRequest(FriendRequest friendRequest){
        addFriendRepository.acceptFriendRequest(friendRequest);
    }

    public void rejectFriendRequest(FriendRequest friendRequest){
        addFriendRepository.rejectFriendRequest(friendRequest);
    }
    public MutableLiveData<Boolean> getUserActionLiveData(){
        return addFriendRepository.getUserActionLiveData();
    }

    public MutableLiveData<List<SearchResult>> getReceiveRequestList(){
        return addFriendRepository.getReceiveRequestList();
    }

    public MutableLiveData<List<SearchResult>> getSentRequestList(){
        return addFriendRepository.getSentRequestList();
    }

    public MutableLiveData<Boolean> isSomeoneRequested(){
        return addFriendRepository.isSomeoneRequested();
    }

    public MutableLiveData<List<SearchResult>> getRandomNonFriendUsers(int count){
        return addFriendRepository.getRandomNonFriendUsers(count);
    }
}
