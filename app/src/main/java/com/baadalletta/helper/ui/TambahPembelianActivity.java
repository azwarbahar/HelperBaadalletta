package com.baadalletta.helper.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baadalletta.helper.R;
import com.baadalletta.helper.models.Helper;
import com.baadalletta.helper.models.Produk;
import com.baadalletta.helper.models.ResponseProduk;
import com.baadalletta.helper.models.ResponseTambahPembelian;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.baadalletta.helper.utils.Constanta;
import com.baadalletta.helper.utils.MoneyTextWatcher;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TambahPembelianActivity extends AppCompatActivity {

    private static int CAMERA_PERMISSION_CODE = 100;
    private static int VIDEO_RECORD_CODE = 111;
    private static final String TAG = TambahPembelianActivity.class.getSimpleName();
    public static final int REQUEST_IMAGE = 110;

    private CardView cv_foto;

    private Uri uri_foto;

    private Bitmap bitmap_thubnail_foto;

    private ImageView img_thumbnail_foto;

    private RelativeLayout rl_simpan;

    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private Helper helper;
    private String helper_id;

    private ArrayList<String> data_nama_produk = new ArrayList<String>();
    private ArrayList<String> data_id_produk = new ArrayList<String>();
    private String selected_id_product = null;

    private AutoCompleteTextView item_pilihan_produk;
    private TextInputEditText tie_harga;
    private TextInputLayout til_harga;
    private TextInputLayout til_produk;

    private ArrayList<Produk> produks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_pembelian);

        sharedpreferences = getApplicationContext().getSharedPreferences(Constanta.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        helper_id = sharedpreferences.getString(Constanta.SESSION_ID_HELPER, "");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24);

        img_thumbnail_foto = findViewById(R.id.img_thumbnail_foto);
        item_pilihan_produk = findViewById(R.id.item_pilihan_produk);
        tie_harga = findViewById(R.id.tie_harga);
        til_harga = findViewById(R.id.til_harga);
        til_produk = findViewById(R.id.til_produk);
        rl_simpan = findViewById(R.id.rl_simpan);

        loadDataProduk();

        tie_harga.addTextChangedListener(new MoneyTextWatcher(tie_harga));

        item_pilihan_produk.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected_id_product = data_id_produk.get(i);
//                Toast.makeText(TambahPembelianActivity.this, selected_id_product, Toast.LENGTH_SHORT).show();
            }
        });

        cv_foto = findViewById(R.id.cv_foto);
        cv_foto.setOnClickListener(this::clickFoto);

        rl_simpan.setOnClickListener(this::clickSimpan);

    }

    private void clickSimpan(View view) {
        til_harga.setError(null);
        til_produk.setError(null);

        String produk_send = selected_id_product;
        String harga_cek = tie_harga.getText().toString();

        if (produk_send.isEmpty()) {
            til_produk.setError("Pilih");
        } else if (harga_cek.isEmpty()) {
            til_harga.setError("Lengkapi");
        } else if (uri_foto == null) {
            new SweetAlertDialog(TambahPembelianActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Maaf..")
                    .setContentText("Foto Produk Belum Ada")
                    .show();
        } else {

            SweetAlertDialog sweetAlertDialogError = new SweetAlertDialog(TambahPembelianActivity.this,
                    SweetAlertDialog.WARNING_TYPE);
            sweetAlertDialogError.setTitleText("Tambah");
            sweetAlertDialogError.setContentText("Kirim data Pembelian?");
            sweetAlertDialogError.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();

                    BigDecimal value = MoneyTextWatcher.parseCurrencyValue(tie_harga.getText().toString());
                    String harga_send = String.valueOf(value);
                    startUploadData(produk_send, harga_send, uri_foto);

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
    }

    private void ClearInput() {

        item_pilihan_produk.setText("");
        selected_id_product = null;
        tie_harga.setText("");

        Glide.with(TambahPembelianActivity.this)
                .load(R.color.transparan)
                .into(img_thumbnail_foto);

        uri_foto = null;
        bitmap_thubnail_foto = null;

    }


    private void startUploadData(String produk_send, String harga_send, Uri uri_foto) {

        SweetAlertDialog pDialog = new SweetAlertDialog(TambahPembelianActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        File file = new File(uri_foto.getPath());
        RequestBody foto = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part foto_pesanan_send = MultipartBody.Part.createFormData("foto", file.getName(), foto);
        RequestBody id_helper = RequestBody.create(MediaType.parse("text/plain"), helper_id);
        RequestBody produk = RequestBody.create(MediaType.parse("text/plain"), produk_send);
        RequestBody harga = RequestBody.create(MediaType.parse("text/plain"), harga_send);

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseTambahPembelian> responseTambahPembelianCall = apiInterface.tambahPembelian(id_helper,
                produk, harga, foto_pesanan_send);
        responseTambahPembelianCall.enqueue(new Callback<ResponseTambahPembelian>() {
            @Override
            public void onResponse(Call<ResponseTambahPembelian> call, Response<ResponseTambahPembelian> response) {
                pDialog.dismiss();
                if (response.isSuccessful()) {
                    int kode = response.body().getStatus_code();
                    String pesan = response.body().getMessage();
                    if (kode == 200) {
                        new SweetAlertDialog(TambahPembelianActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Berhasi")
                                .setContentText("Tambah Pembelian Berhasi")
                                .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        ClearInput();
                                    }
                                })
                                .show();

                    } else {
                        new SweetAlertDialog(TambahPembelianActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText(pesan)
                                .show();
                    }

                } else {
                    new SweetAlertDialog(TambahPembelianActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Opss..")
                            .setContentText("Terjadi Kesalahan Sistem1")
                            .show();
                }

            }

            @Override
            public void onFailure(Call<ResponseTambahPembelian> call, Throwable t) {
                pDialog.dismiss();
                new SweetAlertDialog(TambahPembelianActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Mohon Maaf.")
                        .setContentText("Terjadi Kesalahan Sistem")
                        .show();

            }
        });


    }

    private void clickFoto(View view) {
        ImagePickerActivity.clearCache(TambahPembelianActivity.this);
        Dexter.withActivity(TambahPembelianActivity.this)
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
        ImagePickerActivity.showImagePickerOptions(TambahPembelianActivity.this, new ImagePickerActivity.PickerOptionListener() {
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
        if (uri_foto == null) {
            new SweetAlertDialog(TambahPembelianActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Maaf..")
                    .setContentText("Foto Belum Tersedia.")
                    .show();
        } else {
            Intent intent = new Intent(TambahPembelianActivity.this, PreviewImageActivity.class);
            intent.putExtra("foto", uri_foto.toString());
            startActivity(intent);
        }
    }

    private void launchCameraIntent(int i) {
        Intent intent = new Intent(TambahPembelianActivity.this, ImagePickerActivity.class);
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
        Intent intent = new Intent(TambahPembelianActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, i);
    }


    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TambahPembelianActivity.this);
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


    private void loadDataProduk() {

        data_nama_produk.clear();
        data_id_produk.clear();

        SweetAlertDialog pDialog = new SweetAlertDialog(TambahPembelianActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseProduk> responseProdukCall = apiInterface.getAllProduk();
        responseProdukCall.enqueue(new Callback<ResponseProduk>() {
            @Override
            public void onResponse(Call<ResponseProduk> call, Response<ResponseProduk> response) {
                pDialog.dismiss();
                if (response.isSuccessful()) {
                    int kode = response.body().getStatus_code();
                    String pesan = response.body().getMessage();
                    if (kode == 200) {
                        produks = (ArrayList<Produk>) response.body().getData();
                        initProduk(produks);
                    } else {
                        Toast.makeText(TambahPembelianActivity.this, pesan, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TambahPembelianActivity.this, "gagal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseProduk> call, Throwable t) {
                pDialog.dismiss();
                Toast.makeText(TambahPembelianActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void initProduk(ArrayList<Produk> produks) {

        for (int a = 0; a < produks.size(); a++) {
            data_nama_produk.add(produks.get(a).getNama());
            data_id_produk.add(String.valueOf(produks.get(a).getId()));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, data_nama_produk);
        item_pilihan_produk.setAdapter(adapter);

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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}