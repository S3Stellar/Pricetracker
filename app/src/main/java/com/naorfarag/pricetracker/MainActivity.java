package com.naorfarag.pricetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import com.naorfarag.pricetracker.lv.adapter.CustomListAdapter;
import com.naorfarag.pricetracker.lv.app.AppController;
import com.naorfarag.pricetracker.lv.model.Product;
import com.naorfarag.pricetracker.ui.main.MyViewPager;
//import com.naorfarag.pricetracker.ui.main.PageViewModel;
import com.naorfarag.pricetracker.ui.main.SectionsPagerAdapter;

/*import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;*/

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;*/

public class MainActivity extends AppCompatActivity {

    private ProgressDialog pDialog;
    private List<Product> productList = new ArrayList<>();
    private ListView listView;
    private CustomListAdapter customListAdapter;
    private TabLayout tabs;

/*    private okhttp3.Response response;
    private Request request;
    private OkHttpClient client = new OkHttpClient();
    private JSONObject responseObj;
    private static volatile int respondStatus = 0;
    private boolean firstDataLoad = true;
    private NameViewModel pageViewModel;*/

    private Context context;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private FloatingActionButton fab;
    private MyViewPager viewPager;
    private Toolbar toolbar;
    private CustomJsonObjectRequest jsonObjReq;
    private SearchFragment sf = new SearchFragment();
    private CartFragment cf = new CartFragment();
    private ArrayList<String> urlsInTracklist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Adds arrow left back button to toolbar
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = findViewById(R.id.fab);
        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        sectionsPagerAdapter.addFragment(sf, "SEARCH");
        sectionsPagerAdapter.addFragment(cf, "TRACKLIST");
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);

        customListAdapter = new CustomListAdapter(this, productList);

        setupTabIcons();
        startTabsSelectListener();
        startAddButtonListener(sf);

        // Get the ViewModel.
        /*pageViewModel = new ViewModelProvider(this).get(NameViewModel.class);

        // Create the observer which updates the UI.
        final Observer<Integer> nameObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer newName) {
                if (newName == 1 && a.getUrl() != null) {
                    getJsonFromProductURL(a.getUrl());
                }
                if (newName == 2)
                    loadTrackList(responseObj);
            }
        };

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        pageViewModel.getCurrentName().observe(this, nameObserver);*/

        /*getJsonFromProductURL("https://www.amazon.com/dp/B07T2B23Y2");*/
    }

    /*private void getJsonFromProductURL(final String url) {
        new Thread() {
            @Override
            public void run() {
                try {
                    request = new Request.Builder()
                            .url("https://axesso-axesso-amazon-data-service-v1.p.rapidapi.com/amz/amazon-lookup-product?url=" + url)
                            .get()
                            .addHeader("x-rapidapi-host", "axesso-axesso-amazon-data-service-v1.p.rapidapi.com")
                            .addHeader("x-rapidapi-key", "0a589777dfmshd2f9a3dd32c245cp1edbcfjsn4df31d02bf3e")
                            .build();

                    response = client.newCall(request).execute();

                    if (response == null) {
                        Log.i("response", "failed");
                        respondStatus = -1;
                    } else {
                        String json = response.body().string();
                        responseObj = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                        Log.i("body", "responseObj" + responseObj);
                        respondStatus = 1;
                    }
                } catch (Exception e) {
                    respondStatus = -1;
                    Log.i("FailedResponseException", "I failed");
                    e.printStackTrace();
                }
            }
        }.start();
    }*/

    /*private void getJsonFromProductURL(final String url) {
        new Thread() {
            @Override
            public void run() {
                try {
                    request = new Request.Builder()
                            .url("https://axesso-axesso-amazon-data-service-v1.p.rapidapi.com/amz/amazon-lookup-product?url=" + url)
                            .get()
                            .addHeader("x-rapidapi-host", "axesso-axesso-amazon-data-service-v1.p.rapidapi.com")
                            .addHeader("x-rapidapi-key", "0a589777dfmshd2f9a3dd32c245cp1edbcfjsn4df31d02bf3e")
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            Log.i("response", "failed");
                            respondStatus = -1;
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) throws IOException {
                            try {
                                String json = response.body().string();
                                responseObj = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                                pageViewModel.getCurrentName().postValue(2);
                                Log.i("body", "responseObj" + responseObj);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            loadTrackList(responseObj);
                        }
                    });
                } catch (Exception e) {
                    respondStatus = -1;
                    Log.i("FailedResponseException", "I failed");
                    e.printStackTrace();
                }
            }
        }.start();
    }*/

    private void getJsonFromProductURL(final String url) {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading...");
        pDialog.show();
        jsonObjReq = new CustomJsonObjectRequest(com.android.volley.Request.Method.GET,
                "https://axesso-axesso-amazon-data-service-v1.p.rapidapi.com/amz/amazon-lookup-product?url=" + url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d("responseTAGsuccess", response.toString());

                try {
/*                  String json = response.toString();
                    responseObj = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));*/
                    urlsInTracklist.add(sf.getUrl());
                    loadTrackList(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "onResponse: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                hidePDialog();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("ErrorResponse", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        "Add to tracklist failed", Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                hidePDialog();
            }
        });
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(6000,
                2,
                2));
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void startAddButtonListener(final SearchFragment sf) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrate();
                if (urlsInTracklist.contains(sf.getUrl())) {
                    Toast.makeText(getApplicationContext(),
                            "Product already in tracklist!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String correctUrl = checkAndFixProductURL(sf.getUrl());
                Log.i("OriginalURL: ", sf.getUrl());
                if (correctUrl != null) {
                    getJsonFromProductURL(correctUrl);
                    Log.i("correctUrl", correctUrl);
                    tabs.getTabAt(1).select();
                    /*Snackbar.make(view, "          The item has successfully added to tracklist!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*/
                }
            }
        });
    }

    private String checkAndFixProductURL(String url) {
        int type = 0; // 1 = DP, 0 = GP
        if (url.contains("/dp/") || url.contains("/gp/")) {
            if (url.contains("/dp/") || url.contains("product/") || url.contains("aw/d"))
                type = 1;

            int startIndex = url.indexOf("amazon.");
            int endIndex = url.indexOf("/", startIndex);
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
                    return "https://www.amazon.com/gp" + correctUrl;
            }
            Log.i("Url", url);
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


                    /*if (firstDataLoad) {
                        // Showing progress dialog before making http request
                        pDialog.setMessage("Loading...");
                        pDialog.show();
                        //pageViewModel.getCurrentName().postValue(1);
                        //getJsonFromProductURL("https://www.amazon.com/dp/B07T2B23Y2");
                        //loadTrackList(responseObj);
                    }*/
                }
                //firstDataLoad = false;
                /*if(respondStatus==1)
                    loadTrackList(responseObj);*/
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void loadTrackList(JSONObject responseObj) {
        listView = findViewById(R.id.list);
        listView.setAdapter(customListAdapter);
        try {
            Product product = new Product();
            product.setUrl(sf.getUrl());
            product.setProductTitle(responseObj.getString("productTitle"));
            product.setMainImage(responseObj.getJSONObject("mainImage").getString("imageUrl"));
            product.setSoldBy(responseObj.getString("soldBy"));
            product.setCurrencySymbol(responseObj.getJSONObject("currency").getString("symbol"));
            product.setRating(Double.parseDouble(responseObj.getString("productRating").substring(0, responseObj.getString("productRating").indexOf(' '))));

            if (responseObj.getDouble("dealPrice") > 0)
                product.setCurrentPrice(responseObj.getDouble("dealPrice"));
            else if (responseObj.getDouble("price") > 0) {
                product.setCurrentPrice(responseObj.getDouble("price"));
            } else {
                product.setCurrentPrice(responseObj.getDouble("retailPrice"));
            }
            product.setTargetPrice(product.getCurrentPrice());
            // adding movie to movies array
            productList.add(product);
            Snackbar.make(fab, "          The item has successfully added to tracklist!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //pageViewModel.getCurrentName().postValue(3);
        // notifying list adapter about data changes
        // so that it renders the list view with updated data
        customListAdapter.notifyDataSetChanged();
    }


    /*private void loadTrackList() {
        listView = findViewById(R.id.list);
        listView.setAdapter(adapter);

        // Creating volley request obj
        JsonArrayRequest movieReq = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("TAG", response.toString());
                        hidePDialog();

                        // Parsing json
                        for (int i = 0; i < response.length(); i++) {
                            try {

                                JSONObject obj = response.getJSONObject(i);
                                Product movie = new Product();
                                movie.setTitle(obj.getString("title"));
                                movie.setThumbnailUrl(obj.getString("image"));
                                movie.setRating(((Number) obj.get("rating"))
                                        .doubleValue());
                                movie.setYear(obj.getInt("releaseYear"));

                                // Genre is json array
                                JSONArray genreArry = obj.getJSONArray("genre");
                                ArrayList<String> genre = new ArrayList<String>();
                                for (int j = 0; j < genreArry.length(); j++) {
                                    genre.add((String) genreArry.get(j));
                                }
                                movie.setGenre(genre);

                                // adding movie to movies array
                                productList.add(movie);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        // notifying list adapter about data changes
                        // so that it renders the list view with updated data
                        adapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("VolleyError", "Error: " + error.getMessage());
                hidePDialog();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(movieReq);
    }*/

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
        tabs.getTabAt(0).setIcon(Finals.tabIcons[0]);
        tabs.getTabAt(1).setIcon(Finals.tabIcons[1]);
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
}