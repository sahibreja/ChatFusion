package com.reja.chatapp.UI.Home.PersonalProfile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.reja.chatapp.Model.User;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Authentication.LoginActivity;
import com.reja.chatapp.UI.Home.AddFriend.AddFriendActivity;
import com.reja.chatapp.UI.Home.AddFriend.ShowFriendActivity;
import com.reja.chatapp.UI.Home.HomeScreenActivity;
import com.reja.chatapp.ViewModel.ConversationViewModel;
import com.reja.chatapp.ViewModel.PersonalProfileViewModel;
import com.reja.chatapp.databinding.ActivityPersonalProfileBinding;

public class PersonalProfileActivity extends AppCompatActivity {
    private ActivityPersonalProfileBinding binding;
    private FirebaseAuth auth;
    private PersonalProfileViewModel viewModel;
    private int PICK_IMAGE_REQUEST = 101;
    private Uri imageUri=null;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    private enum EditType {UserProfilePicture,UserName,UserBio};
    private PopupMenu popupMenu;

    private String profilePictureUrl;
    private AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        observer();

        onButtonClick();
    }

    private void init(){
        auth = FirebaseAuth.getInstance();
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(PersonalProfileViewModel.class);
        builder = new AlertDialog.Builder(PersonalProfileActivity.this);

    }

    private void showAlertDialog() {
        builder.setTitle("Log Out !");
        builder.setMessage("Do you want to logout ?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            auth.signOut();
            startActivity(new Intent(PersonalProfileActivity.this, LoginActivity.class));
            finishAffinity();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog -> {
            // Change the color of the Yes button
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.secondary_color));

            // Change the color of the No button
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.secondary_color));
        });
        alertDialog.show();
    }



    private void observer(){
        viewModel.getUserData().observe(this, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                updateUI(user);
            }
        });
    }

    private void onButtonClick(){
        binding.backBtn.setOnClickListener((view)->{
            finish();
        });

        binding.logoutBtn.setOnClickListener((view)->{

            showAlertDialog();
        });

        binding.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGalleryForImage();
            }
        });

        binding.profileImageEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGalleryForImage();
            }
        });

        binding.nameEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomPopupMenu(EditType.UserName);
            }
        });
        binding.aboutMeEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomPopupMenu(EditType.UserBio);
            }
        });
    }

    private void openGalleryForImage(){
        if (checkImagePermission()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            requestImagePermission();
        }
    }


    private void updateUI(User user){
        binding.userName.setText(user.getUserName());
        binding.userNameTxt.setText(user.getUserName());
        binding.emailTxt.setText(user.getUserEmail());
        binding.aboutMeTxt.setText(user.getUserBio());
        profilePictureUrl = user.getUserProfilePicture();
        setProfilePicture();

    }

    private boolean checkImagePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        }else{
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestImagePermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_REQUEST_CODE);
        }else{
            requestStoragePermission();
        }
    }
    private void requestStoragePermission() {
        // Request read and write external storage permissions
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            this.imageUri = data.getData();
            binding.profilePicture.setImageURI(imageUri);
            showCustomPopupMenu(EditType.UserProfilePicture);

        }
    }

    private void showCustomPopupMenu(EditType type) {
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
        setTitleAndHint(type,editText,titleTextView);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedValue = getUpdatedEditTextValue(editText);
                if(type == EditType.UserName){
                    viewModel.updateUserName(updatedValue);
                }else if(type == EditType.UserBio) {
                    viewModel.updateUserBio(updatedValue);
                }else if(type == EditType.UserProfilePicture){
                    viewModel.updateUserProfileImage(imageUri);
                }
                popupWindow.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(type == EditType.UserProfilePicture){
                    setProfilePicture();
                }
                popupWindow.dismiss();
            }
        });

        // Show the PopupWindow anchored to a view
        View anchorView = findViewById(R.id.main); // Replace with an actual view ID to anchor the popup
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0); // Show at center of the anchor view
    }

    private void setTitleAndHint(EditType editType, EditText editText,TextView titleTextView){
        if(editType == EditType.UserName){
            editText.setHint("Enter your name");
            editText.setText(binding.userNameTxt.getText().toString());
            titleTextView.setText("Update Name");
        }else if(editType == EditType.UserBio){
            editText.setHint("Enter your bio");
            editText.setText(binding.aboutMeTxt.getText().toString());
            titleTextView.setText("Update About Me");
        }else if(editType == EditType.UserProfilePicture){
            titleTextView.setText("Do you want to update your Profile Picture?");
            editText.setVisibility(View.GONE);

        }
    }

    private void setProfilePicture(){
        if(profilePictureUrl.isEmpty()){
            binding.profilePicture.setImageResource(R.drawable.account);
        }else{
            Glide.with(this).load(profilePictureUrl).into(binding.profilePicture);
        }
    }

    private String getUpdatedEditTextValue(EditText editText){
        String updatedValue = editText.getText().toString();

        return updatedValue;
    }

}