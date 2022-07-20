package com.wedev.mygel;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.SharedPreferences;
import android.graphics.Color;
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

public class ComandiFragment extends Fragment {

    Button attivablocco;
    Button disattivablocco;
    Button riavvio;
    Button aggiorna;
    Button home;
    Button valvole;

    // UI

    View view;

    //DB
    TMain mainData;
    DB db;

    // Rest
    RestFunctions rf;

    String nomeDevice = "";
    String idProdotto = "";

    String comando="";

    public ComandiFragment() {
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
        return inflater.inflate(R.layout.fragment_comandi, container, false);
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
        SharedPreferences prefs = getContext().getSharedPreferences("gel", 0);
        nomeDevice= prefs.getString("nomedevice" , "GELDEVICE");
        idProdotto= prefs.getString("idProdotto" , "idProdotto");

        setUI();
    }
    private void setUI() {
        attivablocco = view.findViewById(R.id.attivablocco);
        attivablocco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               sendComando("LOCK:ON");
            }
        });
        disattivablocco = view.findViewById(R.id.disattivablocco);
        disattivablocco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComando("LOCK:OFF");
            }
        });
        riavvio = view.findViewById(R.id.riavvio);
        riavvio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComando("REBOOT");
            }
        });
        aggiorna = view.findViewById(R.id.aggiorna);
        aggiorna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComando("UPDATEFW");
            }
        });
        home = view.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.status1Fragment);
            }
        });
    }
    private void sendComando(String s) {
        comando="";
        if (s.equals("LOCK:ON") ||s.equals("LOCK:OFF") ||s.equals("REBOOT")){
            comando= "#COMM#SERVER#1865#";
            comando += nomeDevice;
            comando += "#";
            comando += s;
            comando += "#";
        }
        if (s.equals("UPDATEFW") ){
            comando= "#COMM#SERVER#1865#";
            comando += nomeDevice;
            comando += "#";
            comando += s;
            comando += ":\"https://ym-dev.com/fwgel/AClick.bin\";MD5HASH:eef279d38586b29c37e1fc37971bb972#";
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
                int numero = s.getInt("user_devices_num");
                try {
                    showError("","Comando inviato al dispositivo");
                    Toast.makeText(getContext(), "Comando inviato al dispositivo", Toast.LENGTH_SHORT).show();
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
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundResource(R.drawable.tmessage);
        TextView text = (TextView) view.findViewById(android.R.id.message);
        /*Here you can do anything with above textview like text.setTextColor(Color.parseColor("#000000"));*/
        text.setTextColor(Color.parseColor("#ff0000"));
        text.setBackgroundColor(Color.parseColor("#00FF00"));
        toast.show();
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
        p.setName("IdProdotto");
        p.setValue("1");
        params.add(p);


        p = new RestParams();
        p.setName("SerialNumber");
        p.setValue(nomeDevice);
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