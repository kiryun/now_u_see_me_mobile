package com.dev.kih.nusm;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ApiClient apiClient = new ApiClient();
    private String dbName = "ListDB.db";
    private String sql;
    private Cursor cursor;
    private int dbVersion = 1;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Singleton singleton = Singleton.getInstance();
        singleton.setContext(this);
        dbHelper = new DBHelper(getBaseContext(), dbName, null, dbVersion);
        db = dbHelper.getWritableDatabase();
        sql = "SELECT * FROM Notification ;";
        db = dbHelper.getWritableDatabase();
        cursor = db.rawQuery(sql,null);
        cursor.moveToFirst();
        ArrayList<String> evenTimeData = new ArrayList<String>();
        if(cursor.getCount()>0) {
            while(true) {
                Log.d("DBoutput", cursor.getString(1));
                evenTimeData.add(cursor.getString(1));
                if(!cursor.moveToNext())
                    break;
            }
        }
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            String intentData = String.valueOf(bundle.get("eventTime"));
            if(!intentData.equals("null")) {
                sql = String.format("INSERT INTO " + "Notification" + " VALUES(NULL,'%s');"
                        , intentData);
                db.execSQL(sql);
                Log.d("eventTime", intentData);
            }
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, evenTimeData) ;

        ListView listview = (ListView) findViewById(R.id.listview1) ;
        listview.setAdapter(adapter) ;
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = (String) parent.getItemAtPosition(position);
                apiClient.sendData(value);
            }
        });
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(!task.isSuccessful()){
                            Log.w("FCM Log", "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();
                        Log.d("FCM Log", "FCM 토큰: "+token);
                        apiClient.sendToken(token);
                    }
                });
    }
}
