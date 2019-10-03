package com.dev.kih.nusm;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    private String URL = "http://203.252.91.45";//서버 IP Adress
    private String PORT = ":3000/";//서버 Port
    private String url_media = "event/";//서버 Directory

    public boolean sendToken(String token){//앱 시작과 동시에 FireBase에서 보낸 Token을 서버로 전송
        try{
            OkHttpClient client = new OkHttpClient();//서버통신 모듈로 OkHttp 사용
            String url = URL+PORT+url_media+"token";//서버의 event/token 디렉토리로 전송
            JSONObject postdata = new JSONObject();
            try {
                postdata.put("token", token);//Json 형식으로 변환
            } catch(JSONException e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Request request = new Request.Builder()
                    .url(url)//Post 요청
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
    public boolean sendData( String eventTime){//EventTime에 대한 Data를 서버에 요청
        try{
            Singleton singleton = Singleton.getInstance();
            singleton.setEventTime(eventTime);
            OkHttpClient client = new OkHttpClient();
            String url = URL+PORT+"event/images/"+eventTime;//서버의 event/images/에 있는 eventTime 디렉토리에 전송
            Request request = new Request.Builder()//Get 요청
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
    public boolean sendResult(String eventTime, String[] resultName, String[] resultType){//사람에 대한 Type을 서버에 전송
        try{

            OkHttpClient client = new OkHttpClient();
            String url = URL+PORT+"event/update/";//서버의 event/update 디렉토리에 전송
            JSONObject postdata = new JSONObject();
            try {//Json 형식으로 변환
                postdata.put("eventTime", eventTime);
                JSONArray imagedata =null;
                JSONArray typedata =null;
                imagedata = new JSONArray();
                typedata = new JSONArray();
                for(int i = 0; i<resultName.length;i++) {//각 String 값을 배열로 묶기 위함
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
                    .url(url)//Post 요청
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
    private Callback sendTokenCallback = new Callback() {//Token 콜백 함수
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
    private Callback getCallback = new Callback() {//Get 요청에 대한 콜백 함수
        @Override
        public void onFailure(Call call, IOException e) {
            String error= e.getMessage();
            Log.d("Get Fail!", error);
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            Log.d("Get data", body);
            Singleton singleton = Singleton.getInstance();
            ArrayList<String> img= new ArrayList<String>();//Image url을 담는 변수
            ArrayList<String> name = new ArrayList<String>();//Image 이름을 담는 변수
            try {
                JSONObject jsonObject = new JSONObject(body);//Get 데이터를 Json 형식으로 받기
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
            if(img.size()>0) {//파일을 Json 형식으로 제대로 받은 경우에 Singleton에 저장하고 뷰 시작
                singleton.setDataFrames(img, name);
                Log.d("Get data", "Send Singleton");
                Intent intent = new Intent(singleton.getContext(), ChoiceWho.class);
                intent.putExtra("selectEventTime", singleton.getEventTime());
                singleton.getContext().startActivity(intent);
            }
            else {//파일을 제대로 받지 못한 경우에는 eventTime을 삭제
                Log.d("Get data", "No Data");
                String dbName = "ListDB.db";
                int dbVersion = 1;
                DBHelper dbHelper;
                SQLiteDatabase db;
                String eventTime = singleton.getEventTime();
                dbHelper = new DBHelper(singleton.getContext(), dbName, null, dbVersion);
                db = dbHelper.getWritableDatabase();
                db.execSQL("DELETE FROM Notification WHERE event_time = '" + eventTime + "';");
                db.close();
                Message msg = singleton.getHandler().obtainMessage();//ListView를 갱신하기 위함
                singleton.getHandler().sendMessage(msg);
            }
        }
    };
    private Callback resultCallback = new Callback() {//결과값 Post에 대한 콜백 함수
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d("Result Fail!", e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            Log.d("Result Success!", response.body().string());
        }
    };
}
