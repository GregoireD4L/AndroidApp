package com.example.dataforlife.model;

import java.io.Serializable;

public class CustomMessage implements Serializable {

    private String data;
    private String id;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
