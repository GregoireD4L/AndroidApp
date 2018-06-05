package com.example.dataforlife.loginservice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.dataforlife.*;
import com.example.dataforlife.pairservice.PairPagerActivity;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Author Yousria
 */
public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;
    private FirebaseAuth auth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        auth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            //verify if user is authentified
            if (auth.getCurrentUser() != null) {
                startActivity(new Intent(SplashScreen.this, PairPagerActivity.class));
                finish();
            }else {
                Intent i = new Intent(SplashScreen.this, WelcomeActivity.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
