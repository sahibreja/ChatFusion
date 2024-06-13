package com.reja.chatapp.Adapter;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.reja.chatapp.Interface.Listener.OnInviteAcceptClickListener;
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
    private final int INCOMING_PDF_MSG =4;
    private final int OUTGOING_PDF_MSG =5;

    private final int INCOMING_REQUEST_MSG =6;
    private final int OUTGOING_REQUEST_MSG =7;

    private OnLongClickListener listener;
    private OnInviteAcceptClickListener inviteAcceptClickListener;

    public void setOnInviteAcceptClickListener(OnInviteAcceptClickListener inviteAcceptClickListener){
        this.inviteAcceptClickListener = inviteAcceptClickListener;
    }
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
        }else if(viewType== INCOMING_PDF_MSG){
            View view = LayoutInflater.from(context).inflate(R.layout.incoming_pdf_msg_layout,parent,false);
            return new IncomingPdfMessageViewHolder(view);

        }else if(viewType== INCOMING_MEDIA_MSG){
            View view = LayoutInflater.from(context).inflate(R.layout.incoming_img_msg_layout,parent,false);
            return new IncomingMediaMessageViewHolder(view);

        }else if(viewType== INCOMING_REQUEST_MSG){
            View view = LayoutInflater.from(context).inflate(R.layout.incoming_sent_request_msg_layout,parent,false);
            return new IncomingRequestMessageViewHolder(view);
        } else if(viewType==OUTGOING_TXT_MSG){
            View view = LayoutInflater.from(context).inflate(R.layout.outgoing_txt_msg_layout,parent,false);
            return new OutgoingTextMessageViewHolder(view);

        }else if(viewType==OUTGOING_REQUEST_MSG){
            View view = LayoutInflater.from(context).inflate(R.layout.outgoing_sent_request_msg_layout,parent,false);
            return new OutgoingRequestMessageViewHolder(view);

        } else if(viewType==OUTGOING_PDF_MSG){
            View view = LayoutInflater.from(context).inflate(R.layout.outgoing_pdf_msg_layout,parent,false);
            return new OutgoingPdfMessageViewHolder(view);

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
        }else if(holder.getItemViewType()== INCOMING_PDF_MSG){
            if(message.getMessageType()!=null){
                setIncomingPdfMessage(message,holder);
            }
        }else if(holder.getItemViewType()== INCOMING_REQUEST_MSG){
            if(message.getMessageType()!=null){
                setIncomingRequestMessage(message,holder);
            }
        } else if(holder.getItemViewType()== INCOMING_MEDIA_MSG){
              if(message.getMessageType()!=null){
                  if(message.getMessageType()==MessageType.IMAGE){
                      setIncomingImageMessage(message,holder);
                  }else if(message.getMessageType()==MessageType.VIDEO){
                      setIncomingVideoMessage(message,holder);
                  }
              }
        }else if(holder.getItemViewType()==OUTGOING_TXT_MSG){
           setOutgoingTextMessage(message,holder);
        }else if(holder.getItemViewType()==OUTGOING_PDF_MSG){
            if(message.getMessageType()!=null){
                setOutgoingPdfMessage(message,holder);
            }
        }else if(holder.getItemViewType()==OUTGOING_REQUEST_MSG){
            if(message.getMessageType()!=null){
                setOutgoingRequestMessage(message,holder);
            }
        }else{
            if(message.getMessageType()!=null){
                if(message.getMessageType()==MessageType.IMAGE){
                    setOutgoingImageMessage(message,holder);
                }else if(message.getMessageType()==MessageType.VIDEO){
                    setOutgoingVideoMessage(message,holder);
                }
            }
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if(listener!=null){
                    listener.onLongClick(holder.itemView,message,secondLastMessage,isLast);
                }
                return true;
            }
        });

    }
    private void setIncomingRequestMessage(Message message,@NonNull RecyclerView.ViewHolder holder) {
        IncomingRequestMessageViewHolder viewHolder = ((IncomingRequestMessageViewHolder)holder);
        viewHolder.timetxt.setText(formatTimestamp(message.getTimestamp()));
        String captionText = message.getContentCaptions();
        if(captionText!=null && !captionText.isEmpty()){
            viewHolder.captionTxt.setVisibility(View.VISIBLE);
            viewHolder.captionTxt.setText(message.getContentCaptions());
        }else{
            viewHolder.captionTxt.setVisibility(View.GONE);
        }
        viewHolder.progressBar.setVisibility(View.VISIBLE);

        if(message.getContent()==null || message.getContent().isEmpty()){
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.imgMsg.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.imgMsg.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            viewHolder.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            viewHolder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    }).apply(RequestOptions.placeholderOf(R.drawable.watching_a_movie).centerCrop())
                    .into(viewHolder.imgMsg);
        }
        viewHolder.imgMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getContent()!=null && !message.getContent().isEmpty()){
                    if(inviteAcceptClickListener!=null){
                        inviteAcceptClickListener.onInviteAcceptClick(message.getContent());
                    }
                }
            }
        });


    }
    private void setOutgoingRequestMessage(Message message, @NonNull RecyclerView.ViewHolder holder) {
        OutgoingRequestMessageViewHolder viewHolder = ((OutgoingRequestMessageViewHolder)holder);
        String captionText = message.getContentCaptions();
        if(captionText!=null && !captionText.isEmpty()){
            viewHolder.captionTxt.setVisibility(View.VISIBLE);
            viewHolder.captionTxt.setText(message.getContentCaptions());
        }else{
            viewHolder.captionTxt.setVisibility(View.GONE);
        }

        viewHolder.timetxt.setText(formatTimestamp(message.getTimestamp()));
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        viewHolder.imgMsg.setVisibility(View.GONE);

        if(message.getContent()==null || message.getContent().isEmpty()){
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.imgMsg.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.imgMsg.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            viewHolder.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            viewHolder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .apply(RequestOptions.placeholderOf(R.drawable.watching_a_movie).centerCrop())
                    .into(viewHolder.imgMsg);
        }

        if(message.isSeen()){
            viewHolder.doubleCheck.setVisibility(View.VISIBLE);
        }else{
            viewHolder.doubleCheck.setVisibility(View.GONE);
        }

        viewHolder.imgMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getContent()!=null && !message.getContent().isEmpty()){
                    if(inviteAcceptClickListener!=null){
                        inviteAcceptClickListener.onInviteAcceptClick(message.getContent());
                    }
                }
            }
        });

    }
    private void setOutgoingPdfMessage(Message message, @NonNull RecyclerView.ViewHolder holder) {
        OutgoingPdfMessageViewHolder viewHolder = ((OutgoingPdfMessageViewHolder)holder);
        String captionText = message.getContentCaptions();
        if(captionText!=null && !captionText.isEmpty()){
            viewHolder.captionTxt.setVisibility(View.VISIBLE);
            viewHolder.captionTxt.setText(message.getContentCaptions());
        }else{
           viewHolder.captionTxt.setVisibility(View.GONE);
        }

        viewHolder.timetxt.setText(formatTimestamp(message.getTimestamp()));
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        viewHolder.imgMsg.setVisibility(View.GONE);

        if(message.getContent()==null || message.getContent().isEmpty()){
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.imgMsg.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.imgMsg.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                            viewHolder.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            viewHolder.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .apply(RequestOptions.placeholderOf(R.drawable.pdf).centerCrop())
                    .into(viewHolder.imgMsg);
        }

        if(message.isSeen()){
            viewHolder.doubleCheck.setVisibility(View.VISIBLE);
        }else{
            viewHolder.doubleCheck.setVisibility(View.GONE);
        }

        viewHolder.imgMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getContent()!=null && !message.getContent().isEmpty()){
                    Uri pdfUri = Uri.parse(message.getContent());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(pdfUri, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // Handle scenario where PDF viewer app is not installed
                        Toast.makeText(context, "No PDF viewer installed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
    private void setOutgoingVideoMessage(Message message,@NonNull RecyclerView.ViewHolder holder) {
        OutgoingMediaMessageViewHolder viewHolder  = ((OutgoingMediaMessageViewHolder)holder);
        String captionText = message.getContentCaptions();


        if(captionText!=null && !captionText.isEmpty()){
            viewHolder.captionTxt.setVisibility(View.VISIBLE);
            viewHolder.captionTxt.setText(message.getContentCaptions());
        }else{
            viewHolder.captionTxt.setVisibility(View.GONE);
        }
        viewHolder.timetxt.setText(formatTimestamp(message.getTimestamp()));
        viewHolder.playButton.setVisibility(View.GONE);
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        if(message.getContent()==null || message.getContent().isEmpty()){
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.imgMsg.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.imgMsg.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.playButton.setVisibility(View.VISIBLE);
                    return false;
                }
            }).into(viewHolder.imgMsg);
        }
        if(message.isSeen()){
            viewHolder.doubleCheck.setVisibility(View.VISIBLE);
        }else{
            viewHolder.doubleCheck.setVisibility(View.GONE);
        }

        // Set the click listener for the play button
        viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getContent()!=null && !message.getContent().isEmpty()){
                    Uri videoUri = Uri.parse(message.getContent());
                    Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
                    intent.setDataAndType(videoUri, "video/*");
                    context.startActivity(intent);
                }
            }
        });
    }
    private void setIncomingPdfMessage(Message message,@NonNull RecyclerView.ViewHolder holder) {
        IncomingPdfMessageViewHolder viewHolder = ((IncomingPdfMessageViewHolder)holder);
        viewHolder.timetxt.setText(formatTimestamp(message.getTimestamp()));
        String captionText = message.getContentCaptions();
        if(captionText!=null && !captionText.isEmpty()){
            viewHolder.captionTxt.setVisibility(View.VISIBLE);
            viewHolder.captionTxt.setText(message.getContentCaptions());
        }else{
            viewHolder.captionTxt.setVisibility(View.GONE);
        }
        viewHolder.progressBar.setVisibility(View.VISIBLE);

        if(message.getContent()==null || message.getContent().isEmpty()){
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.imgMsg.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.imgMsg.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).apply(RequestOptions.placeholderOf(R.drawable.pdf).centerCrop())
                    .into(viewHolder.imgMsg);
        }
        viewHolder.imgMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getContent()!=null && !message.getContent().isEmpty()){
                    Uri pdfUri = Uri.parse(message.getContent());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(pdfUri, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        // Handle scenario where PDF viewer app is not installed
                        Toast.makeText(context, "No PDF viewer installed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
    private void setIncomingVideoMessage(Message message,@NonNull RecyclerView.ViewHolder holder) {
        IncomingMediaMessageViewHolder viewHolder = ((IncomingMediaMessageViewHolder)holder);
        String captionText = message.getContentCaptions();
        if(captionText!=null && !captionText.isEmpty()){
            viewHolder.captionTxt.setVisibility(View.VISIBLE);
            viewHolder.captionTxt.setText(message.getContentCaptions());
        }else{
            viewHolder.captionTxt.setVisibility(View.GONE);
        }
        viewHolder.timetxt.setText(formatTimestamp(message.getTimestamp()));
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        viewHolder.playButton.setVisibility(View.GONE);

        if(message.getContent()==null || message.getContent().isEmpty()){
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.imgMsg.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.imgMsg.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                    viewHolder.playButton.setVisibility(View.VISIBLE);
                    return false;
                }
            }).into(viewHolder.imgMsg);
        }

        viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getContent()!=null && !message.getContent().isEmpty()){
                    Uri videoUri = Uri.parse(message.getContent());
                    Intent intent = new Intent(Intent.ACTION_VIEW, videoUri);
                    intent.setDataAndType(videoUri, "video/*");
                    context.startActivity(intent);
                }
            }
        });

    }
    private void setOutgoingImageMessage(Message message,@NonNull RecyclerView.ViewHolder holder){
        OutgoingMediaMessageViewHolder viewHolder = ((OutgoingMediaMessageViewHolder)holder);
        String captionText = message.getContentCaptions();

        if(captionText!=null && !captionText.isEmpty()){
            viewHolder.captionTxt.setVisibility(View.VISIBLE);
            viewHolder.captionTxt.setText(message.getContentCaptions());
        }else{
            viewHolder.captionTxt.setVisibility(View.GONE);
        }
        viewHolder.timetxt.setText(formatTimestamp(message.getTimestamp()));
        viewHolder.playButton.setVisibility(View.GONE);
        viewHolder.progressBar.setVisibility(View.VISIBLE);
         if(message.getContent()==null || message.getContent().isEmpty()){
             viewHolder.progressBar.setVisibility(View.VISIBLE);
             viewHolder.imgMsg.setVisibility(View.INVISIBLE);
         }else {
             viewHolder.imgMsg.setVisibility(View.VISIBLE);
             Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                 @Override
                 public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                     viewHolder.progressBar.setVisibility(View.GONE);
                     return false;
                 }

                 @Override
                 public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                     viewHolder.progressBar.setVisibility(View.GONE);
                     return false;
                 }
             }).into(viewHolder.imgMsg);
         }
        if(message.isSeen()){
            viewHolder.doubleCheck.setVisibility(View.VISIBLE);
        }else{
            viewHolder.doubleCheck.setVisibility(View.GONE);
        }

        viewHolder.imgMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getContent()!=null && !message.getContent().isEmpty()){
                    Uri imageUri = Uri.parse(message.getContent());
                    Intent intent = new Intent(Intent.ACTION_VIEW, imageUri);
                    intent.setDataAndType(imageUri, "image/*");
                    context.startActivity(intent);
                }
            }
        });
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
        IncomingMediaMessageViewHolder viewHolder = ((IncomingMediaMessageViewHolder)holder);
        viewHolder.timetxt.setText(formatTimestamp(message.getTimestamp()));
        viewHolder.playButton.setVisibility(View.GONE);
        String captionText = message.getContentCaptions();
        if(captionText!=null && !captionText.isEmpty()){
            viewHolder.captionTxt.setVisibility(View.VISIBLE);
            viewHolder.captionTxt.setText(message.getContentCaptions());
        }else{
            viewHolder.captionTxt.setVisibility(View.GONE);
        }
        viewHolder.progressBar.setVisibility(View.VISIBLE);

        if(message.getContent()==null || message.getContent().isEmpty()){
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.imgMsg.setVisibility(View.INVISIBLE);
        }else {
            viewHolder.imgMsg.setVisibility(View.VISIBLE);
            Glide.with(context).load(message.getContent()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(viewHolder.imgMsg);
        }
        viewHolder.imgMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(message.getContent()!=null && !message.getContent().isEmpty()){
                    Uri imagUri = Uri.parse(message.getContent());
                    Intent intent = new Intent(Intent.ACTION_VIEW, imagUri);
                    intent.putExtra("force_fullscreen", true);
                    intent.setDataAndType(imagUri, "image/*");
                    context.startActivity(intent);
                }
            }
        });

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
             }else if(m.getMessageType()==MessageType.PDF){
                   return OUTGOING_PDF_MSG;
             }else if(m.getMessageType()==MessageType.ROOM_REQUEST){
                 return OUTGOING_REQUEST_MSG;
             }
             else{
                 return OUTGOING_MEDIA_MSG;
             }
         }else{
             if(m.getMessageType()==MessageType.TEXT){
                 return INCOMING_TXT_MSG;
             }else if(m.getMessageType()==MessageType.PDF){
                 return INCOMING_PDF_MSG;
             }else if(m.getMessageType()==MessageType.ROOM_REQUEST){
                 return INCOMING_REQUEST_MSG;
             }
             else{
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

    static class OutgoingPdfMessageViewHolder extends RecyclerView.ViewHolder{
        TextView timetxt;
        ImageView doubleCheck;
        ImageView imgMsg;
        TextView captionTxt;
        ProgressBar progressBar;


        public OutgoingPdfMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            timetxt = itemView.findViewById(R.id.time_txt);
            doubleCheck = itemView.findViewById(R.id.double_check);
            captionTxt = itemView.findViewById(R.id.caption_txt);
            imgMsg = itemView.findViewById(R.id.image_view);
            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }

    static class IncomingPdfMessageViewHolder extends RecyclerView.ViewHolder{
        TextView timetxt;
        ImageView imgMsg;
        TextView captionTxt;
        ProgressBar progressBar;
        public IncomingPdfMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            timetxt = itemView.findViewById(R.id.time_txt);
            captionTxt = itemView.findViewById(R.id.caption_txt);
            imgMsg = itemView.findViewById(R.id.image_view);
            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }

    static class OutgoingRequestMessageViewHolder extends RecyclerView.ViewHolder{
        TextView timetxt;
        ImageView doubleCheck;
        ImageView imgMsg;
        TextView captionTxt;
        ProgressBar progressBar;


        public OutgoingRequestMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            timetxt = itemView.findViewById(R.id.time_txt);
            doubleCheck = itemView.findViewById(R.id.double_check);
            captionTxt = itemView.findViewById(R.id.caption_txt);
            imgMsg = itemView.findViewById(R.id.image_view);
            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }

    static class IncomingRequestMessageViewHolder extends RecyclerView.ViewHolder{
        TextView timetxt;
        ImageView imgMsg;
        TextView captionTxt;
        ProgressBar progressBar;
        public IncomingRequestMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            timetxt = itemView.findViewById(R.id.time_txt);
            captionTxt = itemView.findViewById(R.id.caption_txt);
            imgMsg = itemView.findViewById(R.id.image_view);
            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }
}