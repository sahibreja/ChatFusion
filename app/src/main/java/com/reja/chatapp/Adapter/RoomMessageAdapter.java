package com.reja.chatapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.reja.chatapp.Model.RoomMessage;
import com.reja.chatapp.R;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomMessageAdapter extends RecyclerView.Adapter {
    private Context context;
    private String currentUserId= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    private List<RoomMessage> roomMessages;
    private final int INCOMING_MSG = 1;
    private final int OUTGOING_MSG = 2;

    public RoomMessageAdapter(Context context, List<RoomMessage> roomMessages) {
        this.context = context;
        this.roomMessages = roomMessages;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRoomMessages(List<RoomMessage> roomMessages){
        this.roomMessages = roomMessages;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == OUTGOING_MSG){
            View view = LayoutInflater.from(context).inflate(R.layout.outgoing_room_txt_msg_layout, parent, false);
            return new OutgoingLayoutHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.incoming_room_txt_msg_layout, parent, false);
            return new IncomingLayoutHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


         RoomMessage roomMessage = roomMessages.get(position);


         if(holder.getItemViewType() == OUTGOING_MSG){
             OutgoingLayoutHolder outgoingHolder = (OutgoingLayoutHolder) holder;
             if(roomMessage.getSenderProfilePicture()!=null && !roomMessage.getSenderProfilePicture().isEmpty()){
                 Glide.with(context).load(roomMessage.getSenderProfilePicture()).into(outgoingHolder.profileImage);
             }else{
                 outgoingHolder.profileImage.setImageResource(R.drawable.account);
             }
             outgoingHolder.userName.setText(roomMessage.getSenderName());
             outgoingHolder.message.setText(roomMessage.getMessage());
         }else{
             IncomingLayoutHolder incomingHolder = (IncomingLayoutHolder) holder;
             if(roomMessage.getSenderProfilePicture()!=null && !roomMessage.getSenderProfilePicture().isEmpty()){
                 Glide.with(context).load(roomMessage.getSenderProfilePicture()).into(incomingHolder.profileImage);
             }else{
                 incomingHolder.profileImage.setImageResource(R.drawable.account);
             }
             incomingHolder.userName.setText(roomMessage.getSenderName());
             incomingHolder.message.setText(roomMessage.getMessage());
         }
    }



    @Override
    public int getItemViewType(int position) {
        RoomMessage roomMessage = roomMessages.get(position);
        if (roomMessage.getSenderId().equals(currentUserId)) {
            return OUTGOING_MSG;
        } else {
            return INCOMING_MSG;
        }
    }

    @Override
    public int getItemCount() {
        return roomMessages.size();
    }

     static class OutgoingLayoutHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView userName;
        TextView message;

        public OutgoingLayoutHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImg);
            userName = itemView.findViewById(R.id.userName);
            message = itemView.findViewById(R.id.messageText);
        }
    }

    static class IncomingLayoutHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView userName;
        TextView message;

        public IncomingLayoutHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImg);
            userName = itemView.findViewById(R.id.userName);
            message = itemView.findViewById(R.id.messageTxt);

        }
    }
}
