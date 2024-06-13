package com.reja.chatapp.UI.Chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.reja.chatapp.R;
import com.reja.chatapp.Utils.DataModel;
import com.reja.chatapp.Utils.DataModelType;
import com.reja.chatapp.Utils.ErrorCallBack;
import com.reja.chatapp.Utils.Listener;
import com.reja.chatapp.Utils.NewEventCallBack;
import com.reja.chatapp.Utils.SuccessCallBack;
import com.reja.chatapp.databinding.ActivityCallBinding;
import com.reja.chatapp.remote.MainRepository;

import org.webrtc.SurfaceViewRenderer;

public class CallActivity extends AppCompatActivity  implements MainRepository.Listener{
    private ActivityCallBinding views;
    private MainRepository mainRepository;
    private Boolean isCameraMuted = false;
    private Boolean isMicrophoneMuted = false;
    private float dX, dY;
    private int lastAction;
    private boolean isAcceptCall;
    private String senderId;
    private String receiverId;
    private String senderName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());

        init();
        onButtonClick();


    }

    @SuppressLint("ClickableViewAccessibility")
    private void onButtonClick() {
        views.localView.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dX = view.getX() - event.getRawX();
                    dY = view.getY() - event.getRawY();
                    lastAction = MotionEvent.ACTION_DOWN;
                    break;

                case MotionEvent.ACTION_MOVE:
                    view.setX(event.getRawX() + dX);
                    view.setY(event.getRawY() + dY);
                    lastAction = MotionEvent.ACTION_MOVE;
                    break;

                case MotionEvent.ACTION_UP:
                    if (lastAction == MotionEvent.ACTION_DOWN) {
                        // Handle click event if needed
                    }
                    break;

                default:
                    return false;
            }
            return true;
        });
    }


    private void init(){
        Intent intent = getIntent();
        senderId = intent.getStringExtra("senderId");
        senderName = intent.getStringExtra("senderName");
        receiverId = intent.getStringExtra("receiverId");
        isAcceptCall = intent.getBooleanExtra("isAcceptCall",false);
        mainRepository = MainRepository.getInstance();
        mainRepository.initWebRtc(senderId, senderName, this, new SuccessCallBack() {
            @Override
            public void onSuccess() {

            }
        });
        mainRepository.listener = this;
        mainRepository.initLocalView(views.localView);
        mainRepository.initRemoteView(views.remoteView);

        if(isAcceptCall){
            mainRepository.startCall(receiverId);
        }else{
            mainRepository.sendCallRequest(receiverId,()->{
                Toast.makeText(this, "couldnt find the target", Toast.LENGTH_SHORT).show();
            });
        }

        views.switchCameraButton.setOnClickListener(v->{
            mainRepository.switchCamera();
        });

        views.micButton.setOnClickListener(v->{
            if (!isMicrophoneMuted){
                views.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
            }else {
                views.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
            }
            mainRepository.toggleAudio(isMicrophoneMuted);
            isMicrophoneMuted=!isMicrophoneMuted;
        });

        views.videoButton.setOnClickListener(v->{
            if (!isCameraMuted){
                views.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24);
            }else {
                views.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24);
            }
            mainRepository.toggleVideo(isCameraMuted);
            isCameraMuted=!isCameraMuted;
        });

        views.endCallButton.setOnClickListener(v->{
            mainRepository.endCall();
            finish();
        });
    }


    @Override
    public void webrtcConnected() {
        runOnUiThread(()->{
            Log.d("CallActivity","webrtcConnected");
        });
    }

    @Override
    public void webrtcClosed() {

        runOnUiThread(()->{
            mainRepository.deleteCallRequest(senderId,receiverId);
            finish();
        });
    }


}
