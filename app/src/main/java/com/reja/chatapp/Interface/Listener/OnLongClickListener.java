package com.reja.chatapp.Interface.Listener;

import android.view.View;

import com.reja.chatapp.Model.Message;

public interface OnLongClickListener {
    void onLongClick(View view, Message message, Message secondLastMessage, boolean isLastMessage);
}
