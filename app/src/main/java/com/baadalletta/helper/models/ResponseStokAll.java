package com.baadalletta.helper.models;

import java.util.List;

public class ResponseStokAll {

    private int status_code;

    private String message;

    private List<Stok> data;

    public int getStatus_code() {
        return status_code;
    }

    public String getMessage() {
        return message;
    }

    public List<Stok> getData() {
        return data;
    }
}
