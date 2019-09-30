package com.dev.kih.nusm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.lang.*;

public class ApiClient {
    private String URL = "http://203.252.91.45";//"http://127.0.0.1"; //return: 203.252.91.45
    private String PORT = ":3000/";
    private String url_media = "event/";
    private Object getData = null;
    public boolean testGet(){
        Log.d("test", "test");
        try{
            OkHttpClient client = new OkHttpClient();
            String url = URL+PORT+url_media+"test";
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            client.newCall(request).enqueue(sendTokenCallback);
            return true;

        }catch (Exception e){
            e.printStackTrace();;
        }

        return false;
    }

    // http://127.0.0.1:3000/media/token
    //
    public boolean sendToken(String token){
        try{
            OkHttpClient client = new OkHttpClient();
            String url = URL+PORT+url_media+"token";
            JSONObject postdata = new JSONObject();
            try {
                postdata.put("token", token);
            } catch(JSONException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse("application/json"), postdata.toString()))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .build();
            client.newCall(request).enqueue(sendTokenCallback);
            Log.i("request: ",request.toString());
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public boolean sendData( String eventTime){
        try{
            OkHttpClient client = new OkHttpClient();
            String url = URL+PORT+"event/images/"+eventTime;
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(getCallback);
            Log.i("request: ",request.toString());
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private Callback sendTokenCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("Fail!", e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String res = response.body().string();
            Log.d("Success!", res);
        }
    };
    private Callback getCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("Get Fail!", e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            InputStream is = response.body().byteStream();
            getData = response.body();
            ChoiceWho.setGetData(getData);
            getData=null;
            //final String res = response.body().string();
            Log.d("Get data", "get receive");
        }
    };
}
