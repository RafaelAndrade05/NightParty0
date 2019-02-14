package com.example.rene.nightparty0.Ayuda;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.rene.nightparty0.ChatsUsuarioActivity;
import com.example.rene.nightparty0.Principal;
import com.example.rene.nightparty0.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MiFirebaseMessasingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String from = remoteMessage.getFrom();
        Log.i("MESSAGEFIREBASE","Mensaje de :" + from);

        if(remoteMessage.getNotification() != null){
            Log.i("MESSAGEFIREBASE","Notificacion 1 =>"+remoteMessage.getNotification().getBody());
            Log.i("MESSAGEFIREBASE","Notificacion 2 =>"+remoteMessage.getData().toString());
            //mostrarNotificacion(remoteMessage.getFrom(),remoteMessage.getNotification().getBody());
        }

    }

    public void mostrarNotificacion(String title,String body){

        Intent intent = new Intent(this, Principal.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.iconosi)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,builder.build());
    }

}
