package com.reja.chatapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.reja.chatapp.Adapter.RoomMessageAdapter;
import com.reja.chatapp.databinding.ActivityTestBinding;

import java.util.Random;

public class TestActivity extends AppCompatActivity {
    private ActivityTestBinding binding;
    private LinearLayoutManager layoutManager;
    private RoomMessageAdapter adapter;

    private ExoPlayer player;

    private  MediaItem mediaItem;

    private ImageButton fullscreenButton;
    private boolean isFullscreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        onButtonClick();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void init() {
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(layoutManager);

        //adapter = new RoomMessageAdapter(TestActivity.this);
        binding.recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        String videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4";
        String videoUri1 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4";
        String videoUri2 = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4";

        player = new ExoPlayer.Builder(this).build();
        binding.playerView.setPlayer(player);
        mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
        userInteractionWithPlayer();


    }

    private void userInteractionWithPlayer(){
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        // Player is ready, send state to Firebase
                        //sendPlaybackStateToFirebase("ready");
                        Toast.makeText(TestActivity.this, "Ready", Toast.LENGTH_SHORT).show();
                        break;
                    case Player.STATE_ENDED:
                        // Player ended, send state to Firebase
                        //sendPlaybackStateToFirebase("ended");
                        Toast.makeText(TestActivity.this, "Ended", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    // Player is playing, send state to Firebase
                    //sendPlaybackStateToFirebase("playing");
                    Toast.makeText(TestActivity.this, "Playing", Toast.LENGTH_SHORT).show();
                } else {
                    // Player is paused, send state to Firebase
                    //sendPlaybackStateToFirebase("paused");
                    Toast.makeText(TestActivity.this, "Pause", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPositionDiscontinuity(@NonNull Player.PositionInfo oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
                // Detect skip or seek
                switch (reason) {
                    case Player.DISCONTINUITY_REASON_SEEK:
                        //sendPlaybackStateToFirebase("seek");
                        Toast.makeText(TestActivity.this, "Seeking", Toast.LENGTH_SHORT).show();
                        break;
                    case Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT:
                        //sendPlaybackStateToFirebase("seek_adjustment");

                        break;
                    case Player.DISCONTINUITY_REASON_SKIP:
                        //sendPlaybackStateToFirebase("skip");
                        break;
                }
            }
        });
    }

    private void onButtonClick(){

    }





    public void sendInvite(String userToken) {
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(userToken + "@fcm.googleapis.com")
                .setMessageId(Integer.toString(new Random().nextInt(9999)))
                .addData("title", "Movie Watch Party")
                .addData("body", "Join me to watch a movie!")
                .build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}