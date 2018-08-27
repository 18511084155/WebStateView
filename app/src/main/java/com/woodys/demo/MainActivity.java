package com.woodys.demo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {
    private EditText urlEditText;
    private String url = "https://h5-auth-renfeng.liangkebang.com/face-recognition?slaveId=865002026301499&token=0f4ae7f82091e243b92a52f263610855796c&quality=0.6&frequency=30";
    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlEditText = (EditText) findViewById(R.id.urlEditText);
        urlEditText.setText(url);


        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OtherWebviewActivity.class);
                //用Bundle携带数据
                Bundle bundle = new Bundle();
                bundle.putString("turl",urlEditText.getText().toString());
                bundle.putString("type", "1");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebActivity.class);
                //用Bundle携带数据
                Bundle bundle = new Bundle();
                bundle.putString("turl",urlEditText.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, CertificaWebViewActivity.class);
                //用Bundle携带数据
                Bundle bundle = new Bundle();
                bundle.putString("turl",urlEditText.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, CameraWebviewActivity.class);
                //用Bundle携带数据
                Bundle bundle = new Bundle();
                bundle.putString("turl",urlEditText.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });
    }
}