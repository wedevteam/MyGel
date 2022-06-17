package com.wedev.mygel;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wedev.mygel.adapters.SSIDSAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfoAttivazioneFragment extends Fragment {

    Button impostazioni;
    Button associa;
    View view;



    public InfoAttivazioneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_attivazione, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view=view;
        setUI();

    }
    private void setUI() {
        impostazioni = view.findViewById(R.id.impostazioni);
        impostazioni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openWirelessSettings = new Intent("android.settings.WIRELESS_SETTINGS"); startActivity(openWirelessSettings);
            }
        });
        associa = view.findViewById(R.id.associa);
        associa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ottieniDeviceId();
            }
        });
    }
    private void ottieniDeviceId() {
        String url = "http://192.168.4.1/hello";
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (s == null) {
                            // CODICE ERRATO
                            Toast.makeText(getContext(),     "Errore durante il trasferimento dati", Toast.LENGTH_SHORT).show();
                        } else {
                           try {
                               JSONObject dati = new JSONObject(s);
                               JSONObject response = dati.getJSONObject("response");
                               String nomeDevice = response.getString("message");
                               JSONObject data = response.getJSONObject("data");
                               JSONArray menu = data.getJSONArray("menu");
                               List<String> ssids = new ArrayList<>();
                               for (int i = 0; i < menu.length(); i++) {
                                   JSONObject dato = menu.getJSONObject(i);
                                   ssids.add(dato.getString("SSID"));
                               }
                               SharedPreferences prefs = getContext().getSharedPreferences("gel", 0);
                               SharedPreferences.Editor editor = prefs.edit();
                               editor.putString("nomedevice", nomeDevice);
                               editor.putInt("numerossid", ssids.size());
                               for(int i=0 ; i<ssids.size() ; i++){
                                   editor.putString("ssid" + "_ " + i, ssids.get(i));
                               }
                               editor.apply();
                               Navigation.findNavController(view).navigate(R.id.assegnazioneNomeFragment);
                           }catch (Exception e){
                               e.printStackTrace();
                           }
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
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
}