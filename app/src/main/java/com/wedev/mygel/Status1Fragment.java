package com.wedev.mygel;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
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
import com.wedev.mygel.models.ModelProdotti;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Status1Fragment extends Fragment {


    // UI

    View view;

    //DB
    TMain mainData;
    DB db;

    // Rest
    RestFunctions rf;

    Button comandi;
    Button grafici;
    TextView titolo;

    ImageView s0,s1,s2,s3,s4,s5,s6,s7,s8;

    String nomeDevice="";
    String idProdotto= "";
    public Status1Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rf = new RestFunctions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inizializza DB
        setDB();
        // Legge DB
        mainData = getMainData();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status1, container, false);
    }
    // ------------ DB -------------
    public void setDB() {
        db = DB.getInstance(getContext());
    }
    public TMain getMainData() {
        return (db.tMainDao().getNumber()==0) ? null : db.tMainDao().getAll().get(0) ;
    }
    // -----------------------------
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view=view;
        setUI();

        getStatus();
    }
    private void setUI() {
        titolo = view.findViewById(R.id.titolo);
        SharedPreferences prefs = getContext().getSharedPreferences("gel", 0);
        nomeDevice= prefs.getString("nomedevice" , "GELDEVICE");
        idProdotto= prefs.getString("idProdotto" , "idProdotto");
        titolo.setText(nomeDevice);

        comandi = view.findViewById(R.id.comandi);
        comandi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.comandiFragment);
            }
        });
        grafici = view.findViewById(R.id.grafici);
        grafici.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),GraficiActivity.class);
                startActivity(intent);
            }
        });
        s0 = view.findViewById(R.id.s0);
        s1 = view.findViewById(R.id.s1);
        s2 = view.findViewById(R.id.s2);
        s3 = view.findViewById(R.id.s3);
        s4 = view.findViewById(R.id.s4);
        s5 = view.findViewById(R.id.s5);
        s6 = view.findViewById(R.id.s6);
        s7 = view.findViewById(R.id.s7);
        s8 = view.findViewById(R.id.s8);

    }

    // ------------ REST DATA -------------
    public void getStatus(){

        // =============================
        // Imposta parametri di input
        ArrayList<RestParams> params=setParams();
        // Imposta richiesta
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.POST,
                getString(R.string.baseapi)+getString(R.string.getstatus),
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
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
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

                try {
                    String flags= s.getString("message");
                    String[] flagsValue = flags.split(",");
                    for (int i = 0; i < flagsValue.length; i++) {
                        if (i==0) {
                            if (flagsValue[0] .equals("1") ){
                                s0.setImageResource(R.drawable.ic_alert);
                            }
                        }
                        if (i==1) {
                            if (flagsValue[1] .equals("1") ){
                                s1.setImageResource(R.drawable.ic_alert);
                            }
                        }
                        if (i==2) {
                            if (flagsValue[2] .equals("1") ){
                                s2.setImageResource(R.drawable.ic_alert);
                            }
                        }
                        if (i==3) {
                            if (flagsValue[3] .equals("1") ){
                                s3.setImageResource(R.drawable.ic_alert);
                            }
                        }
                        if (i==4) {
                            if (flagsValue[4] .equals("1") ){
                                s4.setImageResource(R.drawable.ic_alert);
                            }
                        }
                        if (i==5) {
                            if (flagsValue[5] .equals("1") ){
                                s5.setImageResource(R.drawable.ic_alert);
                            }
                        }
                        if (i==6) {
                            if (flagsValue[6] .equals("1") ){
                                s6.setImageResource(R.drawable.ic_alert);
                            }
                        }
                        if (i==7) {
                            if (flagsValue[7] .equals("1") ){
                                s7.setImageResource(R.drawable.ic_alert);
                            }
                        }
                        if (i==8) {
                            if (flagsValue[8] .equals("1") ){
                                s8.setImageResource(R.drawable.ic_alert);
                            }
                        }
                    }
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
        Toast.makeText(getContext(), messaggio, Toast.LENGTH_SHORT).show();
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
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
        p.setName("SerialNumber");
        p.setValue(nomeDevice);
        params.add(p);

        p = new RestParams();
        p.setName("Type");
        p.setValue("1");
        params.add(p);


        p = new RestParams();
        p.setName("Source");
        p.setValue("android");
        params.add(p);

        return params;
    }
}