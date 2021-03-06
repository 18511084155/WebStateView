package com.woodys.demo;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.woodys.demo.data.MessageBean;
import com.woodys.demo.data.MsgDataBean;
import com.woodys.demo.utils.AESEncoder;
import com.woodys.demo.utils.JsonUtils;
import com.woodys.devicelib.KeplerSdk;
import com.woodys.libsocket.sdk.connection.IConnectionManager;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SendMessageUtils {
    /**
     * 发送设备信息
     * @param connectionManager
     * @param type
     * @param uuid
     */
    public static void sendDeviceInfoMessage(final IConnectionManager connectionManager, final Context context, final String type, final String uuid){
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    JSONObject deviceInfo = KeplerSdk.getInstance().getDeviceInfo(context);
                    deviceInfo.put("appChannel", "ceshi");
                    deviceInfo.put("registerFrom", "217");
                    deviceInfo.put("uuid", uuid);

                    byte[] data = AESEncoder.encrypt(getMessage(type,"DATA",deviceInfo.toString(),null));
                    ByteArrayOutputStream arr = new ByteArrayOutputStream();
                    OutputStream zipper = new GZIPOutputStream(arr);
                    zipper.write(data);
                    zipper.flush();
                    zipper.close();
                    String encoder = Base64.encodeToString(arr.toByteArray(), Base64.NO_WRAP);
                    subscriber.onNext(encoder+"\r\n");
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).observeOn(AndroidSchedulers.mainThread()).
        subscribeOn(Schedulers.io()).
        subscribe(new Action1<String>() {
              @Override
              public void call(String msg) {
                  MsgDataBean msgDataBean = new MsgDataBean(msg);
                  connectionManager.send(msgDataBean);
              }
          }, new Action1<Throwable>() {
              @Override
              public void call(Throwable e) {
                  e.printStackTrace();
              }
          });
    }

    /**
     * 发送消息
     * @param connectionManager
     * @param type
     * @param content
     */
    public static void sendMessage(final IConnectionManager connectionManager,final String type,final String event, final String content,final JsonCallback messageCallback){
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    byte[] data = AESEncoder.encrypt(getMessage(type,event,content,messageCallback));
                    ByteArrayOutputStream arr = new ByteArrayOutputStream();
                    OutputStream zipper = new GZIPOutputStream(arr);
                    zipper.write(data);
                    zipper.flush();
                    zipper.close();
                    String encoder = Base64.encodeToString(arr.toByteArray(), Base64.NO_WRAP);
                    subscriber.onNext(encoder+"\r\n");
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).
                subscribeOn(Schedulers.io()).
                subscribe(new Action1<String>() {
                      @Override
                      public void call(String msg) {
                          MsgDataBean msgDataBean = new MsgDataBean(msg);
                          connectionManager.send(msgDataBean);
                      }
                  }, new Action1<Throwable>() {
                      @Override
                      public void call(Throwable e) {
                          e.printStackTrace();
                      }
                  }
                );
    }

    private static String getMessage(String type,String event,String data,JsonCallback messageCallback){
        MessageBean messageBean=new MessageBean();
        /** 下面四个字段必须传递 **/
        messageBean.userId = "18511084155";
        messageBean.event = event ;
        messageBean.userSource = type;
        messageBean.appId = "0008";
        /** 下面两个字段可传递 **/
        messageBean.data = data;
        messageBean.registerFrom = "217";
        messageBean.versionCode = "6381";
        String jsonStr = null;
        if(messageCallback!=null){
            JsonObject jsonObject= new JsonParser().parse(JsonUtils.toJson(messageBean)).getAsJsonObject();
            jsonStr = messageCallback.convertData(jsonObject);
        }else {
            jsonStr = JsonUtils.toJson(messageBean);
        }
        if (BuildConfig.DEBUG) {
            Log.e("测试", "====getMessage====  jsonStr:" + jsonStr);
        }
        return jsonStr;
    }

}
