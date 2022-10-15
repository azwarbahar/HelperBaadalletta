package com.baadalletta.helper.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.baadalletta.helper.R;
import com.baadalletta.helper.adapter.ProdukAdapter;
import com.baadalletta.helper.models.Helper;
import com.baadalletta.helper.models.Produk;
import com.baadalletta.helper.models.ResponseApi;
import com.baadalletta.helper.models.ResponseHelper;
import com.baadalletta.helper.models.ResponseProduk;
import com.baadalletta.helper.models.ResponseStok;
import com.baadalletta.helper.models.Stok;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.baadalletta.helper.utils.Constanta;
import com.baadalletta.helper.utils.OnItemProdukClickListener;
import com.github.clans.fab.FloatingActionButton;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;

    private SwipeRefreshLayout swipe_continer;

    private RecyclerView rv_produk;
    private ProdukAdapter produkAdapter;

    private ArrayList<Produk> produks;

    private FloatingActionButton menu_produk;
    private FloatingActionButton menu_kategori;
    private FloatingActionButton menu_pembelian_produk;

    private View dialogView;

    private TextView tv_show_kategori;

    private EditText et_cari;

    private ImageView img_user;

    private String helper_id;
    private Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getApplicationContext().getSharedPreferences(Constanta.MY_SHARED_PREFERENCES, MODE_PRIVATE);
        helper_id = sharedpreferences.getString(Constanta.SESSION_ID_HELPER, "");

        tv_show_kategori = findViewById(R.id.tv_show_kategori);
        rv_produk = findViewById(R.id.rv_produk);
        menu_kategori = findViewById(R.id.menu_kategori);
        menu_produk = findViewById(R.id.menu_produk);
        menu_pembelian_produk = findViewById(R.id.menu_pembelian_produk);
        et_cari = findViewById(R.id.et_cari);
        img_user = findViewById(R.id.img_user);

//        Toast.makeText(MainActivity.this, helper_id, Toast.LENGTH_SHORT).show();

        menu_pembelian_produk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PembelianProdukActivity.class));
            }
        });
        menu_produk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TambahProdukActivity.class));
            }
        });

        menu_kategori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogAddKategori();
            }
        });

        tv_show_kategori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(MainActivity.this, KategoriActivity.class));

            }
        });

        swipe_continer = findViewById(R.id.swipe_continer);
        swipe_continer.setOnRefreshListener(this);
        swipe_continer.setColorSchemeResources(R.color.ColorPrimary,
                android.R.color.holo_blue_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_green_dark);
        swipe_continer.post(new Runnable() {
            @Override
            public void run() {
                loadDataProduk();
                loadDataHelper(helper_id);
            }
        });

        img_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AkunActivity.class));
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

    private void loadDataHelper(String helper_id) {
        SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Cek Akun Helper..");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseHelper> responseKurirCall = apiInterface.getHelperId(helper_id);
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
                        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText(message)
                                .show();
                    }

                } else {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Mohon Maaf.")
                            .setContentText("Terjadi Kesalahan Sistem2")
                            .show();
                }

            }

            @Override
            public void onFailure(Call<ResponseHelper> call, Throwable t) {
                pDialog.dismiss();
                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Mohon Maaf.")
                        .setContentText(t.getMessage())
                        .show();
            }
        });


    }

    private void prosesCekData(Helper helper) {

        String status_akun = helper.getStatus_akun();
        String helper_id_session = String.valueOf(helper.getId());

        String status_verif = helper.getStatus_verifikasi();
        if (status_verif.equals("verifikasi")) {
//            rl_verifed_aller.setVisibility(View.GONE);
//            isVerifed = true;
        } else {
//            rl_verifed_aller.setVisibility(View.VISIBLE);
//            isVerifed = false;
        }

        if (status_akun.equals("active")) {
            startSessionSave(helper_id_session);
            Log.e("Kurir", "status : " + status_akun);
        } else {
            SweetAlertDialog sweetAlertDialogError = new SweetAlertDialog(MainActivity.this,
                    SweetAlertDialog.ERROR_TYPE);
            sweetAlertDialogError.setTitleText("Opss..");
            sweetAlertDialogError.setCancelable(false);
            sweetAlertDialogError.setContentText("Akun ini telah di suspend!");
            sweetAlertDialogError.setConfirmButton("OK", new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
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

    private void startSessionSave(String helper_id) {
        editor = sharedpreferences.edit();
        editor.putString(Constanta.SESSION_ID_HELPER, helper_id);
        editor.apply();
    }


    private void changeDrawableEdittext() {
        et_cari.setCompoundDrawablesWithIntrinsicBounds(null, null,
                ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_close_24), null);

    }

    private void filter(String text) {
        ArrayList<Produk> filteredList = new ArrayList<>();
        for (Produk item : produks) {
            if (item.getNama().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        produkAdapter.filterList(filteredList);
    }

    private void showDialogAddKategori() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_add_kategori, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle("Tambah Kategori");

        dialog.setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
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
                                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Berhasi")
                                        .setContentText("Tambah Kategori Berhasi")
                                        .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                sweetAlertDialog.dismiss();
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                            } else {
                                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Opss..")
                                        .setContentText(pesan)
                                        .show();
                            }
                        } else {
                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Opss..")
                                    .setContentText("Terjadi Kesalahan Sistem")
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseApi> call, Throwable t) {
                        pDialog.dismiss();
                        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
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

    private void loadDataProduk() {

        SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseProduk> responseProdukCall = apiInterface.getAllProduk();
        responseProdukCall.enqueue(new Callback<ResponseProduk>() {
            @Override
            public void onResponse(Call<ResponseProduk> call, Response<ResponseProduk> response) {
                swipe_continer.setRefreshing(false);
                pDialog.dismiss();
                if (response.isSuccessful()) {
                    int kode = response.body().getStatus_code();
                    String pesan = response.body().getMessage();
                    if (kode == 200) {
//                        Toast.makeText(MainActivity.this, pesan, Toast.LENGTH_SHORT).show();
                        produks = (ArrayList<Produk>) response.body().getData();
                        rv_produk.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                        produkAdapter = new ProdukAdapter(MainActivity.this, produks, new OnItemProdukClickListener() {
                            @Override
                            public void onEditStok(int position, String id, Stok stok) {
                                showDialogAddStok(position, id, stok);
                            }
                        });
                        rv_produk.setAdapter(produkAdapter);
                    } else {
                        Toast.makeText(MainActivity.this, pesan, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "gagal", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseProduk> call, Throwable t) {
                swipe_continer.setRefreshing(false);
                pDialog.dismiss();
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void showDialogAddStok(int position, String id, Stok stok) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.dialog_add_stok, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        final String[] jumlah_sekarang = new String[0];
        final int[] id_stok = new int[0];

//        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
//        Call<ResponseStok> responseApiCall = apiInterface.getStokProdukId(id);
//        responseApiCall.enqueue(new Callback<ResponseStok>() {
//            @Override
//            public void onResponse(Call<ResponseStok> call, Response<ResponseStok> response) {
//                if (response.isSuccessful()) {
//                    int kode = response.body().getStatus_code();
//                    String pesan = response.body().getMessage();
//                    if (kode == 200) {
//                        id_stok[0] = response.body().getData().getId();
//                        dialog.setTitle("Edit Stok " + response.body().getData().getProduk().getNama());
//                        jumlah_sekarang[0] = String.valueOf(response.body().getData().getJumlah());
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseStok> call, Throwable t) {
//                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });


        dialog.setTitle("Edit Stok " + stok.getProduk().getNama());
        TextView tv_stok_now = dialogView.findViewById(R.id.tv_stok_now);
        tv_stok_now.setText("Stok Sekarang : " + stok.getJumlah() + " Kg");

        dialog.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                SweetAlertDialog pDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
                pDialog.setTitleText("Loading");
                pDialog.setCancelable(false);
                pDialog.show();

                EditText et_stok = dialogView.findViewById(R.id.et_stok);

                String stok_id = String.valueOf(stok.getId());

                ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                Call<ResponseStok> responseStokCall = apiInterface.updateStokId(stok_id, et_stok.getText().toString());
                responseStokCall.enqueue(new Callback<ResponseStok>() {
                    @Override
                    public void onResponse(Call<ResponseStok> call, Response<ResponseStok> response) {
                        pDialog.dismiss();
                        if (response.isSuccessful()) {
                            int kode = response.body().getStatus_code();
                            String pesan = response.body().getMessage();
                            if (kode == 200) {
                                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Berhasi")
                                        .setContentText("Tambah Kategori Berhasi")
                                        .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                sweetAlertDialog.dismiss();
                                                loadDataProduk();
                                            }
                                        })
                                        .show();
                            } else {
                                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Opss..")
                                        .setContentText(pesan)
                                        .show();
                            }
                        } else {
                            new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Opss..")
                                    .setContentText(response.message())
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseStok> call, Throwable t) {
                        new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText(t.getMessage())
                                .show();
                    }
                });

            }
        });

        dialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onRefresh() {
        loadDataProduk();
    }
}