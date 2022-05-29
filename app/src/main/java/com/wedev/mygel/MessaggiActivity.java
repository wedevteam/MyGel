package com.wedev.mygel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wedev.mygel.database.DB;
import com.wedev.mygel.database.tables.TMain;
import com.wedev.mygel.functions.RestFunctions;
import com.wedev.mygel.functions.RestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessaggiActivity extends AppCompatActivity {

    Button attivablocco;
    Button disattivablocco;
    Button riavvio;
    Button aggiorna;
    Button home;

    String comando="";

    //DB
    TMain mainData;
    DB db;

    // Rest
    RestFunctions rf;

    String testo="";
    String titolo="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_messaggi);
        Intent intent = getIntent();
        if (intent!=null){
            try{
                testo = intent.getStringExtra("testo");
                titolo   = intent.getStringExtra("titolo");


            }catch (Exception e){
                e.printStackTrace();
            }
        }
        SharedPreferences prefs = getSharedPreferences("gel", 0);
        titolo= prefs.getString("titolo" , "titolo");
        testo= prefs.getString("testo" , "testo");
        String builder = titolo +
                "\n" +
                testo;
        TextView allarme =  findViewById(R.id.allarme);
        allarme.setText(builder);
        setUI();
        rf = new RestFunctions();
        // Inizializza DB
        setDB();
        // Legge DB
        mainData = getMainData();
    }
    // ------------ DB -------------
    public void setDB() {
        db = DB.getInstance(this);
    }
    public TMain getMainData() {
        return (db.tMainDao().getNumber()==0) ? null : db.tMainDao().getAll().get(0) ;
    }
    // -----------------------------
    private void setUI() {
        attivablocco = findViewById(R.id.attivablocco);
        attivablocco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComando("LOCK:ON");
            }
        });
        disattivablocco = findViewById(R.id.disattivablocco);
        disattivablocco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComando("LOCK:OFF");
            }
        });
        riavvio = findViewById(R.id.riavvio);
        riavvio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComando("REBOOT");
            }
        });
        aggiorna = findViewById(R.id.aggiorna);
        aggiorna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComando("UPDATEFW");
            }
        });
        home = findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MessaggiActivity.this, MainActivity.class));
            }
        });
    }

    private void sendComando(String s) {
        comando="";
        if (s.equals("LOCK:ON") ||s.equals("LOCK:OFF") ||s.equals("REBOOT")){
            comando= "#COMM#SERVER#1865#";
            comando += titolo;
            comando += "#";
            comando += s;
            comando += "#";
        }
        if (s.equals("UPDATEFW") ){
            comando= "#COMM#SERVER#1865#";
            comando += titolo;
            comando += "#";
            comando += s;
            comando += ":\"https://server.com/fw-latest.bin\";MD5HASH:eef279d38586b29c37e1fc37971bb972#";
        }
        inviaComando();
    }
    // ------------ REST DATA -------------
    public void inviaComando(){

        // =============================
        // Imposta parametri di input
        ArrayList<RestParams> params=setParams();
        // Imposta richiesta
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.POST,
                getString(R.string.baseapi)+getString(R.string.sendcmd),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        if (s == null) {
                            String t="v";
                            // TODO
                        } else {
                            String v = "1";
                            fillData(s);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        String t="v";
                        // TODO
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
            @Override
            public byte[] getBody() {
                return rf.setJsonBody(params).toString() == null ? null : rf.setJsonBody(params).toString().getBytes(StandardCharsets.UTF_8);
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        int socketTimeout = 150000;//50 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        // Richiesta
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }


    private void fillData(JSONObject s) {
        // TODO
        try {
            boolean success = s.getBoolean("success");
            if (!success){
                showError(s.getString("error_code"),s.getString("message"));
            }else{
                int numero = s.getInt("user_devices_num");
                try {
                    Toast.makeText(this, "Comando inviato al dispositivo", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            // TODO
        }
        // setStart(s);
        //5
    }
    private void showError(String error_code, String message) {
        hideSoftKeyBoard();
        showErrBase(" "+error_code,message);
    }

    public void showErrBase(String titolo, String messaggio){
        MaterialAlertDialogBuilder dialogo=  new MaterialAlertDialogBuilder(this,R.style.AlertDialogTheme)
                .setTitle(titolo)
                .setMessage(messaggio)
                .setNeutralButton("Ok",null);
        dialogo.show();
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    private ArrayList<RestParams> setParams() {
        ArrayList<RestParams> params = new ArrayList<>();

        RestParams p = new RestParams();
        p.setName("app_token");
        p.setValue(getString(R.string.tokenapp));
        params.add(p);

        p = new RestParams();
        p.setName("Token");
        p.setValue(mainData.getToken());
        params.add(p);


        p = new RestParams();
        p.setName("IdProdotto");
        p.setValue("1");
        params.add(p);


        p = new RestParams();
        p.setName("SerialNumber");
        p.setValue(titolo);
        params.add(p);


        p = new RestParams();
        p.setName("Cmd");
        p.setValue(comando);
        params.add(p);


        p = new RestParams();
        p.setName("Source");
        p.setValue("android");
        params.add(p);


        return params;
    }
}