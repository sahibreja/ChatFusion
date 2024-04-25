package com.reja.chatapp.Adapter;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reja.chatapp.Model.User;
import com.reja.chatapp.R;

import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.LayoutHolder> {
    private Context context;
    private List<User> userList;

    public FriendListAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public FriendListAdapter.LayoutHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_friend_item,parent,false);
        return new LayoutHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListAdapter.LayoutHolder holder, int position) {
         holder.userName.setText(userList.get(position).getUserName());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class LayoutHolder extends RecyclerView.ViewHolder{
        TextView userName;
        TextView userBio;

        public LayoutHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
        }
    }
}
