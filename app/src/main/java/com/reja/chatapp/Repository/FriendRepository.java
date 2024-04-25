package com.reja.chatapp.Repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reja.chatapp.Model.Friend;
import com.reja.chatapp.Model.User;

import java.util.ArrayList;
import java.util.List;

public class FriendRepository {
    private Application application;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private MutableLiveData<List<Friend>> friendMutableLiveData;

    public FriendRepository(Application application) {
        this.application = application;
        friendMutableLiveData = new MutableLiveData<>();
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

    }

    public MutableLiveData<List<Friend>> getFriendMutableLiveData() {
        return friendMutableLiveData;
    }

    public void sendFriendRequest(){

    }

    public void getListOfFriend(){
        MutableLiveData<List<Friend>> listOfFriends = new MutableLiveData<>();
        databaseReference = firebaseDatabase.getReference("Users").child("Friends");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Friend> friend = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Friend frd = dataSnapshot.getValue(Friend.class);
                    friend.add(frd);
                }
                listOfFriends.setValue(friend);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public MutableLiveData<List<User>> getSearchResult(String query){
        databaseReference = firebaseDatabase.getReference("Users");
        MutableLiveData<List<User>> searchResultList = new MutableLiveData<>();
        return  searchResultList;
    }

    public MutableLiveData<List<User>> getAllUserList(){
        databaseReference = firebaseDatabase.getReference("Users");
        MutableLiveData<List<User>> allUserList = new MutableLiveData<>();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> userList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = dataSnapshot.child("userId").getValue(String.class);
                    if(!auth.getCurrentUser().getUid().equals(id)){
                        User user = dataSnapshot.getValue(User.class);
                        userList.add(user);
                    }
                }
                allUserList.setValue(userList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return allUserList;
    }


    public MutableLiveData<List<String>> getRequestList(String userId){
        databaseReference = firebaseDatabase.getReference("FriendRequests").child(userId);
        MutableLiveData<List<String>> requestList = new MutableLiveData<>();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> req = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    String id = dataSnapshot.child("receiverId").getValue(String.class);
                    if(status.equals("pending")){
                        req.add(id);
                    }
                }
                requestList.setValue(req);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return requestList;
    }
}
