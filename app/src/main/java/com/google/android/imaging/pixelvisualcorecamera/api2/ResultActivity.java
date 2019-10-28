package com.google.android.imaging.pixelvisualcorecamera.api2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.google.android.imaging.pixelvisualcorecamera.R;


public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
    }

    public void onClick(View view){
        Intent intent = new Intent(this, CameraApi2Activity.class);
        startActivity(intent);
    }
}