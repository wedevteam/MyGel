package com.wedev.mygel;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.wedev.mygel.functions.ManageBaseData;

import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {

    public static final String MESSAGE_STATUS = "message_status";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    MaterialToolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    ImageView hmmenu;

    // BASE
    ManageBaseData _baseData ;

    Button aggiungi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setMessaggi();
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet())
            {
                String value = getIntent().getExtras().getString(key);
                if (key.equals("click_action")) {
                   startActivity(new Intent(MainActivity.this, MessaggiActivity.class));
                }
            }
        }
        setDrawerMenu();
    }


    private void setDrawerMenu() {
        hmmenu = findViewById(R.id.hmclose);
        hmmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        drawerLayout = findViewById(R.id.drawerlayout);
        navigationView = findViewById(R.id.navview);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,(R.string.menuopen),(R.string.menuclose));
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.homemenu:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.aggiungimenu:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        NavController navController = Navigation.findNavController(MainActivity.this,R.id.nav_host_fragment);
                        navController.navigate(R.id.sceltaProdottoFragment);
                        break;
                    case R.id.sfogliamenu:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        startActivity(new Intent(MainActivity.this,CatActivity.class));
                        break;
                    case R.id.homeguarda:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        NavController navController2 = Navigation.findNavController(MainActivity.this,R.id.nav_host_fragment);
                        navController2.navigate(R.id.videoTFragment);
                        break;
                    case R.id.vaimenu:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.datimenu:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }
                return true;
            }
        });
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