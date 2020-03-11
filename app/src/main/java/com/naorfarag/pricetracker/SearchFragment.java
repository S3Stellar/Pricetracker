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

    public SearchFragment() {
        // Required empty public constructor
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        String url = Finals.AMAZON_COM_HOMEPAGE;
        view = rootView.findViewById(R.id.webView);

        view.setWebViewClient(new WebViewClient());
        view.getSettings().setSupportZoom(true);
        view.getSettings().setBuiltInZoomControls(true);
        view.getSettings().setDisplayZoomControls(false);

        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setAllowContentAccess(true);
        view.getSettings().setAppCacheEnabled(true);
        view.getSettings().setDomStorageEnabled(true);
        view.getSettings().setUseWideViewPort(true);
        view.loadUrl(url);

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
