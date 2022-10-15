package com.baadalletta.helper.ui;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.baadalletta.helper.R;

public class PreviewVideoActivity extends AppCompatActivity {
    boolean paused = false;

    private LinearLayout rl_continer_dialog;
    private LinearLayout ll_replay;
    private LinearLayout ll_play;

    private ProgressBar progres_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video);

        progres_bar = findViewById(R.id.progres_bar);
        rl_continer_dialog = findViewById(R.id.rl_continer_dialog);
        ll_replay = findViewById(R.id.ll_replay);
        ll_play = findViewById(R.id.ll_play);
        rl_continer_dialog.setVisibility(View.GONE);

        String video = getIntent().getStringExtra("video");
        VideoView video_View = findViewById(R.id.video_View);
        ImageView img_close = findViewById(R.id.img_close);

        video_View.setVideoPath(video);

        video_View.start();

        video_View.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                video_View.pause();
                rl_continer_dialog.setVisibility(View.VISIBLE);
                return true;
            }
        });

        ll_replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_continer_dialog.setVisibility(View.GONE);
                progres_bar.setVisibility(View.VISIBLE);
                video_View.stopPlayback();
                video_View.setVideoPath(video);
                video_View.start();
                video_View.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        progres_bar.setVisibility(View.GONE);
                    }
                });
            }
        });

        ll_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rl_continer_dialog.setVisibility(View.GONE);
//                progres_bar.setVisibility(View.VISIBLE);
                video_View.start();

                video_View.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        progres_bar.setVisibility(View.GONE);
                    }
                });
            }
        });

        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        video_View.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progres_bar.setVisibility(View.GONE);
            }
        });

    }
}