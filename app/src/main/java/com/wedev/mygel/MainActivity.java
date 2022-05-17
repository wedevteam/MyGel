package com.wedev.mygel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wedev.mygel.functions.ManageBaseData;

public class MainActivity extends BaseActivity {

    // BASE
    ManageBaseData _baseData ;

    Button aggiungi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}