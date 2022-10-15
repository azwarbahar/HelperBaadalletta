package com.baadalletta.helper.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Helper implements Parcelable {

    private int id;

    private String nama;

    private String alamat;

    private String whatsaap;

    private String status_akun;

    private String foto;

    private String status_verifikasi;

    private String created_at;

    private String updated_at;

    protected Helper(Parcel in) {
        id = in.readInt();
        nama = in.readString();
        alamat = in.readString();
        whatsaap = in.readString();
        status_akun = in.readString();
        foto = in.readString();
        status_verifikasi = in.readString();
        created_at = in.readString();
        updated_at = in.readString();
    }

    public static final Creator<Helper> CREATOR = new Creator<Helper>() {
        @Override
        public Helper createFromParcel(Parcel in) {
            return new Helper(in);
        }

        @Override
        public Helper[] newArray(int size) {
            return new Helper[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getAlamat() {
        return alamat;
    }

    public String getWhatsaap() {
        return whatsaap;
    }

    public String getStatus_akun() {
        return status_akun;
    }

    public String getFoto() {
        return foto;
    }

    public String getStatus_verifikasi() {
        return status_verifikasi;
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
        parcel.writeString(nama);
        parcel.writeString(alamat);
        parcel.writeString(whatsaap);
        parcel.writeString(status_akun);
        parcel.writeString(foto);
        parcel.writeString(status_verifikasi);
        parcel.writeString(created_at);
        parcel.writeString(updated_at);
    }
}
