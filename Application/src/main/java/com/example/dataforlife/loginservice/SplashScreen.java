package com.example.dataforlife.loginservice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.dataforlife.*;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        new Handler().postDelayed(() -> {
            Intent i = new Intent(SplashScreen.this, WelcomeActivity.class);
            startActivity(i);
            finish();
        }, SPLASH_TIME_OUT);
    }
}
