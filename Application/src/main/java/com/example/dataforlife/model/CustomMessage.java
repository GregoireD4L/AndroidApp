package com.example.dataforlife.model;

import java.io.Serializable;
import java.time.Instant;

public class CustomMessage implements Serializable {

    private String data;
    private String id;
    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

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
