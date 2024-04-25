package com.reja.chatapp.UI.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.reja.chatapp.Adapter.ChatListAdapter;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Authentication.LoginActivity;
import com.reja.chatapp.databinding.ActivityHomeScreenBinding;

public class HomeScreenActivity extends AppCompatActivity {
    private ActivityHomeScreenBinding binding;
    private ChatListAdapter adapter;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new ChatListAdapter(HomeScreenActivity.this);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(HomeScreenActivity.this));

        binding.recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();

        onButtonClick();


    }

    private void onButtonClick(){
        binding.addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeScreenActivity.this,AddFriendActivity.class));
            }
        });

        binding.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                startActivity(new Intent(HomeScreenActivity.this, LoginActivity.class));
                finishAffinity();
            }
        });
    }
}