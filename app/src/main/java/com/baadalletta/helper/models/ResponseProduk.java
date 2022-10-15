package com.baadalletta.helper.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ResponseProduk{

    private String message;

    private int status_code;

    private List<Produk> data;


    public String getMessage() {
        return message;
    }

    public int getStatus_code() {
        return status_code;
    }

    public List<Produk> getData() {
        return data;
    }

}
