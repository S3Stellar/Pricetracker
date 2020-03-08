package com.naorfarag.pricetracker;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomJsonObjectRequest extends JsonObjectRequest {

    public CustomJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        try {
            getHeaders();
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
            Log.d("AuthError", Objects.requireNonNull(authFailureError.getMessage()));
        }
    }


    @Override
    public Map getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-rapidapi-host", "axesso-axesso-amazon-data-service-v1.p.rapidapi.com");
        headers.put("x-rapidapi-key", "163390d585mshe3218928e588b9fp1cd5c2jsn7add31f99e96");
        return headers;
    }
}
