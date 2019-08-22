package com.dev.kih.nusm;

import android.util.Log;

import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.lang.*;

public class ApiClient {
    private String URL = "http://127.0.0.1";
    private String PORT = ":3000/";
    private String url_media = "media/";

    // http://127.0.0.1:3000/media/token
    //
    public boolean sendToken(String token){
        try{
            OkHttpClient client = new OkHttpClient();

            String url = URL+PORT+url_media+"token";

            Gson gson = new Gson();
            String json = gson.toJson(token);

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse("application/json"), json))
                    .build();

            Response response = client.newCall(request).execute();

            Log.i("request: ",request.toString());
            Log.i("Response: ",response.toString());

            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
