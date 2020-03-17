package com.naorfarag.pricetracker;

import android.content.Context;
import android.util.Log;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.naorfarag.pricetracker.lv.adapter.CustomListAdapter;
import com.naorfarag.pricetracker.lv.app.AppController;
import com.naorfarag.pricetracker.util.MyFireStoreHelper;
import com.naorfarag.pricetracker.util.Notification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.naorfarag.pricetracker.Service.NOTIFICATION_ID;

public class MainActDup {

    private Context context;
    private HashMap<String, Double> newUpdatedJsons = new HashMap<>();
    private HashMap<String, JSONObject> oldDatabaseJsons = new HashMap<>();
    private MyFireStoreHelper fireStoreHelper;
    private static boolean hasPriceChanged = false;
    private int caller;

    public MainActDup(Context context, int caller) {
        this.context = context;
        fireStoreHelper = new MyFireStoreHelper(context);

        this.caller = caller;
        if (caller == Finals.SERVICE_JOB_CALLER)
            loadProductsFromDatabase();
    }

    private void loadProductsFromDatabase() {
        fireStoreHelper.getDb().collection(MyFireStoreHelper.getUniqueID())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            JSONObject obj = mapToJSON(document.getData());
                            try {
                                oldDatabaseJsons.put(obj.getString(Finals.ASIN_ATTR), obj);
                                getJsonFromProductURL(obj.getString(Finals.CORRECT_URL_ATTR), null, null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Log.d(new Throwable().getStackTrace()[0].getMethodName(), Finals.LOAD_PRODUCTS_FAILED_LMSG, task.getException());
                    }
                }).addOnFailureListener(e -> Log.d(new Throwable().getStackTrace()[0].getMethodName(), Objects.requireNonNull(e.getMessage())));
    }

    public void getJsonFromProductURL(final String url, CustomListAdapter customListAdapter, SwipeRefreshLayout swipeRefreshLayout) {

        CustomJsonObjectRequest jsonObjReq = new CustomJsonObjectRequest(com.android.volley.Request.Method.GET,
                Finals.LOOKUP_PRODUCT_REQUEST + url, null, response -> {

            double d = findCorrectPrice(response);
            try {
                response.put(Finals.PRICE_RA_ATTR, d);
                newUpdatedJsons.put(response.getString(Finals.ASIN_ATTR), d);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (caller == Finals.UPDATE_JOB_CALLER) {
                checkPriceAndUpdate(response, customListAdapter);

                if (newUpdatedJsons.size() == customListAdapter.getCount()) {
                    swipeRefreshLayout.setRefreshing(false);
                    updateSuccessDialog();
                    customListAdapter.notifyDataSetChanged();
                }

            } else if (!newUpdatedJsons.isEmpty()) {
                checkPriceAndUpdate(response, null);
            }
        }, error -> Log.d(new Throwable().getStackTrace()[0].getMethodName(), Finals.JSON_REQ_FAILED_LMSG));

        // Change timeout and number of tries to make the jsonRequest
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(Finals.REQUEST_TIMEOUT * 4,
                Finals.MAX_NUM_RETRIES,
                Finals.BACK_OFF_MULTI));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);

    }

    private void updateSuccessDialog() {
        new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(Finals.UPDATE_TITLE_MSG)
                .setContentText(Finals.TRACKLIST_UPDATED_MSG)
                .show();
    }

    private void checkPriceAndUpdate(JSONObject response, CustomListAdapter customListAdapter) {
        if (caller == Finals.UPDATE_JOB_CALLER && !newUpdatedJsons.isEmpty() && response != null) {
            try {
                double newPrice = response.getDouble(Finals.PRICE_RA_ATTR);
                if (newPrice != 0 && customListAdapter.getProductByAsin(response.getString(Finals.ASIN_ATTR)).getCurrentPrice() != newPrice) {
                    fireStoreHelper.updateDocumentAttributeNumber(response.getString(Finals.ASIN_ATTR), Finals.C_PRICE_ATTR, newPrice);
                    customListAdapter.getProductByAsin(response.getString(Finals.ASIN_ATTR)).setCurrentPrice(newPrice);
                    customListAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (caller == Finals.SERVICE_JOB_CALLER && !newUpdatedJsons.isEmpty() && !oldDatabaseJsons.isEmpty() && response != null) {
            try {
                double newPrice = Objects.requireNonNull(newUpdatedJsons.get(response.getString(Finals.ASIN_ATTR)));
                double oldPrice = Objects.requireNonNull(oldDatabaseJsons.get(response.getString(Finals.ASIN_ATTR))).getDouble(Finals.C_PRICE_ATTR);
                double targetPrice = Objects.requireNonNull(oldDatabaseJsons.get(response.getString(Finals.ASIN_ATTR))).getDouble(Finals.T_PRICE_ATTR);

                if (newPrice != 0) {
                    if (newPrice != oldPrice) {
                        fireStoreHelper.updateDocumentAttributeNumber(response.getString(Finals.ASIN_ATTR), Finals.C_PRICE_ATTR, newPrice);
                        if (newPrice <= targetPrice) {
                            Log.d(new Throwable().getStackTrace()[0].getMethodName(), Finals.LOWER_PRICE_DETECTED_LMSG);
                            MainActivity.vibrate(context);
                            Notification notification = new Notification();
                            Service.getmCurrentService().startForeground(NOTIFICATION_ID, notification.setNotification(context, Finals.PRICE_DROP_TITLE_NOTIFICATION, Finals.PRICE_DROP_ALERT_NOTIFICATION, R.drawable.ic_sleep));
                            hasPriceChanged = true;
                        }
                    }
                }
            } catch (NullPointerException | JSONException e) {
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
}