package com.reja.chatapp.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;


public class YouTubeUrlExtractor {
    private Context context;

    public YouTubeUrlExtractor(Context context) {
        this.context = context;
    }

    @SuppressLint("StaticFieldLeak")
    public void extract(String youtubeLink, final YouTubeUrlCallback callback) {
        new YouTubeExtractor(context) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    for (int i = 0, size = ytFiles.size(); i < size; i++) {
                        YtFile file = ytFiles.valueAt(i);
                        if (file.getFormat().getHeight() == 720) { // Choose the format you prefer
                            callback.onSuccess(file.getUrl());
                            return;
                        }
                    }
                }
                callback.onFailure();
            }
        }.extract(youtubeLink, true, true);
    }

    public interface YouTubeUrlCallback {
        void onSuccess(String videoUrl);
        void onFailure();
    }
}

