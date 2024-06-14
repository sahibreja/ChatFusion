package com.reja.chatapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.reja.chatapp.Interface.Listener.OnAcceptClickListener;
import com.reja.chatapp.Interface.Listener.OnRejectClickListener;
import com.reja.chatapp.Model.SearchResult;
import com.reja.chatapp.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SentRequestListAdapter extends RecyclerView.Adapter<SentRequestListAdapter.LayoutViewHolder> {
    private Context context;
    private List<SearchResult> resultList;
    private OnAcceptClickListener onAcceptClickListener;
    private OnRejectClickListener onRejectClickListener;

    public SentRequestListAdapter(Context context, List<SearchResult> resultList) {
        this.context = context;
        this.resultList = resultList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<SearchResult> resultList){
        this.resultList = resultList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SentRequestListAdapter.LayoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.add_friend_item, parent, false);
        return new LayoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SentRequestListAdapter.LayoutViewHolder holder, int position) {
        SearchResult result = resultList.get(position);
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



    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    public void setOnAcceptClickListener(OnAcceptClickListener listener){
        onAcceptClickListener = listener;
    }
    public void setOnRejectClickListener(OnRejectClickListener listener){
        onRejectClickListener = listener;
    }

    static class LayoutViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        TextView userBio;
        ImageView openMsgBtn;
        Button addFriend;
        LinearLayout requestLayout;
        Button acceptBtn,rejectBtn;

        CircleImageView profileImg;
        public LayoutViewHolder(@NonNull View itemView) {
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
