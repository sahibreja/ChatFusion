package com.reja.chatapp.ViewModel;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.reja.chatapp.Model.User;
import com.reja.chatapp.Repository.AuthenticationRepository;

public class AuthViewModel extends AndroidViewModel {

    private AuthenticationRepository repository;
    private MutableLiveData<FirebaseUser> userData;
    private MutableLiveData<Boolean> loggedStatus;

    public AuthViewModel(Application application){
        super(application);
        repository = new AuthenticationRepository(application);
        userData = repository.getFirebaseUserMutableLiveData();
        loggedStatus = repository.getUserLoggedMutableLiveData();
    }
    public void signUp(User user, Uri fileUri){
        repository.signUp(user,fileUri);
    }

    public void logIn(String email , String pass){
        repository.login(email, pass);
    }
    public void signOut(){
        repository.signOut();
    }

    public void addUserDeviceToken(String userId){
        repository.addUserDeviceToken(userId);
    }

    public void removeUserDeviceToken(String userId){
        repository.removeUserDeviceToken(userId);
    }

    public MutableLiveData<FirebaseUser> getUserData() {
        return userData;
    }

    public MutableLiveData<Boolean> getLoggedStatus() {
        return loggedStatus;
    }
}
