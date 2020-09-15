package com.rohit.examples.android.bookstore.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import com.rohit.examples.android.bookstore.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        /*
         * Getting view ID for the TextView
         */
        TextView textViewSplash = findViewById(R.id.splash_text);

        // Setting visibility to hidden from th screen
        textViewSplash.setVisibility(View.INVISIBLE);

        /*
         * Animating the App name using Fade Animation managed through Handler
         */
        AlphaAnimation Fade_in = new AlphaAnimation(0.0f, 1.0f);
        Fade_in.setDuration(2000);
        textViewSplash.startAnimation(Fade_in);
        textViewSplash.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Sending the state to next activity as soon as the previous callbacks ends
                Intent listIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(listIntent);

                finish();
            }
        }, 3000);
    }
}