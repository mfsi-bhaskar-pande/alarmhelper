package com.mfsi.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.mfsi.alarmhelper.R;

/**
 * Created by Bhaskar Pande on 12/8/2016.
 */
public class AlarmNotificationView {

    static final String ALARM_NOTIFICATION = "com.mfsi.buddy.alarm.notification";

    public static void show(Context context, String title, String contentText, int notificationId,
                            Uri data) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.device_access_time)
                .setContentTitle(title)
                .setContentText(contentText);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        Intent resultIntent = new Intent(context, AlarmReceiver.class);
        resultIntent.setAction(ALARM_NOTIFICATION);
        resultIntent.setData(data);

        PendingIntent resultPendingIntent = PendingIntent.getBroadcast
                (context,notificationId,resultIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        Notification buildNotification = builder.build();
        buildNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        buildNotification.flags |= Notification.FLAG_NO_CLEAR;

        mNotificationManager.notify(notificationId, buildNotification);

    }

}
