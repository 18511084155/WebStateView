package com.woodys.demo.entity;

public class StateViewType {
    //内容type
    public static final int LAYOUT_CONTENT_TYPE = 1;

    //loading 加载type
    public static final int LAYOUT_LOADING_TYPE = 2;

    //失败type
    public static final int LAYOUT_ERROR_TYPE = 3;

    //成功type
    public static final int LAYOUT_SUCCESS_TYPE = 4;

    public int type;
    public int value;

    public StateViewType(int type, int value) {
        this.type = type;
        this.value = value;
    }
}
