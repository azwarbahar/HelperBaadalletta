package com.baadalletta.helper.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Produk implements Parcelable {

    private int id;

    private int id_kategori;

    private String kode;

    private String nama;

    private int harga;

    private int panjang;

    private int lebar;

    private int volume;

    private String foto;

    private String video;

    private String status;

    private String created_at;

    private String updated_at;

    private List<Kategori> kategori;

    protected Produk(Parcel in) {
        id = in.readInt();
        id_kategori = in.readInt();
        kode = in.readString();
        nama = in.readString();
        harga = in.readInt();
        panjang = in.readInt();
        lebar = in.readInt();
        volume = in.readInt();
        foto = in.readString();
        video = in.readString();
        status = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
        kategori = in.createTypedArrayList(Kategori.CREATOR);
    }

    public static final Creator<Produk> CREATOR = new Creator<Produk>() {
        @Override
        public Produk createFromParcel(Parcel in) {
            return new Produk(in);
        }

        @Override
        public Produk[] newArray(int size) {
            return new Produk[size];
        }
    };

    public int getId() {
        return id;
    }

    public int getId_kategori() {
        return id_kategori;
    }

    public String getKode() {
        return kode;
    }

    public String getNama() {
        return nama;
    }

    public int getHarga() {
        return harga;
    }

    public int getPanjang() {
        return panjang;
    }

    public int getLebar() {
        return lebar;
    }

    public int getVolume() {
        return volume;
    }

    public String getFoto() {
        return foto;
    }

    public String getVideo() {
        return video;
    }

    public String getStatus() {
        return status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public List<Kategori> getKategori() {
        return kategori;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeInt(id_kategori);
        parcel.writeString(kode);
        parcel.writeString(nama);
        parcel.writeInt(harga);
        parcel.writeInt(panjang);
        parcel.writeInt(lebar);
        parcel.writeInt(volume);
        parcel.writeString(foto);
        parcel.writeString(video);
        parcel.writeString(status);
        parcel.writeString(created_at);
        parcel.writeString(updated_at);
        parcel.writeTypedList(kategori);
    }
}
