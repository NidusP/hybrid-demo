package com.example.jsbridge;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import wendu.dsbridge.DWebView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    // private DWebView dWebView;
    private EditText editText;
    private Button showBtn;
    private Button showBtn2;
    private Button refreshBtn;
    private MainActivity self = this;
    private NativeSDK nativeSDK = new NativeSDK(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        refreshBtn = findViewById(R.id.refreshBtn);
        showBtn = findViewById(R.id.showBtn);
        showBtn2 = findViewById(R.id.showBtn2);
        editText = findViewById(R.id.editText);

        webView.loadUrl("http://192.168.1.12:8080/index.html?timestap=1");
        webView.getSettings().setJavaScriptEnabled(true);


        webView.setWebChromeClient(new WebChromeClient(){
            // 拦截webView请求的URL Schema实现jsBridge，重写对象拦截弹窗（开源实现JsBridge）
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                if (!message.startsWith("jsBridge://")){
                    return super.onJsAlert(view, url, message, result);
                }
                // 符合自定义url Schema，执行native弹窗方法
                String text = message.substring(message.indexOf("=") + 1);
                self.showNativeDialog(text);

                result.confirm();
                return true;
            }
        });
        // 向webView注入JS api（开源实现DsBridge）
        webView.addJavascriptInterface(new NativeBridge(this), "NativeBridge");

        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 拿到native输入的内容，并调用显示web弹窗的方法
               String iptValue = editText.getText().toString();
               self.showWebDialog(iptValue);
            }
        });

        // Native端的button2，点击获取web端输入内容
        showBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeSDK.getWebEditTextValue(new Cb() {
                    @Override
                    public void invoke(String value) {
                        // native弹窗显示web端内容
                        new AlertDialog.Builder(self).setMessage("web 输入值：" + value)
                                .create()
                                .show();
                    }
                });
            }
        });
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("http://192.168.1.12:8080/index.html?timestap=" + new Date().getTime());
            }
        });
    }
    //  显示web弹窗
    private void showWebDialog (String text) {
        String jsCode = String.format("window.showWebDialog('%s')", text);
        webView.evaluateJavascript(jsCode, null);
    }
    // 显示原生弹窗
    private void showNativeDialog (String text) {
        new AlertDialog.Builder(this).setMessage(text).create().show();
    }


    interface Cb {
        void invoke(String value);
    }

    // SDK封装，定义获取web内容方法
    class NativeSDK {
        private Context ctx;
        private int id = 1;
        private Map<Integer, Cb> cbMap = new HashMap();
        NativeSDK(Context ctx){
            this.ctx = ctx;
        }

        void getWebEditTextValue(Cb cb){
            int cbId = id++;
            cbMap.put(cbId, cb);
            final String jsCode = String.format("window.JSSDK.getWebEditTextValue(%s)", cbId);
            ((MainActivity)ctx).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity)ctx).webView.evaluateJavascript(jsCode, null);
                }
            });
        }
        void receiveMessage(int cbId, String value){
            if (cbMap.containsKey(cbId)){
                cbMap.get(cbId).invoke(value);
            }
        }
    }
    class NativeBridge {
        private Context ctx;
        NativeBridge(Context ctx){
            this.ctx = ctx;
        }

        @JavascriptInterface
        public void showNativeDialog (String text) {
            new AlertDialog.Builder(ctx).setMessage(text).create().show();
        }
        // 获取native端内容，并调用web端receiveMessage方法，接收回调ID，及执行结果
        @JavascriptInterface
        public void getNativeEditTextValue (int cbId){
            final MainActivity mainActivity = (MainActivity)ctx;
            String value = mainActivity.editText.getText().toString();
            final String jsCode = String.format("window.JSSDK.receiveMessage(%s,'%s')", cbId, value);
            // 需要在ui线程中指行调用js代码
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.webView.evaluateJavascript(jsCode, null);
                }
            });
        }
        @JavascriptInterface
        public void receiveMessage(int cbId, String value){
            ((MainActivity)ctx).nativeSDK.receiveMessage(cbId, value);
        }
    }
}
