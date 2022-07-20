package com.wedev.mygel.functions;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wedev.mygel.AllarmiActivity;
import com.wedev.mygel.MessaggiActivity;
import com.wedev.mygel.R;

import java.io.ByteArrayOutputStream;

public class FirebaseService extends FirebaseMessagingService {
    String TAG = "FBM";
    public FirebaseService() {
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        String titolo="";
        String testo="";
        try{
            titolo = remoteMessage.getNotification().getTitle();
        }catch (NullPointerException e){
            titolo="Messaggio senza titolo";
        }
        try{
            testo = remoteMessage.getNotification().getBody();
        }catch (NullPointerException e){
            testo="Messaggio senza contenuto";
        }
        final String CHANNEL_ID = "gel";

        Intent intent  = new Intent(getApplicationContext(), MessaggiActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("titolo",titolo);
        intent.putExtra("testo",testo);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("gel", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("titolo", titolo);
        editor.putString("testo", testo);
        editor.apply();
        String action = remoteMessage.getData().get("click_action");
        sendNotification2(testo);

       /* PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),1,intent,PendingIntent.FLAG_IMMUTABLE);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Allarme Acquaclick",
                    NotificationManager.IMPORTANCE_HIGH
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder  messaggio = new Notification.Builder(this,CHANNEL_ID)
                    .setContentTitle(titolo)
                    .setContentText(testo)
                    .setSmallIcon(R.drawable.ic_alert)
                    .setContentIntent(pendingIntent);
            NotificationManagerCompat.from(this).notify(1,messaggio.build());
        }*/

        super.onMessageReceived(remoteMessage);


    }

    private void sendNotification(String messageBody, String title, Bitmap image, String action) {
        Intent intent = new Intent(this, MessaggiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("title", title);
        intent.putExtra("msg", messageBody);
        intent.putExtra("click_action", action);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new
                NotificationCompat.Builder(this, "Default")
                .setSmallIcon(R.drawable.iconappgel)
                .setAutoCancel(false)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_HIGH)
                .setChannelId("Allarme Acquaclick")
                .setVibrate(new long[]{1000, 1000})
                .setContentIntent(pendingIntent);

        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(messageBody);
        notificationBuilder.setAutoCancel(false);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

    }
    private void sendNotification2(String msg) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        long notificatioId = System.currentTimeMillis();

        Intent intent = new Intent(getApplicationContext(), AllarmiActivity.class); // Here pass your activity where you want to redirect.

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) (Math.random() * 100), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.iconappgel)
                .setContentTitle(this.getResources().getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent);
        mNotificationManager.notify((int) notificatioId, notificationBuilder.build());
    }
    private void handleNow() {

    }

    private void scheduleJob() {

    }
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {

    }
}
