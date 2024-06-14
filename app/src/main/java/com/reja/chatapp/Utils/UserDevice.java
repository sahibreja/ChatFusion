package com.reja.chatapp.Utils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class UserDevice {
    private String userId;

    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;;
    public UserDevice(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

    }
    public void addUserDeviceToken(String userId){
        DatabaseReference userRef = firebaseDatabase.getReference()
                .child("Users")
                .child(userId)
                .child("userDeviceToken");
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful()){
                    String token = task.getResult();
                    userRef.setValue(token);
                }
            }
        });
    }

    public void removeUserDeviceToken(String userId){
        DatabaseReference userRef = firebaseDatabase.getReference()
                .child("Users")
                .child(userId)
                .child("userDeviceToken");
        userRef.removeValue();
    }
}
