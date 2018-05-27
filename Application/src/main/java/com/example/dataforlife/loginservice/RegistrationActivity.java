package com.example.dataforlife.loginservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.dataforlife.R;

/**
 * Author Yousria
 */
public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        Button register = (Button) findViewById(R.id.register_registration);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrationActivity.this, TermsOfUseActivity.class);
                startActivity(intent);
            }
        });

        Button cancel = (Button) findViewById(R.id.cancel_registration);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrationActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
