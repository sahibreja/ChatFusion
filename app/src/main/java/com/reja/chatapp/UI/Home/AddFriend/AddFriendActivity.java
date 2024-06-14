package com.reja.chatapp.UI.Home.AddFriend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.reja.chatapp.Adapter.ShowFriendAndSearchAdapter;
import com.reja.chatapp.Interface.Listener.OnAcceptClickListener;
import com.reja.chatapp.Interface.Listener.OnAddFriendClickListener;
import com.reja.chatapp.Interface.Listener.OnCancelRequestClickListener;
import com.reja.chatapp.Interface.Listener.OnRejectClickListener;
import com.reja.chatapp.Model.FriendRequest;
import com.reja.chatapp.Model.SearchResult;
import com.reja.chatapp.R;
import com.reja.chatapp.ViewModel.AddFriendViewModel;
import com.reja.chatapp.databinding.ActivityAddFriendBinding;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity implements OnAddFriendClickListener, OnCancelRequestClickListener, OnAcceptClickListener, OnRejectClickListener {
    private ActivityAddFriendBinding binding;
    private ShowFriendAndSearchAdapter adapter;
    private AddFriendViewModel viewModel;
    private List<SearchResult> userList;
    private final Object userListLock = new Object();
    private final Handler handler = new Handler();

    private PopupMenu popupMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        observer();

        onButtonClick();


    }

    private void onButtonClick(){
        binding.searchEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //handler.removeCallbacks(searchRunnable);
                handler.removeCallbacks(searchRunnable,userListLock);
                handler.postDelayed(searchRunnable, 500);

            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        binding.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                constructPopupMenu(view);
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void init() {
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(AddFriendViewModel.class);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(AddFriendActivity.this));
        userList = new ArrayList<>();
        adapter = new ShowFriendAndSearchAdapter(AddFriendActivity.this, userList);
        binding.recyclerView.setAdapter(adapter);
        adapter.setOnAddFriendClickListener(this);
        adapter.setOnCancelRequestClickListener(this);
        adapter.setOnAcceptClickListener(this);
        adapter.setOnRejectClickListener(this);
        initResult();
    }

    private void initResult(){
        showLoading();
        viewModel.getFriendAndRandomUser();
    }
    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            String query = binding.searchEditTxt.getText().toString().trim();
            showLoading();// Get text directly from EditText
            if (!query.isEmpty()) {
                viewModel.perFormSearch(query.toLowerCase());
            } else {
                clearUserListAndNotify();
                initResult();
            }
        }
    };
    @SuppressLint("NotifyDataSetChanged")
    private void clearUserListAndNotify() {
        synchronized (userListLock) {
            userList.clear();
            adapter.notifyDataSetChanged();
        }
    }
    private void observer() {
        viewModel.getSearchResult().observe(this, new Observer<List<SearchResult>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<SearchResult> searchResults) {
                    Log.d("change in search result: ",String.valueOf(searchResults.size()));
                    closeLoading();
                    if (!searchResults.isEmpty()) {
                        closeNoResultFound();
                        adapter.updateData(searchResults);
                    }else{
                        showNoResultFound();
                    }
                    adapter.notifyDataSetChanged();
            }
        });

        viewModel.isSomeoneRequested().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    binding.notificationIndicator.setVisibility(View.VISIBLE);
                }else{
                    binding.notificationIndicator.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getUserActionLiveData().observe(this, new Observer<Boolean>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(Boolean aBoolean) {
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void showNoResultFound(){
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
    public void onAddFriendClick(FriendRequest friendRequest) {
        viewModel.sendFriendRequest(friendRequest);
    }
    @Override
    public void onCancelFriendRequest(FriendRequest friendRequest) {
        viewModel.cancelFriendRequest(friendRequest);
    }
    @Override
    public void onAcceptClick(FriendRequest request) {
        viewModel.acceptFriendRequest(request);
    }
    @Override
    public void onRejectClick(FriendRequest friendRequest) {
       viewModel.rejectFriendRequest(friendRequest);
    }
    private void showCustomPopupMenu(View anchorView) {
        // Inflate the custom layout/view
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.add_friend_menu_layout, null);

        // Create the PopupWindow
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // Allows tapping outside the popup to dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // Set up the buttons inside the popup
         Button receivedRequestsButton = popupView.findViewById(R.id.received_requests);
         Button sentRequestsButton = popupView.findViewById(R.id.sent_requests);
         Button addRandomButton = popupView.findViewById(R.id.addRandom);


         addRandomButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(AddFriendActivity.this,ShowFriendActivity.class);
                 intent.putExtra("id",0);
                 startActivity(intent);
                 popupWindow.dismiss();
             }
         });

        receivedRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFriendActivity.this,ShowFriendActivity.class);
                intent.putExtra("id",2);
                startActivity(intent);
                popupWindow.dismiss();
            }
        });

        sentRequestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddFriendActivity.this,ShowFriendActivity.class);
                intent.putExtra("id",1);
                startActivity(intent);
                popupWindow.dismiss();
            }
        });

        // Show the PopupWindow anchored to the view
        popupWindow.showAsDropDown(anchorView, 0, -10); // Adjust x and y offsets as needed
    }
    private void constructPopupMenu(View view) {
        showCustomPopupMenu(view);
    }


}
