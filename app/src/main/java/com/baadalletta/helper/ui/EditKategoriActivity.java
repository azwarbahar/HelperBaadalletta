package com.baadalletta.helper.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.baadalletta.helper.R;
import com.baadalletta.helper.models.Kategori;
import com.baadalletta.helper.models.ResponseApi;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditKategoriActivity extends AppCompatActivity {

    private Kategori kategori;
    private EditText et_kategori;
    private RelativeLayout rl_simpan;

    private String kategori_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_kategori);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24);

        rl_simpan = findViewById(R.id.rl_simpan);
        et_kategori = findViewById(R.id.et_kategori);

        kategori = getIntent().getParcelableExtra("data_kategori");
        kategori_id = String.valueOf(kategori.getId());
        et_kategori.setText(kategori.getNama());

        rl_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data_send = et_kategori.getText().toString();

                if (data_send.isEmpty()) {
                    et_kategori.setError("Lengkapi");
                } else {

                    SweetAlertDialog pDialog = new SweetAlertDialog(EditKategoriActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                    pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
                    pDialog.setTitleText("Loading");
                    pDialog.setCancelable(false);
                    pDialog.show();

                    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                    Call<ResponseApi> responseApiCall = apiInterface.updateKategori(kategori_id, data_send);
                    responseApiCall.enqueue(new Callback<ResponseApi>() {
                        @Override
                        public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                            pDialog.dismiss();
                            if (response.isSuccessful()) {
                                int kode = response.body().getStatus_code();
                                String pesan = response.body().getMessage();
                                if (kode == 200) {
                                    new SweetAlertDialog(EditKategoriActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Berhasi")
                                            .setContentText("Tambah Kategori Berhasi")
                                            .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                    sweetAlertDialog.dismiss();
                                                    finish();
                                                }
                                            })
                                            .show();
                                } else {
                                    new SweetAlertDialog(EditKategoriActivity.this, SweetAlertDialog.ERROR_TYPE)
                                            .setTitleText("Opss..")
                                            .setContentText(pesan)
                                            .show();
                                }
                            } else {
                                new SweetAlertDialog(EditKategoriActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Opss..")
                                        .setContentText("Terjadi Kesalahan Sistem")
                                        .show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseApi> call, Throwable t) {
                            pDialog.dismiss();
                            new SweetAlertDialog(EditKategoriActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Mohon Maaf.")
                                    .setContentText("Terjadi Kesalahan Sistem")
                                    .show();

                        }
                    });
                }

            }
        });

    }
}