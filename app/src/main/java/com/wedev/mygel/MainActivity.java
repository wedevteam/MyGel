package com.wedev.mygel;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.wedev.mygel.functions.ManageBaseData;

import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {

    public static final String MESSAGE_STATUS = "message_status";

    // BASE
    ManageBaseData _baseData ;

    Button aggiungi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setMessaggi();
    }
    private void setMessaggi() {
       String token = returnMeFCMtoken();
    }
    public String returnMeFCMtoken() {
        String msg ="";
        FirebaseMessaging.getInstance().subscribeToTopic("tutti")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = ("OK");
                        if (!task.isSuccessful()) {
                            msg = "NO";
                        }
                        Log.d(TAG, msg);
                       // Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        return msg;
    }
}