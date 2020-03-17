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
    private WebView webView;
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
            webView = rootView.findViewById(R.id.webViewTopDeals);
        else
            webView = rootView.findViewById(R.id.webViewSearch);

        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.getSettings().setAllowFileAccess(true);
        webView.loadUrl(amazonWebsite);

        return rootView;
    }

    public String getUrl() {
        return webView.getUrl();
    }


    public int onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            return -1;
        }
        return 1;
    }

    public WebView getWebView() {
        return webView;
    }
    @Override
    public void onPause() {
        webView.onPause();
        webView.pauseTimers();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.resumeTimers();
        webView.onResume();
    }


    @Override
    public void onDestroy() {
        webView.destroy();
        webView = null;
        super.onDestroy();
    }
}
