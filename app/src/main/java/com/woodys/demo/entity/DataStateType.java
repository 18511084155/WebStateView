package com.woodys.demo.entity;

import com.woodys.demo.JsonCallback;

public class DataStateType {
    public String type;
    public String event;
    public String value;
    public JsonCallback messageCallback;

    public DataStateType(String type,String event,String value,JsonCallback messageCallback) {
        this.type = type;
        this.event = event;
        this.value = value;
        this.messageCallback = messageCallback;
    }
}
