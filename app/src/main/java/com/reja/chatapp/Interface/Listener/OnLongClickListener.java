package com.reja.chatapp.Interface.Listener;

import com.reja.chatapp.Model.Message;

public interface OnLongClickListener {
    void onLongClick(Message message,Message secondLastMessage,boolean isLastMessage);
}
