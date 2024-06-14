package com.reja.chatapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.reja.chatapp.Interface.Listener.OnInviteClickListener;
import com.reja.chatapp.Model.Friend;
import com.reja.chatapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RoomUserAdapter extends RecyclerView.Adapter<RoomUserAdapter.ViewHolder> {
    private Context context;
    private List<Friend> friendList;

    private OnInviteClickListener listener;

    public RoomUserAdapter(Context context, List<Friend> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    public void setOnInviteClickListener(OnInviteClickListener listener){
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFriendList(List<Friend> friendList) {
        this.friendList = friendList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public RoomUserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.send_room_request_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomUserAdapter.ViewHolder holder, int position) {
         Friend friend = friendList.get(position);
        String profileImage = friend.getFriendProfilePicture();

        if (profileImage != null && !profileImage.isEmpty()) {
            Glide.with(context).load(profileImage).into(holder.profile_image);
            holder.profile_image.setPadding(10,10,10,10);
        } else {
            holder.profile_image.setImageResource(R.drawable.account);
        }
        if(friend.isSent()){
            holder.sendBtn.setText("Invited");
            holder.sendBtn.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        }else{
            holder.sendBtn.setText("Invite");
            holder.sendBtn.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        }

         holder.user_name.setText(friend.getFriendName());

         holder.sendBtn.setOnClickListener((view)->{
             if(!friend.isSent()){
                 if(listener!=null){
                     listener.onInviteClick(friend.getFriendId());
                 }
             }
         });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile_image;
        TextView user_name;
        android.widget.Button sendBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profileImg);
            user_name = itemView.findViewById(R.id.userName);
            sendBtn = itemView.findViewById(R.id.sendBtn);
        }
    }
}
