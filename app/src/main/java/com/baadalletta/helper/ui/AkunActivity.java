package com.baadalletta.helper.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.baadalletta.helper.R;
import com.baadalletta.helper.models.Helper;
import com.baadalletta.helper.models.ResponseApi;
import com.baadalletta.helper.models.ResponseHelper;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.baadalletta.helper.utils.Constanta;
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
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AkunActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private String helper_id;
    private Helper helper;

    private LinearLayout ll_edit;
    private CardView cv_edit_password;
    private CardView cv_logout;

    private SwipeRefreshLayout swipe_continer;

    private TextView tv_nama;
    private TextView tv_telpon;
    private TextView tv_status;

    private ImageView img_profile;
    private static final String TAG = AkunActivity.class.getSimpleName();
    public static final int REQUEST_IMAGE = 100;

    private String foto_profil;
    private Uri uri_foto;
    private Bitmap bitmap_thubnail_foto;

    private boolean isPhotoProfileReady = true;

    private TextView tv_status_verived;
    private boolean isVerifed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_akun);

        sharedpreferences = getApplicationContext().getSharedPreferences(Constanta.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        helper_id = sharedpreferences.getString(Constanta.SESSION_ID_HELPER, "");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24);

        img_profile = findViewById(R.id.img_profile);
        tv_nama = findViewById(R.id.tv_nama);
        tv_telpon = findViewById(R.id.tv_telpon);
        tv_status = findViewById(R.id.tv_status);

        ll_edit = findViewById(R.id.ll_edit);
        cv_edit_password = findViewById(R.id.cv_edit_password);
        cv_logout = findViewById(R.id.cv_logout);

        swipe_continer = findViewById(R.id.swipe_continer);
        swipe_continer.setOnRefreshListener(this);
        swipe_continer.setColorSchemeResources(R.color.ColorPrimary,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_green_dark);
        swipe_continer.post(new Runnable() {
            @Override
            public void run() {
                laodDatahelper(helper_id);
            }
        });

        cv_edit_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(AkunActivity.this, EditPasswordActivity.class));

            }
        });

        img_profile.setOnClickListener(this::clickPhoto);

        cv_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SweetAlertDialog(AkunActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Logout")
                        .setContentText("Ingin Keluar Dari Akun ?")
                        .setCancelButton("Tidak", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                startActivity(new Intent(AkunActivity.this, LoginActivity.class));
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.apply();
                                editor.clear();
                                editor.commit();
                                finish();
                            }
                        })
                        .show();
            }
        });

        ImagePickerActivity.clearCache(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    bitmap_thubnail_foto = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    uri_foto = uri;
                    Glide.with(this)
                            .asBitmap()
                            .load(bitmap_thubnail_foto)
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                    img_profile.setImageBitmap(resource);
                                }
                            });
                    startUpdatePhoto(uri_foto);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private void startUpdatePhoto(Uri uri) {

        SweetAlertDialog pDialog = new SweetAlertDialog(AkunActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        File file = new File(uri.getPath());
        RequestBody foto = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part foto_helper_send = MultipartBody.Part.createFormData("foto", file.getName(), foto);

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseApi> responseApiCall = apiInterface.updateFotoHelper(helper_id, foto_helper_send);
        responseApiCall.enqueue(new Callback<ResponseApi>() {
            @Override
            public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                pDialog.dismiss();
                if (response.isSuccessful()) {
                    int kode = response.body().getStatus_code();
                    String pesan = response.body().getMessage();
                    if (kode == 200) {

                        new SweetAlertDialog(AkunActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Berhasi")
                                .setContentText("Edit Foto Berhasil")
                                .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        laodDatahelper(helper_id);
                                    }
                                })
                                .show();

                    } else {
                        new SweetAlertDialog(AkunActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText(pesan)
                                .show();
                    }
                } else {
                    new SweetAlertDialog(AkunActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Opss..")
                            .setContentText("Terjadi Kesalahan Sistem1")
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ResponseApi> call, Throwable t) {
                pDialog.dismiss();
                new SweetAlertDialog(AkunActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Mohon Maaf.")
                        .setContentText(t.getMessage())
                        .show();

            }
        });

    }

    private void clickPhoto(View view) {
        ImagePickerActivity.clearCache(AkunActivity.this);
        Dexter.withActivity(AkunActivity.this)
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
        ImagePickerActivity.showImagePickerOptions(AkunActivity.this, new ImagePickerActivity.PickerOptionListener() {
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

        String foto = helper.getFoto();
        Intent intent = new Intent(AkunActivity.this, PreviewImageActivity.class);
        intent.putExtra("foto", Constanta.URL_FOTO_HELPER + foto);
        startActivity(intent);
    }

    private void launchCameraIntent(int i) {
        Intent intent = new Intent(AkunActivity.this, ImagePickerActivity.class);
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
        Intent intent = new Intent(AkunActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, i);
    }


    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AkunActivity.this);
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

    private void laodDatahelper(String helper_id) {

        SweetAlertDialog pDialog = new SweetAlertDialog(AkunActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading..");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseHelper> responseHelperCall = apiInterface.getHelperId(helper_id);
        responseHelperCall.enqueue(new Callback<ResponseHelper>() {
            @Override
            public void onResponse(Call<ResponseHelper> call, Response<ResponseHelper> response) {
                swipe_continer.setRefreshing(false);
                pDialog.dismiss();
                if (response.isSuccessful()) {
                    int kode = response.body().getStatus_code();
                    String pesan = response.body().getMessage();
                    if (kode == 200) {
                        helper = response.body().getData();
                        prosesCekData(helper);
                        initDataHelper(helper);
                    } else {
                        new SweetAlertDialog(AkunActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText(pesan)
                                .show();
                    }
                } else {
                    new SweetAlertDialog(AkunActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Mohon Maaf.")
                            .setContentText("Terjadi Kesalahan Sistem")
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ResponseHelper> call, Throwable t) {
                pDialog.dismiss();
                swipe_continer.setRefreshing(false);
                new SweetAlertDialog(AkunActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Mohon Maaf.")
                        .setContentText("Terjadi Kesalahan Sistem")
                        .show();
            }
        });

    }

    private void prosesCekData(Helper helper) {

        String status_akun = helper.getStatus_akun();
        String kurir_id_session = String.valueOf(helper.getId());
        if (status_akun.equals("active")) {
            startSessionSave(kurir_id_session);
            Log.e("Kurir", "status : " + status_akun);
        } else {
            SweetAlertDialog sweetAlertDialogError = new SweetAlertDialog(AkunActivity.this,
                    SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialogError.setTitleText("Opss..");
            sweetAlertDialogError.setCancelable(false);
            sweetAlertDialogError.setContentText("Akun ini telah di suspend!");
            sweetAlertDialogError.setConfirmButton("OK", new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                    Intent intent = new Intent(AkunActivity.this, LoginActivity.class);
                    startActivity(intent);
                    SharedPreferences mPreferences1 = getSharedPreferences(Constanta.MY_SHARED_PREFERENCES, MODE_PRIVATE);
                    SharedPreferences.Editor editor = mPreferences1.edit();
                    editor.apply();
                    editor.clear();
                    editor.commit();
                    finish();
                }
            });
            sweetAlertDialogError.show();
        }

    }

    private void startSessionSave(String kurir_id) {
        editor = sharedpreferences.edit();
        editor.putString(Constanta.SESSION_ID_HELPER, kurir_id);
        editor.apply();
    }

    private void initDataHelper(Helper helper) {

        String nama = helper.getNama();
        String alamat = helper.getAlamat();
        String whatsapp = helper.getWhatsaap();
        String status = helper.getStatus_akun();
        String foto = helper.getFoto();

        if (foto == null || foto.equals("")) {

            Glide.with(this)
                    .load(R.drawable.foto_default)
                    .into(img_profile);
            isPhotoProfileReady = false;
        } else {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH);

            Glide.with(AkunActivity.this)
                    .load(Constanta.URL_FOTO_HELPER + foto)
                    .apply(options)
                    .into(img_profile);
            isPhotoProfileReady = true;
        }
        tv_nama.setText(nama);
        tv_telpon.setText(whatsapp);
        tv_status.setText(status);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRefresh() {
        laodDatahelper(helper_id);
    }
}