package com.reja.chatapp.remote;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.reja.chatapp.Utils.DataModel;
import com.reja.chatapp.Utils.DataModelType;
import com.reja.chatapp.Utils.ErrorCallBack;
import com.reja.chatapp.Utils.NewEventCallBack;
import com.reja.chatapp.Utils.SuccessCallBack;
import com.reja.chatapp.WebRTC.MyPeerConnectionObserver;
import com.reja.chatapp.WebRTC.WebRTCClient;
import com.reja.chatapp.remote.FirebaseClient;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

public class MainRepository implements WebRTCClient.Listener {

    public Listener listener;
    private final Gson gson = new Gson();
    private final FirebaseClient firebaseClient;

    private WebRTCClient webRTCClient;
    private String senderId;
    private String senderName;

    private SurfaceViewRenderer remoteView;

    private String target;
    private void updateCurrentUserData(String senderId,String senderName){
        this.senderId = senderId;
        this.senderName = senderName;
    }

    private MainRepository(){
        this.firebaseClient = new FirebaseClient();
    }

    private static MainRepository instance;
    public static MainRepository getInstance(){
        if (instance == null){
            instance = new MainRepository();
        }
        return instance;
    }

    public void initWebRtc(String senderId,String senderName, Context context, SuccessCallBack callBack){
        updateCurrentUserData(senderId,senderName);
        this.webRTCClient = new WebRTCClient(context,new MyPeerConnectionObserver(){
            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                try{
                    mediaStream.videoTracks.get(0).addSink(remoteView);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
                Log.d("TAG", "onConnectionChange: "+newState);
                super.onConnectionChange(newState);
                if (newState == PeerConnection.PeerConnectionState.CONNECTED && listener!=null){
                    listener.webrtcConnected();
                }

                if (newState == PeerConnection.PeerConnectionState.CLOSED ||
                        newState == PeerConnection.PeerConnectionState.DISCONNECTED ){
                    if (listener!=null){
                        listener.webrtcClosed();
                    }
                }
            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                webRTCClient.sendIceCandidate(iceCandidate,target);
            }
        },senderId,senderName);
        webRTCClient.listener = this;
        callBack.onSuccess();
    }

    public void initLocalView(SurfaceViewRenderer view){
        webRTCClient.initLocalSurfaceView(view);
    }

    public void initRemoteView(SurfaceViewRenderer view){
        webRTCClient.initRemoteSurfaceView(view);
        this.remoteView = view;
    }

    public void startCall(String target){
        webRTCClient.call(target);
    }

    public void switchCamera() {
        webRTCClient.switchCamera();
    }

    public void toggleAudio(Boolean shouldBeMuted){
        webRTCClient.toggleAudio(shouldBeMuted);
    }
    public void toggleVideo(Boolean shouldBeMuted){
        webRTCClient.toggleVideo(shouldBeMuted);
    }
    public void sendCallRequest(String receiverId, ErrorCallBack errorCallBack){
        firebaseClient.sendMessageToOtherUser(
                new DataModel(senderId,senderName,receiverId,null, DataModelType.StartCall),errorCallBack
        );
    }

    public void endCall(){
        webRTCClient.closeConnection();
    }

    public void deleteCallRequest(String senderId, String receiverId){
        firebaseClient.deleteLatestEvent(senderId,receiverId);
    }

    public void subscribeForLatestEvent(String senderId,NewEventCallBack callBack){
        firebaseClient.observeIncomingLatestEvent(senderId, new NewEventCallBack() {
            @Override
            public void onNewEventReceived(DataModel model) {
                switch (model.getType()){

                    case Offer:
                        target = model.getSenderId();
                        webRTCClient.onRemoteSessionReceived(new SessionDescription(
                                SessionDescription.Type.OFFER,model.getData()
                        ));
                        webRTCClient.answer(model.getSenderId());
                        break;
                    case Answer:
                        target = model.getSenderId();
                        webRTCClient.onRemoteSessionReceived(new SessionDescription(
                                SessionDescription.Type.ANSWER,model.getData()
                        ));
                        break;
                    case IceCandidate:
                        try{
                            IceCandidate candidate = gson.fromJson(model.getData(),IceCandidate.class);
                            webRTCClient.addIceCandidate(candidate);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    case StartCall:
                        target = model.getSenderId();
                        callBack.onNewEventReceived(model);
                        break;
                }
            }
        });

    }

    @Override
    public void onTransferDataToOtherPeer(DataModel model) {
        firebaseClient.sendMessageToOtherUser(model,()->{});
    }

    public interface Listener{
        void webrtcConnected();
        void webrtcClosed();
    }
}
