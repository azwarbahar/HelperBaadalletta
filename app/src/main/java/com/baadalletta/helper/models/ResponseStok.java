package com.baadalletta.helper.models;

import java.util.List;

public class ResponseStok {

    private int status_code;

    private String message;

    private Stok data;

    public int getStatus_code() {
        return status_code;
    }

    public String getMessage() {
        return message;
    }

    public Stok getData() {
        return data;
    }
}
