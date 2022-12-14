package com.baadalletta.helper.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.baadalletta.helper.R;

public class SplashActivity extends AppCompatActivity {
    private int waktu_loading = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent login = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(login);
                finish();

            }
        }, waktu_loading);
    }
}