package com.baadalletta.helper.ui;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.baadalletta.helper.R;
import com.baadalletta.helper.models.Helper;
import com.baadalletta.helper.models.ResponseApi;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.baadalletta.helper.utils.Constanta;
import com.google.android.material.textfield.TextInputEditText;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPasswordActivity extends AppCompatActivity {

    private RelativeLayout rl_simpan;
    private TextInputEditText tie_password_lama;
    private TextInputEditText tie_password_baru;
    private TextInputEditText tie_password_baru_again;

    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private Helper helper;
    private String helper_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        sharedpreferences = getApplicationContext().getSharedPreferences(Constanta.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        helper_id = sharedpreferences.getString(Constanta.SESSION_ID_HELPER, "");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24);

        tie_password_lama = findViewById(R.id.tie_password_lama);
        tie_password_baru = findViewById(R.id.tie_password_baru);
        tie_password_baru_again = findViewById(R.id.tie_password_baru_again);
        rl_simpan = findViewById(R.id.rl_simpan);
        rl_simpan.setOnClickListener(this::clickSimpan);
    }

    private void clickSimpan(View view) {

        String old_password = tie_password_lama.getText().toString();
        String new_password = tie_password_baru.getText().toString();
        String new_password_again = tie_password_baru_again.getText().toString();

        if (old_password.isEmpty() || old_password.equals("")) {
            new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Gagal...")
                    .setContentText("Password lama tidak boleh kosong!")
                    .show();
        } else if (new_password.isEmpty() || new_password.equals("")) {
            new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Gagal...")
                    .setContentText("Password baru tidak boleh kosong!")
                    .show();
        } else if (new_password_again.isEmpty() || new_password_again.equals("")) {
            new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Gagal...")
                    .setContentText("Konfirmasi Password baru tidak boleh kosong!")
                    .show();
        } else if (new_password.length() < 6) {
            new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Gagal...")
                    .setContentText("Password baru setidaknya minimal 6 karakter!")
                    .show();
        } else if (!new_password.equals(new_password_again)) {
            new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Gagal...")
                    .setContentText("Konfirmasi Password tidak sesuai!")
                    .show();
        } else if (old_password.equals(new_password)) {
            new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Gagal...")
                    .setContentText("Password baru dan lama tidak boleh sama!")
                    .show();
        } else {
            startUpdatePassword(old_password, new_password);
        }

    }

    private void startUpdatePassword(String old_password, String new_password) {

        SweetAlertDialog pDialog = new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseApi> responseLoginCall = apiInterface.updatePasswordHelper(helper_id, old_password, new_password);
        responseLoginCall.enqueue(new Callback<ResponseApi>() {
            @Override
            public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                pDialog.dismiss();
                if (response.isSuccessful()) {
                    String kode = String.valueOf(response.body().getStatus_code());
                    String pesan = response.body().getMessage();
                    if (kode.equals("200")) {
                        SweetAlertDialog success = new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.SUCCESS_TYPE);
                        success.setTitleText("Success..");
                        success.setCancelable(false);
                        success.setContentText("Edit Password Berhasil");
                        success.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                tie_password_lama.setText("");
                                tie_password_baru.setText("");
                                tie_password_baru_again.setText("");
                            }
                        });
                        success.show();
                    } else {
                        new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Gagal...")
                                .setContentText(pesan)
                                .show();
                    }
                } else {
                    new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Gagal...")
                            .setContentText("Permintaan gagal di proses")
                            .show();

                }
            }

            @Override
            public void onFailure(Call<ResponseApi> call, Throwable t) {
                pDialog.dismiss();
                new SweetAlertDialog(EditPasswordActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Gagal...")
                        .setContentText(t.getMessage())
                        .show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}