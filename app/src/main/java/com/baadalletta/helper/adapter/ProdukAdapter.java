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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.baadalletta.helper.R;
import com.baadalletta.helper.models.Kategori;
import com.baadalletta.helper.models.Produk;
import com.baadalletta.helper.models.ResponseApi;
import com.baadalletta.helper.models.ResponseStok;
import com.baadalletta.helper.models.ResponseStokAll;
import com.baadalletta.helper.models.Stok;
import com.baadalletta.helper.network.ApiClient;
import com.baadalletta.helper.network.ApiInterface;
import com.baadalletta.helper.ui.DetailProdukActivity;
import com.baadalletta.helper.ui.EditProdukActivity;
import com.baadalletta.helper.ui.MainActivity;
import com.baadalletta.helper.ui.PreviewImageActivity;
import com.baadalletta.helper.ui.TambahProdukActivity;
import com.baadalletta.helper.utils.Constanta;
import com.baadalletta.helper.utils.MenuPickerItemProdukListener;
import com.baadalletta.helper.utils.OnItemProdukClickListener;
import com.baadalletta.helper.utils.VideoPickerOptionListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdukAdapter extends RecyclerView.Adapter<ProdukAdapter.MyHolderView> {

    private Context context;
    private ArrayList<Produk> produks;
    private Kategori kategori;
    private ArrayList<Stok> stok;
    private OnItemProdukClickListener clickListener;

    public ProdukAdapter(Context context, ArrayList<Produk> produks, OnItemProdukClickListener clickListener) {
        this.context = context;
        this.produks = produks;
        this.clickListener = clickListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<Produk> produks2) {
        produks = produks2;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProdukAdapter.MyHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_produk, parent, false);
        ProdukAdapter.MyHolderView myHolderView = new ProdukAdapter.MyHolderView(view);
        return myHolderView;
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull ProdukAdapter.MyHolderView holder, int position) {

        kategori = produks.get(position).getKategori().get(0);
        String produk_id = String.valueOf(produks.get(position).getId());
        String nama = produks.get(position).getNama();
        String kategori_string = "(" + kategori.getNama() + ")";
        String harga = String.valueOf(produks.get(position).getHarga());
        String panjang = String.valueOf(produks.get(position).getPanjang());
        String lebar = String.valueOf(produks.get(position).getLebar());
        String volume = String.valueOf(produks.get(position).getVolume());
        String foto = produks.get(position).getFoto();
        String video = produks.get(position).getVideo();
        String status = produks.get(position).getStatus();

        loadDataStok(holder, position, produk_id);

        holder.tv_nama_produk.setText(nama);
        holder.tv_kategori.setText(kategori_string);
        holder.tv_ukuran.setText("Ukuran : " + panjang + " x " + lebar);
        holder.tv_volume.setText("Volume : " + volume + " ekor");
        holder.tv_harga.setText("Rp." + harga);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);

        Glide.with(context)
                .load(Constanta.URL_PHOTO_PRODUK + foto)
                .apply(options)
                .into(holder.img_produk);

        holder.img_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowMenuItemProduk(position, produk_id, context, new MenuPickerItemProdukListener() {
                    @Override
                    public void onEditStok() {
                        clickListener.onEditStok(position, produk_id, stok.get(position));
                    }

                    @Override
                    public void onEditItem() {
                        Intent intent = new Intent(context, EditProdukActivity.class);
                        intent.putExtra("DATA_PRODUK", produks.get(position));
                        context.startActivity(intent);
                    }

                    @Override
                    public void onDeleteItem() {
                        SweetAlertDialog sweetAlertDialogError = new SweetAlertDialog(context,
                                SweetAlertDialog.WARNING_TYPE);
                        sweetAlertDialogError.setTitleText("Hapus");
                        sweetAlertDialogError.setContentText("Yakin ingin menghapus item produk?");
                        sweetAlertDialogError.setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                startDeletItem(position, produk_id);
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

        holder.img_produk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, PreviewImageActivity.class);
                intent.putExtra("foto", Constanta.URL_PHOTO_PRODUK + foto);
                context.startActivity(intent);

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, DetailProdukActivity.class);
                intent.putExtra("DATA_PRODUK", produks.get(position));
                context.startActivity(intent);

            }
        });


    }

    private void loadDataStok(MyHolderView holder, int position, String produk_id) {

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseStokAll> responseApiCall = apiInterface.getStokAll();
        responseApiCall.enqueue(new Callback<ResponseStokAll>() {
            @Override
            public void onResponse(Call<ResponseStokAll> call, Response<ResponseStokAll> response) {
                if (response.isSuccessful()) {
                    int kode = response.body().getStatus_code();
                    String pesan = response.body().getMessage();
                    if (kode == 200) {
                        stok = (ArrayList<Stok>) response.body().getData();
                        String jumlah = String.valueOf(stok.get(position).getJumlah());
                        holder.tv_stok.setText("Stok : " + jumlah + " Kg");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseStokAll> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void startDeletItem(int position, String produk_id) {

        SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#0187C6"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ResponseApi> responseApiCall = apiInterface.deleteProduk(produk_id);
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
                                .setContentText("Menghapus item produk berhasi")
                                .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        produks.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, produks.size());
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

    public static void ShowMenuItemProduk(int position, String id, Context context, MenuPickerItemProdukListener listener) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Menu :");

        // add a list
        String[] item = {"Edit Stok", "Edit", "Hapus"};
        builder.setItems(item, (dialog, which) -> {
            switch (which) {
                case 0:
                    listener.onEditStok();
                    break;
                case 1:
                    listener.onEditItem();
                    break;
                case 2:
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
        return produks.size();
    }

    public class MyHolderView extends RecyclerView.ViewHolder {

        private ImageView img_menu;
        private ImageView img_produk;
        private TextView tv_nama_produk;
        private TextView tv_kategori;
        private TextView tv_ukuran;
        private TextView tv_volume;
        private TextView tv_harga;
        private TextView tv_stok;

        public MyHolderView(@NonNull View itemView) {
            super(itemView);

            img_menu = itemView.findViewById(R.id.img_menu);
            img_produk = itemView.findViewById(R.id.img_produk);
            tv_nama_produk = itemView.findViewById(R.id.tv_nama_produk);
            tv_kategori = itemView.findViewById(R.id.tv_kategori);
            tv_ukuran = itemView.findViewById(R.id.tv_ukuran);
            tv_volume = itemView.findViewById(R.id.tv_volume);
            tv_harga = itemView.findViewById(R.id.tv_harga);
            tv_stok = itemView.findViewById(R.id.tv_stok);

        }
    }
}
