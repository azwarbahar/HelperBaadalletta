package com.baadalletta.helper.models;

import java.util.List;

public class Pembelian {

    private int id;

    private int id_helper;

    private int id_produk;

    private String tanggal;

    private String foto;

    private int harga;

    private String created_at;

    private String updated_at;

    private Produk produk;

    private Helper helpers;

    public int getId() {
        return id;
    }

    public int getId_helper() {
        return id_helper;
    }

    public int getId_produk() {
        return id_produk;
    }

    public String getTanggal() {
        return tanggal;
    }

    public String getFoto() {
        return foto;
    }

    public int getHarga() {
        return harga;
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

    public Helper getHelpers() {
        return helpers;
    }
}
