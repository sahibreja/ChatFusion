package com.reja.chatapp.Repository;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

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
import com.reja.chatapp.Model.Conversation;
import com.reja.chatapp.Model.Message;
import com.reja.chatapp.Model.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ConversationRepository {
    private final Application application;
    private final FirebaseAuth auth;
    private final FirebaseDatabase firebaseDatabase;
    private final DatabaseReference databaseReference;
    private final MutableLiveData<List<Conversation>> mutableListOfChatsLiveData;

    public ConversationRepository(Application application) {
        this.application = application;
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        mutableListOfChatsLiveData = new MutableLiveData<>();
    }
    public MutableLiveData<List<Conversation>> getMutableListOfChatsLiveData() {
        return mutableListOfChatsLiveData;
    }
    public void getAllConversation(String userId) {
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users");
        Query convRef = firebaseDatabase.getReference().child("Conversations").child(userId);
        CompletableFuture<List<Conversation>> futureList = new CompletableFuture<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        convRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Conversation> conversationList = new ArrayList<>();
                futures.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String receiverId = dataSnapshot.getKey();
                    Conversation conversation = dataSnapshot.getValue(Conversation.class);
                    if (conversation != null) {
                        conversation.setConversationId(receiverId);
                        conversation.setSenderId(userId);
                        conversation.setRecipientId(receiverId);

                        CompletableFuture<Void> userLoadFuture = new CompletableFuture<>();

                        userRef.child(receiverId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                String userName = userSnapshot.child("userName").getValue(String.class);
                                String userProfilePicture = userSnapshot.child("userProfilePicture").getValue(String.class);
                                conversation.setRecipientName(userName);
                                conversation.setRecipientProfilePicture(userProfilePicture);
                                conversationList.add(conversation);
                                userLoadFuture.complete(null);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                userLoadFuture.completeExceptionally(error.toException());
                            }
                        });

                        futures.add(userLoadFuture);
                    }
                }


                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
                    Collections.sort(conversationList, (conv1, conv2) ->
                            Long.compare(conv2.getLastMessage().getSendTimeStamp(), conv1.getLastMessage().getSendTimeStamp()));
                    mutableListOfChatsLiveData.postValue(conversationList);
                }).exceptionally(ex -> {
                    Log.e("getAllConversation", "Error loading conversations", ex);
                    return null;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("getAllConversation", "Error retrieving conversations", error.toException());
            }
        });
        futureList.thenAccept(mutableListOfChatsLiveData::setValue).exceptionally(ex -> {
            // Log any exceptions
            Log.e("getSentRequestList", "Error posting LiveData", ex);
            return null;
        });
    }
    public void getConversationsByUserName(String userId, String queryUserName) {
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users");
        Query convRef = firebaseDatabase.getReference().child("Conversations").child(userId);

        convRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Conversation> conversationList = new ArrayList<>();
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                Log.d("Conversation size", String.valueOf(snapshot.getChildrenCount()));

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String receiverId = dataSnapshot.getKey();
                    Conversation conversation = dataSnapshot.getValue(Conversation.class);
                    if (conversation != null) {
                        conversation.setConversationId(receiverId);
                        conversation.setSenderId(userId);
                        conversation.setRecipientId(receiverId);

                        CompletableFuture<Void> userLoadFuture = new CompletableFuture<>();

                        userRef.child(receiverId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                String userName = userSnapshot.child("userName").getValue(String.class);
                                String userProfilePicture = userSnapshot.child("userProfilePicture").getValue(String.class);
                                conversation.setRecipientName(userName);
                                conversation.setRecipientProfilePicture(userProfilePicture);
                                if (userName != null && userName.toLowerCase().startsWith(queryUserName.toLowerCase())) {
                                    conversationList.add(conversation);
                                }
                                userLoadFuture.complete(null);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                userLoadFuture.completeExceptionally(error.toException());
                            }
                        });

                        futures.add(userLoadFuture);
                    }
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenAccept(v -> {
                    Collections.sort(conversationList, (conv1, conv2) ->
                            Long.compare(conv2.getLastMessage().getSendTimeStamp(), conv1.getLastMessage().getSendTimeStamp()));
                    mutableListOfChatsLiveData.postValue(conversationList);
                }).exceptionally(ex -> {
                    Log.e("getConversationsByUserName", "Error loading conversations", ex);
                    return null;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("getConversationsByUserName", "Error retrieving conversations", error.toException());
            }
        });
    }

    public void setOnlineStatus(String userId, boolean status) {
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(userId).child("status");
        if (status) {
            new Handler().postDelayed(() -> userRef.setValue(status), 1000);
        } else {
            userRef.removeValue();
        }
    }

    public MutableLiveData<String> getUserName() {
        MutableLiveData<String> stringMutableLiveData = new MutableLiveData<>();
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("userName").getValue(String.class);
                stringMutableLiveData.setValue(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        return stringMutableLiveData;
    }
}
