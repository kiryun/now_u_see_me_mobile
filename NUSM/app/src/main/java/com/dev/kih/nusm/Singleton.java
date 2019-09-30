package com.dev.kih.nusm;


import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;

public class Singleton {
    private Singleton() { }

    private static class SingletonHolder {
        public static final Singleton INSTANCE = new Singleton();
    }

    public static Singleton getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private ArrayList<GetDataFrame> DataFrames = new ArrayList<GetDataFrame>();
    private Context context;
    private String eventTime;
    public void setEventTime(String eventTime){
        this.eventTime = eventTime;
    }
    public String getEventTime(){
        return this.eventTime;
    }
    public void setContext(Context context){
        this.context = context;
    }
    public Context getContext(){
        return this.context;
    }
    public void setDataFrames(ArrayList<String> is, ArrayList<String> names){
        Log.d("Get data", "Save Data");
        DataFrames.add(new GetDataFrame(is, names));
    }
    public GetDataFrame getDataFrames(int idx){
        Log.d("Get data",  DataFrames.get(idx).getNames().get(0));
        return DataFrames.get(idx);
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
        public int getDataFrameSize(){
            return is.size();
        }
        public ArrayList<String> getIs(){
            return is;
        }
        public ArrayList<String> getNames(){
            return names;
        }
    }
}

