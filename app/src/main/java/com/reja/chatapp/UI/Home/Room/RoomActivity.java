package com.reja.chatapp.UI.Home.Room;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.reja.chatapp.Adapter.RoomMessageAdapter;
import com.reja.chatapp.Adapter.RoomUserAdapter;
import com.reja.chatapp.Interface.Listener.OnInviteClickListener;
import com.reja.chatapp.Model.Friend;
import com.reja.chatapp.Model.Room;
import com.reja.chatapp.Model.RoomMessage;
import com.reja.chatapp.Model.User;
import com.reja.chatapp.R;
import com.reja.chatapp.ViewModel.RoomViewModel;
import com.reja.chatapp.databinding.ActivityRoomBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RoomActivity extends AppCompatActivity {
    private ActivityRoomBinding binding;
    private RoomViewModel viewModel;
    private ExoPlayer player;
    private String roomId;
    private String currentUrl;
    private RoomMessageAdapter messageAdapter;
    private User userData;
    private final String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    private LinearLayoutManager layoutManager;
    private enum Type {ADD}
    private Runnable updatePositionRunnable;
    private Handler handler;
    private Room roomData;

    private boolean isSeeking = false;

    private boolean isFullScreen = false;

    private GestureDetectorCompat gestureDetector;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getResources().getColor(R.color.purple));

        init();
        observeRoomData();
        onButtonClick();
    }

    private void init() {
        Intent intent = getIntent();
        roomId = intent.getStringExtra("room_id");
        viewModel = new ViewModelProvider(this).get(RoomViewModel.class);

        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);

        messageAdapter = new RoomMessageAdapter(this, new ArrayList<>());
        binding.recyclerView.setAdapter(messageAdapter);

        setUpPlayer();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onButtonClick() {
        binding.getRoomIdBtn.setOnClickListener(view -> {
            String textToCopy = roomId;

            // Get the clipboard system service
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            // Create a ClipData object with the text
            ClipData clip = ClipData.newPlainText("RoomId", roomId);

            // Set the clipboard's primary clip
            clipboard.setPrimaryClip(clip);

            // Show a toast message to inform the user
            Toast.makeText(RoomActivity.this, "Room id copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        binding.backBtn.setOnClickListener((view) -> {
            finish();
        });

        binding.exitRoomBtn.setOnClickListener((view) -> {
            // Handle exit room button click
        });

        binding.sendBtn.setOnClickListener((view) -> {
            String message = binding.txtEditTxt.getText().toString().trim();
            if (!message.isEmpty() && userData != null) {
                RoomMessage roomMessage = new RoomMessage(roomId, currentUserId, userData.getUserName(), userData.getUserProfilePicture(),
                        message, System.currentTimeMillis());
                viewModel.sendRoomMessage(roomMessage);
                binding.txtEditTxt.setText("");
            }
        });

        binding.addMovie.setOnClickListener((view) -> {
            showCustomPopupMenu();
        });

        binding.fullScreeBtn.setOnClickListener((view)->{
            toggleFullScreen();
        });

        binding.addPerson.setOnClickListener((view)->{
            if(binding.sendLayout.getVisibility() == View.VISIBLE){
                Animation slideOutDown= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_down);
                binding.sendLayout.startAnimation(slideOutDown);
                binding.sendLayout.setVisibility(View.GONE);

            }else{
                Animation slideInUp= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_in_up);
                binding.sendLayout.startAnimation(slideInUp);
                binding.sendLayout.setVisibility(View.VISIBLE);
            }
        });
        addPersonFunctionality();

    }

    private void addPersonFunctionality(){
        View view = findViewById(R.id.sendLayout1);

        RelativeLayout dropDownButton = view.findViewById(R.id.drop_out_down);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager1);
        RoomUserAdapter userAdapter = new RoomUserAdapter(this,new ArrayList<>());
        recyclerView.setAdapter(userAdapter);

        viewModel.getFriendList(currentUserId,roomId).observe(this, new Observer<List<Friend>>() {
            @Override
            public void onChanged(List<Friend> friends) {
                if(friends!=null){
                    userAdapter.setFriendList(friends);
                }
            }
        });
        viewModel.getUserSentActionLiveData().observe(this, new Observer<Boolean>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    userAdapter.notifyDataSetChanged();
                }
            }
        });

        userAdapter.setOnInviteClickListener(new OnInviteClickListener() {
            @Override
            public void onInviteClick(String friendId) {
                viewModel.sendRoomRequest(roomId,currentUserId,friendId);
            }
        });

        dropDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.sendLayout.getVisibility() == View.VISIBLE){
                    Animation slideOutDown= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.slide_out_down);
                    binding.sendLayout.startAnimation(slideOutDown);
                    binding.sendLayout.setVisibility(View.GONE);

                }
            }
        });
    }


    @SuppressLint("SourceLockedOrientationActivity")
    private void toggleFullScreen() {
        if (isFullScreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            binding.playerView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, 240));
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            binding.playerView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        }
        isFullScreen = !isFullScreen;
    }

    private void observeRoomData() {
        viewModel.getRoomData(roomId).observe(this, new Observer<Room>() {
            @Override
            public void onChanged(Room room) {
                roomData = room;
            }
        });

        viewModel.getMediaUrl(roomId).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (currentUrl == null || !currentUrl.equals(s)) {
                    currentUrl = s;
                    playMovie(currentUrl);
                }
            }
        });

        viewModel.isPlaying(roomId).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPlaying) {
                    if (player.isPlaying() != isPlaying) {
                        if (isPlaying) {
                            player.play();
                        } else {
                            player.pause();
                        }
                    }
            }
        });

        viewModel.getCurrentPosition(roomId).observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long position) {
                if (!isSeeking && Math.abs(player.getCurrentPosition() - position) > 2000) {
                    isSeeking = true;
                    player.seekTo(position);
                    isSeeking = false;
                }
            }
        });

        viewModel.getUserDetails().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                userData = user;
            }
        });

        viewModel.getRoomMessages(roomId).observe(this, new Observer<List<RoomMessage>>() {
            @Override
            public void onChanged(List<RoomMessage> roomMessages) {
                if (roomMessages != null) {
                    messageAdapter.setRoomMessages(roomMessages);
                    binding.recyclerView.scrollToPosition(roomMessages.size() - 1);
                }
            }
        });
    }

    private void setUpPlayer() {
        // Initialize ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);

        handler = new Handler(Looper.getMainLooper());
        updatePositionRunnable = new Runnable() {
            @Override
            public void run() {
                if (player.isPlaying() ) {
                    long currentPosition = player.getCurrentPosition();
                    viewModel.updateCurrentPosition(roomId, currentPosition);
                    handler.postDelayed(this, 500);
                }
            }
        };

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                viewModel.updatePlayState(roomId, isPlaying);

                if(roomData != null && roomData.getAdminId().equals(currentUserId)){
                        if (isPlaying && !isSeeking) {
                            handler.post(updatePositionRunnable);
                        } else {
                            handler.removeCallbacks(updatePositionRunnable);
                        }
                    }
            }

            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
                if(!isSeeking){
                    isSeeking = true;
                    viewModel.updateCurrentPosition(roomId, player.getCurrentPosition());
                    isSeeking = false;
                }

            }
        });
    }

    private void playMovie(String movieUrl) {
        if (movieUrl != null && !movieUrl.isEmpty()) {
            MediaItem mediaItem = MediaItem.fromUri(movieUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
        } else {
            Toast.makeText(this, "Invalid movie URL", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCustomPopupMenu() {
        // Inflate the custom layout/view
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.update_layout, null);

        // Create the PopupWindow
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // Allows tapping outside the popup to dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // Set up the buttons inside the popup
        Button updateButton = popupView.findViewById(R.id.btn_update);
        Button cancelButton = popupView.findViewById(R.id.btn_cancel);
        TextView titleTextView = popupView.findViewById(R.id.popup_title);
        EditText editText = popupView.findViewById(R.id.edit_query);
        setTitleAndHint(Type.ADD, editText, titleTextView, updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString().trim();
                if (Type.ADD == Type.ADD) {
                    if (!text.isEmpty()) {
                        viewModel.updateMediaUrl(roomId, text);
                    }
                }
                popupWindow.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        // Show the PopupWindow anchored to a view
        View anchorView = findViewById(R.id.main); // Replace with an actual view ID to anchor the popup
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0); // Show at center of the anchor view
    }

    private void setTitleAndHint(Type type, EditText editText, TextView titleTextView, Button updateButton) {
        if (type == Type.ADD) {
            editText.setHint("Enter URL of movie you want to watch");
            updateButton.setText("ADD");
            titleTextView.setText("Add Media Link");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
}
