package com.reja.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.reja.chatapp.Model.Conversation;
import com.reja.chatapp.Model.MessageType;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Chat.ChatActivity;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.LayoutHolder> {
    private Context context;
    private List<Conversation> conversationList;

    public ChatListAdapter(Context context, List<Conversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
    }

    @NonNull
    @Override
    public LayoutHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_item_layout,parent,false);
        return new LayoutHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LayoutHolder holder, int position) {

        Conversation conversation = conversationList.get(position);
        String profileImage = conversation.getRecipientProfilePicture();

        if (profileImage != null && !profileImage.isEmpty()) {
            Glide.with(context).load(profileImage).into(holder.profileImg);
        } else {
            holder.profileImg.setImageResource(R.drawable.account);
        }

        if(conversation.getUnreadMessageCount() > 0){
            holder.unread_msg_view.setVisibility(View.VISIBLE);
            holder.unread_msg_no.setText(String.valueOf(conversation.getUnreadMessageCount()));
        }else{
            holder.unread_msg_view.setVisibility(View.GONE);
        }

        holder.userName.setText(conversation.getRecipientName());
        setLastMessage(conversation,holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userId",conversation.getConversationId());
                context.startActivity(intent);
            }
        });
    }

    private void setLastMessage(Conversation conversation,@NonNull LayoutHolder holder){
        if(conversation.getLastMessage().getMessageType()!=null){
            if(conversation.getLastMessage().getMessageType().equals(MessageType.TEXT)){
                holder.lastMessage.setText(conversation.getLastMessage().getContent());
            }else if( conversation.getLastMessage().getMessageType().equals(MessageType.IMAGE)){
                holder.lastMessage.setText("\uD83D\uDCF7 Image");
            }else if(conversation.getLastMessage().getMessageType().equals(MessageType.VIDEO)){
                holder.lastMessage.setText("\uD83C\uDFA6 Video");

            }else if(conversation.getLastMessage().getMessageType().equals(MessageType.PDF)){
                 holder.lastMessage.setText("\uD83D\uDCC4 PDF");
            }
        }

    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public static class LayoutHolder extends RecyclerView.ViewHolder{
        TextView unread_msg_no;
        TextView userName,lastMessage;
        CardView unread_msg_view;

        CircleImageView profileImg;
        public LayoutHolder(@NonNull View itemView) {
            super(itemView);

            unread_msg_no = itemView.findViewById(R.id.unread_msg_no);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            unread_msg_view = itemView.findViewById(R.id.unread_msg_view);
            profileImg = itemView.findViewById(R.id.profileImage);
        }
    }
}
