package com.wedev.mygel;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.wedev.mygel.functions.RestParams;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestAcquaClickActivity extends AppCompatActivity {

    TextView domanderisposte;
    Button chiedi,invia,out;
    MaterialButton test;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_acqua_click);
        domanderisposte = findViewById(R.id.domanderisposte);
        test = findViewById(R.id.accedi);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chiediInfo();
            }
        });
        chiedi = findViewById(R.id.btnchiedi);
        chiedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chiediInfo();
            }
        });
        invia = findViewById(R.id.btninvia);
        invia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inviaInfo();
            }
        });
        out = findViewById(R.id.btnout);
        chiedi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scollegati();
            }
        });
        
    }
    private void scollegati() {

    }
    private void inviaInfo() {
        
    }
    private void chiediInfo() {
        // ------------ REST DATA -------------

            // Imposta parametri di input
            ArrayList<RestParams> params=setParams();
            // Imposta richiesta
            JsonObjectRequest stringRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    getString(R.string.addressbase)+getString(R.string.chiedissid),
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject s) {
                            if (s == null) {
                                String t="v";
                                domanderisposte.setText("NO DATI");
                            } else {
                                domanderisposte.setText("OK");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            String t="v";
                            domanderisposte.setText("NO");
                        }
                    }) {

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    return params;
                }
                /*@Override
                public byte[] getBody() {
                    return rf.setJsonBody(params).toString() == null ? null : rf.setJsonBody(params).toString().getBytes(StandardCharsets.UTF_8);
                }*/
            };

            //Creating a Request Queue
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            int socketTimeout = 150000;//50 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            // Richiesta
            stringRequest.setRetryPolicy(policy);
            requestQueue.add(stringRequest);
        }


    private ArrayList<RestParams> setParams() {
        ArrayList<RestParams> params = new ArrayList<>();

        return params;
    }
}