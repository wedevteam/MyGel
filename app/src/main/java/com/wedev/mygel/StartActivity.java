package com.wedev.mygel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

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
        setContentView(R.layout.splashscreen);
       // animate();
        // Dati base
        _baseData = new ManageBaseData(this);
        if (_baseData.firstTimeData==null){
            if (!welcomeActivitiy){
                goSignIn();
            }else {
                goSignIn();
               //  goWelcome(); // da attivare quando c'è grafica
            }

        }else
            if (_baseData.getMainData()==null)
                goSignIn();
            else
                goMain();
    }

    // Legge dati da Server
    private void goMain() {


        intent = new Intent(StartActivity.this,MainActivity.class);
        goApp();

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
        }, 3000);
    }
    private void goSignIn(){
        _baseData.firstTimeData=new TFirstTime();
        _baseData.firstTimeData.setIsFirstTime(1);
        _baseData.db.tFirstTimeDao().insert(_baseData.firstTimeData);

        intent = new Intent(StartActivity.this,SignInActivity.class);
        goApp();
    }

    private void animate(){
        ImageView imageView = findViewById(R.id.image);
        Animation zoomout = AnimationUtils.loadAnimation(this, R.anim.zoomout);
        imageView.setAnimation(zoomout);
        Animation fadeIn = new AlphaAnimation(1, 0);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setStartOffset(500);
        fadeIn.setDuration(1000);
        imageView.setAnimation(fadeIn);
    }
}