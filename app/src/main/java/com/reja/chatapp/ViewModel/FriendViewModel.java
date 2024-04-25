package com.reja.chatapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.reja.chatapp.Model.User;
import com.reja.chatapp.Repository.FriendRepository;

import java.util.List;

public class FriendViewModel extends AndroidViewModel {

    private FriendRepository friendRepository;
    private MutableLiveData<List<User>> userList;
    public FriendViewModel(@NonNull Application application) {
        super(application);
        friendRepository = new FriendRepository(application);
        userList = friendRepository.getAllUserList();
    }

    public MutableLiveData<List<User>> getUserList() {
        return userList;
    }
}
