package com.reja.chatapp.Repository;

import android.app.Application;
import android.util.Log;

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
import com.reja.chatapp.Model.MutualFriend;
import com.reja.chatapp.Model.ProfileDetails;
import com.reja.chatapp.Model.SearchResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
                        sr.setUserBio(dataSnapshot.child("userBio").getValue(String.class));

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
                userActionLiveData.setValue(isIReceived);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onIReceivedCheckCompleted(false);
                userActionLiveData.setValue(false);
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
                userActionLiveData.setValue(isISent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callBack.onIRequestCheckCompleted(false);
                userActionLiveData.setValue(false);
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
                userActionLiveData.setValue(isFriend);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFriendCheckCompleted(false);
                userActionLiveData.setValue(false);
            }
        });
    }
    public void getFriend() {
        DatabaseReference friendsRef = firebaseDatabase.getReference("Users").child(userId).child("Friends");
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users");

        CompletableFuture<List<SearchResult>> futureList = new CompletableFuture<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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
                                sr.setUserBio(dataSnapshot.child("userBio").getValue(String.class));
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
                        sr.setUserBio(dataSnapshot.child("userBio").getValue(String.class));

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
    public MutableLiveData<ProfileDetails> getProfileDetails(String userId, String receiverId){
        DatabaseReference receiverRef = firebaseDatabase
                .getReference()
                .child("Users")
                .child(receiverId);
        MutableLiveData<ProfileDetails> profileDetailsMutableLiveData = new MutableLiveData<>();

        receiverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ProfileDetails profileDetails = new ProfileDetails();
                String userName = snapshot.child("userName").getValue(String.class);
                String userProfilePicture = snapshot.child("userProfilePicture").getValue(String.class);
                String userBio = snapshot.child("userBio").getValue(String.class);
                profileDetails.setUserName(userName);
                profileDetails.setUserProfilePicture(userProfilePicture);
                profileDetails.setUserBio(userBio);

                // Check if this user is present in my friend list
                checkIsFriend(receiverId, new FriendCheckCallBack() {
                    @Override
                    public void onFriendCheckCompleted(boolean isFriend) {
                        profileDetails.setFriend(isFriend);

                        // Check if I have sent request to this user
                        checkISent(receiverId, new FriendRequestSentCallBack() {
                            @Override
                            public void onIRequestCheckCompleted(boolean isISent) {
                                profileDetails.setIsent(isISent);

                                // Check if I have received request from that user
                                checkIReceived(receiverId, new FriendRequestReceivedCallback() {
                                    @Override
                                    public void onIReceivedCheckCompleted(boolean isIReceived) {
                                        profileDetails.setIReceived(isIReceived);
                                        // Update the LiveData after all checks
                                        profileDetailsMutableLiveData.postValue(profileDetails);
                                    }
                                });
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if necessary
            }
        });

        return profileDetailsMutableLiveData;
    }
    public MutableLiveData<List<MutualFriend>> getMutualFriends(String userId, String receiverId) {
        MutableLiveData<List<MutualFriend>> mutualFriendsLiveData = new MutableLiveData<>();

        DatabaseReference userFriendsRef = firebaseDatabase
                .getReference()
                .child("Users")
                .child(userId)
                .child("Friends");

        DatabaseReference receiverFriendsRef = firebaseDatabase
                .getReference()
                .child("Users")
                .child(receiverId)
                .child("Friends");

        userFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userFriendsSnapshot) {
                List<String> userFriends = new ArrayList<>();
                for (DataSnapshot snapshot : userFriendsSnapshot.getChildren()) {
                    String friendId = snapshot.getKey();
                    userFriends.add(friendId);
                }

                receiverFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot receiverFriendsSnapshot) {
                        List<String> receiverFriends = new ArrayList<>();
                        for (DataSnapshot snapshot : receiverFriendsSnapshot.getChildren()) {
                            String friendId = snapshot.getKey();
                            receiverFriends.add(friendId);
                        }

                        List<String> mutualFriendIds = new ArrayList<>(userFriends);
                        mutualFriendIds.retainAll(receiverFriends);

                        fetchMutualFriendsDetails(mutualFriendIds, mutualFriendsLiveData);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error if necessary
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if necessary
            }
        });

        return mutualFriendsLiveData;
    }
    private void fetchMutualFriendsDetails(List<String> mutualFriendIds, MutableLiveData<List<MutualFriend>> mutualFriendsLiveData) {
        List<MutualFriend> mutualFriends = new ArrayList<>();
        DatabaseReference usersRef = firebaseDatabase.getReference().child("Users");

        for (String friendId : mutualFriendIds) {
            usersRef.child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("userName").getValue(String.class);
                    String profilePicture = snapshot.child("userProfilePicture").getValue(String.class);

                    MutualFriend mutualFriend = new MutualFriend(friendId, name, profilePicture);
                    mutualFriends.add(mutualFriend);

                    if (mutualFriends.size() == mutualFriendIds.size()) {
                        mutualFriendsLiveData.postValue(mutualFriends);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error if necessary
                }
            });
        }
    }
    public MutableLiveData<List<SearchResult>> getReceiveRequestList() {
        MutableLiveData<List<SearchResult>> listMutableLiveData = new MutableLiveData<>();
        DatabaseReference requestRef = firebaseDatabase
                .getReference()
                .child("Users")
                .child(userId)
                .child("FriendRequest")
                .child("Receive");

        CompletableFuture<List<SearchResult>> futureList = new CompletableFuture<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SearchResult> searchResults = new ArrayList<>();
                futures.clear(); // Ensure the futures list is cleared each time

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SearchResult sr = new SearchResult();
                    sr.setUserId(dataSnapshot.getKey());

                    CompletableFuture<Void> friendCheckFuture = new CompletableFuture<>();
                    checkIsFriend(sr.getUserId(), new FriendCheckCallBack() {
                        @Override
                        public void onFriendCheckCompleted(boolean isFriend) {
                            sr.setFriend(isFriend);
                            friendCheckFuture.complete(null);
                        }
                    });
                    futures.add(friendCheckFuture);

                    CompletableFuture<Void> requestSentFuture = new CompletableFuture<>();
                    checkISent(sr.getUserId(), new FriendRequestSentCallBack() {
                        @Override
                        public void onIRequestCheckCompleted(boolean isISent) {
                            sr.setIRequested(isISent);
                            requestSentFuture.complete(null);
                        }
                    });
                    futures.add(requestSentFuture);

                    CompletableFuture<Void> requestReceivedFuture = new CompletableFuture<>();
                    checkIReceived(sr.getUserId(), new FriendRequestReceivedCallback() {
                        @Override
                        public void onIReceivedCheckCompleted(boolean isIReceived) {
                            sr.setIReceived(isIReceived);
                            requestReceivedFuture.complete(null);
                        }
                    });
                    futures.add(requestReceivedFuture);

                    searchResults.add(sr);
                }

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allFutures.thenAccept(ignore -> {
                    futureList.complete(searchResults);
                    getUserDetails(searchResults, listMutableLiveData);
                }).exceptionally(ex -> {
                    // Log any exceptions
                    Log.e("getReceiveRequestList", "Error completing all futures", ex);
                    return null;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                futureList.completeExceptionally(error.toException());
            }
        });

        futureList.thenAccept(listMutableLiveData::postValue).exceptionally(ex -> {
            // Log any exceptions
            Log.e("getReceiveRequestList", "Error posting LiveData", ex);
            return null;
        });

        return listMutableLiveData;
    }
    public MutableLiveData<List<SearchResult>> getSentRequestList() {
        MutableLiveData<List<SearchResult>> listMutableLiveData = new MutableLiveData<>();
        DatabaseReference requestRef = firebaseDatabase
                .getReference()
                .child("Users")
                .child(userId)
                .child("FriendRequest")
                .child("Sent");

        CompletableFuture<List<SearchResult>> futureList = new CompletableFuture<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SearchResult> searchResults = new ArrayList<>();
                futures.clear(); // Ensure the futures list is cleared each time

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SearchResult sr = new SearchResult();
                    sr.setUserId(dataSnapshot.getKey());

                    CompletableFuture<Void> friendCheckFuture = new CompletableFuture<>();
                    checkIsFriend(sr.getUserId(), new FriendCheckCallBack() {
                        @Override
                        public void onFriendCheckCompleted(boolean isFriend) {
                            sr.setFriend(isFriend);
                            friendCheckFuture.complete(null);
                        }
                    });
                    futures.add(friendCheckFuture);

                    CompletableFuture<Void> requestSentFuture = new CompletableFuture<>();
                    checkISent(sr.getUserId(), new FriendRequestSentCallBack() {
                        @Override
                        public void onIRequestCheckCompleted(boolean isISent) {
                            sr.setIRequested(isISent);
                            requestSentFuture.complete(null);
                        }
                    });
                    futures.add(requestSentFuture);

                    CompletableFuture<Void> requestReceivedFuture = new CompletableFuture<>();
                    checkIReceived(sr.getUserId(), new FriendRequestReceivedCallback() {
                        @Override
                        public void onIReceivedCheckCompleted(boolean isIReceived) {
                            sr.setIReceived(isIReceived);
                            requestReceivedFuture.complete(null);
                        }
                    });
                    futures.add(requestReceivedFuture);

                    searchResults.add(sr);
                }

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allFutures.thenAccept(ignore -> {
                    futureList.complete(searchResults);
                    getUserDetails(searchResults, listMutableLiveData);
                }).exceptionally(ex -> {
                    // Log any exceptions
                    Log.e("getSentRequestList", "Error completing all futures", ex);
                    return null;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                futureList.completeExceptionally(error.toException());
            }
        });

        futureList.thenAccept(listMutableLiveData::postValue).exceptionally(ex -> {
            // Log any exceptions
            Log.e("getSentRequestList", "Error posting LiveData", ex);
            return null;
        });

        return listMutableLiveData;
    }
    public MutableLiveData<List<SearchResult>> getRandomNonFriendUsers(int count) {
        MutableLiveData<List<SearchResult>> listMutableLiveData = new MutableLiveData<>();
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SearchResult> searchResultList = new ArrayList<>();
                AtomicInteger counter = new AtomicInteger((int) snapshot.getChildrenCount());

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String receiverId = dataSnapshot.getKey();
                    if (receiverId != null) {
                        if(!receiverId.equals(userId)) {
                            String userName = dataSnapshot.child("userName").getValue(String.class);
                            String userProfilePicture = dataSnapshot.child("userProfilePicture").getValue(String.class);
                            String userBio = dataSnapshot.child("userBio").getValue(String.class);
                            checkIsFriend(receiverId, isFriend -> {
                                if (!isFriend) {
                                    checkISent(receiverId, isISent -> {
                                        if (!isISent) {
                                            checkIReceived(receiverId, isIReceived -> {
                                                if (!isIReceived) {
                                                    SearchResult sr = new SearchResult();
                                                    sr.setUserId(receiverId);
                                                    sr.setFriend(false);
                                                    sr.setIRequested(false);
                                                    sr.setIReceived(false);
                                                    sr.setUserName(userName);
                                                    sr.setUserProfileImage(userProfilePicture);
                                                    sr.setUserBio(userBio);
                                                    searchResultList.add(sr);
                                                }
                                                if (counter.decrementAndGet() == 0) {
                                                    listMutableLiveData.setValue(searchResultList);
                                                }
                                            });
                                        } else if (counter.decrementAndGet() == 0) {
                                            listMutableLiveData.setValue(searchResultList);
                                        }
                                    });
                                } else if (counter.decrementAndGet() == 0) {
                                    listMutableLiveData.setValue(searchResultList);
                                }
                            });
                        }else if (counter.decrementAndGet() == 0) {
                            listMutableLiveData.setValue(searchResultList);
                        }
                    }
                }

                // In case the initial snapshot is empty, we need to post an empty list
                if (snapshot.getChildrenCount() == 0) {
                    listMutableLiveData.setValue(searchResultList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("getRandomNonFriendUsers", "Error fetching users", error.toException());
            }
        });

        return listMutableLiveData;
    }
    private void getUserDetails(List<SearchResult> searchResultList, MutableLiveData<List<SearchResult>> searchResultsLiveData) {
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users");
        AtomicInteger counter = new AtomicInteger(searchResultList.size());

        for (SearchResult sr : searchResultList) {
            userRef.child(sr.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userName = snapshot.child("userName").getValue(String.class);
                    String userProfilePicture = snapshot.child("userProfilePicture").getValue(String.class);
                    String userBio = snapshot.child("userBio").getValue(String.class);
                    sr.setUserName(userName);
                    sr.setUserProfileImage(userProfilePicture);
                    sr.setUserBio(userBio);
                    if (counter.decrementAndGet() == 0) {
                        searchResultsLiveData.postValue(searchResultList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("getUserDetails", "Error fetching user details for userId: " + sr.getUserId(), error.toException());
                }
            });
        }

        if (searchResultList.isEmpty()) {
            searchResultsLiveData.postValue(searchResultList);
        }
    }
    public MutableLiveData<Boolean> isSomeoneRequested(){
        MutableLiveData<Boolean> isMutableLiveData = new MutableLiveData<>();
        DatabaseReference userRef = firebaseDatabase.getReference().
                child("Users").
                child(userId).
                child("FriendRequest")
                .child("Receive");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() > 0){
                    isMutableLiveData.setValue(true);
                }else{
                    isMutableLiveData.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isMutableLiveData.setValue(false);
            }
        });
        return isMutableLiveData;
    }
}
