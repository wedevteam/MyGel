package com.wedev.mygel.functions;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wedev.mygel.MessaggiActivity;
import com.wedev.mygel.R;

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

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),1,intent,PendingIntent.FLAG_IMMUTABLE);


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
        }

        super.onMessageReceived(remoteMessage);

      /*  // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (*//* Check if data needs to be processed by long running job *//* true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
*/
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
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
