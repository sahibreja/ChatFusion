package com.reja.chatapp.UI.Home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.reja.chatapp.Adapter.ChatListAdapter;
import com.reja.chatapp.Model.Conversation;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Authentication.LoginActivity;
import com.reja.chatapp.ViewModel.ChatViewModel;
import com.reja.chatapp.ViewModel.ConversationViewModel;
import com.reja.chatapp.databinding.ActivityHomeScreenBinding;

import java.util.List;
import java.util.Objects;

public class HomeScreenActivity extends AppCompatActivity {
    private ActivityHomeScreenBinding binding;
    private ChatListAdapter adapter;
    private FirebaseAuth auth;
    private ConversationViewModel conversationViewModel;
    private String userId;

    private List<Conversation> conversationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        observer();
        onButtonClick();


    }

    private void init(){
        conversationViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(ConversationViewModel.class);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.recyclerView.setLayoutManager(layoutManager);
        auth = FirebaseAuth.getInstance();
        userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        showLoading();
    }

    private void observer(){
        conversationViewModel.getListOfConversation(userId).observe(this, new Observer<List<Conversation>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<Conversation> conversations) {
                closeLoading();
                if (conversations!=null && !conversations.isEmpty()){
                    closeNoResultFound();
                    conversationList =conversations;
                    adapter = new ChatListAdapter(HomeScreenActivity.this,conversationList);
                    binding.recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }else {
                    showNoResultFound();
                }

            }
        });

        conversationViewModel.getUserName().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s!=null){
                    String name = "Hi, "+s+"  \uD83D\uDC4B";
                    binding.userName.setText(name);
                }else{
                    binding.userName.setText("");
                }
            }
        });
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

    private void showNoResultFound(){
        View view = binding.noResultLayout;
        RelativeLayout mainLayout = view.findViewById(R.id.mainLayout);
        ImageView icon = view.findViewById(R.id.search_icon);
        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        TextView startConvoBtn = view.findViewById(R.id.startConvoBtn);

        mainLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        icon.setImageResource(R.drawable.chat_bubble);
        title.setText("No Conversation, Yet!");
        description.setText("You have no conversation with anyone yet!");
        startConvoBtn.setVisibility(View.VISIBLE);
        startConvoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeScreenActivity.this,AddFriendActivity.class));
            }
        });

        binding.noResultLayout.setVisibility(View.VISIBLE);
    }
    private void closeNoResultFound(){
        binding.noResultLayout.setVisibility(View.GONE);
    }

    private void showLoading(){
        binding.loadingLayout.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.INVISIBLE);
        closeNoResultFound();
    }
    private void closeLoading(){
        binding.loadingLayout.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        conversationViewModel.setOnlineStatus(userId,true);
    }



    @Override
    protected void onStop() {
        super.onStop();
        conversationViewModel.setOnlineStatus(userId,false);
    }


}