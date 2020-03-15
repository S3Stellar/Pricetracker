package com.naorfarag.pricetracker;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.naorfarag.pricetracker.lv.adapter.CustomListAdapter;
import com.naorfarag.pricetracker.lv.app.AppController;
import com.naorfarag.pricetracker.util.MyFireStoreHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActDup {

    private Context context;
    private CustomJsonObjectRequest jsonObjReq;
    private List<JSONObject> newUpdatedJsons = new ArrayList<>();
    private List<JSONObject> oldDatabaseJsons = new ArrayList<>();
    private List<JSONObject> productsWithPriceChanged = new ArrayList<>();
    private MyFireStoreHelper fireStoreHelper;
    private static boolean hasPriceChanged = false;
    private static boolean doVibration = true;
    private int caller;

    public MainActDup(Context context, int caller) {
        this.context = context;
        fireStoreHelper = new MyFireStoreHelper(context);
        Log.i("MainAct", "in main act!");
        this.caller = caller;
        if (caller == Finals.SERVICE_JOB_CALLER)
            loadProductsFromDatabase();
    }

    // TODO: 13/03/2020 = need to fix timing of check < prices
    private void loadProductsFromDatabase() {
        fireStoreHelper.getDb().collection(MyFireStoreHelper.getUniqueID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                JSONObject obj = mapToJSON(document.getData());
                                oldDatabaseJsons.add(obj);
                                /*try {
                                    getJsonFromProductURL(obj.getString(Finals.CORRECT_URL_ATTR));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }*/
                            }
                        } else {
                            Log.d(new Throwable().getStackTrace()[0].getMethodName(), Finals.LOAD_PRODUCTS_FAILED_LMSG, task.getException());
                        }

                        for (int i = 0; i < oldDatabaseJsons.size(); i++) {
                            try {
                                getJsonFromProductURL(oldDatabaseJsons.get(i).getString(Finals.CORRECT_URL_ATTR), null, null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(new Throwable().getStackTrace()[0].getMethodName(), Objects.requireNonNull(e.getMessage()));
            }
        });
    }

    // TODO: 14/03/2020 fix update price when refresh (upd text)
    public void getJsonFromProductURL(final String url, CustomListAdapter customListAdapter, SwipeRefreshLayout swipeRefreshLayout) {
        Log.d("getJsonURL", " " + url);

        jsonObjReq = new CustomJsonObjectRequest(com.android.volley.Request.Method.GET,
                Finals.LOOKUP_PRODUCT_REQUEST + url, null, response -> {
            /*if (response != null)*/
            newUpdatedJsons.add(response);
            Double newPrice = findCorrectPrice(response);
            Log.d("ARR SIZE", " newSize = " + newUpdatedJsons.size() + " oldSize = " + oldDatabaseJsons.size());
            if (caller == Finals.UPDATE_JOB_CALLER) {
                Log.d("callerUPDA", "calling checkPrice");
                checkPriceAndUpdate(response, newPrice, customListAdapter);

                if (newUpdatedJsons.size() == customListAdapter.getCount()) {
                    swipeRefreshLayout.setRefreshing(false);
                    new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Update")
                            .setContentText("Tracklist updated successfully")
                            .show();

                    Log.d("Textview", "change current price");
                        /*TextView currentPrice = ((Activity) context).findViewById(R.id.currentPrice);
                        String cprice = Finals.CURRENT_PRICE + Finals.BLUE_FONT_COLOR + " " + Finals.CURRENCY_SYM_SIGN + newPrice;
                        currentPrice.setText(Html.fromHtml(cprice, HtmlCompat.FROM_HTML_MODE_LEGACY));*/
                    //customListAdapter.getView(customListAdapter.getPosition(),customListAdapter.getConvertView(),customListAdapter.getParent());
                    customListAdapter.notifyDataSetChanged();
                }

            } else if (newUpdatedJsons.size() == oldDatabaseJsons.size() && !newUpdatedJsons.isEmpty()
                    && caller == Finals.SERVICE_JOB_CALLER) {
                checkPriceAndUpdate(response, newPrice, customListAdapter);
            }
        }, error -> {
            Log.d("ErrorResponse", "failed jsonReq");
        });

        // Change timeout and number of tries to make the jsonRequest
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(Finals.REQUEST_TIMEOUT + 15000,
                Finals.MAX_NUM_RETRIES,
                Finals.BACK_OFF_MULTI));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);

    }

    private void checkPriceAndUpdate(JSONObject response, double newPrice, CustomListAdapter customListAdapter) {
        for (int i = 0; i < newUpdatedJsons.size(); i++) {
            try {
                //Log.d("mainAct", "if: " + newPrice + " <= " + newUpdatedJsons.get(i).getDouble(Finals.T_PRICE_ATTR) + " newPrice<=TargetPrice");
                if (newUpdatedJsons.get(i).getString(Finals.ASIN_ATTR).equalsIgnoreCase(response.getString(Finals.ASIN_ATTR))) {
                    fireStoreHelper.updateDocumentAttributeNumber(newUpdatedJsons.get(i).getString(Finals.ASIN_ATTR), Finals.C_PRICE_ATTR, newPrice);
                    if(caller == Finals.UPDATE_JOB_CALLER) {
                        customListAdapter.getProductByAsin(response.getString(Finals.ASIN_ATTR)).setCurrentPrice(newPrice);
                        customListAdapter.notifyDataSetChanged();
                    }
                }
                if (caller == Finals.SERVICE_JOB_CALLER && newPrice <= oldDatabaseJsons.get(i).getDouble(Finals.T_PRICE_ATTR) && newPrice != oldDatabaseJsons.get(i).getDouble(Finals.C_PRICE_ATTR)) {
                    productsWithPriceChanged.add(newUpdatedJsons.get(i));
                    Log.d("mainAct", "LOWER PRICE DETECTED!");
                    hasPriceChanged = true;
                    /*if (caller == Finals.UPDATE_JOB_CALLER) {
                        TextView currentPrice = ((Activity) context).findViewById(R.id.currentPrice);
                        String cprice = Finals.CURRENT_PRICE + Finals.BLUE_FONT_COLOR + " " + Finals.CURRENCY_SYM_SIGN + newPrice;
                        currentPrice.setText(Html.fromHtml(cprice, HtmlCompat.FROM_HTML_MODE_LEGACY));
                    }*/
                }

                /*TextView currentPrice = ((Activity) context).findViewById(R.id.currentPrice);
                String cprice = Finals.CURRENT_PRICE + Finals.BLUE_FONT_COLOR + " " + Finals.CURRENCY_SYM_SIGN + newPrice;
                currentPrice.setText(Html.fromHtml(cprice, HtmlCompat.FROM_HTML_MODE_LEGACY));*/

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private double findCorrectPrice(JSONObject response) {
        Double newPrice = null;
        try {
            if (response.getDouble(Finals.DEAL_RA_ATTR) > 0) {
                newPrice = (response.getDouble(Finals.DEAL_RA_ATTR));
            } else if (response.getDouble(Finals.SALE_RA_ATTR) > 0) {
                newPrice = (response.getDouble(Finals.SALE_RA_ATTR));
            } else if (response.getDouble(Finals.PRICE_RA_ATTR) > 0) {
                newPrice = (response.getDouble(Finals.PRICE_RA_ATTR));
            } else {
                newPrice = response.getDouble(Finals.RETAIL_RA_ATTR);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newPrice;
    }


    private JSONObject mapToJSON(Map<String, Object> map) {
        JSONObject obj = new JSONObject();
        try {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Map) {
                    Map<String, Object> subMap = (Map<String, Object>) value;
                    obj.put(key, mapToJSON(subMap));
                } else if (value instanceof List) {
                    obj.put(key, listToJSONArray((List) value));
                } else {

                    obj.put(key, value);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private JSONArray listToJSONArray(List<Object> list) {
        JSONArray arr = new JSONArray();
        for (Object obj : list) {
            if (obj instanceof Map) {
                arr.put(mapToJSON((Map) obj));
            } else if (obj instanceof List) {
                arr.put(listToJSONArray((List) obj));
            } else {
                arr.put(obj);
            }
        }
        return arr;
    }

    public static boolean hasPriceChanged() {
        return hasPriceChanged;
    }

    public static void setHasPriceChanged(boolean hasPriceChanged) {
        MainActDup.hasPriceChanged = hasPriceChanged;
    }

    public static void setDoVibration(boolean doVibration) {
        MainActDup.doVibration = doVibration;
    }

    public static boolean isDoVibration() {
        return doVibration;
    }
}