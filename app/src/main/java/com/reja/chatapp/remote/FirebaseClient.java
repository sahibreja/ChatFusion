package com.reja.chatapp.remote;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.reja.chatapp.Utils.DataModel;
import com.reja.chatapp.Utils.ErrorCallBack;
import com.reja.chatapp.Utils.NewEventCallBack;
import com.reja.chatapp.Utils.SuccessCallBack;

import java.util.Objects;

public class FirebaseClient {

    private static final String LATEST_EVENT_FIELD_NAME = "latest_event";
    private final Gson gson = new Gson();
    private  DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users");

    public void sendMessageToOtherUser(DataModel dataModel, ErrorCallBack errorCallBack) {
        dbRef.child(dataModel.getReceiverId()).child(LATEST_EVENT_FIELD_NAME)
                .setValue(gson.toJson(dataModel)).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        errorCallBack.onError();
                    }
                });
    }

    public void observeIncomingLatestEvent(String senderId,NewEventCallBack callBack) {
        dbRef.child(senderId).child(LATEST_EVENT_FIELD_NAME).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String data = Objects.requireNonNull(snapshot.getValue()).toString();
                    DataModel dataModel = gson.fromJson(data, DataModel.class);
                    callBack.onNewEventReceived(dataModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optionally, handle the cancellation error
            }
        });
    }

    public void deleteLatestEvent(String senderId, String receiverId) {
        DatabaseReference senderRef = dbRef.child(senderId).child(LATEST_EVENT_FIELD_NAME);
        DatabaseReference receiverRef = dbRef.child(receiverId).child(LATEST_EVENT_FIELD_NAME);
        senderRef.removeValue();
        receiverRef.removeValue();
    }
}
