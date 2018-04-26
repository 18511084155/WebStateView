package com.woodys.demo.data;

import com.woodys.libsocket.sdk.bean.ISendable;

import java.nio.charset.Charset;

/**
 * Created by woodys 2018/3/24.
 */

public class MsgDataBean implements ISendable {
    private String content = null;

    public MsgDataBean(String content) {
        this.content = content;
    }

    @Override
    public byte[] parse() {
        return content.getBytes(Charset.defaultCharset());
    }
}
