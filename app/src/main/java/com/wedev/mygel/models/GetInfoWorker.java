package com.wedev.mygel.models;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.wedev.mygel.MainActivity;
import com.wedev.mygel.database.DB;
import com.wedev.mygel.database.tables.TMain;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GetInfoWorker extends Worker {
    private static final String WORK_RESULT = "work_result";
    Context ctx;
    String taskDataString = "";
    Map<String, String> params;
    //DB
    TMain mainData;
    DB db;

    public GetInfoWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        ctx = context;
    }
    @NonNull
    @Override
    public Result doWork() {
        Data taskData = getInputData();
        taskDataString = taskData.getString(MainActivity.MESSAGE_STATUS);
        setDB();
        mainData = getMainData();
        getServerData(ctx, ctx.getString(R.string.GetNewMessage));

        Data outputData = new Data.Builder().putString(WORK_RESULT, "Jobs Finished").build();
        return Result.success(outputData);
    }

    private void showNotification(String task, String desc) {
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.putExtra("type", "gomsg");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "task_channel";
        String channelName = "task_name";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setContentTitle(task)
                .setContentText(desc)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round);
        manager.notify(1, builder.build());
    }

    public void getServerData(final Context context, final String address) {
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, address, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject s) {
                        if (s == null) {

                        } else {
                            try {
                                if (s.getInt("Codice")==0){
                                    JSONArray s2 = s.getJSONArray("Data");
                                    if (s2.length()>0){
                                        showNotification("Telesan", taskDataString != null ? taskDataString : "Hai nuovi messaggi da leggere");
                                    }
                                }
                            } catch (Exception e) {

                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Token", main.getToken());
                return params;
            }

        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(ctx);
        int socketTimeout = 150000;//50 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        // Richiesta
        stringRequest.setRetryPolicy(policy);
        requestQueue.add(stringRequest);
    }


    public Map<String, String> setParams(String address) {
        params = new HashMap<>();

        params.put("Token",main.getToken() );

        return params;
    }
    /*************************************
     * DB
     ************************************/
    // ------------ DB -------------
    public void setDB() {
        db = DB.getInstance(ctx);
    }
    public TMain getMainData() {
        return (db.tMainDao().getNumber()==0) ? null : db.tMainDao().getAll().get(0) ;
    }
    // -----------------------------
}