package com.example.dataforlife.loginservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.dataforlife.R;
import com.example.dataforlife.bluetoothservice.DeviceScanActivity;

public class InstructionsPagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions_pager);

        ViewPager viewPager = (ViewPager) findViewById(R.id.instructions_pager);
        viewPager.setAdapter(new CustomPagerAdapter(this));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 3){
                    Button gotIt = (Button) findViewById(R.id.gotit);
                    gotIt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(InstructionsPagerActivity.this, DeviceScanActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
