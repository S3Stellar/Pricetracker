package com.naorfarag.pricetracker;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomJsonObjectRequest extends JsonObjectRequest {

    public CustomJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }


    @Override
    public Map getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("x-rapidapi-host", "axesso-axesso-amazon-data-service-v1.p.rapidapi.com");
        headers.put("x-rapidapi-key", "163390d585mshe3218928e588b9fp1cd5c2jsn7add31f99e95");
        return headers;
    }
}