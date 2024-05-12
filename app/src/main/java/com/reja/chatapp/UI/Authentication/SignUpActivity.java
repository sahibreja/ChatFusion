package com.reja.chatapp.UI.Authentication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;
import com.reja.chatapp.MainActivity;
import com.reja.chatapp.Model.User;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Home.HomeScreenActivity;
import com.reja.chatapp.ViewModel.AuthViewModel;
import com.reja.chatapp.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private ActivitySignUpBinding binding;
    private ProgressDialog progressDialog;
    private int PICK_IMAGE_REQUEST = 101;

    private Uri imageUri=null;

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
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
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