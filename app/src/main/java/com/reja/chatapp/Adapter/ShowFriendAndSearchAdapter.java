package com.reja.chatapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.reja.chatapp.Interface.Listener.OnAcceptClickListener;
import com.reja.chatapp.Interface.Listener.OnAddFriendClickListener;
import com.reja.chatapp.Interface.Listener.OnCancelRequestClickListener;
import com.reja.chatapp.Interface.Listener.OnRejectClickListener;
import com.reja.chatapp.Model.FriendRequest;
import com.reja.chatapp.Model.SearchResult;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Chat.ChatActivity;
import com.reja.chatapp.UI.Chat.ProfileActivity;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowFriendAndSearchAdapter extends RecyclerView.Adapter<ShowFriendAndSearchAdapter.LayoutHolder> {
    private Context context;
    private List<SearchResult> resultList;
    private FirebaseAuth auth;
    private OnAddFriendClickListener onAddFriendClickListener;
    private OnCancelRequestClickListener onCancelRequestClickListener;
    private OnAcceptClickListener onAcceptClickListener;
    private OnRejectClickListener onRejectClickListener;


    public ShowFriendAndSearchAdapter(Context context, List<SearchResult> resultList) {
        this.context = context;
        this.resultList = resultList;
        auth = FirebaseAuth.getInstance();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<SearchResult> searchResultList){
        this.resultList = searchResultList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShowFriendAndSearchAdapter.LayoutHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.add_friend_item, parent, false);
        return new LayoutHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowFriendAndSearchAdapter.LayoutHolder holder, int position) {

        SearchResult result = resultList.get(position);
        String senderId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        FriendRequest friendRequest = new FriendRequest(senderId, result.getUserId(), "pending");
        String profileImage = result.getUserProfileImage();
        holder.userName.setText(result.getUserName());
        holder.userBio.setText(result.getUserBio());

        if (profileImage != null && !profileImage.isEmpty()) {
            Glide.with(context).load(profileImage).into(holder.profileImg);
        } else {
            holder.profileImg.setImageResource(R.drawable.account);
        }

        if (result.isFriend()) {
            holder.addFriend.setVisibility(View.GONE);
            holder.requestLayout.setVisibility(View.GONE);
            holder.openMsgBtn.setVisibility(View.VISIBLE);
        } else if (result.isIRequested()) {
            holder.requestLayout.setVisibility(View.GONE);
            holder.openMsgBtn.setVisibility(View.GONE);
            holder.addFriend.setVisibility(View.VISIBLE);
            holder.addFriend.setText("Cancel Request");
            holder.addFriend.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        } else if (result.isIReceived()) {
            holder.requestLayout.setVisibility(View.VISIBLE);
            holder.openMsgBtn.setVisibility(View.GONE);
            holder.addFriend.setVisibility(View.GONE);
        } else {
            holder.requestLayout.setVisibility(View.GONE);
            holder.openMsgBtn.setVisibility(View.GONE);
            holder.addFriend.setVisibility(View.VISIBLE);
            holder.addFriend.setText("Add Friend");
            holder.addFriend.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.secondary_color));
        }

        holder.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(result.isIRequested()){
                    if(onCancelRequestClickListener!=null){
                        onCancelRequestClickListener.onCancelFriendRequest(friendRequest);
                    }
                }else{
                    if (onAddFriendClickListener != null) {
                        onAddFriendClickListener.onAddFriendClick(friendRequest);
                    }
                }

            }
        });

        holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onAcceptClickListener!=null){
                    onAcceptClickListener.onAcceptClick(friendRequest);
                }
            }
        });

        holder.rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onRejectClickListener!=null){
                    onRejectClickListener.onRejectClick(friendRequest);
                }
            }
        });

        holder.profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("userId",result.getUserId());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(result.isFriend()){
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("userId",result.getUserId());
                    context.startActivity(intent);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    public void setOnAddFriendClickListener(OnAddFriendClickListener listener) {
        onAddFriendClickListener = listener;
    }
    public void setOnCancelRequestClickListener(OnCancelRequestClickListener listener){
        onCancelRequestClickListener = listener;
    }
    public void setOnAcceptClickListener(OnAcceptClickListener listener){
        onAcceptClickListener = listener;
    }
    public void setOnRejectClickListener(OnRejectClickListener listener){
        onRejectClickListener = listener;
    }

    public static class LayoutHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userBio;
        ImageView openMsgBtn;
        Button addFriend;
        LinearLayout requestLayout;
        Button acceptBtn,rejectBtn;

        CircleImageView profileImg;

        public LayoutHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userBio = itemView.findViewById(R.id.bio);
            openMsgBtn = itemView.findViewById(R.id.openConversation);
            addFriend = itemView.findViewById(R.id.addFriendBtn);
            requestLayout = itemView.findViewById(R.id.requestLayout);
            acceptBtn = itemView.findViewById(R.id.acceptBtn);
            rejectBtn = itemView.findViewById(R.id.rejectBtn);
            profileImg = itemView.findViewById(R.id.profileImg);
        }
    }
}
