package com.baadalletta.helper.models;

import java.util.List;

public class ResponsePembelian {

    private String message;

    private int status_code;

    private List<Pembelian> data;

    public String getMessage() {
        return message;
    }

    public int getStatus_code() {
        return status_code;
    }

    public List<Pembelian> getData() {
        return data;
    }
}
