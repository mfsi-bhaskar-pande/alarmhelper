package com.mfsi.alarm;

import android.app.AlarmManager;

import java.io.Serializable;

/**
 * Created by Bhaskar Pande on 12/8/2016.
 */
public class AlarmDetails implements Serializable{

    static final int UNIT_DAY = 1;
    static final int UNIT_MILLISECONDS = 2;
    static final int UNIT_HOURS = 3;

    private long mTriggerTime;
    private long mInterval;
    private boolean mIsPeriodic;
    private String mAlarmNotificationMsg;
    private String mAlarmNotificationTitle;

    private AlarmDetails(){

    }

    public static AlarmDetails prepareOneTimeAlarm(long triggerTime, String notifTitle, String notifMsg){

        AlarmDetails alarmDetails = new AlarmDetails();
        alarmDetails.setPeriodic(false);
        alarmDetails.setTriggerTime(triggerTime);
        alarmDetails.setAlarmNotificationTitle(notifTitle);
        alarmDetails.setAlarmNotificationMsg(notifMsg);
        return alarmDetails;
    }

    public static AlarmDetails preparePeriodicAlarm(long triggerTime, String notifTitle, String notifMsg,
                                                    long period, int periodUnit){

        AlarmDetails alarmDetails = new AlarmDetails();
        alarmDetails.setPeriodic(true);
        alarmDetails.setTriggerTime(triggerTime);
        alarmDetails.setAlarmNotificationTitle(notifTitle);
        alarmDetails.setAlarmNotificationMsg(notifMsg);

        long interval;

        switch (periodUnit){
            case UNIT_DAY:
                interval = period* AlarmManager.INTERVAL_DAY;
                break;
            case UNIT_HOURS:
                interval = period* AlarmManager.INTERVAL_HOUR;
                break;
            case UNIT_MILLISECONDS:
                interval = period;
                break;
            default:
                interval=0;
        }

        alarmDetails.setIntervalInMilliseconds(interval);
        return alarmDetails;

    }





    public String getAlarmNotificationMsg() {
        return mAlarmNotificationMsg;
    }

    public String getAlarmNotificationTitle() {
        return mAlarmNotificationTitle;
    }

    public boolean isPeriodic(){
        return mIsPeriodic;
    }

    public void setPeriodic(boolean periodic){
        mIsPeriodic = periodic;
    }



    public void setAlarmNotificationMsg(String alarmNotificationMsg) {
        this.mAlarmNotificationMsg = alarmNotificationMsg;
    }

    public void setAlarmNotificationTitle(String alarmNotificationTitle) {
        this.mAlarmNotificationTitle = alarmNotificationTitle;
    }

    public long getTriggerTime() {
        return mTriggerTime;
    }

    public void setTriggerTime(long triggerTime) {
        this.mTriggerTime = triggerTime;
    }

    public long getIntervalInMilliseconds() {
        return mInterval;
    }


    public void setIntervalInMilliseconds(long interval) {
        this.mInterval = interval;
    }

}
