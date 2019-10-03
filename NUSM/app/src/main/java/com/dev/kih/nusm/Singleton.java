package com.dev.kih.nusm;


import android.content.Context;
import android.os.Handler;
import android.util.Log;
import java.util.ArrayList;

public class Singleton {
    private Singleton() { }

    private static class SingletonHolder {
        public static final Singleton INSTANCE = new Singleton();
    }

    public static Singleton getInstance() {
        return SingletonHolder.INSTANCE;
    }//Singleton 객체 생성
    /*Singleton 사용 이유
    Singleton 디자인 패턴을 사용함으로써 하나의 객체만 생성됨
    다른 Activity와 Service 루틴에서 이 객체를 생성하면 생성되었던 객체를 그대로 사용 가능
    Singleton 안에 있는 변수값을 어떠한 Activity와 Service 루틴에서 사용할 수 있음.
     */
    private ArrayList<GetDataFrame> DataFrames = new ArrayList<GetDataFrame>();//서버에서 보낸 데이터를 저장할 때 사용
    private Context context; //서비스루틴에서의 DB 활성화를 위한 Context
    private String eventTime; //서버에서 받은 eventTime을 서비스루틴과 다른 Activity에서 원활하게 사용하기 위함
    private Handler handler;//서비스루틴에서 ListView를 Refresh하기 위함

    ///////////////////////////////////////////////////////////////////////객체 저장하기
    public void setHandler(Handler handler){
        this.handler = handler;
    }
    public void setEventTime(String eventTime){
        this.eventTime = eventTime;
    }
    public void setContext(Context context){
        this.context = context;
    }
    public void setDataFrames(ArrayList<String> is, ArrayList<String> names){
        Log.d("Get data", "Save Data");
        DataFrames.add(new GetDataFrame(is, names));
    }

    ///////////////////////////////////////////////////////////////////////객체 불러오기
    public Handler getHandler(){
        return this.handler;
    }
    public String getEventTime(){
        return this.eventTime;
    }
    public Context getContext(){
        return this.context;
    }
    public GetDataFrame getDataFrames(int idx){
        if(DataFrames.size()>0) {
            Log.d("Get data", DataFrames.get(idx).getNames().get(0));
            return DataFrames.get(idx);
        }
        return null;
    }
    public int size_DataFrames(){
        return DataFrames.size();
    }
    public void removeDataFrames(){
        DataFrames.clear();
    }
    public class GetDataFrame{
        private ArrayList<String> is;
        private ArrayList<String> names;
        public GetDataFrame(ArrayList<String> is, ArrayList<String> names){
            this.is = is;
            this.names = names;
        }
        public ArrayList<String> getIs(){
            return is;
        }
        public ArrayList<String> getNames(){
            return names;
        }
    }
}

