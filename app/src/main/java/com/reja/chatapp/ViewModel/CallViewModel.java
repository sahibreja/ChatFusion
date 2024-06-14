package com.reja.chatapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class CallViewModel extends AndroidViewModel {
    private final MutableLiveData<String> connectionStatus = new MutableLiveData<>();
    private final MutableLiveData<MediaStream> localVideoStream = new MutableLiveData<>();
    private final MutableLiveData<MediaStream> remoteVideoStream = new MutableLiveData<>();

    private DatabaseReference databaseReference;
    private PeerConnection peerConnection;
    private MediaStream localStream;
    private MediaStream remoteStream;
    private PeerConnectionFactory peerConnectionFactory;

    public CallViewModel(@NonNull Application application) {
        super(application);
        setupFirebase();
        setupWebRTC();
    }

    private void setupFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void setupWebRTC() {
        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(getApplication())
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();

        EglBase eglBase = EglBase.create();
        VideoCapturer videoCapturer = createVideoCapturer();
        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
        SurfaceTextureHelper surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
        videoCapturer.initialize(surfaceTextureHelper, getApplication(), videoSource.getCapturerObserver());
        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);

        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        AudioTrack audioTrack = peerConnectionFactory.createAudioTrack("101", audioSource);

        localStream = peerConnectionFactory.createLocalMediaStream("localStream");
        localStream.addTrack(videoTrack);
        localStream.addTrack(audioTrack);

        localVideoStream.postValue(localStream);

        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {

            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

            }

            @Override
            public void onIceConnectionReceivingChange(boolean b) {

            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

            }

            @Override
            public void onIceCandidate(IceCandidate candidate) {
                // Send ICE candidate to remote peer via Firebase
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {

            }

            @Override
            public void onAddStream(MediaStream stream) {
                remoteStream = stream;
                remoteVideoStream.postValue(stream);
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {

            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {

            }

            @Override
            public void onRenegotiationNeeded() {

            }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

            }

            // Implement other PeerConnection.Observer methods
        });

        peerConnection.addStream(localStream);
    }

    private VideoCapturer createVideoCapturer() {
        CameraEnumerator enumerator;
        if (Camera2Enumerator.isSupported(getApplication())) {
            enumerator = new Camera2Enumerator(getApplication());
        } else {
            enumerator = new Camera1Enumerator(true);
        }
        final String[] deviceNames = enumerator.getDeviceNames();

        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    public void initiateCall(String remoteUserId) {
        // Create offer and send to remote user via Firebase
    }

    public void answerCall(String remoteUserId) {
        // Create answer and send to remote user via Firebase
    }

    public LiveData<String> getConnectionStatus() {
        return connectionStatus;
    }

    public LiveData<MediaStream> getLocalVideoStream() {
        return localVideoStream;
    }

    public LiveData<MediaStream> getRemoteVideoStream() {
        return remoteVideoStream;
    }
}

