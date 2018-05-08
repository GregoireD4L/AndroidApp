package com.example.dataforlife.loginservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.dataforlife.R;
import com.example.dataforlife.bluetoothservice.DeviceScanActivity;

public class InstructionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions_screen);

        Button instructions = (Button) findViewById(R.id.instructions);
        instructions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InstructionsActivity.this, DeviceScanActivity.class);
                startActivity(intent);
            }
        });
    }
}
