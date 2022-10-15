package com.baadalletta.helper.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.baadalletta.helper.R;
import com.baadalletta.helper.adapter.KategoriAdapter;
import com.baadalletta.helper.models.Kategori;
import com.baadalletta.helper.models.Produk;
import com.baadalletta.helper.models.ResponseApi;
import com.baadalletta.helper.models.ResponseKategori;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KategoriActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipe_continer;

    private RecyclerView rv_ketagori;
    private ExtendedFloatingActionButton fab_add_kategori;

    private KategoriAdapter kategoriAdapter;
    private ArrayList<Kategori> kategoris;

    private View dialogView;

    private EditText et_cari;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kategori);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24);

        et_cari = findViewById(R.id.et_cari);
        rv_ketagori = findViewById(R.id.rv_ketagori);
        fab_add_kategori = findViewById(R.id.fab_add_kategori);

        fab_add_kategori.setOnClickListener(this::showDialogAddKategori);

        swipe_continer = findViewById(R.id.swipe_continer);
        swipe_continer.setOnRefreshListener(this);
        swipe_continer.setColorSchemeResources(R.color.ColorPrimary,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_green_dark);
        swipe_continer.post(new Runnable() {
            @Override
            public void run() {
                laodDataKategori();
            }
        });

        et_cari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty() || editable.toString().equals("")) {

                } else {
                    filter(editable.toString());
                    changeDrawableEdittext();
                }
            }
        });

    }

    private void changeDrawableEdittext() {
        et_cari.setCompoundDrawablesWithIntrinsicBounds(null, null,
                ContextCompat.getDrawable(KategoriActivity.this, R.drawable.ic_baseline_close_24), null);

    }

    private void filter(String text) {
        ArrayList<Kategori> filteredList = new ArrayList<>();
        for (Kategori item : kategoris) {
            if (item.getNama().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        kategoriAdapter.filterList(filteredList);
    }

    private void laodDataKategori() {

        SweetAlertDialog pDialog = new SweetAlertDialog(KategoriActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseKategori> responseKategoriCall = apiInterface.getAllkategori();
        responseKategoriCall.enqueue(new Callback<ResponseKategori>() {
            @Override
            public void onResponse(Call<ResponseKategori> call, Response<ResponseKategori> response) {
                swipe_continer.setRefreshing(false);
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
                swipe_continer.setRefreshing(false);
                pDialog.dismiss();

            }
        });
    }

    private void initDataList(ArrayList<Kategori> kategoris) {
        rv_ketagori.setLayoutManager(new LinearLayoutManager(KategoriActivity.this));
        kategoriAdapter = new KategoriAdapter(KategoriActivity.this, kategoris);
        rv_ketagori.setAdapter(kategoriAdapter);
    }

    private void showDialogAddKategori(View view) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(KategoriActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_add_kategori, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle("Tambah Kategori");

        dialog.setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SweetAlertDialog pDialog = new SweetAlertDialog(KategoriActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
                pDialog.setTitleText("Loading");
                pDialog.setCancelable(false);
                pDialog.show();

                EditText et_kategori = dialogView.findViewById(R.id.et_kategori);

                ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                Call<ResponseApi> responseApiCall = apiInterface.addKategori(et_kategori.getText().toString());
                responseApiCall.enqueue(new Callback<ResponseApi>() {
                    @Override
                    public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                        pDialog.dismiss();
                        if (response.isSuccessful()) {
                            int kode = response.body().getStatus_code();
                            String pesan = response.body().getMessage();
                            if (kode == 200) {
                                new SweetAlertDialog(KategoriActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Berhasi")
                                        .setContentText("Tambah Kategori Berhasi")
                                        .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                sweetAlertDialog.dismiss();
                                                dialog.dismiss();
                                                laodDataKategori();
                                            }
                                        })
                                        .show();
                            } else {
                                new SweetAlertDialog(KategoriActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Opss..")
                                        .setContentText(pesan)
                                        .show();
                            }
                        } else {
                            new SweetAlertDialog(KategoriActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Opss..")
                                    .setContentText("Terjadi Kesalahan Sistem")
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseApi> call, Throwable t) {
                        pDialog.dismiss();
                        new SweetAlertDialog(KategoriActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Mohon Maaf.")
                                .setContentText("Terjadi Kesalahan Sistem")
                                .show();
                    }
                });
            }
        });


        dialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRefresh() {
        laodDataKategori();
    }
}