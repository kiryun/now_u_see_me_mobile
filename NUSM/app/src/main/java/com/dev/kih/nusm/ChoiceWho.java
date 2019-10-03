package com.dev.kih.nusm;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.os.Message;
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
        singleton = Singleton.getInstance();//Singleton 객체 불러오기
        scrollView = findViewById(R.id.scrollView);
        eventTime = getIntent().getExtras().getString("selectEventTime");//서비스에서 ChoiceWho Intent를 시작할 때 변수를 보낸 것을 받기 위함
        urls = new ArrayList<String>();
        names = new ArrayList<String>();
        bitmaps = new ArrayList<Bitmap>();

        if(singleton.size_DataFrames() >0) {//Singleton에 있는 데이터 가져오기. EventTime에 대한 url과 이미지 이름이 있음ㄴ
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
        singleton.removeDataFrames();//사용한 Get Data를 지움
        //////////////////////////////////////////////////
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {//Image를 Http로 받아오기 위해 Thread 사용
                    for (int i = 0; i < urls.size(); i++) {
                        URL url = new URL("http://203.252.91.45:3000"+urls.get(i)+"/"+names.get(i));
                        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();

                        InputStream is = conn.getInputStream();
                        bitmaps.add(BitmapFactory.decodeStream(is));//이미지를 비트맵 형식으로 받아옴
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
            thread.join();//Thread가 종료될 때까지 기다림
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        if(bitmaps.size() > 0) {//이미지를 제대로 받아올 경우 실행
            resultType = new String[urls.size()];
            for(int i = 0; i<resultType.length;i++){
                resultType[i]="Unknown";//결과값을 생성. 선택하지 않은 결과값은 자동으로 Unknown으로 처리하기 위함
            }
            resultName = new String[urls.size()];
            createView(urls.size());//뷰 생성
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
                apiClient.sendResult(eventTime, resultName, resultType);//서버에 결과를 전송
                dbHelper = new DBHelper(getBaseContext(), dbName, null, dbVersion);
                db = dbHelper.getWritableDatabase();
                db.execSQL("DELETE FROM Notification WHERE event_time = '" + eventTime + "';");//DB에 eventTime을 삭제
                Singleton singleton = Singleton.getInstance();
                Message msg = singleton.getHandler().obtainMessage();
                singleton.getHandler().sendMessage(msg);//메인 Activity에 있는 ListView를 갱신함
                finish();
            }
        });
    }

    public void createView(int dataSize){//동적으로 이미지와 Unknown에 대한 타입을 설정하는 버튼을 생성
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
        final String[] selectType = {"Unknown", "Family", "Friends"};//Unknown에 대한 타입

        LinearLayout.LayoutParams paramsVer = new LinearLayout.LayoutParams(//Vertical LinearLayout
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsVer.height =size.y/2;
        LinearLayout.LayoutParams paramsHori = new LinearLayout.LayoutParams(//Horizontal LinearLayout
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams paramsRadi = new LinearLayout.LayoutParams(//Radio Button
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsRadi.weight = 1;
        LinearLayout.LayoutParams paramsImg = new LinearLayout.LayoutParams(//Image View
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsImg.height = 250;
        paramsImg.width = 250;
        LinearLayout.LayoutParams paramsAll = new LinearLayout.LayoutParams(//위에 모든 객체를 담는 뷰
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        allLayout = new LinearLayout(getBaseContext());
        allLayout.setLayoutParams(paramsAll);
        allLayout.setOrientation(LinearLayout.VERTICAL);
        for(; createIdx < dataSize; createIdx++){//이미지 개수만큼 생성
            verticalLayout = new LinearLayout(getBaseContext());
            verticalLayout.setOrientation(LinearLayout.VERTICAL);
            verticalLayout.setLayoutParams(paramsVer);
            imageView = new ImageView(getBaseContext());
            imageView.setAdjustViewBounds(true);
            imageView.setLayoutParams(paramsImg);
            imageView.setImageBitmap(bitmaps.get(createIdx));//imageView에 이미지 넣기
            verticalLayout.addView(imageView);
            horizonLayout = new LinearLayout(getBaseContext());
            horizonLayout.setOrientation(LinearLayout.HORIZONTAL);
            horizonLayout.setLayoutParams(paramsHori);
            radioGroup = new RadioGroup(getBaseContext());
            radioGroup.setOrientation(LinearLayout.HORIZONTAL);
            radioGroup.setLayoutParams(paramsRadi);
            for(int j = 0; j < 3; j++){//RadioButton은 3개를 생성한다.
                radioButton = new RadioButton(getBaseContext());
                radioButton.setLayoutParams(paramsRadi);
                radioButton.setText(selectType[j]);
                radioButton.setId(createIdx*1000+j);//어떤 RadioButton이 눌렸는지 알기 위함
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index = v.getId();
                        resultType[index/1000] = selectType[index%1000];//어떤 버튼이 눌렸는지를 resultType에 저장
                        for(int i = 0; i<resultType.length;i++)
                            Log.d("Radio","id : "+resultType[i]);
                    }
                });
                radioGroup.addView(radioButton);//RadioButton을 RadioGroup으로 묶음
            }
            horizonLayout.addView(radioGroup);
            verticalLayout.addView(horizonLayout);
            allLayout.addView(verticalLayout);//Image -> RadioButton -> HorizontalLayout->VerticalLayout이 차례로 들어감.
        }
        scrollView.addView(allLayout);//scrollView에는 하나의 뷰만 들어갈 수 있으므로 AllLayout을 넣는다.
    }
    public void onDestroy() {
        super.onDestroy();
        Singleton singleton = Singleton.getInstance();
        Message msg = singleton.getHandler().obtainMessage();
        singleton.getHandler().sendMessage(msg);
    }
}
