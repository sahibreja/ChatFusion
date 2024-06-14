package com.reja.chatapp.Repository;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.reja.chatapp.Model.SignalMessage;

public class CallRepository {
    private final DatabaseReference databaseReference;

    public CallRepository() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void sendSignalMessage(String remoteUserId, SignalMessage message) {
        databaseReference.child("calls").child(remoteUserId).push().setValue(message);
    }

    public void observeIncomingCalls(String localUserId, ChildEventListener callback) {
        databaseReference.child("calls").child(localUserId).addChildEventListener(callback);
    }
}


