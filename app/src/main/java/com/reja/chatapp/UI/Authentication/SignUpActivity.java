package com.reja.chatapp.UI.Authentication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.reja.chatapp.MainActivity;
import com.reja.chatapp.Model.User;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Home.HomeScreenActivity;
import com.reja.chatapp.ViewModel.AuthViewModel;
import com.reja.chatapp.databinding.ActivitySignUpBinding;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private ActivitySignUpBinding binding;
    private ProgressDialog progressDialog;
    private int PICK_IMAGE_REQUEST = 101;
    private Uri imageUri=null;

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        init();
        observer();
        onButtonClick();
    }

    private void init(){
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(AuthViewModel.class);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Sign Up");
        progressDialog.setMessage("Please wait .. \nWe are taking you to the World of Love");
        progressDialog.setCancelable(false);
        progressDialog.setInverseBackgroundForced(false);
    }

    private void observer(){
        viewModel.getLoggedStatus().observe(SignUpActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    viewModel.addUserDeviceToken(userId);
                    startActivity(new Intent(SignUpActivity.this, HomeScreenActivity.class));
                    finishAffinity();
                }
                progressDialog.dismiss();
            }
        });
    }

    private void onButtonClick(){
        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.nameEditTxt.getText().toString().trim();
                String email = binding.emailEditTxt.getText().toString().trim();
                String pass = binding.passEditTxt.getText().toString().trim();
                String confirmPass = binding.confirmPassEditTxt.getText().toString().trim();
                if(checkEntryField(name,email,pass,confirmPass)){
                    User user = new User(name,name.toLowerCase(),email,pass,"");
                    signUp(user);
                }
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            this.imageUri = data.getData();
            binding.profileImg.setImageURI(imageUri);
        }
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

    private boolean checkEntryField(String name,String email,String pass,String confirmPass){

        if(name.isEmpty()){
            binding.nameEditTxt.setError("Please Enter Your Name");
            return false;
        }else if(email.isEmpty()){
            binding.emailEditTxt.setError("Please Enter Your Email");
            return false;
        }else if(pass.isEmpty()){
            binding.passEditTxt.setError("Please Enter Your Password");
            return false;
        }else if(confirmPass.isEmpty()){
            binding.confirmPassEditTxt.setError("Confirm Your Password");
            return false;
        }else{
            if(pass.equals(confirmPass)){
                return true;
            }else{
                binding.confirmPassEditTxt.setError("Your Password is not Matching");
                return false;
            }
        }
    }

    private void signUp(User user){
        progressDialog.show();
        viewModel.signUp(user,imageUri);
    }
}