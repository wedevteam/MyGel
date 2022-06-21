package com.wedev.mygel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.graphics.drawscope.Fill;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.SeekBar;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.wedev.mygel.database.DB;
import com.wedev.mygel.database.tables.TMain;
import com.wedev.mygel.functions.RestFunctions;
import com.wedev.mygel.functions.RestParams;
import com.wedev.mygel.models.DayAxisValueFormatter;
import com.wedev.mygel.models.MyAxisValueFormatter;
import com.wedev.mygel.models.XYMarkerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraficiActivity  extends GraficiBase implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {
    private BarChart chart;
    private SeekBar seekBarX, seekBarY;
    private TextView tvX, tvY;
    String nomeDevice="";
    String idProdotto= "";

    String flags="";

    ArrayList<Integer> vt1 = new ArrayList<Integer>();
    ArrayList<Integer> vt2 = new ArrayList<Integer>();
    ArrayList<Integer> vt3 = new ArrayList<Integer>();
    ArrayList<Integer> vt4 = new ArrayList<Integer>();
    ArrayList<Integer> vt5 = new ArrayList<Integer>();
    ArrayList<Integer> vt6 = new ArrayList<Integer>();
    ArrayList<Integer> vt7 = new ArrayList<Integer>();
    ArrayList<Integer> vt8 = new ArrayList<Integer>();
    ArrayList<Integer> vtBase = new ArrayList<Integer>();

    Button t4;
    Button t5;
    Button t6;
    Button t7;
    Button t8;
    TextView titolo;
    //DB
    TMain mainData;
    DB db;

    // Rest
    RestFunctions rf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_grafici);
        rf = new RestFunctions();
        SharedPreferences prefs = getSharedPreferences("gel", 0);
        nomeDevice= prefs.getString("nomedevice" , "GELDEVICE");
        idProdotto= prefs.getString("idProdotto" , "idProdotto");
        // Inizializza DB
        setDB();
        // Legge DB
        mainData = getMainData();
        getStatistiche();
      //  setUI();

    }
    // ------------ DB -------------
    public void setDB() {
        db = DB.getInstance(this);
    }
    public TMain getMainData() {
        return (db.tMainDao().getNumber()==0) ? null : db.tMainDao().getAll().get(0) ;
    }
    // -----------------------------
    // ------------ REST DATA -------------
    public void getStatistiche(){

        // =============================
        // Imposta parametri di input
        ArrayList<RestParams> params=setParams();
        // Imposta richiesta
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.POST,
                getString(R.string.baseapi)+getString(R.string.getgrafici),
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

                try {
                    flags= s.getString("message");
                    vt1.clear();
                    vt2.clear();
                    vt3.clear();
                    vt4.clear();
                    vt5.clear();
                    vt6.clear();
                    vt7.clear();
                    vt8.clear();
                    vtBase.clear();
                    try {
                        String[] tipi = flags.split(";");
                        for (int i = 0; i < tipi.length; i++) {
                            String[] valori = tipi[i].split(",");
                            if (i==0){
                                for (int y = 0; y < valori.length-1; y++) {
                                    vt1.add(Integer.parseInt(valori[y]));
                                }
                            }
                            if (i==1){
                                for (int y = 0; y < valori.length-1; y++) {
                                    vt2.add(Integer.parseInt(valori[y]));
                                }
                            }
                            if (i==2){
                                for (int y = 0; y < valori.length-1; y++) {
                                    vt3.add(Integer.parseInt(valori[y]));
                                }
                            }
                            if (i==3){
                                for (int y = 0; y < valori.length-1; y++) {
                                    vt4.add(Integer.parseInt(valori[y]));
                                }
                            }
                            if (i==4){
                                for (int y = 0; y < valori.length-1; y++) {
                                    vt5.add(Integer.parseInt(valori[y]));
                                }
                            }
                            if (i==5){
                                for (int y = 0; y < valori.length-1; y++) {
                                    vt6.add(Integer.parseInt(valori[y]));
                                }
                            }
                            if (i==6){
                                for (int y = 0; y < valori.length-1; y++) {
                                    vt7.add(Integer.parseInt(valori[y]));
                                }
                            }
                            if (i==7){
                                for (int y = 0; y < valori.length-1; y++) {
                                    vt8.add(Integer.parseInt(valori[y]));
                                }
                            }

                        }
                    }catch (Exception e){

                    }
                    setUI();
                    try {
                        setvtbase(vt4);
                        setData(12,12);
                        chart.invalidate();
                        titolo.setText(t4.getText().toString());
                    }catch (Exception e){
                        Toast.makeText(GraficiActivity.this, "Impossibile legegre dati", Toast.LENGTH_SHORT).show();
                    }

                    setData(seekBarX.getProgress(), seekBarY.getProgress());
                    chart.invalidate();
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
    private void setvtbase(ArrayList<Integer> arrayvalori){
        vtBase.clear();
        for (int i = 0; i < arrayvalori.size(); i++) {
            vtBase.add(arrayvalori.get(i));
        }
    }
    private void showError(String error_code, String message) {
        hideSoftKeyBoard();
        showErrBase(" "+error_code,message);
    }

    public void showErrBase(String titolo, String messaggio){

        Toast.makeText(this, messaggio, Toast.LENGTH_SHORT).show();
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

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

    private void setUI(){
        titolo = findViewById(R.id.titolo);
        t4 = findViewById(R.id.t4);
        t4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setvtbase(vt4);
                    setData(12,12);
                    chart.invalidate();
                    titolo.setText(t4.getText().toString());
                }catch (Exception e){
                    Toast.makeText(GraficiActivity.this, "Impossibile legegre dati", Toast.LENGTH_SHORT).show();
                }

            }
        });
        t5 = findViewById(R.id.t5);
        t5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setvtbase(vt5);
                    setData(12,12);
                    chart.invalidate();
                    titolo.setText(t5.getText().toString());
                }catch (Exception e){
                    Toast.makeText(GraficiActivity.this, "Impossibile legegre dati", Toast.LENGTH_SHORT).show();
                }

            }
        });
        t6 = findViewById(R.id.t6);
        t6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setvtbase(vt6);
                    setData(12,12);
                    chart.invalidate();
                    titolo.setText(t6.getText().toString());
                }catch (Exception e){
                    Toast.makeText(GraficiActivity.this, "Impossibile legegre dati", Toast.LENGTH_SHORT).show();
                }

            }
        });
        t7 = findViewById(R.id.t7);
        t7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setvtbase(vt7);
                    setData(12,12);
                    chart.invalidate();
                    titolo.setText(t7.getText().toString());
                }catch (Exception e){
                    Toast.makeText(GraficiActivity.this, "Impossibile legegre dati", Toast.LENGTH_SHORT).show();
                }

            }
        });
        t8 = findViewById(R.id.t8);
        t8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    setvtbase(vt8);
                    setData(12,12);
                    chart.invalidate();
                    titolo.setText(t8.getText().toString());
                }catch (Exception e){
                    Toast.makeText(GraficiActivity.this, "Impossibile legegre dati", Toast.LENGTH_SHORT).show();
                }

            }
        });

        tvX = findViewById(R.id.tvXMax);
        tvY = findViewById(R.id.tvYMax);

        seekBarX = findViewById(R.id.seekBar1);
        seekBarY = findViewById(R.id.seekBar2);

        seekBarY.setOnSeekBarChangeListener(this);
        seekBarX.setOnSeekBarChangeListener(this);

        chart = findViewById(R.id.chart1);
        chart.setOnChartValueSelectedListener(this);

        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        // chart.setDrawYLabels(false);

        IAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(chart);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(12);
        //   xAxis.setValueFormatter((ValueFormatter)xAxisFormatter);

        IAxisValueFormatter custom = new MyAxisValueFormatter();

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        //  leftAxis.setValueFormatter((ValueFormatter) custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        //   rightAxis.setValueFormatter((ValueFormatter)custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        XYMarkerView mv = new XYMarkerView(this, xAxisFormatter);
        mv.setChartView(chart); // For bounds control
        chart.setMarker(mv); // Set the marker to the chart

        // setting data
        seekBarY.setProgress(50);
        seekBarX.setProgress(12);

        // chart.setDrawLegend(false);
    }
    private void setData(int count, float range) {

        float start = 1f;

        ArrayList<BarEntry> values = new ArrayList<>();

        try {
            for (int i = 1; i <vtBase.size() ; i++) {
                values.add(new BarEntry(i,(float) vtBase.get(i)));
            }

        }catch(Exception e){
            e.printStackTrace();
        }




        BarDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();

        } else {
            String anno="anno...";
            set1 = new BarDataSet(values, anno);

            set1.setDrawIcons(false);

            int startColor1 = ContextCompat.getColor(this, android.R.color.holo_orange_light);
            int startColor2 = ContextCompat.getColor(this, android.R.color.holo_blue_light);
            int startColor3 = ContextCompat.getColor(this, android.R.color.holo_orange_light);
            int startColor4 = ContextCompat.getColor(this, android.R.color.holo_green_light);
            int startColor5 = ContextCompat.getColor(this, android.R.color.holo_red_light);
            int endColor1 = ContextCompat.getColor(this, android.R.color.holo_blue_dark);
            int endColor2 = ContextCompat.getColor(this, android.R.color.holo_purple);
            int endColor3 = ContextCompat.getColor(this, android.R.color.holo_green_dark);
            int endColor4 = ContextCompat.getColor(this, android.R.color.holo_red_dark);
            int endColor5 = ContextCompat.getColor(this, android.R.color.holo_orange_dark);



            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(0.9f);

            chart.setData(data);
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        tvX.setText(String.valueOf(seekBarX.getProgress()));
        tvY.setText(String.valueOf(seekBarY.getProgress()));


    }

    @Override
    protected void saveToGallery() {
        saveToGallery(chart, "BarChartActivity");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    private final RectF onValueSelectedRectF = new RectF();

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null)
            return;

        RectF bounds = onValueSelectedRectF;
        chart.getBarBounds((BarEntry) e, bounds);
        MPPointF position = chart.getPosition(e, YAxis.AxisDependency.LEFT);

        Log.i("bounds", bounds.toString());
        Log.i("position", position.toString());

        Log.i("x-index",
                "low: " + chart.getLowestVisibleX() + ", high: "
                        + chart.getHighestVisibleX());

        MPPointF.recycleInstance(position);
    }


    @Override
    public void onNothingSelected() { }
}