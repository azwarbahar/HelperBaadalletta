package com.baadalletta.helper.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.baadalletta.helper.R;
import com.baadalletta.helper.adapter.PembelianAdapter;
import com.baadalletta.helper.adapter.ProdukAdapter;
import com.baadalletta.helper.models.Helper;
import com.baadalletta.helper.models.Pembelian;
import com.baadalletta.helper.models.Produk;
import com.baadalletta.helper.models.ResponsePembelian;
import com.baadalletta.helper.models.ResponseProduk;
import com.baadalletta.helper.models.Stok;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.baadalletta.helper.utils.Constanta;
import com.baadalletta.helper.utils.MoneyTextWatcher;
import com.baadalletta.helper.utils.OnItemProdukClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PembelianProdukActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView rv_pembelian_produk;

    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private Helper helper;
    private String helper_id;

    private TextView tv_no_data;

    private SwipeRefreshLayout swipe_continer;

    private ArrayList<Pembelian> pembelians;

    private PembelianAdapter pembelianAdapter;


    private FloatingActionButton fab_tambah_pembelian;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembelian_produk);

        sharedpreferences = getApplicationContext().getSharedPreferences(Constanta.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        helper_id = sharedpreferences.getString(Constanta.SESSION_ID_HELPER, "");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_chevron_left_24);

        fab_tambah_pembelian = findViewById(R.id.fab_tambah_pembelian);
        tv_no_data = findViewById(R.id.tv_no_data);
        rv_pembelian_produk = findViewById(R.id.rv_pembelian_produk);

        swipe_continer = findViewById(R.id.swipe_continer);
        swipe_continer.setOnRefreshListener(this);
        swipe_continer.setColorSchemeResources(R.color.ColorPrimary,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_green_dark);
        swipe_continer.post(new Runnable() {
            @Override
            public void run() {
                loadDataPembelian(helper_id);
            }
        });

        fab_tambah_pembelian.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PembelianProdukActivity.this, TambahPembelianActivity.class));
            }
        });

    }
    private void loadDataPembelian(String helper_id) {

        SweetAlertDialog pDialog = new SweetAlertDialog(PembelianProdukActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading..");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponsePembelian> responsePembelianCall = apiInterface.getDataPembelian(helper_id);
        responsePembelianCall.enqueue(new Callback<ResponsePembelian>() {
            @Override
            public void onResponse(Call<ResponsePembelian> call, Response<ResponsePembelian> response) {
                pDialog.dismiss();
                swipe_continer.setRefreshing(false);

                if (response.isSuccessful()) {
                    int kode = response.body().getStatus_code();
                    String pesan = response.body().getMessage();
                    if (kode == 200) {
                        pembelians = (ArrayList<Pembelian>) response.body().getData();

                        if (pembelians.size() < 1) {
                            tv_no_data.setVisibility(View.VISIBLE);
                        } else {
                            tv_no_data.setVisibility(View.GONE);
                            rv_pembelian_produk.setLayoutManager(new LinearLayoutManager(PembelianProdukActivity.this));
                            pembelianAdapter = new PembelianAdapter(PembelianProdukActivity.this, pembelians);
                            rv_pembelian_produk.setAdapter(pembelianAdapter);

                        }
                    } else {
                        tv_no_data.setVisibility(View.VISIBLE);
                        Toast.makeText(PembelianProdukActivity.this, pesan, Toast.LENGTH_SHORT).show();

                    }

                } else {
                    tv_no_data.setVisibility(View.VISIBLE);
                    Toast.makeText(PembelianProdukActivity.this, response.message(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<ResponsePembelian> call, Throwable t) {
                pDialog.dismiss();
                swipe_continer.setRefreshing(false);
                Toast.makeText(PembelianProdukActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                tv_no_data.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRefresh() {
        loadDataPembelian(helper_id);
    }
}