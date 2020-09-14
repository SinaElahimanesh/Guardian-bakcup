package com.guardian.guardian_v1.SleepManager;

import java.util.ArrayList;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.PendingIntent;
import android.app.Service;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.gson.Gson;
import android.content.Intent;


import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


import com.google.gson.reflect.TypeToken;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class SleepDetectorService extends Service {
    protected static final String TAG = "Activity";

    private static HashMap<Date,DetectedActivity> sleepData= new HashMap<>();
    private static ArrayList<Date> allDates = new ArrayList<>();

    private  ActivityRecognitionClient mActivityRecognitionClient;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mActivityRecognitionClient== null){
            mActivityRecognitionClient = new ActivityRecognitionClient(this);
            mActivityRecognitionClient.requestActivityUpdates(1000 * 60 * 5,PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT));
        }

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
            detectedActivitiesToJson(detectedActivities);
        }
        return START_STICKY;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("bind");
        return null;
    }


    static String detectedActivitiesToJson(ArrayList<DetectedActivity> detectedActivitiesList) {
        Type type = new TypeToken<ArrayList<DetectedActivity>>() {}.getType();
        System.out.println(detectedActivitiesList.toString());
        if(detectedActivitiesList != null){
            deleteOldData();
            Date date = getDate(Calendar.getInstance().getTime());
            sleepData.put(date,detectedActivitiesList.get(0));
            allDates.add(date);
            Log.d("my sleep data",sleepData.toString());
        }
        return new Gson().toJson(detectedActivitiesList, type);
    }

    static void clear(){
        sleepData.clear();
        allDates.clear();
    }

    private static void deleteOldData(){
        if(allDates.isEmpty()) return;
        while((allDates.get(0).getDay()!=allDates.get(allDates.size()-1).getDay()) && (allDates.get(allDates.size()-1).getHours()+24-allDates.get(0).getHours()>24)){
            sleepData.remove(allDates.get(0));
            allDates.remove(0);
        }
    }

    public static boolean isSleepValid(Date sleepTime, Date awakeTime){
        Date date = getDate(sleepTime);
        int error = 0;
        while(date.after(awakeTime)  == false){
            if((sleepData.containsKey(date))&&(sleepData.get(date).getType()!=DetectedActivity.STILL)){
                error++;
            }
        }
        return error <= 3;
    }

    public static ArrayList<Date> getSleepTime(){
        return null;
    }

    private static Date getDate(Date date){
        return new Date(date.getYear(),date.getMonth(),date.getDay(),date.getHours(),(date.getMinutes()/5)*5,0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("service destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        System.out.println("task removed");
    }

}
