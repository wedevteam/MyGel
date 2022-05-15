package com.wedev.mygel.functions;

import android.content.Context;

import com.wedev.mygel.R;
import com.wedev.mygel.database.DB;
import com.wedev.mygel.database.tables.TFirstTime;
import com.wedev.mygel.database.tables.TMain;

import org.json.JSONArray;

import java.util.ArrayList;

public class ManageBaseData {

    // Costanti
    public String NOTENTERED = "out";
    public String ENTERED = "in";
    public String PARAMapp_token = "app_token";
    public String PARAMuser_token = "user_token";
    public String PARAMsection = "section";
    public String PARAMtoken = "token";

    //DB
    public TMain mainData;
    public TFirstTime firstTimeData;
    public DB db;

    // Rest
    RestFunctions rf;
    public String token="";
    public JSONArray menuRest, subMenuRest;

    // VAR
    public String status = "";
    Context ctx;


    // DEBUG
    public boolean DEBUG = true; // mettere false in produzione

    // Costruttore
    public ManageBaseData(Context ctx) {
        this.ctx=ctx;

        // Inizializza funzioni Rest
        rf = new RestFunctions();
        // Inizializza DB
        setDB();
        // Legge DB
        mainData = getMainData();
        firstTimeData = getFirstTimeData();
        // Set status
        setStatus();
        // Set token
        setToken();
    }

    // ------------ DB -------------
    public void setDB() {
        db = DB.getInstance(ctx);
    }
    public TMain getMainData() {
        if (DEBUG) {
            db.tMainDao().deleteAll();
            db.tFirstTimeDao().deleteAll();
        }
        return (db.tMainDao().getNumber()==0) ? null : db.tMainDao().getAll().get(0) ;
    }
    public TFirstTime getFirstTimeData() {
        return (db.tFirstTimeDao().getNumber()==0) ? null : db.tFirstTimeDao().getAll().get(0) ;
    }
    // -----------------------------

    // ------------ TOKEN -------------
    public void setToken(){
        token = status.equals(NOTENTERED) ? "" : mainData.getToken();
    }

    // ------------ STATUS -------------
    public void setStatus(){
        status = mainData == null ? NOTENTERED : ENTERED;
    }
    // ---------------------------------

    // ------------ REST PARAMS BASE  -------------
    public ArrayList<RestParams> setParamsBase(String sez) {
        ArrayList<RestParams> params = new ArrayList<>();

        RestParams p = new RestParams();
        p.setName("app_token");
        p.setValue(ctx.getString(R.string.tokenapp));
        params.add(p);

        p = new RestParams();
        p.setName(PARAMsection);
        p.setValue(sez);
        params.add(p);

        p = new RestParams();
        p.setName(PARAMuser_token);
        p.setValue(token);
        params.add(p);

        return params;
    }
    // ---------------------------------



}
