package com.reja.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.reja.chatapp.R;
import com.reja.chatapp.UI.Chat.ChatActivity;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.LayoutHolder> {
    private Context context;

    public ChatListAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public LayoutHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item_layout,parent,false);
        return new LayoutHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LayoutHolder holder, int position) {
        holder.unread_msg_no.setText(String.valueOf(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ChatActivity.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return 101;
    }

    public class LayoutHolder extends RecyclerView.ViewHolder{
        TextView unread_msg_no;

        public LayoutHolder(@NonNull View itemView) {
            super(itemView);

            unread_msg_no = itemView.findViewById(R.id.unread_msg_no);
        }
    }
}
