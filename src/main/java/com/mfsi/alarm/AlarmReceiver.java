package com.mfsi.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.net.Uri;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Bhaskar Pande on 12/8/2016.
 */
public class AlarmReceiver extends BroadcastReceiver{

    private static Ringtone mRingtone;

    private void handleAlarmAction(Intent intent, Context context){

        boolean reCreateOneTimeExact = intent.getBooleanExtra(AlarmHelper.RECREATE_ONETIME_EXACT, false);
        AlarmDetails alarmInput = (AlarmDetails) intent.getSerializableExtra(AlarmHelper.ALARM_INPUT);

        if(mRingtone != null && mRingtone.isPlaying()){
            mRingtone.stop();
        }
        mRingtone = AlarmHelper.soundAlarm(context);

        Uri alarmUri = intent.getData();

        AlarmHelper.notifyUser(context,alarmUri,alarmInput);

        if(reCreateOneTimeExact){
            /*alarm input is updated by the function,as per the new alarm*/
            boolean isSet = AlarmHelper.setOneTimeExactAlarm(intent.getData(),alarmInput,context);
            if(isSet){
                /*add the updated the alarm input.It shall replace the existing one*/
                AlarmStore.addTheAlarm(intent.getData(),alarmInput, context);
            }
        }else{
            if(!alarmInput.isPeriodic()){
                AlarmHelper.cancelAlarm(alarmUri, context);
                cleanDbReferences(alarmUri, context);
            }
        }

    }

    private void handleNotificationAction(Intent intent, Context context){

        if(mRingtone != null) {
            AlarmHelper.stopAlarm(mRingtone);
        }else{
            Toast.makeText(context,"No Ringtone Playing", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {
            switch (intent.getAction()){
                case AlarmHelper.ALARM_ACTION:
                    handleAlarmAction(intent,context);
                    break;
                case AlarmNotificationView.ALARM_NOTIFICATION:
                    handleNotificationAction(intent,context);
                    break;
                case Intent.ACTION_BOOT_COMPLETED:
                    reconfigureAllAlarms(context);
                    break;
            }
        }
    }

    private void reconfigureAllAlarms(Context context) {
        HashMap<Uri,AlarmDetails> details = AlarmStore.retrieveAllAlarms(context);
        Set<Map.Entry<Uri,AlarmDetails>> entries = details.entrySet();
        Iterator<Map.Entry<Uri,AlarmDetails>> alarms = entries.iterator();

        while(alarms.hasNext()){
            Map.Entry<Uri,AlarmDetails> entry = alarms.next();
            AlarmDetails alarm = entry.getValue();
            Uri alarmUri = entry.getKey();
            if(alarm.isPeriodic()){
                AlarmHelper.setRepeatingExactAlarm(alarmUri,alarm, context);
            }else{
                AlarmHelper.setOneTimeExactAlarm(alarmUri,alarm, context);
            }
        }
    }

    public void cleanDbReferences(Uri uri, Context context){
        try {
            context.getContentResolver().delete(uri, null, null);
        }catch(Exception exception){
            exception.printStackTrace();
        }
    }

}
