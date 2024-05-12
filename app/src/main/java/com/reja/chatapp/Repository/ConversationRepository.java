package com.reja.chatapp.Repository;

import android.app.Application;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.reja.chatapp.Model.Conversation;
import com.reja.chatapp.Model.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ConversationRepository {
    private Application application;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private MutableLiveData<List<Conversation>> mutableListOfChatsLiveData;

    public ConversationRepository(Application application) {
        this.application = application;
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();


        mutableListOfChatsLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<Conversation>> getAllConversation(String userId) {
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users");
        Query convRef = firebaseDatabase.getReference().child("Conversations").child(userId);

        MutableLiveData<List<Conversation>> mutableListOfChatsLiveData = new MutableLiveData<>();
        CompletableFuture<List<Conversation>> futureList = new CompletableFuture<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        convRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Conversation> conversationList = new ArrayList<>(); // Use LinkedList for efficiency
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Conversation conversation = dataSnapshot.getValue(Conversation.class);
                    String receiverId = dataSnapshot.getKey();
                    assert conversation != null;
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
                            //Toast.makeText(application, "Check", Toast.LENGTH_SHORT).show();
                             // Add conversation after updating data
                            userLoadFuture.complete(null);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle user data fetch error (propagate or update LiveData)
                        }
                    });
                    futures.add(userLoadFuture);
                    conversationList.add(conversation);
                }


                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allFutures.thenAccept(ignore -> {
                    Collections.sort(conversationList, (conv1, conv2) ->
                            Long.compare(conv2.getLastMessage().getSendTimeStamp(), conv1.getLastMessage().getSendTimeStamp()));

                    // Update your MutableLiveData with the sorted list
                    mutableListOfChatsLiveData.setValue(conversationList);
                });

                // Sort the conversations by lastMessage timestamp

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle conversation retrieval error (propagate or update LiveData)
                futureList.completeExceptionally(error.toException());
            }
        });
        futureList.thenAccept(mutableListOfChatsLiveData::setValue);


        return mutableListOfChatsLiveData;
    }

    public void setOnlineStatus(String userId,boolean status){
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(userId).child("status");
        if(status){
            userRef.setValue(true);
        }else{
            userRef.removeValue();
        }

    }

    public MutableLiveData<String> getUserName (){
        MutableLiveData<String> stringMutableLiveData = new MutableLiveData<>();
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(Objects.requireNonNull(auth.getCurrentUser()).getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("userName").getValue(String.class);
                stringMutableLiveData.setValue(userName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return stringMutableLiveData;

    }


}
