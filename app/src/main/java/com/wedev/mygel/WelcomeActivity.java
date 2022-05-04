package com.wedev.mygel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.wedev.mygel.adapters.OnBoardingAdapter;
import com.wedev.mygel.models.OnBoardingItem;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 3;

    private OnBoardingAdapter onBoardingAdapter;
    private LinearLayout linearLayoutIndicatori;
    private ViewPager2 viewPager2;
    private MaterialButton materialButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // elimina area titolo
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_welcome);

        // nasconde aree android
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideSystemUI();

        // imposta indicatori, Pager e buttons
        setIndicators();
        setOnBoardingItems();
        setPager();
        setIndicatorsValues();
        setIndicatoreAttivo(0);
        setButtons();

        // imposta Pager Callback sul PageChange
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setIndicatoreAttivo(position);
            }
        });

    }
    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    // UI dei buttons
    private void setButtons() {
        materialButton = findViewById(R.id.buttonOnBoardingAction);
        materialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goSignIn();
            }
        });
    }

    // va a main
    private void goSignIn(){
        Intent intent;
        intent = new Intent(WelcomeActivity.this,SignInActivity.class);
        startActivity(intent);
        finish();
    }

    // UI layout indicatori
    private void setIndicators() {
        linearLayoutIndicatori = findViewById(R.id.indicatori);
    }

    // UI comportamento indicatori
    private void setIndicatorsValues() {
        ImageView[] indicatori = new ImageView[onBoardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(18,0,18,8);
        for (int i = 0; i<indicatori.length;i++){
            indicatori[i] = new ImageView(getApplicationContext());
            indicatori[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.indicatoriwlecomeinactive
            ));
            indicatori[i].setLayoutParams(layoutParams);
            linearLayoutIndicatori.addView(indicatori[i]);
        }
    }

    // UI Pager
    private void setPager() {
        viewPager2 = findViewById(R.id.vp2);
        viewPager2.setAdapter(onBoardingAdapter);
    }

    // inizializza Adapter del Pager
    private void setOnBoardingAdapter(List<OnBoardingItem> onBoardingItemList){
        onBoardingAdapter = new OnBoardingAdapter(onBoardingItemList);
    }

    // imposta UI slides
    private void setOnBoardingItems(){
        List<OnBoardingItem> onBoardingItemList = new ArrayList<>();
        OnBoardingItem boardingItem1 = new OnBoardingItem();
        boardingItem1.setTesto(getString(R.string.stringawelcome1));

        OnBoardingItem boardingItem2 = new OnBoardingItem();
        boardingItem2.setTesto(getString(R.string.stringawelcome2));

        OnBoardingItem boardingItem3 = new OnBoardingItem();
        boardingItem3.setTesto(getString(R.string.stringawelcome3));

        onBoardingItemList.add(boardingItem1);
        onBoardingItemList.add(boardingItem2);
        onBoardingItemList.add(boardingItem3);

        setOnBoardingAdapter(onBoardingItemList);
    }

    // gestisce UI indicatori nella navigazione
    private void setIndicatoreAttivo(int indice){
        int numero = linearLayoutIndicatori.getChildCount();
        for (int i = 0; i <numero ; i++) {
            ImageView immagine = (ImageView) linearLayoutIndicatori.getChildAt(i);
            if (i==indice){
                immagine.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(),R.drawable.indicatoriwlecomeactive));
            }else{
                immagine.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(),R.drawable.indicatoriwlecomeinactive));
            }
        }
    }

    public void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}