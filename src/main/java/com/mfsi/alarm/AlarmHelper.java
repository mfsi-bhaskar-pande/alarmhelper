package com.mfsi.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by Bhaskar Pande on 12/8/2016.
 */
public class AlarmHelper {

    static final String RECREATE_ONETIME_EXACT = "com.mfsi.alarm.CreateOneTimeExact";
    static final String ALARM_INPUT = "com.mfsi.alarm.OneTimeAlarmInput";
    static final String ALARM_ACTION = "com.mfsi.alarm.action";
    static final String ALARM_CATEGORY = "com.mfsi.alarm.category";
    static final int ILLEGAL_ALARM_ID = -1;


    private static String createFakeType() {
        return "com.mfsi.alarm.dataType";
    }


    private static Uri createFakeUri(long uid) {
        return Uri.parse("content://com.mfsi.alarm/" + uid);
    }

    public static boolean setOneTimeExactAlarm(long uid, AlarmDetails alarmDetails, Context context) {
        return setOneTimeExactAlarm(createFakeUri(uid), alarmDetails, context);
    }

    public static boolean setOneTimeInExactAlarm(long uid, AlarmDetails alarmDetails, Context context) {
        return setOneTimeInExactAlarm(createFakeUri(uid), alarmDetails, context);
    }

    public static boolean setRepeatingExactAlarm(long uid, AlarmDetails alarmDetails, Context context) {
        return setRepeatingExactAlarm(createFakeUri(uid), alarmDetails, context);
    }

    public static boolean setRepeatingInExactAlarm(long uid, AlarmDetails alarmDetails, Context context) {
        return setRepeatingInExactAlarm(createFakeUri(uid), alarmDetails, context);
    }

    public static boolean cancelAlarm(long uid, Context context) {
        return cancelAlarm(createFakeUri(uid), context);
    }

    public static boolean setOneTimeExactAlarm(Uri alarmUri, AlarmDetails alarmDetails, Context context) {
        boolean set = setOneTimeExactAlarm(alarmUri, alarmDetails, context, false);
        if (set) {
            AlarmStore.addTheAlarm(alarmUri, alarmDetails, context);
        }
        return set;
    }

    public static boolean setOneTimeInExactAlarm(Uri alarmUri, AlarmDetails alarmDetails, Context context) {
        boolean set = setInexactOnce(alarmUri, alarmDetails, context);
        if (set) {
            AlarmStore.addTheAlarm(alarmUri, alarmDetails, context);
        }
        return set;
    }

    public static boolean setRepeatingExactAlarm(Uri alarmUri, AlarmDetails alarmDetails, Context context) {
        boolean set = setExactRepeatingAlarm(alarmUri, alarmDetails, context);
        if (set) {
            AlarmStore.addTheAlarm(alarmUri, alarmDetails, context);
        }
        return set;
    }

    public static boolean setRepeatingInExactAlarm(Uri alarmUri, AlarmDetails alarmDetails, Context context) {
        boolean set = setInexactRepeating(alarmUri, alarmDetails, context);
        if (set) {
            AlarmStore.addTheAlarm(alarmUri, alarmDetails, context);
        }
        return set;
    }

    private static int getLastSegmentNumber(Uri uri) {

        int number = ILLEGAL_ALARM_ID;
        String uriString = Uri.decode(uri.toString());
        if (uriString != null) {
            int pathSeparatorIndex = uriString.lastIndexOf(File.separator);
            if (pathSeparatorIndex != -1) {
                String numberStr = uriString.substring(pathSeparatorIndex + 1, uriString.length());
                if (TextUtils.isDigitsOnly(numberStr)) {
                    number = Integer.parseInt(numberStr);
                }
            }
        }
        return number;
    }

    private static Intent initIntent(Context context, Uri alarmUri){

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ALARM_ACTION);
        intent.addCategory(ALARM_CATEGORY);
        intent.setDataAndType(alarmUri, createFakeType());

        return intent;

    }


    public static boolean cancelAlarm(Uri alarmUri, Context context) {

        int senderRequestCode = getLastSegmentNumber(alarmUri);
        boolean alarmCancelled = false;

        if (senderRequestCode != ILLEGAL_ALARM_ID) {


            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = initIntent(context,alarmUri);

            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, senderRequestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        /*Uses Intent.filterEquals to matchIntents : Determine if two intents are the same for the purposes of intent resolution (filtering).
        That is, if their action, data, type, class, and categories are the same. This does not
        compare any extra data included in the intents.*/
            alarmManager.cancel(alarmIntent);
            alarmCancelled = true;
            /*remove alarm from the alarm store*/
            AlarmStore.removeAlarm(alarmUri, context);
        }
        return alarmCancelled;
    }


    private static boolean setExactRepeatingAlarm(Uri alarmUri, AlarmDetails alarmDetails, Context context) {

        int senderRequestCode = getLastSegmentNumber(alarmUri);
        boolean alarmSet = false;

        if (senderRequestCode != ILLEGAL_ALARM_ID) {

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = initIntent(context,alarmUri);

            PendingIntent alarmIntent;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                intent.putExtra(ALARM_INPUT, alarmDetails);
                intent.putExtra(RECREATE_ONETIME_EXACT, true);
                alarmIntent = PendingIntent.getBroadcast(context, senderRequestCode, intent, PendingIntent.FLAG_ONE_SHOT);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmDetails.getTriggerTime(), alarmIntent);
                alarmSet = true;

            } else {
                alarmIntent = PendingIntent.getBroadcast(context, senderRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmDetails.getTriggerTime(), alarmDetails.getIntervalInMilliseconds()
                        , alarmIntent);
                alarmSet = true;

            }
        }
        return alarmSet;
    }

    static boolean setInexactOnce(Uri alarmUri, AlarmDetails alarmDetails, Context context) {

        int senderRequestCode = getLastSegmentNumber(alarmUri);
        boolean alarmSet = false;

        if (senderRequestCode != ILLEGAL_ALARM_ID) {

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = initIntent(context,alarmUri);

            intent.putExtra(ALARM_INPUT, alarmDetails);

            long triggerTime = alarmDetails.getTriggerTime();

            if (triggerTime > 0 && triggerTime > System.currentTimeMillis()) {
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, senderRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, alarmIntent);
                alarmSet = true;
            }

        }
        return alarmSet;

    }

    static boolean setInexactRepeating(Uri alarmUri, AlarmDetails alarmDetails, Context context) {

        int senderRequestCode = getLastSegmentNumber(alarmUri);
        boolean alarmSet = false;

        if (senderRequestCode != ILLEGAL_ALARM_ID) {

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = initIntent(context,alarmUri);
            intent.putExtra(ALARM_INPUT, alarmDetails);

            long triggerTime = alarmDetails.getTriggerTime();
            long period = alarmDetails.getIntervalInMilliseconds();

            if (triggerTime > 0 && triggerTime > System.currentTimeMillis() && period > 0) {
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, senderRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerTime, period, alarmIntent);
                alarmSet = true;
            }

        }
        return alarmSet;

    }


    static boolean setOneTimeExactAlarm(Uri alarmUri, AlarmDetails alarmDetails, Context context, boolean reCreateAfterTrigger) {

        int senderRequestCode = getLastSegmentNumber(alarmUri);
        boolean alarmSet = false;

        if (senderRequestCode != ILLEGAL_ALARM_ID) {

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = initIntent(context,alarmUri);
            intent.putExtra(ALARM_INPUT, alarmDetails);

            long triggerTime = alarmDetails.getTriggerTime();

            if (reCreateAfterTrigger) {
                if (triggerTime < System.currentTimeMillis()) {
                    triggerTime = triggerTime + alarmDetails.getIntervalInMilliseconds();
                    alarmDetails.setTriggerTime(triggerTime);
                }
                intent.putExtra(RECREATE_ONETIME_EXACT, true);
            }

            if (triggerTime > 0 && triggerTime > System.currentTimeMillis()) {
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, senderRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, alarmIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, alarmIntent);
                }
                alarmSet = true;
            }

        }
        return alarmSet;
    }


    public static void notifyUser(Context context, Uri alarmUri, AlarmDetails alarmInput) {
        if (alarmInput != null) {
            String notifTitle = alarmInput.getAlarmNotificationTitle();
            String notifContent = alarmInput.getAlarmNotificationMsg();
            int requestCode = getLastSegmentNumber(alarmUri);
            AlarmNotificationView.show(context, notifTitle, notifContent, requestCode, alarmUri);
        }
    }

    public static Ringtone soundAlarm(Context context) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
        ringtone.play();
        return ringtone;
    }


    public static void stopAlarm(Ringtone ringtone) {
        ringtone.stop();
    }


}
