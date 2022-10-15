package com.baadalletta.helper.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Kategori implements Parcelable {

    private int id;

    private String kode;

    private String nama;

    private String created_at;

    private String updated_at;

    protected Kategori(Parcel in) {
        id = in.readInt();
        kode = in.readString();
        nama = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
    }

    public static final Creator<Kategori> CREATOR = new Creator<Kategori>() {
        @Override
        public Kategori createFromParcel(Parcel in) {
            return new Kategori(in);
        }

        @Override
        public Kategori[] newArray(int size) {
            return new Kategori[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getKode() {
        return kode;
    }

    public String getNama() {
        return nama;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(kode);
        parcel.writeString(nama);
        parcel.writeString(created_at);
        parcel.writeString(updated_at);
    }
}
