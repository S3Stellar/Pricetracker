package com.naorfarag.pricetracker;

import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomJsonObjectRequest extends JsonObjectRequest {

    public CustomJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        getHeaders();
    }


    @Override
    public Map getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(Finals.RAPID_API_HOST, Finals.MY_RAPID_API_HOST);
        headers.put(Finals.RAPID_API_KEY, Finals.MY_RAPID_API_KEY);
        return headers;
    }
}
