package com.naorfarag.pricetracker;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.naorfarag.pricetracker.lv.adapter.CustomListAdapter;
import com.naorfarag.pricetracker.lv.app.AppController;
import com.naorfarag.pricetracker.lv.model.Product;
import com.naorfarag.pricetracker.ui.main.MyViewPager;
import com.naorfarag.pricetracker.ui.main.SectionsPagerAdapter;
import com.naorfarag.pricetracker.util.MyFireStoreHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private Context context;

    private CustomJsonObjectRequest jsonObjReq;
    private ProgressDialog pDialog;

    private FloatingActionButton fab;

    private TabLayout tabs;

    private CartFragment cf = new CartFragment();
    private SearchFragment sf = new SearchFragment();

    private ListView listView;
    private CustomListAdapter customListAdapter;
    private List<Product> productList = new ArrayList<>();
    public static final List<String> urlsInTracklist = new ArrayList<>();

    private MyFireStoreHelper fireStoreHelper;
    private boolean loadedFromDatabase = false;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        tabs = findViewById(R.id.tabs);
        MyViewPager viewPager = findViewById(R.id.view_pager);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        sectionsPagerAdapter.addFragment(sf, context.getResources().getString(Finals.TAB_TITLES[0]));
        sectionsPagerAdapter.addFragment(cf, context.getResources().getString(Finals.TAB_TITLES[1]));
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);

        fireStoreHelper = new MyFireStoreHelper(context);
        customListAdapter = new CustomListAdapter(this, productList);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupTabIcons();
        startTabsSelectListener();
        startAddButtonListener();
    }

    private void loadProductsFromDatabase() {
        fireStoreHelper.getDb().collection(MyFireStoreHelper.getUniqueID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                JSONObject obj = mapToJSON(document.getData());
                                addProductToTrackList(obj, "", Finals.FIRESTORE);
                            }
                        } else {
                            Log.d(new Throwable().getStackTrace()[0].getMethodName(), Finals.LOAD_PRODUCTS_FAILED_LMSG, task.getException());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void getJsonFromProductURL(final String url) {
        showLoadingDialog();
        jsonObjReq = new CustomJsonObjectRequest(com.android.volley.Request.Method.GET,
                Finals.LOOKUP_PRODUCT_REQUEST + url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                addProductToTrackList(response, url, Finals.WEBVIEW);
                hidePDialog();
                listView.setSelection(Finals.TOP_LIST_PRODUCT);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context,
                        Finals.ADD_TO_TRACKLIST_FAILED_MSG, Toast.LENGTH_SHORT).show();
                hidePDialog();
            }
        });
        // Change timeout and number of tries to make the jsonRequest
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(Finals.REQUEST_TIMEOUT,
                Finals.MAX_NUM_RETRIES,
                Finals.BACK_OFF_MULTI));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showLoadingDialog() {
        hidePDialog();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(Finals.LOADING_MSG);
        pDialog.setCancelable(false);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, Finals.CANCEL_BT, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (jsonObjReq != null)
                    jsonObjReq.cancel();
                hidePDialog();
            }
        });
        pDialog.show();
    }

    private void startAddButtonListener() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YoYo.with(Techniques.Pulse).duration(200).repeat(1).playOn(view);
                vibrate(context);
                String correctUrl;
                if (sf != null) {
                    try {
                        sf.getUrl();
                    } catch (Exception e) {
                        return;
                    }

                    correctUrl = checkAndFixProductURL(sf.getUrl());
                    if (urlsInTracklist.contains(correctUrl)) {
                        Toast.makeText(getApplicationContext(),
                                Finals.ALREADY_IN_TRACKLIST_MSG, Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else return;


                if (correctUrl != null) {
                    getJsonFromProductURL(correctUrl);
                    Objects.requireNonNull(tabs.getTabAt(1)).select();
                } else {
                    Toast.makeText(getApplicationContext(),
                            Finals.GO_PRODUCT_PAGE_MSG, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String checkAndFixProductURL(String url) {
        int type = 0; // 1 = DP, 0 = GP (type of products on amazon)
        if (url.contains(Finals.DP_SITE_TYPE) || url.contains(Finals.GP_SITE_TYPE)) {
            if (url.contains(Finals.DP_SITE_TYPE) || url.contains(Finals.PRODUCT_SLASH_TYPE) || url.contains(Finals.AW_D_TYPE))
                type = 1;

            int startIndex = url.indexOf("amazon.");
            int endIndex = url.indexOf('/', startIndex);
            String siteRegion = url.substring(startIndex + 7, endIndex);
            String correctUrl = null;
            Pattern p = Pattern.compile(Finals.AMAZON_PATTERN);
            Matcher m = p.matcher(url);

            if (m.find()) {
                correctUrl = m.group();
                if (correctUrl.contains("?"))
                    correctUrl = correctUrl.replace("?", "");
            }

            if (correctUrl != null) {
                if (type == 1)
                    return "https://www.amazon." + siteRegion + "/dp" + correctUrl;
                else
                    return "https://www.amazon." + siteRegion + "/gp" + correctUrl;
            }
        }
        return null;
    }

    private void startTabsSelectListener() {
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    findViewById(R.id.fab).setVisibility(View.VISIBLE);
                    getWindow().setNavigationBarColor(ContextCompat.getColor(context, R.color.design_default_color_background));
                } else {
                    getWindow().setNavigationBarColor(ContextCompat.getColor(context, R.color.trackList_layout_color));
                    findViewById(R.id.fab).setVisibility(View.INVISIBLE);
                    if (!loadedFromDatabase) {
                        loadProductsFromDatabase();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void addProductToTrackList(JSONObject responseObj, String correctUrl, int from) {
        if (!loadedFromDatabase) {
            listView = findViewById(R.id.list);
            listView.setAdapter(customListAdapter);
            loadedFromDatabase = true;
        }

        try {
            Product product = new Product();
            product.setAsin(responseObj.getString(Finals.ASIN_ATTR));
            product.setProductTitle(responseObj.getString(Finals.PRODUCT_TITLE_ATTR));
            if (responseObj.getString(Finals.SOLD_BY_ATTR).equals("") &&
                    !responseObj.getString(Finals.SOLD_BY_ATTR).isEmpty() &&
                    responseObj.getString(Finals.SOLD_BY_ATTR).equals("null"))
                product.setSoldBy(responseObj.getString(Finals.SOLD_BY_ATTR));
            else
                product.setSoldBy(Finals.AMAZON_NAME);

            if (from == Finals.WEBVIEW) {
                addProductFromWeb(responseObj, correctUrl, product);
            } else {
                addProductFromDatabase(responseObj, product);
            }

            productList.add(0, product);

        } catch (JSONException e) {
            Log.d(new Throwable().getStackTrace()[0].getMethodName(), Objects.requireNonNull(e.getMessage()));
        }
        customListAdapter.notifyDataSetChanged();
    }

    private void addProductFromDatabase(JSONObject responseObj, Product product) throws JSONException {
        urlsInTracklist.add(responseObj.getString(Finals.CORRECT_URL_ATTR));
        product.setOriginalUrl(responseObj.getString(Finals.ORIGINAL_URL_ATTR));
        product.setMainImage(responseObj.getString(Finals.MAIN_IMAGE_ATTR));
        product.setCurrencySymbol(responseObj.getString(Finals.CURRENCY_SYM_ATTR));
        product.setCorrectUrl(responseObj.getString(Finals.CORRECT_URL_ATTR));
        product.setCurrentPrice(responseObj.getDouble(Finals.C_PRICE_ATTR));
        product.setTargetPrice(responseObj.getDouble(Finals.T_PRICE_ATTR));
        product.setRating(responseObj.getDouble(Finals.RATING_ATTR));
        snackBarMessage(Finals.ITEMS_ADDED_SUCC_MSG);
    }

    private void addProductFromWeb(JSONObject responseObj, String correctUrl, Product product) throws JSONException {
        urlsInTracklist.add(correctUrl);
        product.setOriginalUrl(sf.getUrl());
        product.setCorrectUrl(correctUrl);
        product.setMainImage(responseObj.getJSONObject(Finals.MAIN_IMAGE_ATTR).getString(Finals.IMAGE_URL_RA_ATTR));
        product.setCurrencySymbol(responseObj.getJSONObject(Finals.CURRENCY_RA_ATTR).getString(Finals.SYMBOL_RA_ATTR));
        if (responseObj.getDouble(Finals.DEAL_RA_ATTR) > 0) {
            product.setCurrentPrice(responseObj.getDouble(Finals.DEAL_RA_ATTR));
        } else if (responseObj.getDouble(Finals.SALE_RA_ATTR) > 0) {
            product.setCurrentPrice(responseObj.getDouble(Finals.SALE_RA_ATTR));
        } else if (responseObj.getDouble(Finals.PRICE_RA_ATTR) > 0) {
            product.setCurrentPrice(responseObj.getDouble(Finals.PRICE_RA_ATTR));
        } else {
            product.setCurrentPrice(responseObj.getDouble(Finals.RETAIL_RA_ATTR));
        }
        product.setTargetPrice(product.getCurrentPrice());

        try {
            product.setRating(Double.parseDouble(responseObj.getString(Finals.RATING_RA_ATTR)
                    .substring(0, responseObj.getString(Finals.RATING_RA_ATTR).indexOf(' '))));
        } catch (Exception e) {
            product.setRating(0);
        }
        fireStoreHelper.addProductToDatabase(product);
        snackBarMessage(Finals.PRODUCT_ADDED_SUCC_MSG);
    }

    private void snackBarMessage(String msg) {
        Snackbar.make(fab, msg, BaseTransientBottomBar.LENGTH_LONG)
                .setAction(Finals.ACTION_FAB, null).show();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupTabIcons() {
        Objects.requireNonNull(tabs.getTabAt(0)).setIcon(Finals.tabIcons[0]);
        Objects.requireNonNull(tabs.getTabAt(1)).setIcon(Finals.tabIcons[1]);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    public static void vibrate(Context ctx) {
        Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 250 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && v != null) {
            v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.EFFECT_HEAVY_CLICK));
        } else {
            //deprecated in API 26
            Objects.requireNonNull(v).vibrate(200);
        }
    }

    @Override
    public void onBackPressed() {
        int canBack = sf.onBackPressed();
        if (canBack == -1)
            super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (listView != null)
                listView.onTouchEvent(event);
        } catch (Exception e) {

        }
        return false;
    }
}