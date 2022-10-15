package com.baadalletta.helper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.baadalletta.helper.R;
import com.baadalletta.helper.models.Kategori;
import com.baadalletta.helper.models.Pembelian;
import com.baadalletta.helper.models.Produk;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PembelianAdapter extends RecyclerView.Adapter<PembelianAdapter.MyHolderView> {

    private Context context;
    private ArrayList<Pembelian> pembelians;
    private Produk produk;

    public PembelianAdapter(Context context, ArrayList<Pembelian> pembelians) {
        this.context = context;
        this.pembelians = pembelians;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<Pembelian> pembelians2) {
        pembelians = pembelians2;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PembelianAdapter.MyHolderView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(context).inflate(R.layout.item_pembelian, parent, false);
        PembelianAdapter.MyHolderView myHolderView = new PembelianAdapter.MyHolderView(view);
        return myHolderView;
    }

    @Override
    public void onBindViewHolder(@NonNull PembelianAdapter.MyHolderView holder, int position) {

        String tanggal = pembelians.get(position).getTanggal();
        int harga = pembelians.get(position).getHarga();

        produk = pembelians.get(position).getProduk();

        if (produk != null){
            holder.tv_nama_produk.setText(produk.getNama());
        } else {

            holder.tv_nama_produk.setText("-");
        }

        NumberFormat format_uang = new DecimalFormat("#,###");
        String harga_format = format_uang.format(harga);

        holder.tv_tanggal.setText(ConvertDate(tanggal));
        holder.tv_harga.setText("Harga: Rp. " + harga_format);
    }

    private String ConvertDate(String date) {
        String value = null;
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy");
        try {
            value = output.format(input.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public int getItemCount() {
        return pembelians.size();
    }

    public class MyHolderView extends RecyclerView.ViewHolder {

        private TextView tv_nama_produk;
        private TextView tv_harga;
        private TextView tv_tanggal;

        public MyHolderView(@NonNull View itemView) {
            super(itemView);

            tv_nama_produk = itemView.findViewById(R.id.tv_nama_produk);
            tv_harga = itemView.findViewById(R.id.tv_harga);
            tv_tanggal = itemView.findViewById(R.id.tv_tanggal);

        }
    }
}
