package com.wedev.mygel.functions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RestFunctions {

    public RestFunctions() {
    }

    public JSONObject setJsonBody(ArrayList<RestParams> params) {
        JSONObject jsonBodyObj = new JSONObject();
        try {
            for (int i = 0; i < params.size(); i++){
                jsonBodyObj.put(params.get(i).getName(),params.get(i).getValue() );
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonBodyObj;
    }

}
