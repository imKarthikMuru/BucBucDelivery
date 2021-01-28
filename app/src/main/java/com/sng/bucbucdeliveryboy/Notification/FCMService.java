package com.sng.bucbucdeliveryboy.Notification;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String body=remoteMessage.getNotification().getBody();
        String title=remoteMessage.getNotification().getTitle();
//        String image=remoteMessage.getNotification().getImageUrl().toString();
        String clickAction=remoteMessage.getNotification().getClickAction();

       NotificationHelper helper=new NotificationHelper(this);
       if (!clickAction.equals(null)||!clickAction.isEmpty()){
           helper.triggerNotification(title,body,clickAction);
       }else {
           helper.triggerNotification(title,body);
       }


    }
}
