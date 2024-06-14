package com.reja.chatapp.Repository;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.reja.chatapp.Model.User;

import java.util.Objects;

public class UserRepository {

    private Application application;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private String currentUserId;

    public UserRepository(Application application){
        this.application = application;
        auth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public MutableLiveData<User> getUserDetails(){
        MutableLiveData<User> userMutableLiveData = new MutableLiveData<>();
        DatabaseReference userRef = firebaseDatabase.getReference("Users").child(currentUserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                userMutableLiveData.setValue(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return userMutableLiveData;
    }

    public void updateBio(String bio){
        DatabaseReference userRef = firebaseDatabase.getReference("Users").child(currentUserId);
        userRef.child("userBio").setValue(bio);
    }

    public void updateName(String name){
        DatabaseReference userRef = firebaseDatabase.getReference("Users").child(currentUserId);
        userRef.child("userName").setValue(name);
        userRef.child("lowerCaseUserName").setValue(name.toLowerCase());
    }

    public void updateProfileImage(Uri fileUri){
        DatabaseReference userRef = firebaseDatabase.getReference("Users").child(currentUserId);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");
        StorageReference imageRef = storageRef.child(currentUserId);

        UploadTask uploadTask = imageRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        userRef.child("userProfilePicture").setValue(uri.toString());
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });
    }
}
