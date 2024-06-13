package com.reja.chatapp.UI.Chat;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.C;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.permissionx.guolindev.PermissionX;
import com.reja.chatapp.Adapter.ChatAdapter;
import com.reja.chatapp.Adapter.ChatListAdapter;
import com.reja.chatapp.Interface.Listener.OnInviteAcceptClickListener;
import com.reja.chatapp.Interface.Listener.OnLongClickListener;
import com.reja.chatapp.Interface.MediaPickListener;
import com.reja.chatapp.Model.Message;
import com.reja.chatapp.Model.MessageType;
import com.reja.chatapp.Model.User;
import com.reja.chatapp.R;
import com.reja.chatapp.Session.ChatSessionManager;
import com.reja.chatapp.TestActivity;
import com.reja.chatapp.UI.Home.AddFriend.AddFriendActivity;
import com.reja.chatapp.UI.Home.AddFriend.ShowFriendActivity;
import com.reja.chatapp.UI.Home.Room.CreateRoomActivity;
import com.reja.chatapp.UI.Home.Room.RoomActivity;
import com.reja.chatapp.Utils.DataModel;
import com.reja.chatapp.Utils.DataModelType;
import com.reja.chatapp.Utils.ErrorCallBack;
import com.reja.chatapp.Utils.Listener;
import com.reja.chatapp.Utils.MediaPicker;
import com.reja.chatapp.Utils.NewEventCallBack;
import com.reja.chatapp.Utils.SuccessCallBack;
import com.reja.chatapp.ViewModel.AddFriendViewModel;
import com.reja.chatapp.ViewModel.ChatViewModel;
import com.reja.chatapp.databinding.ActivityChatBinding;
import com.reja.chatapp.databinding.IncomingCallLayoutBinding;
import com.reja.chatapp.remote.MainRepository;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ChatActivity extends AppCompatActivity  implements MediaPickListener{
    private ActivityChatBinding binding;
    private ChatAdapter adapter;
    private LinearLayoutManager layoutManager;
    private List<Message> messageList;
    private ChatViewModel chatViewModel;
    private final int PAGE_SIZE = 30;
    private String receiverId;
    private String senderId;
    private String receiverDeviceToken;
    private String senderName = "User1";
    private FirebaseAuth auth;
    private boolean isChattingWithMe = false;
    private MediaPicker mediaPicker;
    private IncomingCallLayoutBinding incomingCallLayoutBinding;

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1004;
    private static final int MIC_PERMISSION_REQUEST_CODE = 2003;
    private static final int MAX_IMAGE_COUNT = 10;
    private static final String TAG = "ChatActivity";

    private boolean mStoragePermissionRequestedForImage = false;
    private boolean mStoragePermissionRequestedForVideo = false;
    private boolean mStoragePermissionRequestedForPdf = false;

    private MainRepository mainRepository;

    private User currentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        observer();
        onButtonClick();

    }

    private void onButtonClick(){

        binding.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        binding.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this,ProfileActivity.class);
                intent.putExtra("userId",receiverId);
                startActivity(intent);
            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String txtMessage = binding.txtEditTxt.getText().toString().trim();
               if(!txtMessage.isEmpty()){
                  Message message = new Message(senderId,receiverId,receiverId,txtMessage,System.currentTimeMillis(),false,-1,MessageType.TEXT);
                  chatViewModel.sendTextMessage(message,isChattingWithMe);
                  binding.txtEditTxt.setText("");
               }
            }
        });

        binding.txtEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                 if(charSequence.length() > 0){
                     chatViewModel.setTypingStatus(senderId,receiverId,true);
                 }else{
                     chatViewModel.setTypingStatus(senderId,receiverId,false);
                 }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.txtEditTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                } else {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }
            }
        });

        binding.attachmentBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                }
            }
        });

        binding.attachmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ChatActivity.this);
                bottomSheetDialog.setContentView(R.layout.attachment_bottom_sheet_layout);
                handleBottomSheetLayout(bottomSheetDialog);
                bottomSheetDialog.show();
            }
        });

        adapter.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public void onLongClick(View view,Message message,Message secondLastMessage,boolean isLastMessage) {
                 showPopupMenu(view);
                //chatViewModel.deleteMessage(message,secondLastMessage,isLastMessage);
            }
        });

        binding.videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionX.init(ChatActivity.this)
                        .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                        .request((allGranted, grantedList, deniedList) -> {
                            if(allGranted){
                                if(receiverDeviceToken!=null){
                                    sendInvite(receiverDeviceToken);
                                }
                                Intent intent = new Intent(ChatActivity.this,CallActivity.class);
                                intent.putExtra("senderId",senderId);
                                intent.putExtra("senderName",currentUser.getUserName());
                                intent.putExtra("receiverId",receiverId);
                                intent.putExtra("isAcceptCall",false);
                                intent.putExtra("isAudioCall",false);
                                startActivity(intent);
                            }
                        });
            }
        });

        binding.audioCall.setOnClickListener((view) ->{
            PermissionX.init(ChatActivity.this)
                    .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                    .request((allGranted, grantedList, deniedList) -> {
                        if(allGranted){
                            if(receiverDeviceToken!=null){
                                sendInvite(receiverDeviceToken);
                            }
                            Intent intent = new Intent(ChatActivity.this,CallActivity.class);
                            intent.putExtra("senderId",senderId);
                            intent.putExtra("senderName",currentUser.getUserName());
                            intent.putExtra("receiverId",receiverId);
                            intent.putExtra("isAcceptCall",false);
                            intent.putExtra("isAudioCall",true);

                            startActivity(intent);
                        }
                    });
        });

        adapter.setOnInviteAcceptClickListener(new OnInviteAcceptClickListener() {
            @Override
            public void onInviteAcceptClick(String roomId) {
                if(currentUser!=null){
                    chatViewModel.joinRoom(senderId,currentUser.getUserName(),currentUser.getUserProfilePicture(),roomId);
                }
            }
        });

    }

    public void showPopupMenu(View view) {
        // Create the PopupMenu instance
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.chat_popup_menu, popupMenu.getMenu());
        popupMenu.setForceShowIcon(true);

        // Show the popup menu
        popupMenu.show();
    }



    private void loadOlderMessages() {
        showLoading();
        chatViewModel.loadOlderMessages(senderId,receiverId,messageList.get(0).getTimestamp(),5);
    }

    private void loadNewerMessages() {
        showLoading();
        chatViewModel.loadNewerMessages(senderId,receiverId,messageList.get(messageList.size()-1).getTimestamp(),5);
    }


    private void init(){

        incomingCallLayoutBinding = IncomingCallLayoutBinding.bind(binding.incomingCallLayout);
        chatViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(ChatViewModel.class);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        showLoading();
        auth= FirebaseAuth.getInstance();
        senderId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        Intent intent = getIntent();
        receiverId = intent.getStringExtra("userId");
        long seenTime = System.currentTimeMillis();
        mediaPicker = new MediaPicker(this, this);
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(ChatActivity.this,messageList);
        binding.recyclerView.setAdapter(adapter);
        mainRepository =  MainRepository.getInstance();

    }

    private void observer(){
        chatViewModel.getListOfMessage(senderId,receiverId).observe(this, new Observer<List<Message>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<Message> messages) {
                closeLoading();
                messageList.clear();
                if(!messages.isEmpty()){
                    closeNoResultFound();
                    messageList.addAll(messages);
                }else{
                    showNoResultFound();
                }
                adapter.notifyDataSetChanged();
                binding.recyclerView.scrollToPosition(messageList.size()-1);
            }
        });



        chatViewModel.getCombinedStatus(senderId, receiverId).observe(this, pair -> {
            boolean isTyping = pair.first;
            boolean isOnline = pair.second;
            setUserStatus(isTyping, isOnline);

        });

        chatViewModel.isChattingWith(senderId,receiverId).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                isChattingWithMe = aBoolean;
            }
        });

        chatViewModel.getUserprofilePicture(receiverId).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String profileImage) {
                if (profileImage != null && !profileImage.isEmpty()) {
                    Glide.with(ChatActivity.this).load(profileImage).into(binding.profile);
                } else {
                    binding.profile.setImageResource(R.drawable.account);
                }
            }
        });

        chatViewModel.getReceiverDeviceToken(receiverId).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                receiverDeviceToken = s;
            }
        });

        mainRepository.subscribeForLatestEvent(senderId,new NewEventCallBack() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onNewEventReceived(DataModel data) {
                if (data.getType()== DataModelType.StartCall){
                    runOnUiThread(()->{
                        String firstName = data.getSenderName().split(" ")[0];
                        incomingCallLayoutBinding.userName.setText(firstName+" is Calling you");
                        binding.incomingCallLayout.setVisibility(View.VISIBLE);
                        incomingCallLayoutBinding.acceptCallBtn.setOnClickListener(v->{
                            PermissionX.init(ChatActivity.this)
                                    .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                                    .request((allGranted, grantedList, deniedList) -> {
                                        if(allGranted){
                                            if (data != null && data.getSenderId() != null) {
                                                Log.d(TAG, "Starting call with sender: " + data.getSenderName());
                                                Intent intent = new Intent(ChatActivity.this,CallActivity.class);
                                                intent.putExtra("senderId",senderId);
                                                intent.putExtra("senderName",data.getSenderName());
                                                intent.putExtra("receiverId",data.getSenderId());
                                                intent.putExtra("isAcceptCall",true);
                                                startActivity(intent);
                                                binding.incomingCallLayout.setVisibility(View.GONE);
                                            } else {
                                                Log.e(TAG, "Data or sender is null. Cannot start call.");

                                                Toast.makeText(ChatActivity.this, "Error: Unable to start call. Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        });
                        incomingCallLayoutBinding.rejectCallBtn.setOnClickListener(v->{
                            binding.incomingCallLayout.setVisibility(View.GONE);
                            //mainRepository.deleteCallRequest(senderId,data.getSenderId());
                        });
                    });
                }
            }
        });

        chatViewModel.getUserDetails().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                currentUser = user;
            }
        });

        chatViewModel.getUserActionLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s!=null){
                    Intent intent = new Intent(ChatActivity.this, RoomActivity.class);
                    intent.putExtra("room_id",s);
                    startActivity(intent);
                }
            }
        });


    }

    private void showNoResultFound(){
        View view = binding.noResultLayout;
        RelativeLayout mainLayout = view.findViewById(R.id.mainLayout);
        ImageView icon = view.findViewById(R.id.search_icon);
        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);

        mainLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        icon.setImageResource(R.drawable.chat_bubble);
        title.setText(R.string.no_messages_yet);
        description.setText(R.string.no_messages_in_your_inbox_yet_start_a_new_conversation);

        binding.noResultLayout.setVisibility(View.VISIBLE);
    }
    private void closeNoResultFound(){
        binding.noResultLayout.setVisibility(View.GONE);
    }
    private void showLoading(){
        binding.loadingLayout.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.INVISIBLE);
        closeNoResultFound();
    }
    private void closeLoading(){
        binding.loadingLayout.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
    }
    @SuppressLint("SetTextI18n")
    private void setUserStatus(boolean isTyping,boolean isOnline){
        if(isTyping){
            binding.userStatus.setVisibility(View.VISIBLE);
            binding.userStatus.setText("Typing...");
        }else if(isOnline){
            binding.userStatus.setVisibility(View.VISIBLE);
            binding.userStatus.setText("Online");
        }else{
            binding.userStatus.setVisibility(View.GONE);
        }

    }

    private void handleBottomSheetLayout(BottomSheetDialog bottomSheetDialog){

        LinearLayout imageBtn = bottomSheetDialog.findViewById(R.id.image);
        LinearLayout videoBtn = bottomSheetDialog.findViewById(R.id.video);
        LinearLayout cameraBtn = bottomSheetDialog.findViewById(R.id.camera);
        LinearLayout pdfBtn = bottomSheetDialog.findViewById(R.id.pdf);

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                mStoragePermissionRequestedForImage = true;
                mStoragePermissionRequestedForVideo = false;
                mStoragePermissionRequestedForPdf = false;
                if (checkImagePermission()) {
                    mediaPicker.pickImages(MAX_IMAGE_COUNT);
                } else {
                    requestImagePermission();
                }
            }
        });

        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                mStoragePermissionRequestedForImage = false;
                mStoragePermissionRequestedForVideo = true;
                mStoragePermissionRequestedForPdf = false;
                if (checkVideoPermission()) {
                    mediaPicker.pickVideos();
                } else {
                    requestVideoPermission();
                }
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                mStoragePermissionRequestedForImage = false;
                mStoragePermissionRequestedForVideo = false;
                mStoragePermissionRequestedForPdf = false;
                if (checkCameraPermission()) {
                    // Start camera activity
                    mediaPicker.pickCamera();
                } else {
                    requestCameraPermission();
                }
            }
        });

        pdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                mStoragePermissionRequestedForImage = false;
                mStoragePermissionRequestedForVideo = false;
                mStoragePermissionRequestedForPdf = true;
                if (checkStoragePermission()) {
                    mediaPicker.pickPdf();
                } else {
                    requestStoragePermission();
                }
            }
        });
    }

    private boolean checkImagePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        }else{
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestImagePermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_REQUEST_CODE);
        }else{
            requestStoragePermission();
        }
    }

    private void requestVideoPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_VIDEO}, STORAGE_PERMISSION_REQUEST_CODE);
        }else{
            requestStoragePermission();
        }
    }

    private boolean checkVideoPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        }else{
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private boolean checkStoragePermission() {
        // Check if the app has permission to read and write to external storage
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ;
        }
        return true;

    }

    private boolean checkCameraPermission() {
        // Check if the app has permission to access the camera
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        // Request read and write external storage permissions
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }

    }

    private void requestCameraPermission() {
        // Request camera permission
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with media selection
                handleStoragePermissionGranted();
            } else {
                // Permission denied for storage, handle accordingly
                // Optionally, you may check if permission should be requested again using shouldShowRequestPermissionRationale() method
                Toast.makeText(this, "Permission have denied please give permission", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start camera activity
                handleCameraPermissionGranted();
            } else {
                Toast.makeText(this, "Permission have denied please give permission", Toast.LENGTH_SHORT).show();
                // Permission denied for camera, handle accordingly
                // Optionally, you may check if permission should be requested again using shouldShowRequestPermissionRationale() method
            }
        }
    }


    private void handleStoragePermissionGranted() {
        // Determine which button was clicked before requesting permission
        // and handle media selection accordingly
        if (mStoragePermissionRequestedForImage) {
            mediaPicker.pickImages(MAX_IMAGE_COUNT);
        } else if (mStoragePermissionRequestedForVideo) {
            mediaPicker.pickVideos();
        } else if (mStoragePermissionRequestedForPdf) {
            mediaPicker.pickPdf();
        }
    }

    private void handleCameraPermissionGranted() {
        // Start camera activity
        mediaPicker.pickCamera();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            mediaPicker.handleMediaPickerResult(requestCode, resultCode, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onMediaPicked(List<Uri> selectedMediaUris) {
        if (selectedMediaUris.size() > MAX_IMAGE_COUNT) {
            Toast.makeText(this, "You can select up to " + MAX_IMAGE_COUNT + " media files at a time", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!selectedMediaUris.isEmpty()){
            Intent intent = new Intent(ChatActivity.this,SendMediaActivity.class);
            intent.putParcelableArrayListExtra("selectedMediaUris", (ArrayList<Uri>) selectedMediaUris);
            intent.putExtra("receiverId",receiverId);
            intent.putExtra("senderId",senderId);
            startActivity(intent);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        chatViewModel.markAllMessagesAsSeen(senderId, receiverId, System.currentTimeMillis());
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatViewModel.setTypingStatus(senderId,receiverId,false);
        chatViewModel.setOnlineStatus(senderId,false);
        chatViewModel.setChattingWith(senderId,receiverId,false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatViewModel.setOnlineStatus(senderId,true);
        chatViewModel.setChattingWith(senderId,receiverId,true);
    }

    public void sendInvite(String userToken) {
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(userToken + "@fcm.googleapis.com")
                .setMessageId(Integer.toString(new Random().nextInt(9999)))
                .addData("title", "Movie Watch Party")
                .addData("body", "Join me to watch a movie!")
                .build());
    }

}