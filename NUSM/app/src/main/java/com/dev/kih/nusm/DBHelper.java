package com.dev.kih.nusm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    //생성자 - database 파일을 생성합니다.
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    //DB 처음 만들때 한번만 호출 -테이블을 생성합니다.

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Notification (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "   event_time TEXT);");
    }
    public void onSettingCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS Notification");
    }
    //버전이 업데이트되면 DB를 다시 만듭니다.

    public void onReCreate(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS Notification");
        onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Notification");
        onCreate(db);
    }
}