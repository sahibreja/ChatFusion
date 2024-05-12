package com.reja.chatapp.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.reja.chatapp.Interface.Listener.OnLongClickListener;
import com.reja.chatapp.Model.Message;
import com.reja.chatapp.Model.MessageType;
import com.reja.chatapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<Message> messageList;
    private String currentUserId= Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

    private final int INCOMING_TXT_MSG=0;
    private final int OUTGOING_TXT_MSG=1;
    private final int INCOMING_MEDIA_MSG =2;
    private final int OUTGOING_MEDIA_MSG =3;

    private OnLongClickListener listener;
    public ChatAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==INCOMING_TXT_MSG){
            View view = LayoutInflater.from(context).inflate(R.layout.incoming_txt_msg_layout,parent,false);
            return new IncomingTextMessageViewHolder(view);
        }else if(viewType== INCOMING_MEDIA_MSG){
            View view = LayoutInflater.from(context).inflate(R.layout.incoming_img_msg_layout,parent,false);
            return new IncomingMediaMessageViewHolder(view);

        }else if(viewType==OUTGOING_TXT_MSG){
            View view = LayoutInflater.from(context).inflate(R.layout.outgoing_txt_msg_layout,parent,false);
            return new OutgoingTextMessageViewHolder(view);

        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.outgoing_img_msg_layout,parent,false);
            return new OutgoingMediaMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,  int position) {
        Message message = messageList.get(position);
        int n = messageList.size();
        Message secondLastMessage = n - 1 - 1 >= 0 ? messageList.get(n-1-1) : null;
        boolean isLast = position == n - 1;
        if(holder.getItemViewType()==INCOMING_TXT_MSG){
           setIncomingTextMessage(message,holder);
        }else if(holder.getItemViewType()== INCOMING_MEDIA_MSG){
            if(message.getMessageType()!=null){
                if(message.getMessageType()==MessageType.IMAGE){
                    setIncomingImageMessage(message,holder);
                }else if(message.getMessageType()==MessageType.VIDEO){
                    setIncomingVideoMessage(message,holder);
                }else if(message.getMessageType()== MessageType.PDF){
                    setIncomingPdfMessage(message,holder);
                }
            }

        }else if(holder.getItemViewType()==OUTGOING_TXT_MSG){
           setOutgoingTextMessage(message,holder);
        }else{
            if(message.getMessageType()!=null){
                if(message.getMessageType()==MessageType.IMAGE){
                    setOutgoingImageMessage(message,holder);
                }else if(message.getMessageType()==MessageType.VIDEO){
                     setOutgoingVideoMessage(message,holder);
                }else if(message.getMessageType()== MessageType.PDF){
                     setOutgoingPdfMessage(message,holder);
                }
            }
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if(listener!=null){
                    listener.onLongClick(message,secondLastMessage,isLast);
                }
                return true;
            }
        });

    }

    private void setOutgoingPdfMessage(Message message,@NonNull RecyclerView.ViewHolder holder) {
        ((OutgoingMediaMessageViewHolder)holder).playButton.setVisibility(View.GONE);
    }

    private void setOutgoingVideoMessage(Message message,@NonNull RecyclerView.ViewHolder holder) {
        String captionText = message.getContentCaptions();

        if(captionText!=null && !captionText.isEmpty()){
            ((OutgoingMediaMessageViewHolder)holder).captionTxt.setVisibility(View.VISIBLE);
            ((OutgoingMediaMessageViewHolder)holder).captionTxt.setText(message.getContentCaptions());
        }else{
            ((OutgoingMediaMessageViewHolder)holder).captionTxt.setVisibility(View.GONE);
        }
        ((OutgoingMediaMessageViewHolder)holder).timetxt.setText(formatTimestamp(message.getTimestamp()));
        ((OutgoingMediaMessageViewHolder) holder).playButton.setVisibility(View.GONE);
        ((OutgoingMediaMessageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
        if(message.getContent()==null || message.getContent().isEmpty()){
            ((OutgoingMediaMessageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
            ((OutgoingMediaMessageViewHolder) holder).imgMsg.setVisibility(View.INVISIBLE);
        }else {
            ((OutgoingMediaMessageViewHolder) holder).imgMsg.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    ((OutgoingMediaMessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    ((OutgoingMediaMessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                    ((OutgoingMediaMessageViewHolder)holder).playButton.setVisibility(View.VISIBLE);
                    return false;
                }
            }).into(((OutgoingMediaMessageViewHolder) holder).imgMsg);
        }
        if(message.isSeen()){
            ((OutgoingMediaMessageViewHolder)holder).doubleCheck.setVisibility(View.VISIBLE);
        }else{
            ((OutgoingMediaMessageViewHolder)holder).doubleCheck.setVisibility(View.GONE);
        }
    }

    private void setIncomingPdfMessage(Message message,@NonNull RecyclerView.ViewHolder holder) {
        ((IncomingMediaMessageViewHolder)holder).playButton.setVisibility(View.GONE);

    }
    private void setIncomingVideoMessage(Message message,@NonNull RecyclerView.ViewHolder holder) {


        String captionText = message.getContentCaptions();
        if(captionText!=null && !captionText.isEmpty()){
            ((IncomingMediaMessageViewHolder)holder).captionTxt.setVisibility(View.VISIBLE);
            ((IncomingMediaMessageViewHolder)holder).captionTxt.setText(message.getContentCaptions());
        }else{
            ((IncomingMediaMessageViewHolder)holder).captionTxt.setVisibility(View.GONE);
        }
        ((IncomingMediaMessageViewHolder)holder).timetxt.setText(formatTimestamp(message.getTimestamp()));
        ((IncomingMediaMessageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
        ((IncomingMediaMessageViewHolder) holder).playButton.setVisibility(View.GONE);

        if(message.getContent()==null || message.getContent().isEmpty()){
            ((IncomingMediaMessageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
            ((IncomingMediaMessageViewHolder) holder).imgMsg.setVisibility(View.INVISIBLE);
        }else {
            ((IncomingMediaMessageViewHolder) holder).imgMsg.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    ((IncomingMediaMessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    ((IncomingMediaMessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                    ((IncomingMediaMessageViewHolder)holder).playButton.setVisibility(View.VISIBLE);
                    return false;
                }
            }).into(((IncomingMediaMessageViewHolder) holder).imgMsg);
        }

    }
    private void setOutgoingImageMessage(Message message,@NonNull RecyclerView.ViewHolder holder){
        String captionText = message.getContentCaptions();

        if(captionText!=null && !captionText.isEmpty()){
            ((OutgoingMediaMessageViewHolder)holder).captionTxt.setVisibility(View.VISIBLE);
            ((OutgoingMediaMessageViewHolder)holder).captionTxt.setText(message.getContentCaptions());
        }else{
            ((OutgoingMediaMessageViewHolder)holder).captionTxt.setVisibility(View.GONE);
        }
        ((OutgoingMediaMessageViewHolder)holder).timetxt.setText(formatTimestamp(message.getTimestamp()));
        ((OutgoingMediaMessageViewHolder) holder).playButton.setVisibility(View.GONE);
         ((OutgoingMediaMessageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
         if(message.getContent()==null || message.getContent().isEmpty()){
             ((OutgoingMediaMessageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
             ((OutgoingMediaMessageViewHolder) holder).imgMsg.setVisibility(View.INVISIBLE);
         }else {
             ((OutgoingMediaMessageViewHolder) holder).imgMsg.setVisibility(View.VISIBLE);
             Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                 @Override
                 public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                     ((OutgoingMediaMessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                     return false;
                 }

                 @Override
                 public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                     ((OutgoingMediaMessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                     return false;
                 }
             }).into(((OutgoingMediaMessageViewHolder) holder).imgMsg);
         }
        if(message.isSeen()){
            ((OutgoingMediaMessageViewHolder)holder).doubleCheck.setVisibility(View.VISIBLE);
        }else{
            ((OutgoingMediaMessageViewHolder)holder).doubleCheck.setVisibility(View.GONE);
        }
    }
    private void setOutgoingTextMessage(Message message,@NonNull RecyclerView.ViewHolder holder){
        ((OutgoingTextMessageViewHolder)holder).txtMsg.setText(message.getContent());
        ((OutgoingTextMessageViewHolder)holder).timetxt.setText(formatTimestamp(message.getTimestamp()));

        if(message.isSeen()){
            ((OutgoingTextMessageViewHolder)holder).doubleCheck.setVisibility(View.VISIBLE);
        }else{
            ((OutgoingTextMessageViewHolder)holder).doubleCheck.setVisibility(View.GONE);
        }
    }
    private void setIncomingTextMessage(Message message,@NonNull RecyclerView.ViewHolder holder){
        ((IncomingTextMessageViewHolder)holder).txtMsg.setText(message.getContent());
        ((IncomingTextMessageViewHolder)holder).timetxt.setText(formatTimestamp(message.getTimestamp()));
    }
    private void setIncomingImageMessage(Message message,@NonNull RecyclerView.ViewHolder holder){
        ((IncomingMediaMessageViewHolder)holder).timetxt.setText(formatTimestamp(message.getTimestamp()));
        ((IncomingMediaMessageViewHolder) holder).playButton.setVisibility(View.GONE);
        String captionText = message.getContentCaptions();
        if(captionText!=null && !captionText.isEmpty()){
            ((IncomingMediaMessageViewHolder)holder).captionTxt.setVisibility(View.VISIBLE);
            ((IncomingMediaMessageViewHolder)holder).captionTxt.setText(message.getContentCaptions());
        }else{
            ((IncomingMediaMessageViewHolder)holder).captionTxt.setVisibility(View.GONE);
        }
         ((IncomingMediaMessageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);

        if(message.getContent()==null || message.getContent().isEmpty()){
            ((IncomingMediaMessageViewHolder) holder).progressBar.setVisibility(View.GONE);
            ((IncomingMediaMessageViewHolder) holder).imgMsg.setVisibility(View.INVISIBLE);
        }else {
            ((IncomingMediaMessageViewHolder) holder).imgMsg.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    ((IncomingMediaMessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    ((IncomingMediaMessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(((IncomingMediaMessageViewHolder) holder).imgMsg);
        }

    }
    public void setOnLongClickListener(OnLongClickListener listener){
        this.listener = listener;
    }
    @Override
    public int getItemCount() {
        return messageList.size();
    }
    @Override
    public int getItemViewType(int position) {
        Message m = messageList.get(position);

         if(m.getSenderId().equals(currentUserId)){
             if(m.getMessageType()==MessageType.TEXT){
                 return OUTGOING_TXT_MSG;
             }else{
                 return OUTGOING_MEDIA_MSG;
             }
         }else{
             if(m.getMessageType()==MessageType.TEXT){
                 return INCOMING_TXT_MSG;
             }else{
                 return INCOMING_MEDIA_MSG;
             }
         }
    }

    private static String formatTimestamp(long timestamp) {
        // Create a Date object using the timestamp
        Date date = new Date(timestamp);

        // Create a SimpleDateFormat object to format the date
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());

        // Format the date and return the formatted string
        return sdf.format(date);
    }

    static class IncomingTextMessageViewHolder extends RecyclerView.ViewHolder{
        TextView timetxt,txtMsg;
        public IncomingTextMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            timetxt = itemView.findViewById(R.id.time_txt);
            txtMsg = itemView.findViewById(R.id.txt_msg);
        }
    }

    static class IncomingMediaMessageViewHolder extends RecyclerView.ViewHolder{
        TextView timetxt;
        ImageView imgMsg;
        TextView captionTxt;
        ProgressBar progressBar;
        ImageView playButton;
        public IncomingMediaMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            timetxt = itemView.findViewById(R.id.time_txt);
            captionTxt = itemView.findViewById(R.id.caption_txt);
            imgMsg = itemView.findViewById(R.id.image_view);
            progressBar = itemView.findViewById(R.id.progressbar);
            playButton = itemView.findViewById(R.id.play_button);
        }
    }

    static class OutgoingTextMessageViewHolder extends RecyclerView.ViewHolder{
        TextView timetxt,txtMsg;
        ImageView doubleCheck;
        public OutgoingTextMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            timetxt = itemView.findViewById(R.id.time_txt);
            txtMsg = itemView.findViewById(R.id.txt_msg);
            doubleCheck = itemView.findViewById(R.id.double_check);

        }
    }

    static class OutgoingMediaMessageViewHolder extends RecyclerView.ViewHolder{
        TextView timetxt;
        ImageView doubleCheck;
        ImageView imgMsg;
        TextView captionTxt;
        ProgressBar progressBar;

        ImageView playButton;
        public OutgoingMediaMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            timetxt = itemView.findViewById(R.id.time_txt);
            doubleCheck = itemView.findViewById(R.id.double_check);
            captionTxt = itemView.findViewById(R.id.caption_txt);
            imgMsg = itemView.findViewById(R.id.image_view);
            progressBar = itemView.findViewById(R.id.progressbar);
            playButton = itemView.findViewById(R.id.play_button);
        }
    }
}
