package com.evansappwriter.sharkfeed.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.evansappwriter.sharkfeed.R;
import com.evansappwriter.sharkfeed.util.ActivitySwipeDetector;

public class SplashActivity extends AppCompatActivity implements ActivitySwipeDetector.SwipeInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        ActivitySwipeDetector swipe = new ActivitySwipeDetector(this);
        FrameLayout swipe_layout = (FrameLayout) findViewById(R.id.splash_fl);
        swipe_layout.setOnTouchListener(swipe);
    }


    @Override
    public void bottom2top(View v) {
        lanuchSharkPhotosActivity();
    }

    @Override
    public void left2right(View v) {
        lanuchSharkPhotosActivity();
    }

    @Override
    public void right2left(View v) {
        lanuchSharkPhotosActivity();
    }

    @Override
    public void top2bottom(View v) {
        lanuchSharkPhotosActivity();
    }

    private void lanuchSharkPhotosActivity() {
        Intent i = new Intent(this, SharkPhotosActivity.class);
        startActivity(i);
        finish();
    }
}
