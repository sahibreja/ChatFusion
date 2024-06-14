package com.reja.chatapp.Repository;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.reja.chatapp.Interface.CallBack.SentRoomRequestCallBack;
import com.reja.chatapp.Model.Friend;
import com.reja.chatapp.Model.Message;
import com.reja.chatapp.Model.MessageType;
import com.reja.chatapp.Model.Room;
import com.reja.chatapp.Model.RoomMessage;
import com.reja.chatapp.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RoomRepository {
    private Application application;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth auth;
    private String userId;
    private MutableLiveData<String> userActionLiveData;

    private MutableLiveData<String> roomIdLiveData;

    private ChatRepository chatRepository;

    private MutableLiveData<Boolean> userSentActionLiveData;

    public RoomRepository(Application application){
        this.application = application;
        chatRepository = new ChatRepository(application);
        firebaseDatabase = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        userActionLiveData = new MutableLiveData<>();
        roomIdLiveData = new MutableLiveData<>();
        userSentActionLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<String> getUserActionLiveData(){
        return userActionLiveData;
    }

    public MutableLiveData<User> getUserDetails(){
        MutableLiveData<User> mutableLiveData = new MutableLiveData<>();
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mutableLiveData.setValue(snapshot.getValue(User.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return mutableLiveData;
    }

    public void createRoom(String videoUrl){
        DatabaseReference roomRef = firebaseDatabase.getReference().child("Rooms").push();
        String roomId = roomRef.getKey();
        Room room = new Room();
        room.setRoomId(roomId);
        room.setAdminId(userId);
        room.setVideoUrl(videoUrl);
        room.setPlaying(false);  // Initial state
        room.setCurrentPosition(0);  // Initial position

        roomRef.setValue(room).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("RoomCreation", "Room created successfully");
                userActionLiveData.setValue(roomId);
            } else {
                userActionLiveData.setValue(null);
                Log.e("RoomCreation", "Failed to create room", task.getException());
            }
        });
    }

    public void joinRoom(String userId, String userName, String userProfilePicture, String roomId) {
        DatabaseReference roomRef = firebaseDatabase.getReference().child("Rooms").child(roomId);
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Room room = snapshot.getValue(Room.class);
                    if (room != null) {
                        User user = new User();
                        user.setUserId(userId);
                        user.setUserName(userName);
                        user.setUserProfilePicture(userProfilePicture);

                        Map<String, Object> userListUpdates = new HashMap<>();
                        userListUpdates.put("userList/" + userId, user);

                        roomRef.updateChildren(userListUpdates).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("JoinRoom", "User added to room successfully");
                                userActionLiveData.setValue(roomId);
                            } else {
                                userActionLiveData.setValue(null);
                                Log.e("JoinRoom", "Failed to add user to room", task.getException());
                            }
                        });
                    }
                } else {
                    Toast.makeText(application, "Room does not exist", Toast.LENGTH_SHORT).show();
                    userActionLiveData.setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("JoinRoom", "Database error", error.toException());
                userActionLiveData.setValue(null);
            }
        });
    }

    public void updatePlayState(String roomId, boolean isPlaying) {
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Rooms");
        databaseReference.child(roomId).child("playing").setValue(isPlaying);
    }

    public void updateCurrentPosition(String roomId, long position) {
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Rooms").child(roomId).child("currentPosition");
       databaseReference.setValue(position);
    }


    public MutableLiveData<Room> getRoomData(String roomId){
        MutableLiveData<Room> roomMutableLiveData = new MutableLiveData<>();
        DatabaseReference roomRef = firebaseDatabase.getReference().child("Rooms").child(roomId);
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Room room = snapshot.getValue(Room.class);
                    roomMutableLiveData.setValue(room);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return roomMutableLiveData;
    }
    public MutableLiveData<Boolean> isPlaying(String roomId){
        MutableLiveData<Boolean> playingMutableLiveData = new MutableLiveData<>();
        DatabaseReference playingRef = firebaseDatabase.getReference().child("Rooms").child(roomId).child("playing");
        playingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    playingMutableLiveData.setValue(snapshot.getValue(Boolean.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return playingMutableLiveData;
    }
    public MutableLiveData<String> getMediaUrl(String roomId){
        MutableLiveData<String> mediaUrlMutableLiveData = new MutableLiveData<>();
        DatabaseReference mediaUrlRef = firebaseDatabase.getReference().child("Rooms").child(roomId).child("videoUrl");
        mediaUrlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    mediaUrlMutableLiveData.setValue(snapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return mediaUrlMutableLiveData;
    }

    public MutableLiveData<Long> getCurrentPosition(String roomId){
        MutableLiveData<Long> currentPositionMutableLiveData = new MutableLiveData<>();
        DatabaseReference currentPositionRef = firebaseDatabase.getReference().child("Rooms").child(roomId).child("currentPosition");

        currentPositionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentPositionMutableLiveData.setValue(snapshot.getValue(Long.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return currentPositionMutableLiveData;
    }
    public void sendRoomMessage(RoomMessage roomMessage){
        DatabaseReference databaseReference = firebaseDatabase.getReference()
                .child("Rooms")
                .child(roomMessage.getRoomId())
                .child("messages").push();

        roomMessage.setMessageId(databaseReference.getKey());
        databaseReference.setValue(roomMessage);
    }

    public MutableLiveData<List<RoomMessage>> getRoomMessages(String roomId){
        MutableLiveData<List<RoomMessage>> roomMessagesMutableLiveData = new MutableLiveData<>();

        DatabaseReference roomMessagesRef = firebaseDatabase.getReference()
                .child("Rooms")
                .child(roomId)
                .child("messages");

        roomMessagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<RoomMessage> listOfRoomMessages = new ArrayList<>();
                for(DataSnapshot messageSnapshot : snapshot.getChildren()){
                    RoomMessage roomMessage = messageSnapshot.getValue(RoomMessage.class);
                    listOfRoomMessages.add(roomMessage);
                }
                roomMessagesMutableLiveData.setValue(listOfRoomMessages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return roomMessagesMutableLiveData;
    }

    public void updateMediaUrl(String roomId, String mediaUrl){
        DatabaseReference roomRef = firebaseDatabase.getReference().child("Rooms").child(roomId);
        roomRef.child("videoUrl").setValue(mediaUrl);
        roomRef.child("playing").setValue(false);
        roomRef.child("currentPosition").setValue(0);
    }

    public MutableLiveData<List<Friend>> getFriendList(String userId,String roomId) {
        MutableLiveData<List<Friend>> listMutableLiveData = new MutableLiveData<>();
        CompletableFuture<List<Friend>>  futureList = new CompletableFuture<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        DatabaseReference userRef = firebaseDatabase.getReference().child("Users").child(userId).child("Friends");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Friend> friendList = new ArrayList<>();
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendId = friendSnapshot.getKey();
                    Friend friend  = new Friend();
                    friend.setFriendId(friendId);
                    DatabaseReference friendRef = firebaseDatabase.getReference().child("Users").child(friendId);
                    CompletableFuture<Void> friendDetailFuture = new CompletableFuture<>();
                    friendRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userName = snapshot.child("userName").getValue(String.class);
                            String userProfilePicture = snapshot.child("userProfilePicture").getValue(String.class);
                            friend.setFriendName(userName);
                            friend.setFriendProfilePicture(userProfilePicture);
                            friendDetailFuture.complete(null);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle error if needed
                        }
                    });
                    futures.add(friendDetailFuture);

                    CompletableFuture<Void> requestSentFuture = new CompletableFuture<>();

                    checkIsSentRoomRequest(roomId, friendId, new SentRoomRequestCallBack() {
                        @Override
                        public void onSentRoomRequest(boolean isSent) {
                             friend.setSent(isSent);
                             requestSentFuture.complete(null);
                        }
                    });
                    futures.add(requestSentFuture);


                    friendList.add(friend);

                }
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allFutures.thenAccept(ignore -> {
                    futureList.complete(friendList);
                });

                listMutableLiveData.setValue(friendList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
                futureList.completeExceptionally(error.toException());
            }
        });
        futureList.thenAccept(searchResults -> listMutableLiveData.setValue(searchResults));
        return listMutableLiveData;
    }


    private void checkIsSentRoomRequest(String roomId,String friendId, final SentRoomRequestCallBack callback) {
        DatabaseReference roomRequestRef = firebaseDatabase.getReference().child("Rooms").child(roomId).child("RoomRequests").child(friendId);
        roomRequestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFriend = snapshot.exists();
                callback.onSentRoomRequest(isFriend);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onSentRoomRequest(false);
            }
        });
    }

    public void sendRoomRequest(String roomId,String senderId, String friendId){
        boolean isChattingWithMe = false;
        Message message = new Message(senderId,friendId,friendId,roomId,System.currentTimeMillis(),false,-1, MessageType.ROOM_REQUEST);
        chatRepository.sendTextMessage(message,false);
        DatabaseReference roomRequestRef = firebaseDatabase.getReference().child("Rooms").child(roomId).child("RoomRequests");
        roomRequestRef.child(friendId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    userSentActionLiveData.setValue(true);
                }else{
                    userSentActionLiveData.setValue(false);
                }
            }
        });
    }

    public MutableLiveData<Boolean> getUserSentActionLiveData(){
        return userSentActionLiveData;
    }

}
