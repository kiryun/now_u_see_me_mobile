package com.dev.kih.nusm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyFireBaseMessagingService extends FirebaseMessagingService {
    private String dbName = "ListDB.db";
    private String sql;
    private int dbVersion = 1;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    @Override
    public void onNewToken(String token){
        Log.d("FCM Log", "Refreshed token: "+token);
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){

        if(remoteMessage.getNotification() != null){
            Log.d("FCM Log", "알림 메시지: "+remoteMessage.getNotification().getBody());
            String messageBody = remoteMessage.getNotification().getBody();

            /////////////////////////////// /////////////////////////////////////////////////////////////데이터 읽고 DB에 저장하기
            Map<String, String> eventTime = remoteMessage.getData();
            Log.d("Noti Log", "알림 데이터: "+eventTime.get("eventTime"));
            dbHelper = new DBHelper(this, dbName, null, dbVersion);
            db = dbHelper.getWritableDatabase();
            sql = String.format("INSERT INTO " + "Notification" + " VALUES(NULL,'%s');"
                    , eventTime.get("eventTime"));
            db.execSQL(sql);
            //////////////////////////////////////////////////////////////////////////////////////////////Notification 생성
            String messageTitle = remoteMessage.getNotification().getTitle();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("eventTime", eventTime.get("eventTime"));//
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            String channelId = "Channel ID";
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                String channelName = "Channel Name";
                NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0, notificationBuilder.build());
            //////////////////////////////////////////////////////////////////////////////////////////////ListView 갱신
            Singleton singleton = Singleton.getInstance();
            Message msg = singleton.getHandler().obtainMessage();
            singleton.getHandler().sendMessage(msg);

        }
    }
}
