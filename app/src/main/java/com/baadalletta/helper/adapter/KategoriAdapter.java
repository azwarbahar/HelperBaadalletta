package com.baadalletta.helper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.baadalletta.helper.R;
import com.baadalletta.helper.models.Kategori;
import com.baadalletta.helper.models.ResponseApi;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.baadalletta.helper.ui.EditKategoriActivity;
import com.baadalletta.helper.utils.MenuPickerItemKategoriListener;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KategoriAdapter extends RecyclerView.Adapter<KategoriAdapter.MyHolderView> {

    private Context context;
    private ArrayList<Kategori> kategoris;
    private String kategori_id;
    private View dialogView;

    public KategoriAdapter(Context context, ArrayList<Kategori> kategoris) {
        this.context = context;
        this.kategoris = kategoris;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<Kategori> kategoris2) {
        kategoris = kategoris2;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public KategoriAdapter.MyHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_kategori, parent, false);
        KategoriAdapter.MyHolderView myHolderView = new KategoriAdapter.MyHolderView(view);
        return myHolderView;
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull KategoriAdapter.MyHolderView holder, int position) {

        kategori_id = String.valueOf(kategoris.get(position).getId());

        holder.tv_nama_kategori.setText(kategoris.get(position).getNama());
        holder.kode_kategori.setText("Kode : " + kategoris.get(position).getKode());

        holder.img_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowMenuItemProduk(context, new MenuPickerItemKategoriListener() {
                    @Override
                    public void onEditItem() {
                        Intent intent = new Intent(context, EditKategoriActivity.class);
                        intent.putExtra("data_kategori", kategoris.get(position));
                        context.startActivity(intent);
                    }

                    @Override
                    public void onDeleteItem() {
                        SweetAlertDialog sweetAlertDialogError = new SweetAlertDialog(context,
                                SweetAlertDialog.WARNING_TYPE);
                        sweetAlertDialogError.setTitleText("Hapus");
                        sweetAlertDialogError.setContentText("Yakin ingin menghapus item kategori?");
                        sweetAlertDialogError.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                startDeletItem(position, kategori_id);
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
                });
            }
        });

    }

    private void startDeletItem(int position, String kategori_id) {

        SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseApi> responseApiCall = apiInterface.deleteKategori(kategori_id);
        responseApiCall.enqueue(new Callback<ResponseApi>() {
            @Override
            public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                pDialog.dismiss();
                if (response.isSuccessful()) {
                    int kode = response.body().getStatus_code();
                    String pesan = response.body().getMessage();
                    if (kode == 200) {
                        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Berhasi")
                                .setContentText("Menghapus item kategori berhasi")
                                .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        kategoris.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, kategoris.size());
                                    }
                                })
                                .show();
                    } else {
                        new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Opss..")
                                .setContentText(pesan)
                                .show();
                    }
                } else {
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Opss..")
                            .setContentText("Terjadi Kesalahan Sistem")
                            .show();
                }
            }

            @Override
            public void onFailure(Call<ResponseApi> call, Throwable t) {
                pDialog.dismiss();
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Mohon Maaf.")
                        .setContentText("Terjadi Kesalahan Sistem")
                        .show();

            }
        });
    }

    public static void ShowMenuItemProduk(Context context, MenuPickerItemKategoriListener listener) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Menu :");

        // add a list
        String[] item = {"Edit", "Hapus"};
        builder.setItems(item, (dialog, which) -> {
            switch (which) {
                case 0:
                    listener.onEditItem();
                    break;
                case 1:
                    listener.onDeleteItem();
                    break;
            }
        });

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public int getItemCount() {
        return kategoris.size();
    }

    public class MyHolderView extends RecyclerView.ViewHolder {

        private TextView tv_nama_kategori;
        private TextView kode_kategori;
        private ImageView img_menu;

        public MyHolderView(@NonNull View itemView) {
            super(itemView);

            tv_nama_kategori = itemView.findViewById(R.id.tv_nama_kategori);
            kode_kategori = itemView.findViewById(R.id.kode_kategori);
            img_menu = itemView.findViewById(R.id.img_menu);

        }
    }
}
