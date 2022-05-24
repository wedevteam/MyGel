package com.wedev.mygel;

import static android.content.ContentValues.TAG;
import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;
import com.wedev.mygel.adapters.ProdottiAdapter;
import com.wedev.mygel.adapters.SSIDSAdapter;
import com.wedev.mygel.database.DB;
import com.wedev.mygel.database.tables.TMain;
import com.wedev.mygel.functions.ManageBaseData;
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

public class HomeFragment extends Fragment {

    // UI

    View view;

    //DB
    TMain mainData;
    DB db;

    // Rest
    RestFunctions rf;


    // BASE
    ManageBaseData _baseData ;

    Button aggiungi;

    ProdottiAdapter prodottiAdapter;
    private RecyclerView elencoProdottiRecycler;
    public ArrayList<ModelProdotti> elencoProdotti = new ArrayList<>();

    LinearLayout layoutnoprodotti;

    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rf = new RestFunctions();
    //    _baseData = new ManageBaseData(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inizializza DB
        setDB();
        // Legge DB
        mainData = getMainData();



        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);

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
        getDevices();
    }

    private void setUI() {
        aggiungi = view.findViewById(R.id.aggiungi);
        aggiungi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.sceltaProdottoFragment);
            }
        });
        elencoProdottiRecycler = view.findViewById(R.id.elencoProdotti);
        layoutnoprodotti = view.findViewById(R.id.layoutnoprodotti);

    }
    private void setRecycler() {
        prodottiAdapter = new ProdottiAdapter(view.getContext(), elencoProdotti,this);
        elencoProdottiRecycler.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.VERTICAL,false));
        elencoProdottiRecycler.setItemAnimator(new DefaultItemAnimator());
        elencoProdottiRecycler.setAdapter(prodottiAdapter);
    }

    public void goStatus(String idProdotto, String nomeDevice){
        SharedPreferences prefs = getContext().getSharedPreferences("gel", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nomedevice", nomeDevice);
        editor.putString("idProdotto", idProdotto);
        editor.apply();
        Navigation.findNavController(view).navigate(R.id.status1Fragment);
    }
    // ------------ REST DATA -------------
    public void getDevices(){

        // =============================
        // Imposta parametri di input
        ArrayList<RestParams> params=setParams();
        // Imposta richiesta
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.POST,
                getString(R.string.baseapi)+getString(R.string.deviceslist),
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
                    JSONArray listaDevices = s.getJSONArray("user_devices");
                    int numeroDevices = s.getInt("user_devices_num");
                    if (numeroDevices==0){

                    }else{
                        layoutnoprodotti.setVisibility(View.GONE);
                        elencoProdotti.clear();
                        for (int i = 0; i <listaDevices.length() ; i++) {
                            ModelProdotti prodotto = new ModelProdotti();
                            JSONObject oggettoProdotto = listaDevices.getJSONObject(i);
                            prodotto.setIdServer(oggettoProdotto.getString("id"));
                            prodotto.setNome(oggettoProdotto.getString("nome"));
                            prodotto.setSerialNumber(oggettoProdotto.getString("serialNumber"));
                            elencoProdotti.add(prodotto);
                            setMessageType(prodotto.getSerialNumber());
                        }
                        setRecycler();
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
    public void setMessageType(String prod) {
        String msg ="";
        FirebaseMessaging.getInstance().subscribeToTopic(prod)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = ("OK");
                        if (!task.isSuccessful()) {
                            msg = "NO";
                        }
                        Log.d(TAG, msg);

                    }
                });
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
        p.setName("Source");
        p.setValue("android");
        params.add(p);

        return params;
    }
}