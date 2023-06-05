package com.example.myhomework4;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity2 extends AppCompatActivity implements Runnable
{
    private static final String TAG = "Net";
    Handler handler;
    Button button1;
    TextView text1;
    TextView text2;
    TextView text3;
    TextView text4;
    TextView text5;
    TextView text6;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        button1 = findViewById(R.id.btn1);
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);
        text4 = findViewById(R.id.text4);
        text5 = findViewById(R.id.text5);
        text6 = findViewById(R.id.text6);
        Intent intent = getIntent();
        String Bv = intent.getStringExtra("url");
        text1.setText(Bv);

        handler = new Handler(Looper.myLooper()){
            public void handleMessage(@NonNull Message msg1)
            {
                //处理返回
                if(msg1.what==5){
                    String str = (String) msg1.obj;
                    Log.i(TAG,"handleMessage: str="+str);
                    text2.setText(str);
                }
                super.handleMessage(msg1);
            }
            public void handleMessage2(@NonNull Message msg2)
            {
                //处理返回
                if(msg2.what==5){
                    String str = (String) msg2.obj;
                    Log.i(TAG,"handleMessage2: str="+str);
                    text3.setText(str);
                }
                super.handleMessage(msg2);
            }


        };


        Log.i(TAG, "onCreat: start Thread");
        Thread t = new Thread(this);
        t.start();

    }

    @Override
    public void run() {
        Log.i(TAG,"run:线程已启动");

        //获取网络数据
        String html = "";
        String html1 = "";
        //个人信息
        String uid = "63231";
        String url_main = "https://api.bilibili.com/x/relation/stat?vmid=" + uid;
        String referer_main = "https://space.bilibili.com/" + uid;
        String content_main = getContent(url_main,referer_main);
        Log.i(TAG,"个人信息（关注数+观众数）：" + content_main);

        //个人信息（名称、性别、头像、描述、个人认证信息、大会员状态、直播间地址、预览图、标题、房间号、观看人数、直播间状态[开启/关闭]等）
        String url_number = "https://api.bilibili.com/x/space/acc/info?mid=" + uid;
        String referer_number = "https://space.bilibili.com/" + uid;
        String content_number = getContent(url_number,referer_number);
        Log.i(TAG,"个人信息（视频数+文章数+播放总量+获赞量）：" + content_number);

        //视频数据
        String Bv = "BV19k4y1W7JZ";
        String Bv_number_url = "https://api.bilibili.com/x/web-interface/view?bvid=" + Bv;
        String Bv_number_referer = "https://www.bilibili.com/video/" + Bv;
        String content_Bv_number = getContent(Bv_number_url,Bv_number_referer);
        Log.i(TAG,"视频数据：" + content_Bv_number);

        //视频弹幕
        String Bv_url = "https://api.bilibili.com/x/player/pagelist?bvid=" + Bv;
        String Bv_referer = "https://www.bilibili.com/video/" + Bv;
        String content_Bv = getContent(Bv_url,Bv_referer);
        String cid = Cid(content_Bv);
            String url_word = "https://comment.bilibili.com/" + cid + ".xml";
        Document doc = null;
        try {
            doc = Jsoup.connect(url_word).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Log.i(TAG,"视频弹幕：" +doc);

        //发送消息
        Message msg1 = handler.obtainMessage(5,content_main);
        handler.sendMessage(msg1);
        Message msg2 = handler.obtainMessage(5,content_number);
        handler.sendMessage(msg2);
    }

    public void onClick(View view)
    {
        Intent intent = new Intent(MainActivity2.this,SearchActivity.class);
        startActivity(intent);
    }
    public String getContent(String url, String referer){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36 Edg/114.0.1823.37")
                .addHeader("Referer",referer)
                .addHeader("Accept-Charset", "UTF-8")
                .build();

        String result = null;
        try{
            Call call = client.newCall(request);
            Response rep = call.execute();
            int code = rep.code();
            Log.i(TAG,"状态码为：" + code);
            result = rep.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    public static String Cid(String content) {
        // 将JSON字符串解析为JSONObject对象
        JSONObject jsonObject = JSONObject.parseObject(content);
        // 获取"data"字段对应的JSONArray
        JSONArray dataArray = jsonObject.getJSONArray("data");
        // 获取第一个元素的JSONObject
        JSONObject dataObject = dataArray.getJSONObject(0);
        //JSONObject dataObject2 = dataArray.getJSONObject(12);
        // 提取"cid"字段的值
        //String Frame_int = dataObject2.getString("first_frame");
        String cid = dataObject.getString("cid");
        return cid;
    }




}