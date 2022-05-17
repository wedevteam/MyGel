package com.wedev.mygel;

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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wedev.mygel.functions.ManageBaseData;
import com.wedev.mygel.models.GetInfoWorker;

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
        setWM();
        String gomsg = getIntent().getStringExtra("type");
        if (gomsg!=null){
            if (gomsg.equals("gomsg")){
              /*  navController = Navigation.findNavController(this,R.id.fragmentnav);
                navController.navigate(R.id.listaMessaggiFragment,null);*/
            }
        }
    }
    public void setWM() {
        WorkManager mWorkManager = WorkManager.getInstance();
        final String TAG = "PeriodicWorkTag";
        final int PERIODIC_WORK_INTERVAL = 5;
        // OneTimeWorkRequest mRequest = new OneTimeWorkRequest.Builder(GetInfoWorker.class).build();
        PeriodicWorkRequest mRequest =
                new PeriodicWorkRequest.Builder(GetInfoWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(new Constraints.Builder()
                                .setRequiresCharging(false)
                                .build()
                        )
                        .build();
        mWorkManager.enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.KEEP,mRequest);
        mWorkManager.getWorkInfoByIdLiveData(mRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(@Nullable WorkInfo workInfo) {
                if (workInfo != null) {
                    WorkInfo.State state = workInfo.getState();
                    Log.d("dati: ","xx");
                }
            }
        });
    }
}