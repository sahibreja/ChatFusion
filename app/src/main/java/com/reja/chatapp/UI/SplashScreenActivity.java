package com.reja.chatapp.UI;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseUser;
import com.reja.chatapp.MainActivity;
import com.reja.chatapp.R;
import com.reja.chatapp.UI.Authentication.LoginActivity;
import com.reja.chatapp.UI.Authentication.SignUpActivity;
import com.reja.chatapp.UI.Home.HomeScreenActivity;
import com.reja.chatapp.ViewModel.AuthViewModel;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {
    private AuthViewModel viewModel;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getApplication())).get(AuthViewModel.class);

        viewModel.getUserData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebase) {
               firebaseUser = firebase;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(firebaseUser!=null){
                    startActivity(new Intent(SplashScreenActivity.this, HomeScreenActivity.class));
                }else{
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                }
                finish();
            }
        },2000);
    }

}