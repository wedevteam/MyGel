package com.wedev.mygel;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.google.android.material.textfield.TextInputEditText;
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

public class ProfiloFragment extends Fragment {

    Button registra;
    TextInputEditText password;
    TextInputEditText citta;
    TextInputEditText indirizzo;
    TextInputEditText cognome;
    TextInputEditText nome;
    TextInputEditText email;
    View view;

    //DB
    TMain mainData;
    DB db;

    // Rest
    RestFunctions rf;





    public ProfiloFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inizializza DB
        setDB();
        // Legge DB
        mainData = getMainData();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profilo, container, false);
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
        password = view.findViewById(R.id.password);
        citta = view.findViewById(R.id.citta);
        indirizzo = view.findViewById(R.id.indirizzo);
        cognome = view.findViewById(R.id.cognome);
        nome = view.findViewById(R.id.nome);
        email = view.findViewById(R.id.email);
        registra = view.findViewById(R.id.registra);
        registra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verify())
                    registraServer();
            }
        });
    }
    private boolean verify() {
        if (!emailIsValid()){
            email.requestFocus();
            return false;
        }
        if (email.getText().toString().isEmpty()){
            email.requestFocus();
            return false;
        }

        if (nome.getText().toString().isEmpty()){
            nome.requestFocus();
            return false;
        }
        if (cognome.getText().toString().isEmpty()){
            cognome.requestFocus();
            return false;
        }
        if (citta.getText().toString().isEmpty()){
            citta.requestFocus();
            return false;
        }
        if (indirizzo.getText().toString().isEmpty()){
            indirizzo.requestFocus();
            return false;
        }

        if (password.getText().toString().isEmpty()){
            password.requestFocus();
            return false;
        }

        if (password.getText().length()<8){
            Toast.makeText(getContext(), "La password deve essere lunga almeno 8 caratteri", Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return false;
        }
        return true;
    }
    private boolean emailIsValid() {
        if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            Toast.makeText(getContext(), "Email non valida", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ------------ REST DATA -------------
    public void registraServer(){
        registra.setEnabled(false);
        // Imposta parametri di input
        ArrayList<RestParams> params=setParams();
        // Imposta richiesta
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.POST,
                getString(R.string.baseapi)+getString(R.string.signup),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        if (s == null) {
                            String t="v";
                            registra.setEnabled(true);
                        } else {
                            registra.setEnabled(false);
                            fillData(s);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        String t="v";
                        registra.setEnabled(true);
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
        try {
            //JSONObject response = s.getJSONObject("response");
            boolean success = s.getBoolean("success");
            if (!success){
                registra.setEnabled(true);
                showError(s.getString("error_code"),s.getString("message"));
            }else{
                String tokenFromWeb = s.getString("token");
                if (!tokenFromWeb.equals("null") && !tokenFromWeb.isEmpty()){
                    mainData = new TMain();
                    mainData.setToken(tokenFromWeb);
                    db.tMainDao().insert(mainData);
                }
                Navigation.findNavController(view).navigate(R.id.registrazioneEffettuataFragment);
            }
        } catch (JSONException e) {
            registra.setEnabled(true);
            e.printStackTrace();
            // TODO
        }
        // setStart(s);
        //5
    } private void showError(String error_code, String message) {
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
        p.setName("Nome");
        p.setValue(nome.getText().toString());
        params.add(p);

        p = new RestParams();
        p.setName("Cognome");
        p.setValue(cognome.getText().toString());
        params.add(p);


        p = new RestParams();
        p.setName("Indirizzo");
        p.setValue(indirizzo.getText().toString());
        params.add(p);

        p = new RestParams();
        p.setName("Citta");
        p.setValue(citta.getText().toString());
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

        p = new RestParams();
        p.setName("TipoProfilo");
        p.setValue(getString(R.string.profprivato));
        params.add(p);


        return params;
    }
}