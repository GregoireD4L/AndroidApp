package com.example.dataforlife.loginservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.dataforlife.*;


/**
 * Author Yousria
 */
public class TermsOfUseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_of_use_screen);

        Button agree = findViewById(R.id.agree_terms);
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TermsOfUseActivity.this, InstructionsActivity.class);
                startActivity(intent);
            }
        });
    }

    public void returnHome(View view){
        super.onBackPressed();
    }
}
