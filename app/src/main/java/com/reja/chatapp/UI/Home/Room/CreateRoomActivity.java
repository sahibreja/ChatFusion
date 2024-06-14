package com.reja.chatapp.UI.Home.Room;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.reja.chatapp.Model.User;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Home.PersonalProfile.PersonalProfileActivity;
import com.reja.chatapp.ViewModel.PersonalProfileViewModel;
import com.reja.chatapp.ViewModel.RoomViewModel;
import com.reja.chatapp.databinding.ActivityCreateRoomBinding;

public class CreateRoomActivity extends AppCompatActivity {
    private ActivityCreateRoomBinding binding;
    private enum Type{CREATE,JOIN};
    private RoomViewModel viewModel;
    private PopupMenu popupMenu;

    private User userData;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(R.color.purple);
        init();
        observer();
        onButtonClick();
    }


    private void init(){
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(RoomViewModel.class);
    }

    private void observer(){
        viewModel.getUserDetails().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                userData = user;
            }
        });

        viewModel.getUserActionLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s!=null){
                    Intent intent = new Intent(CreateRoomActivity.this,RoomActivity.class);
                    intent.putExtra("room_id",s);
                    startActivity(intent);
                }
            }
        });
    }

    private void onButtonClick(){
        binding.createBtn.setOnClickListener((view) ->{
            showCustomPopupMenu(Type.CREATE);
        });

        binding.joinBtn.setOnClickListener((view)->{
            showCustomPopupMenu(Type.JOIN);
        });

        binding.backBtn.setOnClickListener((view)->{
            finish();
        });
    }

    private void showCustomPopupMenu(Type type) {
        // Inflate the custom layout/view
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.update_layout, null);

        // Create the PopupWindow
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // Allows tapping outside the popup to dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // Set up the buttons inside the popup
        Button updateButton = popupView.findViewById(R.id.btn_update);
        Button cancelButton = popupView.findViewById(R.id.btn_cancel);
        TextView titleTextView = popupView.findViewById(R.id.popup_title);
        EditText editText = popupView.findViewById(R.id.edit_query);
        setTitleAndHint(type,editText,titleTextView,updateButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if(type == Type.CREATE){
                    viewModel.createRoom(text);
                }else if(type == Type.JOIN) {
                    if(userData!=null && userData.getUserId()!=null){
                        viewModel.joinRoom(userData.getUserId(),userData.getUserName(),userData.getUserProfilePicture(),text);
                    }

                }
                popupWindow.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                popupWindow.dismiss();
            }
        });

        // Show the PopupWindow anchored to a view
        View anchorView = findViewById(R.id.main); // Replace with an actual view ID to anchor the popup
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0); // Show at center of the anchor view
    }

    private void setTitleAndHint(Type type, EditText editText, TextView titleTextView, Button updateButton){

        if(type == Type.CREATE){
            editText.setHint("Enter url of movie you want to watch");
            updateButton.setText("Create");
            titleTextView.setText("Create Room");
        }else if(type == Type.JOIN){
            editText.setHint("Enter room id you want to join");
            updateButton.setText("Join");
            titleTextView.setText("Join Room");
        }
    }


}