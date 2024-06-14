package com.reja.chatapp.ViewModel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.reja.chatapp.Model.User;
import com.reja.chatapp.Repository.UserRepository;

public class PersonalProfileViewModel extends AndroidViewModel {
    UserRepository repository;
    public PersonalProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public MutableLiveData<User> getUserData() {
        return repository.getUserDetails();
    }

    public void updateUserName(String userName){
        repository.updateName(userName);
    }

   public void updateUserBio(String userBio){
        repository.updateBio(userBio);
   }

   public void updateUserProfileImage(Uri uri){
        repository.updateProfileImage(uri);
   }
}
