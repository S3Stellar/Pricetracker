package com.naorfarag.pricetracker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {
    private WebView view;
    private String amazonWebsite = Finals.AMAZON_COM_HOMEPAGE;
    private int layout;

    public SearchFragment() {
        this.layout = R.layout.fragment_search;
        // Required empty public constructor
    }

    public SearchFragment(String amazonWebsite, int layout) {
        this.amazonWebsite = amazonWebsite;
        this.layout = layout;
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(layout, container, false);

        if (layout == R.layout.fragment_topdeals)
            view = rootView.findViewById(R.id.webViewTopDeals);
        else
            view = rootView.findViewById(R.id.webViewSearch);

        view.setWebViewClient(new WebViewClient());
        view.getSettings().setSupportZoom(true);
        view.getSettings().setBuiltInZoomControls(true);
        view.getSettings().setDisplayZoomControls(false);
        view.getSettings().setGeolocationEnabled(true);
        view.getSettings().setSupportMultipleWindows(true);

        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setAllowContentAccess(true);
        view.getSettings().setAppCacheEnabled(true);
        view.getSettings().setDomStorageEnabled(true);
        view.getSettings().setUseWideViewPort(true);

        view.getSettings().setAllowFileAccess(true);
        view.loadUrl(amazonWebsite);

        return rootView;
    }

    public String getUrl() {
        return view.getUrl();
    }


    public int onBackPressed() {
        if (view.canGoBack()) {
            view.goBack();
        } else {
            return -1;
        }
        return 1;
    }
}
