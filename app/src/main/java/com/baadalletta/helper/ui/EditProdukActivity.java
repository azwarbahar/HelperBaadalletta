package com.baadalletta.helper.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.baadalletta.helper.R;
import com.baadalletta.helper.models.Kategori;
import com.baadalletta.helper.models.Produk;
import com.baadalletta.helper.models.ResponseApi;
import com.baadalletta.helper.models.ResponseKategori;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.baadalletta.helper.utils.Constanta;
import com.baadalletta.helper.utils.VideoPickerOptionListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProdukActivity extends AppCompatActivity {

    private Produk produk_parcelable;

    private static int CAMERA_PERMISSION_CODE = 100;
    private static int VIDEO_RECORD_CODE = 111;
    private static final String TAG = EditProdukActivity.class.getSimpleName();
    public static final int REQUEST_IMAGE = 110;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_produk);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24);

        produk_parcelable = getIntent().getParcelableExtra("DATA_PRODUK");

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

        et_kategori.setOnClickListener(this::clickKategori);
        img_help_volume.setOnClickListener(this::clickHelpVolume);
        cv_foto.setOnClickListener(this::clickFoto);
        cv_video.setOnClickListener(this::clickVideo);

        initDataIntent(produk_parcelable);
        loadDataKategori();

        rl_simpan.setOnClickListener(this::clickSimpan);

    }

    private void clickSimpan(View view) {
        SweetAlertDialog sweetAlertDialogError = new SweetAlertDialog(EditProdukActivity.this,
                SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialogError.setTitleText("Edit");
        sweetAlertDialogError.setContentText("Kirim data produk?");
        sweetAlertDialogError.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                checkInput();
            }
        });
        sweetAlertDialogError.setCancelButton("Batal", new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
            }
        });
        sweetAlertDialogError.show();
    }

    private void checkInput() {
        String kategori_send = selected_kategori;
        String nama_send = et_nama.getText().toString();
        String harga_send = et_harga.getText().toString();
        String panjang_send = et_panjang.getText().toString();
        String lebar_send = et_lebar.getText().toString();
        String volume_send = et_volume.getText().toString();

        if (kategori_send.isEmpty()) {
            et_kategori.setError("Lengkapi");
        } else if (nama_send.isEmpty()) {
            et_nama.setError("Lengkapi");
        } else if (harga_send.isEmpty()) {
            et_harga.setError("Lengkapi");
        } else if (panjang_send.isEmpty()) {
            et_panjang.setError("Lengkapi");
        } else if (lebar_send.isEmpty()) {
            et_lebar.setError("Lengkapi");
        } else if (volume_send.isEmpty()) {
            et_volume.setError("Lengkapi");
        } else if (uri_foto == null) {
            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Maaf..")
                    .setContentText("Foto Produk Belum Ada")
                    .show();
        } else if (uri_video == null) {
            startUploadProduk(kategori_send, nama_send, harga_send, panjang_send, lebar_send,
                    volume_send, uri_foto);
        } else {
            startUploadProdukWithVideo(kategori_send, nama_send, harga_send, panjang_send, lebar_send,
                    volume_send, uri_foto, uri_video);
        }
    }

    private void startUploadProduk(String kategori_send, String nama_send, String harga_send,
                                   String panjang_send, String lebar_send, String volume_send,
                                   Uri uri_foto) {

        SweetAlertDialog pDialog = new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        if (isPhotoNew) {
            File file = new File(uri_foto.getPath());
            RequestBody foto = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part foto_pesanan_send = MultipartBody.Part.createFormData("foto", file.getName(), foto);
            RequestBody kategori = RequestBody.create(MediaType.parse("text/plain"), kategori_send);
            RequestBody nama = RequestBody.create(MediaType.parse("text/plain"), nama_send);
            RequestBody harga = RequestBody.create(MediaType.parse("text/plain"), harga_send);
            RequestBody panjang = RequestBody.create(MediaType.parse("text/plain"), panjang_send);
            RequestBody lebar = RequestBody.create(MediaType.parse("text/plain"), lebar_send);
            RequestBody volume = RequestBody.create(MediaType.parse("text/plain"), volume_send);

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ResponseApi> responseApiCall = apiInterface.updateProduk(produk_id, kategori, nama, harga, panjang,
                    lebar, volume, foto_pesanan_send);
            responseApiCall.enqueue(new Callback<ResponseApi>() {
                @Override
                public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                    pDialog.dismiss();
                    if (response.isSuccessful()) {
                        int kode = response.body().getStatus_code();
                        String pesan = response.body().getMessage();
                        if (kode == 200) {

                            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Berhasi")
                                    .setContentText("Edit Produk Berhasi")
                                    .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            finish();
                                        }
                                    })
                                    .show();

                        } else {
                            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Opss..")
                                    .setContentText(pesan)
                                    .show();
                        }
                    } else {
                        new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText("Terjadi Kesalahan Sistem1")
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseApi> call, Throwable t) {
                    pDialog.dismiss();
                    new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Mohon Maaf.")
                            .setContentText(t.getMessage())
                            .show();

                }
            });

        } else {
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ResponseApi> responseApiCall = apiInterface.updateProdukNoFile(produk_id, kategori_send,
                    nama_send, harga_send, panjang_send, lebar_send, volume_send);
            responseApiCall.enqueue(new Callback<ResponseApi>() {
                @Override
                public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                    pDialog.dismiss();
                    if (response.isSuccessful()) {
                        int kode = response.body().getStatus_code();
                        String pesan = response.body().getMessage();
                        if (kode == 200) {

                            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Berhasi")
                                    .setContentText("Edit Produk Berhasi")
                                    .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            finish();
                                        }
                                    })
                                    .show();

                        } else {
                            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Opss..")
                                    .setContentText(pesan)
                                    .show();
                        }
                    } else {
                        new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText("Terjadi Kesalahan Sistem1")
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseApi> call, Throwable t) {
                    pDialog.dismiss();
                    new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Mohon Maaf.")
                            .setContentText(t.getMessage())
                            .show();

                }
            });
        }
    }

    private void startUploadProdukWithVideo(String kategori_send, String nama_send,
                                            String harga_send, String panjang_send, String lebar_send,
                                            String volume_send, Uri uri_foto, Uri uri_video) {

        SweetAlertDialog pDialog = new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        if (isPhotoNew) {
            if (isVideoNew) {
                File file = new File(uri_foto.getPath());
                RequestBody foto = RequestBody.create(MediaType.parse("*/*"), file);
                MultipartBody.Part foto_pesanan_send = MultipartBody.Part.createFormData("foto", file.getName(), foto);

                File file2 = new File(getPath(uri_video));
                RequestBody video = RequestBody.create(MediaType.parse("*/*"), file2);
                MultipartBody.Part video_pesanan_send = MultipartBody.Part.createFormData("video", file2.getName(), video);

                RequestBody kategori = RequestBody.create(MediaType.parse("text/plain"), kategori_send);
                RequestBody nama = RequestBody.create(MediaType.parse("text/plain"), nama_send);
                RequestBody harga = RequestBody.create(MediaType.parse("text/plain"), harga_send);
                RequestBody panjang = RequestBody.create(MediaType.parse("text/plain"), panjang_send);
                RequestBody lebar = RequestBody.create(MediaType.parse("text/plain"), lebar_send);
                RequestBody volume = RequestBody.create(MediaType.parse("text/plain"), volume_send);

                ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                Call<ResponseApi> responseApiCall = apiInterface.updateProdukWithVideo(produk_id, kategori, nama, harga,
                        panjang, lebar, volume, foto_pesanan_send, video_pesanan_send);
                responseApiCall.enqueue(new Callback<ResponseApi>() {
                    @Override
                    public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                        pDialog.dismiss();
                        if (response.isSuccessful()) {
                            int kode = response.body().getStatus_code();
                            String pesan = response.body().getMessage();
                            if (kode == 200) {
                                new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Berhasi")
                                        .setContentText("Edit Produk Berhasi")
                                        .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                sweetAlertDialog.dismiss();
                                                finish();
                                            }
                                        })
                                        .show();

                            } else {
                                new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Opss..")
                                        .setContentText(pesan)
                                        .show();
                            }
                        } else {
                            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Opss..")
                                    .setContentText("Terjadi Kesalahan Sistem")
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseApi> call, Throwable t) {
                        pDialog.dismiss();
                        new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Mohon Maaf.")
                                .setContentText(t.getMessage())
                                .show();
                    }
                });

            } else {
                File file = new File(uri_foto.getPath());
                RequestBody foto = RequestBody.create(MediaType.parse("image/*"), file);
                MultipartBody.Part foto_pesanan_send = MultipartBody.Part.createFormData("foto", file.getName(), foto);
                RequestBody kategori = RequestBody.create(MediaType.parse("text/plain"), kategori_send);
                RequestBody nama = RequestBody.create(MediaType.parse("text/plain"), nama_send);
                RequestBody harga = RequestBody.create(MediaType.parse("text/plain"), harga_send);
                RequestBody panjang = RequestBody.create(MediaType.parse("text/plain"), panjang_send);
                RequestBody lebar = RequestBody.create(MediaType.parse("text/plain"), lebar_send);
                RequestBody volume = RequestBody.create(MediaType.parse("text/plain"), volume_send);

                ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                Call<ResponseApi> responseApiCall = apiInterface.updateProduk(produk_id, kategori, nama, harga, panjang,
                        lebar, volume, foto_pesanan_send);
                responseApiCall.enqueue(new Callback<ResponseApi>() {
                    @Override
                    public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                        pDialog.dismiss();
                        if (response.isSuccessful()) {
                            int kode = response.body().getStatus_code();
                            String pesan = response.body().getMessage();
                            if (kode == 200) {
                                new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Berhasi")
                                        .setContentText("Edit Produk Berhasi")
                                        .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                sweetAlertDialog.dismiss();
                                                finish();
                                            }
                                        })
                                        .show();

                            } else {
                                new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Opss..")
                                        .setContentText(pesan)
                                        .show();
                            }
                        } else {
                            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Opss..")
                                    .setContentText("Terjadi Kesalahan Sistem1")
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseApi> call, Throwable t) {
                        pDialog.dismiss();
                        new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Mohon Maaf.")
                                .setContentText(t.getMessage())
                                .show();
                    }
                });
            }
        } else if (isVideoNew) {
            File file2 = new File(getPath(uri_video));
            RequestBody video = RequestBody.create(MediaType.parse("*/*"), file2);
            MultipartBody.Part video_pesanan_send = MultipartBody.Part.createFormData("video", file2.getName(), video);

            RequestBody kategori = RequestBody.create(MediaType.parse("text/plain"), kategori_send);
            RequestBody nama = RequestBody.create(MediaType.parse("text/plain"), nama_send);
            RequestBody harga = RequestBody.create(MediaType.parse("text/plain"), harga_send);
            RequestBody panjang = RequestBody.create(MediaType.parse("text/plain"), panjang_send);
            RequestBody lebar = RequestBody.create(MediaType.parse("text/plain"), lebar_send);
            RequestBody volume = RequestBody.create(MediaType.parse("text/plain"), volume_send);

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ResponseApi> responseApiCall = apiInterface.updateProdukVideo(produk_id, kategori, nama, harga,
                    panjang, lebar, volume, video_pesanan_send);
            responseApiCall.enqueue(new Callback<ResponseApi>() {
                @Override
                public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                    pDialog.dismiss();
                    if (response.isSuccessful()) {
                        int kode = response.body().getStatus_code();
                        String pesan = response.body().getMessage();
                        if (kode == 200) {
                            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Berhasi")
                                    .setContentText("Edit Produk Berhasi")
                                    .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            finish();
                                        }
                                    })
                                    .show();

                        } else {
                            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Opss..")
                                    .setContentText(pesan)
                                    .show();
                        }
                    } else {
                        new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText("Terjadi Kesalahan Sistem")
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseApi> call, Throwable t) {
                    pDialog.dismiss();
                    new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Mohon Maaf.")
                            .setContentText(t.getMessage())
                            .show();
                }
            });

        } else {
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ResponseApi> responseApiCall = apiInterface.updateProdukNoFile(produk_id, kategori_send,
                    nama_send, harga_send, panjang_send, lebar_send, volume_send);
            responseApiCall.enqueue(new Callback<ResponseApi>() {
                @Override
                public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                    pDialog.dismiss();
                    if (response.isSuccessful()) {
                        int kode = response.body().getStatus_code();
                        String pesan = response.body().getMessage();
                        if (kode == 200) {
                            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                    .setTitleText("Berhasi")
                                    .setContentText("Edit Produk Berhasi")
                                    .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            finish();
                                        }
                                    })
                                    .show();

                        } else {
                            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Opss..")
                                    .setContentText(pesan)
                                    .show();
                        }
                    } else {
                        new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText("Terjadi Kesalahan Sistem1")
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseApi> call, Throwable t) {
                    pDialog.dismiss();
                    new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Mohon Maaf.")
                            .setContentText(t.getMessage())
                            .show();

                }
            });
        }
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void clickVideo(View view) {
        Dexter.withActivity(EditProdukActivity.this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showVideoPickerOptions();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public static void showVideoPickerOptions(Context context, VideoPickerOptionListener listener) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.lbl_set_profile_video));

        // add a list
        String[] animals = {context.getString(R.string.lbl_take_camera_video), context.getString(R.string.lbl_show_video)};
        builder.setItems(animals, (dialog, which) -> {
            switch (which) {
                case 0:
                    listener.onTakeCameraSelected();
                    break;
                case 1:
                    listener.onViewVideo();
                    break;
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showVideoPickerOptions() {
        showVideoPickerOptions(EditProdukActivity.this, new VideoPickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                recordVideo();
            }

            @Override
            public void onViewVideo() {
                launchViewVideo();
            }
        });

    }

    private void launchViewVideo() {
        if (isVideoReady) {
            Intent intent = new Intent(EditProdukActivity.this, PreviewImageActivity.class);
            intent.putExtra("foto", Constanta.URL_VIDEO_PRODUK + produk_parcelable.getVideo());
            startActivity(intent);
        } else if (uri_video == null) {
            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Maaf..")
                    .setContentText("Video Belum Tersedia.")
                    .show();
        } else {
            Intent intent = new Intent(EditProdukActivity.this, PreviewVideoActivity.class);
            intent.putExtra("video", uri_video.toString());
            startActivity(intent);
        }
    }

    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, VIDEO_RECORD_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_RECORD_CODE) {
            if (resultCode == RESULT_OK) {
                uri_video = data.getData();
                isVideoReady = false;
                isVideoNew = true;
                Glide.with(this)
                        .asBitmap()
                        .load(uri_video)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                img_thumbnail_video.setImageBitmap(resource);
                            }
                        });
            }
        } else if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    bitmap_thubnail_foto = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    uri_foto = uri;
                    isPhotoReady = false;
                    isPhotoNew = true;
                    img_play.setVisibility(View.VISIBLE);
                    Glide.with(this)
                            .asBitmap()
                            .load(bitmap_thubnail_foto)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                    img_thumbnail_foto.setImageBitmap(resource);
                                }
                            });
//                    startUpdatePhoto(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private void clickFoto(View view) {
        ImagePickerActivity.clearCache(EditProdukActivity.this);
        Dexter.withActivity(EditProdukActivity.this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showImagePickerOptions(REQUEST_IMAGE);
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void showImagePickerOptions(int i) {
        ImagePickerActivity.showImagePickerOptions(EditProdukActivity.this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent(i);
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent(i);
            }

            @Override
            public void onViewImage() {
                launchViewImage(i);
            }
        });
    }

    private void launchViewImage(int i) {
        if (isPhotoReady) {
            Intent intent = new Intent(EditProdukActivity.this, PreviewImageActivity.class);
            intent.putExtra("foto", Constanta.URL_PHOTO_PRODUK + produk_parcelable.getFoto());
            startActivity(intent);
        } else if (uri_foto == null) {
            new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Maaf..")
                    .setContentText("Foto Belum Tersedia.")
                    .show();
        } else {
            Intent intent = new Intent(EditProdukActivity.this, PreviewImageActivity.class);
            intent.putExtra("foto", uri_foto.toString());
            startActivity(intent);
        }
    }

    private void launchCameraIntent(int i) {
        Intent intent = new Intent(EditProdukActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, i);
    }

    private void launchGalleryIntent(int i) {
        Intent intent = new Intent(EditProdukActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, i);
    }


    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProdukActivity.this);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void clickHelpVolume(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProdukActivity.this);
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

    private void clickKategori(View view) {

        if (kategori_list.isEmpty()) {
            SweetAlertDialog sweetAlertDialogError = new SweetAlertDialog(EditProdukActivity.this,
                    SweetAlertDialog.WARNING_TYPE);
            sweetAlertDialogError.setTitleText("Maaf..");
            sweetAlertDialogError.setContentText("Data Kategori Tidak Ditemukan!");
            sweetAlertDialogError.setConfirmButton("Reload", new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                    loadDataKategori();
                }
            });
            sweetAlertDialogError.setCancelButton("Tutup", new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                }
            });
            sweetAlertDialogError.show();
        } else {
            showDialogKategori();
        }
    }

    private void showDialogKategori() {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProdukActivity.this);
        builder.setTitle("Pilih Kategori");

        // add a list
        if (!kategori_list.isEmpty()) {
            builder.setItems(kategori_list.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    selected_kategori = String.valueOf(kategori_id_list.get(i));
                    et_kategori.setText(kategori_list.get(i));
                    dialogInterface.dismiss();
                }
            });
        }

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
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

            Glide.with(EditProdukActivity.this)
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

    }

    private void loadDataKategori() {

        SweetAlertDialog pDialog = new SweetAlertDialog(EditProdukActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseKategori> responseKategoriCall = apiInterface.getAllkategori();
        responseKategoriCall.enqueue(new Callback<ResponseKategori>() {
            @Override
            public void onResponse(Call<ResponseKategori> call, Response<ResponseKategori> response) {
                pDialog.dismiss();
                if (response.isSuccessful()) {
                    int kode = response.body().getStatus_code();
                    String pesan = response.body().getMessage();
                    if (kode == 200) {
                        kategoris = (ArrayList<Kategori>) response.body().getData();
                        initDataList(kategoris);
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseKategori> call, Throwable t) {
                pDialog.dismiss();

            }
        });

    }

    private void initDataList(ArrayList<Kategori> kategoris) {

        for (int a = 0; a < kategoris.size(); a++) {
            kategori_list.add(kategoris.get(a).getNama());
            kategori_id_list.add(kategoris.get(a).getId());
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}