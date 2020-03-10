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
import com.android.volley.VolleyLog;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.naorfarag.pricetracker.lv.adapter.CustomListAdapter;
import com.naorfarag.pricetracker.lv.app.AppController;
import com.naorfarag.pricetracker.lv.model.Product;
import com.naorfarag.pricetracker.ui.main.MyViewPager;
import com.naorfarag.pricetracker.ui.main.SectionsPagerAdapter;

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
    public static ArrayList<String> urlsInTracklist = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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

        customListAdapter = new CustomListAdapter(this, productList);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupTabIcons();
        startTabsSelectListener();
        startAddButtonListener();
    }

    private void loadProductsFromDatabase() {
        db.collection(CustomListAdapter.getUniqueID())
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
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                        //hidePDialog();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //hidePDialog();
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                //hidePDialog();
            }
        });
    }

    private void getJsonFromProductURL(final String url) {
        showLoadingDialog();
        jsonObjReq = new CustomJsonObjectRequest(com.android.volley.Request.Method.GET,
                "https://axesso-axesso-amazon-data-service-v1.p.rapidapi.com/amz/amazon-lookup-product?url=" + url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("responseTAGsuccess", response.toString());
                addProductToTrackList(response, url, Finals.WEBVIEW);
                hidePDialog();
                listView.setSelection(customListAdapter.getCount() - 1);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("ErrorResponse", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Adding to tracklist failed", Toast.LENGTH_SHORT).show();
                hidePDialog();
            }
        });
        // Change timeout and number of tries to make the jsonRequest
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(6000,
                2,
                2));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void showLoadingDialog() {
        hidePDialog();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
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
                vibrate();
                String correctUrl;
                if (sf != null) {
                    try {
                        sf.getUrl();
                    } catch (Exception e) {
                        return;
                    }

                    Log.d("MyTracklist: ", urlsInTracklist.toString());
                    correctUrl = checkAndFixProductURL(sf.getUrl());
                    if (urlsInTracklist.contains(correctUrl)) {
                        Toast.makeText(getApplicationContext(),
                                "Product already in tracklist!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else return;

                Log.i("OriginalURL: ", sf.getUrl());
                if (correctUrl != null) {
                    Log.i("correctUrl", correctUrl);
                    getJsonFromProductURL(correctUrl);
                    Objects.requireNonNull(tabs.getTabAt(1)).select();
                }
            }
        });
    }

    private String checkAndFixProductURL(String url) {
        int type = 0; // 1 = DP, 0 = GP (type of products on amazon)
        if (url.contains("/dp/") || url.contains("/gp/")) {
            if (url.contains("/dp/") || url.contains("product/") || url.contains("aw/d"))
                type = 1;

            int startIndex = url.indexOf("amazon.");
            int endIndex = url.indexOf('/', startIndex);
            String siteRegion = url.substring(startIndex + 7, endIndex);
            String correctUrl = null;
            Pattern p = Pattern.compile("/([a-zA-Z0-9]{10})(?:[/?]|$)");
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
            //swipe();
        }
        try {
            Product product = new Product();
            if (from == Finals.WEBVIEW) {
                urlsInTracklist.add(correctUrl);
                product.setOriginalUrl(sf.getUrl());
                product.setCorrectUrl(correctUrl);
                product.setMainImage(responseObj.getJSONObject("mainImage").getString("imageUrl"));
                product.setCurrencySymbol(responseObj.getJSONObject("currency").getString("symbol"));

                if (responseObj.getDouble("dealPrice") > 0)
                    product.setCurrentPrice(responseObj.getDouble("dealPrice"));
                else if (responseObj.getDouble("salePrice") > 0)
                    product.setCurrentPrice(responseObj.getDouble("salePrice"));
                else if (responseObj.getDouble("price") > 0) {
                    product.setCurrentPrice(responseObj.getDouble("price"));
                } else {
                    product.setCurrentPrice(responseObj.getDouble("retailPrice"));
                }

                try {
                    product.setRating(Double.parseDouble(responseObj.getString("productRating").substring(0, responseObj.getString("productRating").indexOf(' '))));
                } catch (Exception e) {
                    product.setRating(0);
                }
                snackBarMessage("          The item has successfully added to tracklist!");
                Log.d("Count", "setSelection");
            } else {
                urlsInTracklist.add(responseObj.getString("correctUrl"));
                product.setOriginalUrl(responseObj.getString("originalUrl"));
                product.setMainImage(responseObj.getString("mainImage"));
                product.setCurrencySymbol(responseObj.getString("currencySymbol"));
                product.setCorrectUrl(responseObj.getString("correctUrl"));
                product.setCurrentPrice(responseObj.getDouble("currentPrice"));
                product.setRating(responseObj.getDouble("rating"));
                snackBarMessage("          Items loaded successfully");
            }
            product.setAsin(responseObj.getString("asin"));
            product.setProductTitle(responseObj.getString("productTitle"));
            product.setSoldBy(responseObj.getString("soldBy"));
            product.setTargetPrice(product.getCurrentPrice());
            productList.add(product);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("loadTrackListExcept", Objects.requireNonNull(e.getMessage()));
        }
        customListAdapter.notifyDataSetChanged();
    }

    private void snackBarMessage(String msg) {
        Snackbar.make(fab, msg, BaseTransientBottomBar.LENGTH_LONG)
                .setAction("Action", null).show();
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
            //getWindow().getDecorView().setSystemUiVisibility(Finals.UI_FLAGS);
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && v != null) {
            v.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.EFFECT_HEAVY_CLICK));
        } else {
            //deprecated in API 26
            Objects.requireNonNull(v).vibrate(250);
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