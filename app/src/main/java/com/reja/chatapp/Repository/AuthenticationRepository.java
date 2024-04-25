package com.reja.chatapp.Repository;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.reja.chatapp.Model.User;

public class AuthenticationRepository {
    private Application application;
    private FirebaseAuth auth;
    private MutableLiveData<FirebaseUser> firebaseUserMutableLiveData;
    private MutableLiveData<Boolean> userLoggedMutableLiveData;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    public AuthenticationRepository(Application application){
        this.application = application;
        firebaseUserMutableLiveData = new MutableLiveData<>();
        userLoggedMutableLiveData = new MutableLiveData<>();
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        if (auth.getCurrentUser() != null){
            firebaseUserMutableLiveData.postValue(auth.getCurrentUser());
        }
    }


    public MutableLiveData<FirebaseUser> getFirebaseUserMutableLiveData() {
        return firebaseUserMutableLiveData;
    }

    public MutableLiveData<Boolean> getUserLoggedMutableLiveData() {
        return userLoggedMutableLiveData;
    }

    public void login(String email , String pass){
        auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()){
                    userLoggedMutableLiveData.postValue(true);
                    firebaseUserMutableLiveData.postValue(auth.getCurrentUser());
                }else{
                    userLoggedMutableLiveData.postValue(false);
                    Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    public void signUp(User user){
        auth.createUserWithEmailAndPassword(user.getUserEmail(), user.getUserPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if(task.isSuccessful()){
                    userLoggedMutableLiveData.postValue(true);
                    firebaseUserMutableLiveData.postValue(auth.getCurrentUser());
                    user.setUserId(auth.getCurrentUser().getUid());
                    addDataInFirebaseDatabase(user);
                }else{
                    userLoggedMutableLiveData.postValue(false);
                    Toast.makeText(application, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addDataInFirebaseDatabase(User user){
        databaseReference.child(user.getUserId()).setValue(user);
    }

    public void signOut(){
        auth.signOut();
        userLoggedMutableLiveData.postValue(false);
    }

}
