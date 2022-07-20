package com.wedev.mygel.fragments;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.wedev.mygel.R;
import com.wedev.mygel.SignInActivity;
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
import java.util.Map;

public class AccediFragment extends Fragment {

    // UI
    ImageView indietroaccedi;
    TextInputEditText email;
    TextInputEditText password;
    MaterialButton accedi;
    TextView registrati;
    TextView pwdimenticata;
    View view;
    NavController navController;

    Context ctx;
    //DB
    TMain mainData;
    DB db;

    // Rest
    RestFunctions rf;

    boolean DEBUG = false;


    public AccediFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ctx = getContext();
        // Inizializza DB
        setDB();
        // Legge DB
        mainData = getMainData();

        if (DEBUG){
            db.tMainDao().deleteAll();
            db.tFirstTimeDao().deleteAll();
        }else{
            if (mainData!=null){
                SignInActivity signin = (SignInActivity)getActivity();
                assert signin != null;
                signin.goMain();
            }
        }


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accedi, container, false);
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
        this.view = view;
        setUI();
        rf = new RestFunctions();
    }
    private void setUI() {
        registrati = view.findViewById(R.id.registrati);
        registrati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.sceltaProfiloFragment);
            }
        });
        pwdimenticata = view.findViewById(R.id.pwdimenticata);
        pwdimenticata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.recuperaPasswordFragment);
            }
        });
        accedi = view.findViewById(R.id.accedi);
        accedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verify())
                    accediServer();
                else
                    showError("","Inserisci correttamente i dati");
            }
        });
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
    }
    private boolean verify() {
        if (email.getText().toString().isEmpty()){
            email.requestFocus();
            return false;
        }
        if (password.getText().toString().isEmpty()){
            password.requestFocus();
            return false;
        }
        return true;
    }
    // ------------ REST DATA -------------
    public void accediServer(){

        // =============================
        // Imposta parametri di input
        ArrayList<RestParams> params=setParams();
        // Imposta richiesta
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.POST,
                getString(R.string.baseapi)+getString(R.string.signin),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        if (s == null) {
                            String t="v";
                            // TODO
                        } else {
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
                String tokenFromWeb = s.getString("token");
                if (!tokenFromWeb.equals("null") && !tokenFromWeb.isEmpty()){
                    db.tMainDao().deleteAll();
                    mainData = new TMain();
                    mainData.setToken(tokenFromWeb);
                    mainData.setIdServer("1");
                    db.tMainDao().insert(mainData);
                }
                SignInActivity signin = (SignInActivity)getActivity();
                signin.goMain();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // TODO
        }
        // setStart(s);
        //5
    }
    private void showError(String error_code, String message) {
        Toast toast = Toast.makeText(ctx, message, Toast.LENGTH_LONG);
        View view = toast.getView();
        view.setBackgroundResource(R.drawable.tmessage);
        TextView text = (TextView) view.findViewById(android.R.id.message);
        /*Here you can do anything with above textview like text.setTextColor(Color.parseColor("#000000"));*/
        text.setTextColor(Color.parseColor("#ff0000"));
        toast.show();
    }

    public void showErrBase(String titolo, String messaggio){
        Toast.makeText(ctx, messaggio, Toast.LENGTH_SHORT).show();
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
        p.setName("Email");
        p.setValue(email.getText().toString());
        params.add(p);


        p = new RestParams();
        p.setName("Password");
        p.setValue(password.getText().toString());
        params.add(p);

        p = new RestParams();
        p.setName("Source");
        p.setValue("android");
        params.add(p);

        return params;
    }
}