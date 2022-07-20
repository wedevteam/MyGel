package com.wedev.mygel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.wedev.mygel.database.DB;
import com.wedev.mygel.database.tables.TFirstTime;
import com.wedev.mygel.database.tables.TMain;
import com.wedev.mygel.functions.ManageBaseData;
import com.wedev.mygel.functions.RestFunctions;

public class StartActivity extends AppCompatActivity {

    // Rest
    RestFunctions rf = new RestFunctions();

    // BASE
    ManageBaseData _baseData ;

    //DB
    TMain mainData;
    DB db;

    // VAR
    Intent intent;
    boolean welcomeActivitiy = true; // se non si desidera mettere false

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        // Dati base
        _baseData = new ManageBaseData(this);
        if (_baseData.firstTimeData==null){
            if (!welcomeActivitiy){
                goSignIn();
            }else {
                goWelcome();
            }

        }else
            if (_baseData.getMainData()==null)
                goSignIn();
            else
                goMain();
    }

    // Legge dati da Server
    private void goMain() {
        Intent intent = new Intent(StartActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }


    // Set firsttimedata e va a welcome
    private void goWelcome(){
        // set firstime record
        _baseData.firstTimeData=new TFirstTime();
        _baseData.firstTimeData.setIsFirstTime(1);
        _baseData.db.tFirstTimeDao().insert(_baseData.firstTimeData);

        intent = new Intent(StartActivity.this,WelcomeActivity.class);
        goApp();
    }

    // va a punto di atterraggio su app
    private void goApp(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
    private void goSignIn(){
        _baseData.firstTimeData=new TFirstTime();
        _baseData.firstTimeData.setIsFirstTime(1);
        _baseData.db.tFirstTimeDao().insert(_baseData.firstTimeData);

        intent = new Intent(StartActivity.this,SignInActivity.class);
        goApp();
    }
}