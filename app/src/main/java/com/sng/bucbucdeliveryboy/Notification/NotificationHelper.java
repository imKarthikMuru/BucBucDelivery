package com.sng.bucbucdeliveryboy.Notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.sng.bucbucdeliveryboy.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NotificationHelper {

    private Context context;
    private static  final int id=100;

    public NotificationHelper(Context context) {
        this.context = context;
    }
    public    void triggerNotification(String title, String body){

        NotificationCompat.Builder builder =new NotificationCompat.Builder(context, Config.id)
                .setContentTitle(title).setContentText(body)
                .setSmallIcon(R.drawable.logotext)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(image)))
                ;

        NotificationManagerCompat managerCompat= NotificationManagerCompat.from(context);
        managerCompat.notify(id,builder.build());
    }
    public    void triggerNotification(String title, String body, String myclick){

        Intent intent=new Intent(myclick);
        PendingIntent pendingIntent= PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =new NotificationCompat.Builder(context,Config.id)
                .setContentTitle(title).setContentText(body)
                .setSmallIcon(R.drawable.logotext)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(getBitmapFromURL(image)))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat= NotificationManagerCompat.from(context);
        managerCompat.notify(id,builder.build());
    }
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
