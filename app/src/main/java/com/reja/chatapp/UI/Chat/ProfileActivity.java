package com.reja.chatapp.UI.Chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.reja.chatapp.Adapter.ChatListAdapter;
import com.reja.chatapp.Adapter.MutualFriendAdapter;
import com.reja.chatapp.Model.FriendRequest;
import com.reja.chatapp.Model.MutualFriend;
import com.reja.chatapp.Model.ProfileDetails;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Home.HomeScreenActivity;
import com.reja.chatapp.ViewModel.AddFriendViewModel;
import com.reja.chatapp.ViewModel.ProfileViewModel;
import com.reja.chatapp.databinding.ActivityProfileBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private ProfileViewModel viewModel ;

    private String userId;
    private String receiverId;
    private MutualFriendAdapter adapter;
    private List<MutualFriend> mutualFriendList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        init();
        observer();
    }

    private void init(){
       viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(ProfileViewModel.class);
        Intent intent = getIntent();
        receiverId = intent.getStringExtra("userId");
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.recyclerView.setLayoutManager(layoutManager);
    }

    private void observer(){
        viewModel.getProfileDetails(userId,receiverId).observe(this, new Observer<ProfileDetails>() {
            @Override
            public void onChanged(ProfileDetails profileDetails) {
                setUserInterFace(profileDetails);
            }
        });

        viewModel.getMutualFriends(userId,receiverId).observe(this, new Observer<List<MutualFriend>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<MutualFriend> mutualFriends) {
                //closeLoading();
                if (mutualFriends!=null && !mutualFriends.isEmpty()){
                   // closeNoResultFound();
                    mutualFriendList =mutualFriends;
                    adapter = new MutualFriendAdapter(ProfileActivity.this,mutualFriendList);
                    binding.recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }else {
                    //showNoResultFound();
                }
            }
        });
    }

    private void setAdapter(List<MutualFriend> mutualFriends) {

    }

    private void setUserInterFace(ProfileDetails profileDetails) {
        Glide.with(this).load(profileDetails.getUserProfilePicture()).addListener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).apply(RequestOptions.placeholderOf(R.drawable.account).centerCrop())
                .into(binding.profileImage);
        binding.userName.setText(profileDetails.getUserName());
        binding.userBio.setText(profileDetails.getUserBio());

        if(profileDetails.isFriend()){
            binding.isFriendLayout.setVisibility(View.VISIBLE);
            binding.requestLayout.setVisibility(View.GONE);
            binding.addFriendBtn.setVisibility(View.GONE);
        }else if(profileDetails.isIsent()){
            binding.isFriendLayout.setVisibility(View.GONE);
            binding.requestLayout.setVisibility(View.GONE);
            binding.addFriendBtn.setVisibility(View.VISIBLE);
            binding.addFriendBtn.setText("Cancel Request");
            binding.addFriendBtn.setTextColor(ContextCompat.getColor(this, R.color.red));
        }else if(profileDetails.isIReceived()){
            binding.isFriendLayout.setVisibility(View.GONE);
            binding.requestLayout.setVisibility(View.VISIBLE);
            binding.addFriendBtn.setVisibility(View.GONE);
        }else{
            binding.isFriendLayout.setVisibility(View.GONE);
            binding.requestLayout.setVisibility(View.GONE);
            binding.addFriendBtn.setVisibility(View.VISIBLE);
            binding.addFriendBtn.setText("Add Friend");
            binding.addFriendBtn.setTextColor(ContextCompat.getColor(this, R.color.white));
        }

        onButtonClick(profileDetails);
    }

    private void onButtonClick(ProfileDetails profileDetails){
        FriendRequest friendRequest = new FriendRequest(userId,receiverId,"pending");
        binding.addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(profileDetails.isIsent()){
                    viewModel.cancelFriendRequest(friendRequest);
                }else{
                    viewModel.sendFriendRequest(friendRequest);
                }

            }
        });

        binding.acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.acceptFriendRequest(friendRequest);
            }
        });

        binding.rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.rejectFriendRequest(friendRequest);
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}