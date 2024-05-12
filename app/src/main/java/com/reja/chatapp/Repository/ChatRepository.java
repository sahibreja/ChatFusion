package com.reja.chatapp.Repository;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.reja.chatapp.Interface.CallBack.ConvoCheckCallBack;
import com.reja.chatapp.Model.Message;
import com.reja.chatapp.Model.MessageType;
import com.reja.chatapp.Model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatRepository {
    private Application application;
    private FirebaseAuth auth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private String userId;

    private MutableLiveData<List<Message>> mutableListOfMessageLiveData;

    public ChatRepository(Application application) {
        this.application = application;
        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        databaseReference = firebaseDatabase.getReference();
        mutableListOfMessageLiveData = new MutableLiveData<>();

    }

    public MutableLiveData<List<Message>> getListOfMessages(String senderId,String receiverId){
        MutableLiveData<List<Message>> mutableListOfMessages = new MutableLiveData<>();
        DatabaseReference messageRef = firebaseDatabase.getReference().child("Conversations").child(senderId).child(receiverId).child("Messages");
        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> listOfMessage = new ArrayList<>();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Message message = dataSnapshot.getValue(Message.class);
                    listOfMessage.add(message);
                }
                mutableListOfMessages.setValue(listOfMessage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return mutableListOfMessages;

    }

    public MutableLiveData<List<Message>> getInitialMessages(String senderId, String receiverId, int limit) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();
        Query messageRef = databaseReference
                .child("Conversations")
                .child(senderId)
                .child(receiverId)
                .child("Messages")
                .orderByChild("timestamp")
                .limitToLast(limit);

        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> initialMessages = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    initialMessages.add(message);
                }
                messagesLiveData.setValue(initialMessages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });

        return messagesLiveData;
    }

    public MutableLiveData<List<Message>> loadOlderMessages(String senderId, String receiverId, long oldestMessageTimestamp, int limit) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();
        Query messageRef = databaseReference
                .child("Conversations")
                .child(senderId)
                .child(receiverId)
                .child("Messages")
                .orderByChild("timestamp")
                .endAt(oldestMessageTimestamp - 1)
                .limitToLast(limit);

        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> olderMessages = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    olderMessages.add(message);
                }
                messagesLiveData.setValue(olderMessages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });

        return messagesLiveData;
    }

    public MutableLiveData<List<Message>> getMoreMessages(String senderId, String receiverId, long oldestMessageTimestamp, int limit) {
        MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();
        Query messageRef = databaseReference
                .child("Conversations")
                .child(senderId)
                .child(receiverId)
                .child("Messages")
                .orderByChild("timestamp")
                .endAt(oldestMessageTimestamp - 1)
                .limitToLast(limit);

        messageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> moreMessages = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Message message = dataSnapshot.getValue(Message.class);
                    moreMessages.add(message);
                }
                messagesLiveData.setValue(moreMessages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });

        return messagesLiveData;
    }

    public void sendTextMessage(Message message,boolean isChattingWithMe) {
        DatabaseReference conversationRef = databaseReference.child("Conversations");

        DatabaseReference senderRef = conversationRef.child(message.getSenderId()).child(message.getRecipientId()).child("Messages").push();
        DatabaseReference receiverRef = conversationRef.child(message.getRecipientId()).child(message.getSenderId()).child("Messages");
        message.setMessageId(senderRef.getKey());
        if(isChattingWithMe){
            message.setSeenTimeStamp(System.currentTimeMillis());
            message.setSeen(true);
        }
        senderRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    receiverRef.child(message.getMessageId()).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateLastMessage(message);
                                if (!isChattingWithMe) {
                                    incrementUnreadMessage(message.getSenderId(), message.getRecipientId());
                                }
                            }
                        }
                    });
                }
            }
        });

    }

    public void sendMediaMessage(Message message, Uri fileUri, boolean isChattingWithMe) {

        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference()
                .child("Conversations")
                .child(message.getSenderId())
                .child(message.getRecipientId())
                .child("Messages")
                .push();

        DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference()
                .child("Conversations")
                .child(message.getRecipientId())
                .child(message.getSenderId())
                .child("Messages");
        message.setMessageId(senderRef.getKey());
        if(isChattingWithMe){
            message.setSeenTimeStamp(System.currentTimeMillis());
            message.setSeen(true);
        }

        senderRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    receiverRef.child(message.getMessageId()).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateLastMessage(message);
                                setContentLink(message,fileUri, message.getMessageId());
                                if (!isChattingWithMe) {
                                    incrementUnreadMessage(message.getSenderId(), message.getRecipientId());
                                }

                            }
                        }
                    });
                }
            }
        });// Assign messageId from senderRef

    }

    public void deleteMessage(Message message,Message secondLastMessage,boolean isLastMessage){

        DatabaseReference senderRef = firebaseDatabase.getReference()
                .child("Conversations")
                .child(message.getSenderId())
                .child(message.getRecipientId())
                .child("Messages").child(message.getMessageId());

        DatabaseReference receiverRef = firebaseDatabase.getReference()
                .child("Conversations")
                .child(message.getRecipientId())
                .child(message.getSenderId())
                .child("Messages").child(message.getMessageId());



        senderRef.removeValue();
        receiverRef.removeValue();

        if(isLastMessage){
            if(secondLastMessage!=null){
                updateLastMessage(secondLastMessage);
            }else{
                DatabaseReference senderConvoRef = firebaseDatabase.getReference().child("Conversations").child(message.getSenderId()).child(message.getRecipientId());
                DatabaseReference receiverConvoRef = firebaseDatabase.getReference().child("Conversations").child(message.getRecipientId()).child(message.getSenderId());
                senderConvoRef.removeValue();
                receiverConvoRef.removeValue();
            }
        }

        if(!message.getMessageType().equals(MessageType.TEXT)){
            StorageReference storageRef = FirebaseStorage
                    .getInstance()
                    .getReference()
                    .child("media").child(message.getMessageId());
            storageRef.delete();
        }

    }

    private void setContentLink(Message message, Uri fileUri,String messageId) {
        DatabaseReference senderRef = FirebaseDatabase.getInstance().getReference()
                .child("Conversations")
                .child(message.getSenderId())
                .child(message.getRecipientId())
                .child("Messages").child(messageId);

        DatabaseReference receiverRef = FirebaseDatabase.getInstance().getReference()
                .child("Conversations")
                .child(message.getRecipientId())
                .child(message.getSenderId())
                .child("Messages").child(messageId);

        StorageReference storageRef = FirebaseStorage
                .getInstance()
                .getReference()
                .child("media");

        StorageReference fileRef = storageRef.child(message.getMessageId());
        UploadTask uploadTask = fileRef.putFile(fileUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        senderRef.child("content").setValue(uri.toString());
                        receiverRef.child("content").setValue(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        senderRef.removeValue();
                        receiverRef.removeValue();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                senderRef.removeValue();
                receiverRef.removeValue();
            }
        });

    }

    private void retryStoringReceiverMessage(Message message, DatabaseReference receiverRef) {
        // Implement retry logic for storing receiver message
    }

    private void retryStoringSenderMessage(Message message, DatabaseReference senderRef, DatabaseReference receiverRef, boolean isChattingWithMe) {
        // Implement retry logic for storing sender message
    }

    private void retryGettingMediaUrl(Message message, Uri fileUri, boolean isChattingWithMe) {
        // Implement retry logic for getting media URL
    }

    private void retryUploadingMediaFile(Message message, Uri fileUri, boolean isChattingWithMe) {
        // Implement retry logic for uploading media file
    }


    private void updateLastMessage(Message message) {
        DatabaseReference senderRef = databaseReference.child("Conversations")
                .child(message.getSenderId())
                .child(message.getRecipientId())
                .child("lastMessage");

        DatabaseReference receiverRef = databaseReference.child("Conversations")
                .child(message.getRecipientId())
                .child(message.getSenderId())
                .child("lastMessage");

        senderRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> senderTask) {
                if (senderTask.isSuccessful()) {
                    // Handle failure to update sender's last message
                }
            }
        });

        receiverRef.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> receiverTask) {
                if (receiverTask.isSuccessful()) {
                    // Handle failure to update receiver's last message
                }
            }
        });
    }

    private void incrementUnreadMessage(String senderId, String recipientId) {


        DatabaseReference unreadMessageRef = databaseReference.child("Conversations")
                .child(recipientId)
                .child(senderId)
                .child("unreadMessageCount");

        unreadMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer unreadCount = snapshot.getValue(Integer.class);
                if (unreadCount == null) {
                    unreadCount = 0;
                }
                snapshot.getRef().setValue(unreadCount + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    public void markAllMessagesAsSeen(String senderId, String receiverId,long seenTime) {
        DatabaseReference messageRef = databaseReference.child("Conversations").child(receiverId).child(senderId).child("Messages");

        // Query all unread messages
        Query unreadMessagesQuery = messageRef.orderByChild("seen").equalTo(false);

        // Mark all messages as seen
        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    unreadMessagesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Message message = dataSnapshot.getValue(Message.class);
                                // Mark message as seen and update seen time
                                assert message != null;
                                message.setSeen(true);
                                message.setSeenTimeStamp(seenTime);
                                dataSnapshot.getRef().setValue(message);
                            }
                            // Update unread message count to zero
                            databaseReference.child("Conversations").child(senderId).child(receiverId).child("unreadMessageCount").setValue(0);
                            databaseReference.child("Conversations").child(senderId).child(receiverId).child("lastMessage").child("seenTimeStamp").setValue(seenTime);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setOnlineStatus(String userId,boolean status){
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(userId).child("status");
        if(status){
            userRef.setValue(true);
        }else{
            userRef.removeValue();
        }

    }

    public MutableLiveData<Boolean> isOnline(String userId){
        MutableLiveData<Boolean> isOnlineLiveData = new MutableLiveData<>();
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(userId).child("status");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isOnline = snapshot.getValue(Boolean.class);
                if(isOnline==null){
                    isOnline = false;
                }
                isOnlineLiveData.setValue(isOnline);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
               isOnlineLiveData.setValue(false);
            }
        });
        return isOnlineLiveData;
    }

    public void setChattingWith(String userId, String receiverId,boolean isChatting){
        DatabaseReference senderChatRef = firebaseDatabase.getReference().child("Users").child(userId).child("chattingWith");

        if(isChatting){
            senderChatRef.setValue(receiverId);
        }else{
            senderChatRef.removeValue();
        }
    }

    public MutableLiveData<Boolean> isChattingWith(String userId, String receiverId) {
        MutableLiveData<Boolean> isChattingLiveData = new MutableLiveData<>();
        DatabaseReference chattingStatusRef = firebaseDatabase.getReference().child("Users").child(receiverId).child("chattingWith");
        chattingStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isChatting = null;
                String chattingWithUserId = snapshot.getValue(String.class);
                isChatting = chattingWithUserId != null && chattingWithUserId.equals(userId);
                isChattingLiveData.setValue(isChatting);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isChattingLiveData.setValue(false);
            }
        });
        return isChattingLiveData;
    }

    public void setUserTypingStatus(String userId, String typingToUserId, boolean isTyping) {
        DatabaseReference typingRef = firebaseDatabase.getReference().child("Users").child(userId).child("typingTo");

        if (isTyping) {
            typingRef.setValue(typingToUserId);
        } else {
            typingRef.removeValue();
        }
    }

    public MutableLiveData<Boolean> isUserTyping(String userId, String receiverId) {
        MutableLiveData<Boolean> isTypingLiveData = new MutableLiveData<>();
        DatabaseReference typingRef = firebaseDatabase.getReference().child("Users").child(receiverId).child("typingTo");
        typingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the user is typing to the specified receiver
                Boolean isTyping = null;
                String typingToUserId = snapshot.getValue(String.class);
                isTyping = typingToUserId != null && typingToUserId.equals(userId);

                isTypingLiveData.setValue(isTyping);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        return isTypingLiveData;
    }

    public MutableLiveData<String> getUserprofilePicture(String receiverId){
        MutableLiveData<String> profilePictureLiveData = new MutableLiveData<>();
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(receiverId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profilePicture = snapshot.child("userProfilePicture").getValue(String.class);
                profilePictureLiveData.setValue(profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return profilePictureLiveData;
    }

}
