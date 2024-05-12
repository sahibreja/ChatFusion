package com.reja.chatapp.UI.Chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.reja.chatapp.Adapter.ChatAdapter;
import com.reja.chatapp.Adapter.ChatListAdapter;
import com.reja.chatapp.Interface.Listener.OnLongClickListener;
import com.reja.chatapp.Interface.MediaPickListener;
import com.reja.chatapp.Model.Message;
import com.reja.chatapp.Model.MessageType;
import com.reja.chatapp.R;
import com.reja.chatapp.Session.ChatSessionManager;
import com.reja.chatapp.Utils.MediaPicker;
import com.reja.chatapp.ViewModel.AddFriendViewModel;
import com.reja.chatapp.ViewModel.ChatViewModel;
import com.reja.chatapp.databinding.ActivityChatBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity implements MediaPickListener {
    private ActivityChatBinding binding;
    private ChatAdapter adapter;
    private List<Message> messageList;
    private ChatViewModel chatViewModel;
    private final int PAGE_SIZE = 30;
    private String receiverId;
    private String senderId;
    private FirebaseAuth auth;
    private boolean isChattingWithMe = false;
    private MediaPicker mediaPicker;


    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2001;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1004;
    private static final int MIC_PERMISSION_REQUEST_CODE = 2003;
    private static final int MAX_IMAGE_COUNT = 10;
    private static final String TAG = "MainActivity";

    private boolean mStoragePermissionRequestedForImage = false;
    private boolean mStoragePermissionRequestedForVideo = false;
    private boolean mStoragePermissionRequestedForPdf = false;


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
        binding.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this,ProfileActivity.class));
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
            public void onLongClick(Message message,Message secondLastMessage,boolean isLastMessage) {
                chatViewModel.deleteMessage(message,secondLastMessage,isLastMessage);
            }
        });


    }



    private void init(){
        chatViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(ChatViewModel.class);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
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

    private void loadInitialMessages() {
        Toast.makeText(this, "initial Message", Toast.LENGTH_SHORT).show();
    }

    private void loadOlderMessages() {
        Toast.makeText(this, "Older Message", Toast.LENGTH_SHORT).show();
    }

    private void loadMoreMessages() {
        Toast.makeText(this, "Load more message", Toast.LENGTH_SHORT).show();
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
                if (checkStoragePermission()) {
                    mediaPicker.pickImages(MAX_IMAGE_COUNT);
                } else {
                    requestStoragePermission();
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
                if (checkStoragePermission()) {
                    mediaPicker.pickVideos();
                } else {
                    requestStoragePermission();
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




    private boolean checkStoragePermission() {
        // Check if the app has permission to read and write to external storage
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkCameraPermission() {
        // Check if the app has permission to access the camera
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        // Request read and write external storage permissions
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
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
        chatViewModel.setOnlineStatus(senderId,true);
        chatViewModel.setChattingWith(senderId,receiverId,true);
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

    @Override
    public void onStop() {
        super.onStop();
        // Update user's online status to false when the activity stops
        chatViewModel.setTypingStatus(senderId,receiverId,false);
        chatViewModel.setOnlineStatus(senderId,false);
        chatViewModel.setChattingWith(senderId,receiverId,false);
    }

}