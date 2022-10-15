package com.baadalletta.helper.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.baadalletta.helper.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;

public class PreviewImageActivity extends AppCompatActivity {

    private ImageView img_close;
    private PhotoView img_zoom;
    String foto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_image);
        img_close = findViewById(R.id.img_close);
        img_zoom = findViewById(R.id.img_zoom);

        foto = getIntent().getStringExtra("foto");
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(PreviewImageActivity.this)
                .load(foto)
                .apply(options)
                .into(img_zoom);

        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}