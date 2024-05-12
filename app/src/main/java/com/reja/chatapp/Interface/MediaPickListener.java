package com.reja.chatapp.Interface;

import android.net.Uri;

import java.util.List;

public interface MediaPickListener {
    void onMediaPicked(List<Uri> selectedMediaUris);
}
