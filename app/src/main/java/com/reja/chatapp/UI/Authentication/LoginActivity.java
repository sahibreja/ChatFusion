package com.reja.chatapp.UI.Authentication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.reja.chatapp.MainActivity;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Home.HomeScreenActivity;
import com.reja.chatapp.ViewModel.AuthViewModel;
import com.reja.chatapp.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        observer();
        onButtonClick();
    }

    private void onButtonClick(){
        binding.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignUpActivity.class));
            }
        });

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.emailEditTxt.getText().toString().trim();
                String pass = binding.passwordEditTxt.getText().toString().trim();
                if(checkEntryField(email,pass)){
                    login(email,pass);
                }
            }
        });

    }

    private boolean checkEntryField(String email, String pass){
        if(email.isEmpty()){
            binding.emailEditTxt.setError("Please enter your email");
            return false;
        }else if(pass.isEmpty()){
            binding.passwordEditTxt.setError("Please enter your password");
            return false;
        }else{
            return true;
        }
    }

    private void login(String email,String password){
        progressDialog.show();
        viewModel.logIn(email,password);
    }

    private void observer(){
        viewModel.getLoggedStatus().observe(LoginActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                    viewModel.addUserDeviceToken(uid);
                    startActivity(new Intent(LoginActivity.this, HomeScreenActivity.class));
                    finish();
                }
                progressDialog.dismiss();
            }
        });
    }


    private void init(){
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(AuthViewModel.class);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Please wait .. \nWe are taking you to the World of Love");
        progressDialog.setCancelable(false);
        progressDialog.setInverseBackgroundForced(false);
        auth = FirebaseAuth.getInstance();

    }

}