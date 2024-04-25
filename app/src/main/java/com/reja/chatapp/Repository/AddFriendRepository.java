package com.reja.chatapp.Repository;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.reja.chatapp.Interface.CallBack.FriendCheckCallBack;
import com.reja.chatapp.Interface.CallBack.FriendRequestReceivedCallback;
import com.reja.chatapp.Interface.CallBack.FriendRequestSentCallBack;
import com.reja.chatapp.Model.Friend;
import com.reja.chatapp.Model.FriendRequest;
import com.reja.chatapp.Model.SearchResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AddFriendRepository {
    private Application application;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private MutableLiveData<List<SearchResult>> friendMutableLiveData;
    private MutableLiveData<Boolean> userActionLiveData;
    private String userId;

    public AddFriendRepository(Application application) {
        this.application = application;
        friendMutableLiveData = new MutableLiveData<>();
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        friendMutableLiveData = new MutableLiveData<>();
        userActionLiveData = new MutableLiveData<>();

    }

    public MutableLiveData<List<SearchResult>> getFriendsData(){
        return friendMutableLiveData;
    }

    public MutableLiveData<List<SearchResult>> getSearchResultData(){
        return friendMutableLiveData;
    }
    public MutableLiveData<Boolean> getUserActionLiveData(){
        return userActionLiveData;
    }

    public void performSearch(String query) {
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users");
        Query query1 = userRef.orderByChild("lowerCaseUserName").startAt(query).endAt(query + "\uf8ff");

        // Use CompletableFuture to represent asynchronous tasks
        CompletableFuture<List<SearchResult>> futureList = new CompletableFuture<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SearchResult> searchResultList = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SearchResult sr = new SearchResult();
                    String searchUserId = dataSnapshot.child("userId").getValue(String.class);
                    if (searchUserId != null && !searchUserId.equals(userId)) {
                        sr.setUserId(searchUserId);
                        sr.setUserName(dataSnapshot.child("userName").getValue(String.class));
                        sr.setUserProfileImage(dataSnapshot.child("userProfilePicture").getValue(String.class));

                        // Check if this user is present in my friend list
                        CompletableFuture<Void> friendCheckFuture = new CompletableFuture<>();
                        checkIsFriend(searchUserId, new FriendCheckCallBack() {
                            @Override
                            public void onFriendCheckCompleted(boolean isFriend) {
                                sr.setFriend(isFriend);
                                friendCheckFuture.complete(null);
                            }
                        });
                        futures.add(friendCheckFuture);

                        // Check if I have sent request to this user
                        CompletableFuture<Void> requestSentFuture = new CompletableFuture<>();
                        checkISent(searchUserId, new FriendRequestSentCallBack() {
                            @Override
                            public void onIRequestCheckCompleted(boolean isISent) {
                                sr.setIRequested(isISent);
                                requestSentFuture.complete(null);
                            }
                        });
                        futures.add(requestSentFuture);

                        // Check if I have received request from that user
                        CompletableFuture<Void> requestReceivedFuture = new CompletableFuture<>();
                        checkIReceived(searchUserId, new FriendRequestReceivedCallback() {
                            @Override
                            public void onIReceivedCheckCompleted(boolean isIReceived) {
                                sr.setIReceived(isIReceived);
                                requestReceivedFuture.complete(null);
                            }
                        });
                        futures.add(requestReceivedFuture);

                        searchResultList.add(sr);
                    }
                }

                // After iterating through all results, update the LiveData
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allFutures.thenAccept(ignore -> {
                    futureList.complete(searchResultList);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                futureList.completeExceptionally(error.toException());
            }
        });

        futureList.thenAccept(searchResults -> friendMutableLiveData.setValue(searchResults));
    }

    private void checkIReceived(String searchUserId, final FriendRequestReceivedCallback callback) {
        DatabaseReference userFriendRef = firebaseDatabase.getReference("Users").child(userId).child("FriendRequest").child("Receive").child(searchUserId);
        userFriendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isIReceived = snapshot.exists();
                callback.onIReceivedCheckCompleted(isIReceived);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onIReceivedCheckCompleted(false);
            }
        });

    }
    private void checkISent(String searchUserId, final FriendRequestSentCallBack callBack) {
        DatabaseReference userFriendRef = firebaseDatabase.getReference("Users").child(userId).child("FriendRequest").child("Sent").child(searchUserId);
        userFriendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isISent = snapshot.exists();
                callBack.onIRequestCheckCompleted(isISent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack.onIRequestCheckCompleted(false);
            }
        });
    }
    private void checkIsFriend(String searchUserId, final FriendCheckCallBack callback) {
        DatabaseReference userFriendRef = firebaseDatabase.getReference("Users").child(userId).child("Friends").child(searchUserId);
        userFriendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFriend = snapshot.exists();
                callback.onFriendCheckCompleted(isFriend);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFriendCheckCompleted(false);
            }
        });
    }
    public void getFriend() {
        DatabaseReference friendsRef = firebaseDatabase.getReference("Users").child(userId).child("Friends");
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users");

        CompletableFuture<List<SearchResult>> futureList = new CompletableFuture<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SearchResult> searchResults = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String id = dataSnapshot.child("friendId").getValue(String.class);
                    if (id != null) {
                        CompletableFuture<Void> future = new CompletableFuture<>();
                        futures.add(future);

                        userRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                SearchResult sr = new SearchResult();
                                sr.setFriend(true);
                                sr.setUserId(id);
                                sr.setUserName(userSnapshot.child("userName").getValue(String.class));
                                sr.setUserProfileImage(userSnapshot.child("userProfilePicture").getValue(String.class));
                                searchResults.add(sr);
                                future.complete(null);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                future.completeExceptionally(error.toException());
                            }
                        });
                    }
                }

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allFutures.thenRun(() -> futureList.complete(searchResults));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                futureList.completeExceptionally(error.toException());
            }
        });

        futureList.thenAccept(searchResults -> friendMutableLiveData.postValue(searchResults));
    }
    public void getRandomUser() {
        DatabaseReference userRef = firebaseDatabase.getReference("Users");
        CompletableFuture<List<SearchResult>> futureList = new CompletableFuture<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SearchResult> searchResultList = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SearchResult sr = new SearchResult();
                    String searchUserId = dataSnapshot.child("userId").getValue(String.class);
                    if (searchUserId != null && !searchUserId.equals(userId)) {
                        sr.setUserId(searchUserId);
                        sr.setUserName(dataSnapshot.child("userName").getValue(String.class));
                        sr.setUserProfileImage(dataSnapshot.child("userProfilePicture").getValue(String.class));

                        // Check if this user is present in my friend list
                        CompletableFuture<Void> friendCheckFuture = new CompletableFuture<>();
                        checkIsFriend(searchUserId, new FriendCheckCallBack() {
                            @Override
                            public void onFriendCheckCompleted(boolean isFriend) {
                                sr.setFriend(isFriend);
                                friendCheckFuture.complete(null);
                            }
                        });
                        futures.add(friendCheckFuture);

                        // Check if I have sent request to this user
                        CompletableFuture<Void> requestSentFuture = new CompletableFuture<>();
                        checkISent(searchUserId, new FriendRequestSentCallBack() {
                            @Override
                            public void onIRequestCheckCompleted(boolean isISent) {
                                sr.setIRequested(isISent);
                                requestSentFuture.complete(null);
                            }
                        });
                        futures.add(requestSentFuture);

                        // Check if I have received request from that user
                        CompletableFuture<Void> requestReceivedFuture = new CompletableFuture<>();
                        checkIReceived(searchUserId, new FriendRequestReceivedCallback() {
                            @Override
                            public void onIReceivedCheckCompleted(boolean isIReceived) {
                                sr.setIReceived(isIReceived);
                                requestReceivedFuture.complete(null);
                            }
                        });
                        futures.add(requestReceivedFuture);

                        searchResultList.add(sr);
                    }
                }

                // After iterating through all results, update the LiveData
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allFutures.thenAccept(ignore -> {
                    Collections.shuffle(searchResultList);
                    futureList.complete(searchResultList);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                futureList.completeExceptionally(error.toException());
            }
        });

        futureList.thenAccept(searchResults -> friendMutableLiveData.setValue(searchResults));

    }
    public void getFriendAndRandomUser() {
        DatabaseReference friendsRef = firebaseDatabase.getReference("Users").child(userId).child("Friends");

        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    // User has friends, get friend list
                    getFriend();
                } else {
                    // User has no friends, get random user list
                    getRandomUser();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled event
            }
        });
    }
    public void sendFriendRequest(FriendRequest friendRequest){
        DatabaseReference senderRef = firebaseDatabase.getReference().child("Users").child(friendRequest.getSenderId()).child("FriendRequest").child("Sent");
        DatabaseReference receiverRef = firebaseDatabase.getReference().child("Users").child(friendRequest.getReceiverId()).child("FriendRequest").child("Receive");

        senderRef.child(friendRequest.getReceiverId()).setValue(friendRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    receiverRef.child(friendRequest.getSenderId()).setValue(friendRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> innerTask) {
                            if (innerTask.isSuccessful()) {
                                userActionLiveData.setValue(true);
                            } else {
                                userActionLiveData.setValue(false);
                            }
                        }
                    });
                } else {
                    userActionLiveData.setValue(false);
                }
            }
        });
    }
    public void acceptFriendRequest(FriendRequest friendRequest) {
        long timeStamp = System.currentTimeMillis();
        Friend friend = new Friend(friendRequest.getReceiverId(), String.valueOf(timeStamp));

        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(friendRequest.getSenderId()).child("Friends");
        DatabaseReference friendRef = firebaseDatabase.getReference().child("Users").child(friendRequest.getReceiverId()).child("Friends");

        userRef.child(friendRequest.getReceiverId()).setValue(friend).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    friend.setFriendId(friendRequest.getSenderId());
                    friendRef.child(friendRequest.getSenderId()).setValue(friend).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                rejectFriendRequest(friendRequest);
                                userActionLiveData.setValue(true);
                            } else {
                                userActionLiveData.setValue(false);
                            }
                        }
                    });
                } else {
                    userActionLiveData.setValue(false);
                }
            }
        });
    }

    public void rejectFriendRequest(FriendRequest friendRequest){
        DatabaseReference receiverRef = firebaseDatabase.getReference().child("Users").child(friendRequest.getSenderId()).child("FriendRequest").child("Receive");
        DatabaseReference senderRef = firebaseDatabase.getReference().child("Users").child(friendRequest.getReceiverId()).child("FriendRequest").child("Sent");

        receiverRef.child(friendRequest.getReceiverId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    senderRef.child(friendRequest.getSenderId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                userActionLiveData.setValue(true);
                            }else{
                                userActionLiveData.setValue(false);
                            }
                        }
                    });
                }
            }
        });
    }
    public void cancelFriendRequest(FriendRequest friendRequest){
        DatabaseReference senderRef = firebaseDatabase.getReference().child("Users").child(friendRequest.getSenderId()).child("FriendRequest").child("Sent");
        DatabaseReference receiverRef = firebaseDatabase.getReference().child("Users").child(friendRequest.getReceiverId()).child("FriendRequest").child("Receive");

        senderRef.child(friendRequest.getReceiverId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    receiverRef.child(friendRequest.getSenderId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> innerTask) {
                            if (innerTask.isSuccessful()) {
                                userActionLiveData.setValue(true);
                            } else {
                                userActionLiveData.setValue(false);
                            }
                        }
                    });
                } else {
                    userActionLiveData.setValue(false);
                }
            }
        });
    }


}
