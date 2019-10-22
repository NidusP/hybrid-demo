package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import wendu.dsbridge.CompletionHandler;
import wendu.dsbridge.DWebView;

public class MainActivity extends AppCompatActivity {

    private DWebView dwebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dwebView= (DWebView) findViewById(R.id.dWebView);
        dwebView.addJavascriptObject(new JsApi(), null);
        dwebView.loadUrl("http://192.168.1.12:8080/index.html?timestap=" + new Date());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        changeTheme(0xFFFF0000);
        return true;
    }

    private void changeTheme (int color){
        // 状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(color);

        // 标题栏
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));

        // 导航栏
        getWindow().setNavigationBarColor(color);

        // web网页
        dwebView.callHandler("changeTheme", new Object[]{color});
    }

    public class JsApi{
        @JavascriptInterface
        public void nativeRequest(Object params, CompletionHandler handler)  {
            // Log.d("", "nativeRequest" + params);
            try {
                String url = ((JSONObject)params).getString("url");
                String data = request(url);
                handler.complete(data);
            } catch (Exception e) {
                handler.complete(e.getMessage());
                e.printStackTrace();
            }
        }

        private String request(String urlSpec) throws Exception {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlSpec).openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer result = new StringBuffer();
            String line;
            while ( (line = reader.readLine()) != null){
                result.append(line);
            }
            return result.toString();
        }
    }
}
