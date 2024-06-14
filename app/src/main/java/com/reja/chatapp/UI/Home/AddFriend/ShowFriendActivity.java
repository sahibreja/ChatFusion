package com.reja.chatapp.UI.Home.AddFriend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.reja.chatapp.Adapter.SectionsPagerAdapter;
import com.reja.chatapp.R;
import com.reja.chatapp.ViewModel.AddFriendViewModel;
import com.reja.chatapp.databinding.ActivityShowFriendBinding;

public class ShowFriendActivity extends AppCompatActivity {
    private ActivityShowFriendBinding binding;
    private AddFriendViewModel addFriendViewModel;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        onButtonClick();
    }

    private void init() {
        addFriendViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(AddFriendViewModel.class);
        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        setTabLayout();

    }

    private void setTabLayout(){
        setUpWithViewPager(binding.viewPager);
        binding.tabLayout.setupWithViewPager(binding.viewPager);
        binding.viewPager.setCurrentItem(id);
    }

    private void setUpWithViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(AddFriendFragment.newInstance(addFriendViewModel),"Add Random");
        adapter.addFragment(SentRequestFragment.newInstance(addFriendViewModel),"Sent Request");
        adapter.addFragment(ReceiveRequestFragment.newInstance(addFriendViewModel),"Received Request");
        viewPager.setAdapter(adapter);
    }
    
    private void onButtonClick(){
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}