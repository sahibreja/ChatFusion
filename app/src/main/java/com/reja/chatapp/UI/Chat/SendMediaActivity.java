package com.reja.chatapp.UI.Chat;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

//import com.davemorrissey.labs.subscaleview.ImageSource;
import com.reja.chatapp.Adapter.MediaPagerAdapter;
import com.reja.chatapp.Adapter.ThumbnailAdapter;
import com.reja.chatapp.Interface.Listener.OnItemClickListener;
import com.reja.chatapp.Model.Message;
import com.reja.chatapp.Model.MessageType;
import com.reja.chatapp.R;
import com.reja.chatapp.ViewModel.ChatViewModel;
import com.reja.chatapp.databinding.ActivitySendMediaBinding;

import java.util.List;

public class SendMediaActivity extends AppCompatActivity {
    private ActivitySendMediaBinding binding;
    private String senderId;
    private String receiverId;
    private List<Uri> listOfSelectedMediaUri;
    private MediaPagerAdapter pagerAdapter;
    private ThumbnailAdapter thumbnailAdapter;

    private ChatViewModel chatViewModel;

    private MessageType messageType;
    private boolean isChattingWithMe = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendMediaBinding.inflate(getLayoutInflater());
        //EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
        observer();
        setupViewPager();
        clickOnButton();

        //binding.imageView.setImage(ImageSource.uri(listOfSelectedMediaUri.get(0)));
    }




    private void observer(){
        chatViewModel.isChattingWith(senderId,receiverId).observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                isChattingWithMe = aBoolean;
            }
        });
    }

    private void init(){
        chatViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(ChatViewModel.class);
        Intent intent = getIntent();
        receiverId = intent.getStringExtra("receiverId");
        senderId = intent.getStringExtra("senderId");
        listOfSelectedMediaUri = intent.getParcelableArrayListExtra("selectedMediaUris");
        //Toast.makeText(this, listOfSelectedMediaUri.get(0).toString(), Toast.LENGTH_SHORT).show();
        String type = getItemType(0);
        messageType = getMediaType();
        pagerAdapter = new MediaPagerAdapter(this,SendMediaActivity.this, listOfSelectedMediaUri,type);
        thumbnailAdapter = new ThumbnailAdapter(this,listOfSelectedMediaUri,type);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(thumbnailAdapter);
        binding.section4.bringToFront();


    }

    private void setListOfThumbnailsUri(){

    }

    private void setupViewPager() {
        binding.viewPager.setAdapter(pagerAdapter);
    }

    private String getItemType(int position) {
        String mimeType = getContentResolver().getType(listOfSelectedMediaUri.get(position));
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return "image";
            } else if (mimeType.startsWith("video/")) {
                return "video";
            } else if (mimeType.equals("application/pdf")) {
                return "pdf";
            }
        }
        return "unsupported";
    }

    private void clickOnButton(){

        binding.crossBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                thumbnailAdapter.setSelectedPosition(position);
                binding.recyclerView.smoothScrollToPosition(position);

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        thumbnailAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                thumbnailAdapter.setSelectedPosition(position);
                binding.viewPager.setCurrentItem(position,true);

            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMediaMessages();
            }
        });



    }

    private void sendMediaMessages() {
        String captions = binding.txtEditTxt.getText().toString().trim();

        for (int i = 0; i < listOfSelectedMediaUri.size(); i++) {
            String setCaption= "";
            if(i==listOfSelectedMediaUri.size()-1){
                setCaption = captions;
            }
            Message message = new Message(senderId,receiverId,receiverId,"",setCaption,System.currentTimeMillis(),false,-1, messageType.equals("UNSUPPORTED") ? null : messageType);
            chatViewModel.sendMediaMessage(message,listOfSelectedMediaUri.get(i),isChattingWithMe);
            finish();
        }
    }


    private MessageType getMediaType(){
        String mimeType = getContentResolver().getType(listOfSelectedMediaUri.get(0));
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return MessageType.IMAGE;
            } else if (mimeType.startsWith("video/")) {
                return MessageType.VIDEO;
            } else if (mimeType.equals("application/pdf")) {
               return MessageType.PDF;
            } else {
                Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
            }
        }
        return MessageType.valueOf("UNSUPPORTED");
    }



    @Override
    protected void onStart() {
        super.onStart();
        chatViewModel.setChattingWith(senderId,receiverId,true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatViewModel.setChattingWith(senderId,receiverId,false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatViewModel.setChattingWith(senderId,receiverId,true);

    }

    @Override
    public void onStop() {
        super.onStop();
        chatViewModel.setChattingWith(senderId,receiverId,false);
    }
}