package com.baadalletta.helper.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.baadalletta.helper.R;
import com.baadalletta.helper.models.Kategori;
import com.baadalletta.helper.models.Produk;
import com.baadalletta.helper.models.ResponseApi;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.baadalletta.helper.utils.Constanta;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailProdukActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private Produk produk_parcelable;

    private EditText et_kategori;
    private EditText et_nama;
    private EditText et_harga;
    private EditText et_panjang;
    private EditText et_lebar;
    private EditText et_volume;

    private Uri uri_video;
    private Uri uri_foto;
    private Bitmap bitmap_thubnail_video;
    private Bitmap bitmap_thubnail_foto;
    private ImageView img_thumbnail_foto;
    private ImageView img_thumbnail_video;
    private boolean isPhotoNew = false;
    private boolean isVideoNew = false;
    private boolean isPhotoReady = false;
    private boolean isVideoReady = false;
    private ImageView img_help_volume;
    private ImageView img_play;

    private RelativeLayout rl_simpan;

    private ArrayList<Kategori> kategoris;
    private int kategori_id;
    private String produk_id;
    List<String> kategori_list = new ArrayList<String>();
    List<Integer> kategori_id_list = new ArrayList<Integer>();
    private String selected_kategori;

    private CardView cv_foto;
    private CardView cv_video;

    private SwipeRefreshLayout swipe_continer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_produk);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24);

        produk_parcelable = getIntent().getParcelableExtra("DATA_PRODUK");

        swipe_continer = findViewById(R.id.swipe_continer);
        et_kategori = findViewById(R.id.et_kategori);
        et_nama = findViewById(R.id.et_nama);
        et_harga = findViewById(R.id.et_harga);
        et_panjang = findViewById(R.id.et_panjang);
        et_lebar = findViewById(R.id.et_lebar);
        et_volume = findViewById(R.id.et_volume);

        cv_foto = findViewById(R.id.cv_foto);
        cv_video = findViewById(R.id.cv_video);

        img_thumbnail_foto = findViewById(R.id.img_thumbnail_foto);
        img_thumbnail_video = findViewById(R.id.img_thumbnail_video);
        img_help_volume = findViewById(R.id.img_help_volume);
        img_play = findViewById(R.id.img_play);
        rl_simpan = findViewById(R.id.rl_simpan);

        img_play.setVisibility(View.GONE);

        swipe_continer.setOnRefreshListener(this);
        swipe_continer.setColorSchemeResources(R.color.ColorPrimary,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_green_dark);
        swipe_continer.post(new Runnable() {
            @Override
            public void run() {
                initDataIntent(produk_parcelable);
            }
        });

        img_help_volume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickHelpVolume(view);
            }
        });

        cv_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchViewImage();
            }
        });
        cv_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchViewVideo();
            }
        });

        rl_simpan.setOnClickListener(this::clickSimpan);

    }


    private void clickSimpan(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailProdukActivity.this);
        builder.setTitle("Pilih :");
        // add a list
        String[] item = {"Edit Produk", "Hapus"};
        builder.setItems(item, (dialog, which) -> {
            switch (which) {
                case 0:
                    Intent intent = new Intent(DetailProdukActivity.this, DetailProdukActivity.class);
                    intent.putExtra("DATA_PRODUK", produk_parcelable);
                    startActivity(intent);
                    finish();
                    break;
                case 1:
                    startDeletItem(produk_parcelable);
                    break;
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startDeletItem(Produk produk) {

        SweetAlertDialog pDialog = new SweetAlertDialog(DetailProdukActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseApi> responseApiCall = apiInterface.deleteProduk(produk_id);
        responseApiCall.enqueue(new Callback<ResponseApi>() {
            @Override
            public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                pDialog.dismiss();
                if (response.isSuccessful()) {
                    int kode = response.body().getStatus_code();
                    String pesan = response.body().getMessage();
                    if (kode == 200) {
                        new SweetAlertDialog(DetailProdukActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Berhasi")
                                .setContentText("Menghapus produk berhasi")
                                .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        finish();
                                    }
                                })
                                .show();
                    } else {
                        new SweetAlertDialog(DetailProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText(pesan)
                                .show();
                    }
                } else {
                    new SweetAlertDialog(DetailProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Opss..")
                            .setContentText("Terjadi Kesalahan Sistem")
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ResponseApi> call, Throwable t) {
                pDialog.dismiss();
                new SweetAlertDialog(DetailProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Mohon Maaf.")
                        .setContentText(t.getMessage())
                        .show();

            }
        });
    }


    private void launchViewImage() {
        if (isPhotoReady) {
            Intent intent = new Intent(DetailProdukActivity.this, PreviewImageActivity.class);
            intent.putExtra("foto", Constanta.URL_PHOTO_PRODUK + produk_parcelable.getFoto());
            startActivity(intent);
        } else if (uri_foto == null) {
            new SweetAlertDialog(DetailProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Maaf..")
                    .setContentText("Foto Belum Tersedia.")
                    .show();
        } else {
            Intent intent = new Intent(DetailProdukActivity.this, PreviewImageActivity.class);
            intent.putExtra("foto", uri_foto.toString());
            startActivity(intent);
        }
    }

    private void launchViewVideo() {
        if (isVideoReady) {
            Intent intent = new Intent(DetailProdukActivity.this, PreviewImageActivity.class);
            intent.putExtra("foto", Constanta.URL_VIDEO_PRODUK + produk_parcelable.getVideo());
            startActivity(intent);
        } else if (uri_video == null) {
            new SweetAlertDialog(DetailProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Maaf..")
                    .setContentText("Video Belum Tersedia.")
                    .show();
        } else {
            Intent intent = new Intent(DetailProdukActivity.this, PreviewVideoActivity.class);
            intent.putExtra("video", uri_video.toString());
            startActivity(intent);
        }
    }

    private void initDataIntent(Produk produk_parcelable) {
        produk_id = String.valueOf(produk_parcelable.getId());
//        Toast.makeText(EditProdukActivity.this, produk_id, Toast.LENGTH_SHORT).show();
        kategori_id = produk_parcelable.getId_kategori();
        selected_kategori = String.valueOf(kategori_id);
        et_kategori.setText(produk_parcelable.getKategori().get(0).getNama());
        String nama = produk_parcelable.getNama();
        et_nama.setText(nama);
        String harga = String.valueOf(produk_parcelable.getHarga());
        et_harga.setText(harga);
        String panjang = String.valueOf(produk_parcelable.getPanjang());
        et_panjang.setText(panjang);
        String lebar = String.valueOf(produk_parcelable.getLebar());
        et_lebar.setText(lebar);
        String volume = String.valueOf(produk_parcelable.getVolume());
        et_volume.setText(volume);

        String foto = produk_parcelable.getFoto();

        if (foto != null) {
            uri_foto = Uri.parse(Constanta.URL_PHOTO_PRODUK + foto);
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);

            Glide.with(DetailProdukActivity.this)
                    .load(Constanta.URL_PHOTO_PRODUK + foto)
                    .apply(options)
                    .into(img_thumbnail_foto);
        }

        String video = produk_parcelable.getVideo();
        if (video != null) {
            uri_video = Uri.parse(Constanta.URL_VIDEO_PRODUK + video);
            img_play.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .asBitmap()
                    .load(Constanta.URL_VIDEO_PRODUK + video)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            img_thumbnail_video.setImageBitmap(resource);
                        }
                    });
        }
        swipe_continer.setRefreshing(false);
    }

    private void clickHelpVolume(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailProdukActivity.this);
        builder.setTitle("Info");
        builder.setIcon(R.drawable.ic_baseline_help_24);
        builder.setMessage("Volume ini diartikan jumlah ikan dalam satu kilogram.");
        builder.setPositiveButton("Tutup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRefresh() {
        initDataIntent(produk_parcelable);
    }
}