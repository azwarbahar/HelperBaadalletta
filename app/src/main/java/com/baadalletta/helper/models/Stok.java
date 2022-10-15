package com.baadalletta.helper.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Stok implements Parcelable {

    private int id;

    private int id_produk;

    private int jumlah;

    private String created_at;

    private String updated_at;

    private Produk produk;

    protected Stok(Parcel in) {
        id = in.readInt();
        id_produk = in.readInt();
        jumlah = in.readInt();
        created_at = in.readString();
        updated_at = in.readString();
        produk = in.readParcelable(Produk.class.getClassLoader());
    }

    public static final Creator<Stok> CREATOR = new Creator<Stok>() {
        @Override
        public Stok createFromParcel(Parcel in) {
            return new Stok(in);
        }

        @Override
        public Stok[] newArray(int size) {
            return new Stok[size];
        }
    };

    public int getId() {
        return id;
    }

    public int getId_produk() {
        return id_produk;
    }

    public int getJumlah() {
        return jumlah;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public Produk getProduk() {
        return produk;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(id_produk);
        parcel.writeInt(jumlah);
        parcel.writeString(created_at);
        parcel.writeString(updated_at);
        parcel.writeParcelable(produk, i);
    }
}
