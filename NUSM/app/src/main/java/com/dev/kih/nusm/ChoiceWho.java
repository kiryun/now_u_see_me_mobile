package com.dev.kih.nusm;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ChoiceWho extends AppCompatActivity {
    private ApiClient apiClient = new ApiClient();
    ScrollView scrollView;
    Singleton singleton;
    ArrayList<String> urls;
    ArrayList<String> names;
    ArrayList<Bitmap> bitmaps;
    int createIdx;
    String[] resultType;
    String[] resultName;
    String eventTime;
    private String dbName = "ListDB.db";
    private int dbVersion = 1;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        singleton = Singleton.getInstance();
        scrollView = findViewById(R.id.scrollView);
        eventTime = getIntent().getExtras().getString("selectEventTime");
        urls = new ArrayList<String>();
        names = new ArrayList<String>();
        bitmaps = new ArrayList<Bitmap>();

        if(singleton.size_DataFrames() >0) {
            Singleton.GetDataFrame getDataFrame =singleton.getDataFrames(0);
            if(!getDataFrame.equals(null)) {
                urls = getDataFrame.getIs();
                names = getDataFrame.getNames();
            }
        }
        for(int i = 0; i<urls.size();i++){
            Log.d("Test Get url", urls.get(i));
            Log.d("Test Get name", names.get(i));
        }
        singleton.removeDataFrames();
        //////////////////////////////////////////////////
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < urls.size(); i++) {
                        URL url = new URL("http://203.252.91.45:3000"+urls.get(i)+"/"+names.get(i));
                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();

                        InputStream is = conn.getInputStream();
                        bitmaps.add(BitmapFactory.decodeStream(is));
                    }
                }catch (MalformedURLException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try{
            thread.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        if(bitmaps.size() > 0) {
            resultType = new String[urls.size()];
            resultName = new String[urls.size()];
            createView(urls.size());
        }
        else {
            Log.d("URL Failure", "URL PROBLEM");
            Toast.makeText(this, "URL Failure", Toast.LENGTH_SHORT).show();
        }

        //////////////////////////////////////////////////
        Button bt = (Button)findViewById(R.id.button);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i<names.size(); i++) {
                    resultName[i] = names.get(i);
                }
                apiClient.sendResult(eventTime, resultName, resultType);
                dbHelper = new DBHelper(getBaseContext(), dbName, null, dbVersion);
                db = dbHelper.getWritableDatabase();
                db = dbHelper.getWritableDatabase();
                db.execSQL("DELETE FROM Notification WHERE event_time = '" + eventTime + "';");
                finish();
                //이 뷰 끄고 main으로 이동
            }
        });
    }

    public void createView(int dataSize){
        createIdx = 0;
        ImageView imageView;
        RadioButton radioButton;
        RadioGroup radioGroup;
        LinearLayout verticalLayout;
        LinearLayout horizonLayout;
        LinearLayout allLayout;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final String[] selectType = {"Unknown", "Family", "Friends"};

        LinearLayout.LayoutParams paramsVer = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsVer.height =size.y/2;
        LinearLayout.LayoutParams paramsHori = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams paramsRadi = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsRadi.weight = 1;
        LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams paramsAll = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        allLayout = new LinearLayout(getBaseContext());
        allLayout.setLayoutParams(paramsAll);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        for(; createIdx < dataSize; createIdx++){
            verticalLayout = new LinearLayout(getBaseContext());
            verticalLayout.setOrientation(LinearLayout.VERTICAL);
            verticalLayout.setLayoutParams(paramsVer);
            imageView = new ImageView(getBaseContext());
            imageView.setAdjustViewBounds(true);
            imageView.setLayoutParams(paramsImg);
            imageView.setImageBitmap(bitmaps.get(createIdx));
            //imageView에 이미지 넣기
            verticalLayout.addView(imageView);
            horizonLayout = new LinearLayout(getBaseContext());
            horizonLayout.setOrientation(LinearLayout.HORIZONTAL);
            horizonLayout.setLayoutParams(paramsHori);
            radioGroup = new RadioGroup(getBaseContext());
            radioGroup.setOrientation(LinearLayout.HORIZONTAL);
            radioGroup.setLayoutParams(paramsRadi);
            for(int j = 0; j < 3; j++){
                radioButton = new RadioButton(getBaseContext());
                radioButton.setLayoutParams(paramsRadi);
                radioButton.setText(selectType[j]);
                radioButton.setId(createIdx*1000+j);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = v.getId();
                        resultType[index/1000] = selectType[index%1000];
                        for(int i = 0; i<resultType.length;i++)
                            Log.d("Radio","id : "+resultType[i]);
                    }
                });
                radioGroup.addView(radioButton);
            }
            horizonLayout.addView(radioGroup);
            verticalLayout.addView(horizonLayout);
            allLayout.addView(verticalLayout);
        }
        scrollView.addView(allLayout);
    }
}
