package com.baadalletta.helper.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.baadalletta.helper.R;
import com.baadalletta.helper.models.Helper;
import com.baadalletta.helper.models.ResponseHelper;
import com.baadalletta.helper.models.ResponseLogin;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.baadalletta.helper.utils.Constanta;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private RelativeLayout rl_masuk;
    private TextInputEditText tie_telpon;
    private TextInputEditText tie_password;
    private TextInputLayout til_telpon;
    private TextInputLayout til_password;

    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;

    private String helper_id;
    private Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        sharedpreferences = getApplicationContext().getSharedPreferences(Constanta.MY_SHARED_PREFERENCES,
                MODE_PRIVATE);
        String id_kurur = sharedpreferences.getString(Constanta.SESSION_ID_HELPER, "");
        if (!id_kurur.isEmpty()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        tie_telpon = findViewById(R.id.tie_telpon);
        tie_password = findViewById(R.id.tie_password);
        til_telpon = findViewById(R.id.til_telpon);
        til_password = findViewById(R.id.til_password);

        rl_masuk = findViewById(R.id.rl_masuk);
        rl_masuk.setOnClickListener(this::clickMasuk);

    }

    private void clickMasuk(View view) {

        til_telpon.setError(null);
        til_password.setError(null);

        String telpon = tie_telpon.getText().toString();
        String password = tie_password.getText().toString();

        if (telpon.equals("") || telpon.isEmpty()) {
            til_telpon.setError("Lengkapi");
        } else if (password.equals("") || password.isEmpty()) {
            til_password.setError("Lengkapi");
        } else {
            loadLogin(telpon, password);
        }
    }

    private void loadLogin(String telpon, String password) {

        SweetAlertDialog pDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseLogin> responseLoginCall = apiInterface.postLogin(telpon, password);
        responseLoginCall.enqueue(new Callback<ResponseLogin>() {
            @Override
            public void onResponse(Call<ResponseLogin> call, Response<ResponseLogin> response) {
                pDialog.dismiss();

                if (response.isSuccessful()) {

                    String kode = String.valueOf(response.body().getStatus_code());
                    String message = response.body().getMessage();
                    String helper_id_get = String.valueOf(response.body().getId_helpers());
                    helper_id = helper_id_get;
                    if (kode.equals("200")) {
//                        Toast.makeText(LoginActivity.this, kode, Toast.LENGTH_SHORT).show();
                        laodDataHelper(helper_id_get);
                    } else {
                        new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText(message)
                                .show();
                    }
                } else {
                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Mohon Maaf.")
                            .setContentText("Terjadi Kesalahan Sistem")
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ResponseLogin> call, Throwable t) {
                pDialog.dismiss();
                new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Mohon Maaf.")
                        .setContentText(t.getMessage())
                        .show();

            }
        });
    }

    private void laodDataHelper(String helper_id_get) {

        SweetAlertDialog pDialog = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Cek Akun Helper..");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseHelper> responseKurirCall = apiInterface.getHelperId(helper_id_get);
        responseKurirCall.enqueue(new Callback<ResponseHelper>() {
            @Override
            public void onResponse(Call<ResponseHelper> call, Response<ResponseHelper> response) {
                pDialog.dismiss();
                if (response.isSuccessful()) {
                    String message = response.body().getMessage();
                    String status_code = String.valueOf(response.body().getStatus_code());
                    if (status_code.equals("200")) {
                        helper = response.body().getData();
                        prosesCekData(helper);
                    } else {
                        new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText(message)
                                .show();
                    }

                } else {
                    new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Mohon Maaf.")
                            .setContentText("Terjadi Kesalahan Sistem")
                            .show();
                }

            }

            @Override
            public void onFailure(Call<ResponseHelper> call, Throwable t) {
                pDialog.dismiss();
                new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Mohon Maaf.")
                        .setContentText("Terjadi Kesalahan Sistem")
                        .show();
            }
        });
    }

    private void prosesCekData(Helper kurir) {

        String status_akun = kurir.getStatus_akun();
        String kurir_id_session = String.valueOf(kurir.getId());
        if (status_akun.equals("active")) {
            startSessionSave(kurir_id_session);
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            SweetAlertDialog sweetAlertDialogError = new SweetAlertDialog(LoginActivity.this,
                    SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialogError.setTitleText("Opss..");
            sweetAlertDialogError.setCancelable(false);
            sweetAlertDialogError.setContentText("Akun ini telah di suspend!");
            sweetAlertDialogError.setConfirmButton("OK", new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
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

}