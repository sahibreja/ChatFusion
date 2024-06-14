package com.reja.chatapp.UI.Chat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.reja.chatapp.Model.MessageType;
import com.reja.chatapp.R;
import com.reja.chatapp.databinding.ActivityViewMediaBinding;

public class ViewMediaActivity extends AppCompatActivity {
    private String messageType;
    private String mediaUrl;
    private ActivityViewMediaBinding binding;
    private Uri mediaUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewMediaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        //setMedia();
        setPdf();
        handleOnClick();
    }

    private void handleOnClick() {
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setMedia(){
        if(messageType.equals(MessageType.IMAGE.toString())){
            setImage();
        } else if(messageType.equals(MessageType.VIDEO.toString())){
            setVideo();
        } else if(messageType.equals(MessageType.PDF.toString())){
            setPdf();
        }
    }


    private void setImage() {
        binding.webView.setVisibility(View.GONE);
        binding.videoView.setVisibility(View.GONE);
        binding.imageView.setVisibility(View.VISIBLE);
        binding.progressbar.setVisibility(View.VISIBLE);
        binding.imageView.setImage(ImageSource.uri(mediaUrl));
        binding.imageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
            @Override
            public void onReady() {
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onImageLoaded() {
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onPreviewLoadError(Exception e) {
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onImageLoadError(Exception e) {
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onTileLoadError(Exception e) {
                binding.progressbar.setVisibility(View.GONE);
            }

            @Override
            public void onPreviewReleased() {
                binding.progressbar.setVisibility(View.GONE);
            }
        });
    }

    private void setVideo() {
        binding.webView.setVisibility(View.GONE);
        binding.videoView.setVisibility(View.VISIBLE);
        binding.imageView.setVisibility(View.GONE);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(binding.videoView);
        binding.videoView.setMediaController(mediaController);
        binding.videoView.setVideoURI(mediaUri);
        binding.progressbar.setVisibility(View.VISIBLE);
        binding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                binding.progressbar.setVisibility(View.GONE);
                binding.videoView.start();
            }
        });

    }

    private void setPdf() {
        binding.pdfFrame.setVisibility(View.VISIBLE);
        binding.videoFrame.setVisibility(View.GONE);
        binding.imageFrame.setVisibility(View.GONE);
        binding.webView.loadUrl("https://www.clickdimensions.com/links/TestPDFfile.pdf");

    }

    private void init() {
        Intent intent = getIntent();
        messageType = intent.getStringExtra("messageType");
        mediaUrl = intent.getStringExtra("mediaUrl");
        mediaUri = Uri.parse(mediaUrl);

        Log.d("ViewMediaActivity", "Message type: " + messageType);

    }
}