package com.wedev.mygel;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wedev.mygel.adapters.SSIDSAdapter;
import com.wedev.mygel.database.DB;
import com.wedev.mygel.database.tables.TMain;
import com.wedev.mygel.functions.RestFunctions;
import com.wedev.mygel.functions.RestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssegnazioneNomeFragment extends Fragment {

    SSIDSAdapter ssidsAdapter;
    private RecyclerView elencoSSID;
    ArrayList<String> ssids = new ArrayList<>();
    String nomeDevice="";

    EditText passwordrete;
    public TextView nomerete;
    View view;

    Button invia;

    //DB
    TMain mainData;
    DB db;

    // Rest
    RestFunctions rf;


    public AssegnazioneNomeFragment() {
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
        return inflater.inflate(R.layout.fragment_assegnazione_nome, container, false);
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
        getListSSID();
        this.view=view;
        setUI();
        setRecycler();
    }
    private void setRecycler() {
        ssidsAdapter = new SSIDSAdapter(view.getContext(), ssids,this);
        elencoSSID.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL,false));
        elencoSSID.setItemAnimator(new DefaultItemAnimator());
        elencoSSID.setAdapter(ssidsAdapter);
    }
    private void setUI() {
        invia = view.findViewById(R.id.invia);
        invia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inviaDatiRete();
            }
        });
        nomerete = view.findViewById(R.id.nomerete);
        passwordrete = view.findViewById(R.id.passwordrete);
        elencoSSID = view.findViewById(R.id.elencoReti);
    }
    private void inviaDatiRete() {
        if (verify()){
            String dati = "SSID="+nomerete.getText().toString()+":PASS="+passwordrete.getText().toString()+"&";
            inviaDati(dati);
        }else{
            Toast.makeText(getContext(), "Password o rete non indicata", Toast.LENGTH_SHORT).show();
        }

    }
    private void inviaDati(String dati) {
        String url = "http://192.168.4.1/cmd";
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (s == null) {
                            // CODICE ERRATO
                            Toast.makeText(getContext(),     "Errore durante il trasferimento dati", Toast.LENGTH_SHORT).show();
                        } else {
                           chiudiConnessioneDevice();
                            /*CountDownTimer countDownTimer = new CountDownTimer(10000,1000) {
                                @Override
                                public void onTick(long l) {
                                    String x = "1";
                                }
                                @Override
                                public void onFinish() {
                                    inviaDatiServer();
                                }
                            }.start();*/
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getContext(),     "Errore di connessione", Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("cmd", dati);
                return params;
            }
            @Override
            public byte[] getBody() {
                String x = dati;
                return x.getBytes(StandardCharsets.UTF_8);
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
    private void chiudiConnessioneDevice() {
        String url = "http://192.168.4.1/cmd";
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (s == null) {
                            // CODICE ERRATO
                            Toast.makeText(getContext(),     "Errore durante il trasferimento dati", Toast.LENGTH_SHORT).show();
                        } else {
                            CountDownTimer countDownTimer = new CountDownTimer(10000,1000) {
                                @Override
                                public void onTick(long l) {
                                    invia.setText("Connessione in corso.....");invia.setEnabled(false);
                                }
                                @Override
                                public void onFinish() {
                                    inviaDatiServer();
                                }
                            }.start();

                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getContext(),     "Errore di connessione", Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
            @Override
            public byte[] getBody() {
                String x = "QUIT&";
                return x.getBytes(StandardCharsets.UTF_8);
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    public void inviaDatiServer(){

        // =============================
        // Imposta parametri di input
        ArrayList<RestParams> params=setParams();
        // Imposta richiesta
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.POST,
                getString(R.string.baseapi)+getString(R.string.joindevice),
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
                        Toast.makeText(getActivity(), "Connessione internet inesistente", Toast.LENGTH_SHORT).show();
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
                Navigation.findNavController(view).navigate(R.id.attivazioneFragment);
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
        MaterialAlertDialogBuilder dialogo=  new MaterialAlertDialogBuilder(getContext(),R.style.AlertDialogTheme)
                .setTitle(titolo)
                .setMessage(messaggio)
                .setNeutralButton("Ok",null);
        dialogo.show();
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
        p.setName("ProductId");
        p.setValue("1"); // acquaclick
        params.add(p);

        p = new RestParams();
        p.setName("SerialNumber");
        p.setValue(nomeDevice);
        params.add(p);

        p = new RestParams();
        p.setName("Nome");
        p.setValue("");
        params.add(p);


        p = new RestParams();
        p.setName("Source");
        p.setValue("android");
        params.add(p);



        return params;
    }
    private boolean verify() {
        if (nomerete.getText().toString().equals(getString(R.string.retescelta))) return false;
        if (passwordrete.getText().toString().isEmpty()) return false;
        return true;
    }
    private void getListSSID() {

            SharedPreferences prefs = getContext().getSharedPreferences("gel", 0);
            int size = prefs.getInt("numerossid" , 0);
            for(int i=0 ; i<size; i++){
                ssids.add(prefs.getString("ssid" + "_ " + i, "")) ;
            }
            nomeDevice= prefs.getString("nomedevice" , "GELDEVICE");
    }
}