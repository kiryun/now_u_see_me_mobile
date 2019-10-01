package com.dev.kih.nusm;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.Map;

public class ApiClient {
    private String URL = "http://203.252.91.45";//"http://127.0.0.1"; //return: 203.252.91.45
    private String PORT = ":3000/";
    private String url_media = "event/";

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
            Singleton singleton = Singleton.getInstance();
            singleton.setEventTime(eventTime);
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
    public boolean sendResult(String eventTime, String[] resultName, String[] resultType){
        try{

            OkHttpClient client = new OkHttpClient();
            String url = URL+PORT+"event/update/";
            JSONObject postdata = new JSONObject();
            try {
                postdata.put("eventTime", eventTime);
                JSONArray imagedata =null;
                JSONArray typedata =null;
                imagedata = new JSONArray();
                typedata = new JSONArray();
                for(int i = 0; i<resultName.length;i++) {
                    imagedata.put(resultName[i]);
                    typedata.put(resultType[i]);
                }
                postdata.put("types", typedata);
                postdata.put("imageName", imagedata);
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
            client.newCall(request).enqueue(resultCallback);
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
            Log.d("Token Fail!", e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String res = response.body().string();
            Log.d("Token Success!", res);
        }
    };
    private Callback getCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("Get Fail!", e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            Log.d("Get data", body);
            Singleton singleton = Singleton.getInstance();
            ArrayList<String> img= new ArrayList<String>();
            ArrayList<String> name = new ArrayList<String>();
            try {
                JSONObject jsonObject = new JSONObject(body);
                JSONArray jsonArray = jsonObject.getJSONArray("images");
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    img.add(jsonObject1.optString("url"));
                    name.add(jsonObject1.optString("name"));
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }
            if(img.size()>0) {
                singleton.setDataFrames(img, name);
                Log.d("Get data", "Send Singleton");
                Intent intent = new Intent(singleton.getContext(), ChoiceWho.class);
                intent.putExtra("selectEventTime", singleton.getEventTime());
                singleton.getContext().startActivity(intent);
            }
            else
                Log.d("Get data", "No Data");
        }
    };
    private Callback resultCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("Result Fail!", e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String res = response.body().string();
            Log.d("Result Success!", res);
        }
    };
}
