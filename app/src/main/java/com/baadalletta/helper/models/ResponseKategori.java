package com.baadalletta.helper.models;

import java.util.List;

public class ResponseKategori {

    private String message;

    private int status_code;

    private List<Kategori> data;

    public String getMessage() {
        return message;
    }

    public int getStatus_code() {
        return status_code;
    }

    public List<Kategori> getData() {
        return data;
    }
}
