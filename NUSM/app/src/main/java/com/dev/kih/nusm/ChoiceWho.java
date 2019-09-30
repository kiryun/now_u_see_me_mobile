package com.dev.kih.nusm;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.IOException;
import java.util.ArrayList;

public class ChoiceWho extends AppCompatActivity {
    private ApiClient apiClient = new ApiClient();
    static Object s_getData = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        String getEventTime= getIntent().getExtras().getString("selectEventTime");
        apiClient.sendData(getEventTime);
        ImageView imView = findViewById(R.id.imageView);
////////////////////////////////////////////////////
        /*
        InputStream is = response.body().byteStream();
        getData = response.body();
        ChoiceWho.setGetData(getData);
        setObject();
        Bitmap bitmap = BitmapFactory.decodeStream(is);
         */
        String readData;
        for(int i = 0; i<10; i++) {
            readData = String.valueOf(s_getData);
            if (readData != "null")
                Log.d("Show Data ", String.valueOf(s_getData));
            else
                break;
            MatrixTime(500);
        }
    }
    static void setGetData(Object object){
        s_getData = object;
    }
    public void MatrixTime(int delayTime){
        long saveTime = System.currentTimeMillis();
        long currTime = 0;
        while( currTime - saveTime < delayTime){
            currTime = System.currentTimeMillis();
        }
    }
}
