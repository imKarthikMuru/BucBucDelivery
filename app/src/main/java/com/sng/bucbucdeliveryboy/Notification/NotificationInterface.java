package com.sng.bucbucdeliveryboy.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationInterface {
    @Headers({"Content-Type:application/json","Authorization:key=AAAANWUanyg:APA91bHKuc4iZNx9FWCCRTO2B1wD2b-_eTYDEyjiGu3r-a1NYfiGJS_lPkuKojpAD0-MCSfdrotiDstUOnj-6zZ593XajXavAWKB3PXjucDSA6_nLjT3jHvJo77kN7wKPSxyMiQld5HT"})
    @POST("send")
    Call<NotificationResponse> sent(@Body NotificationRequest req);

}

