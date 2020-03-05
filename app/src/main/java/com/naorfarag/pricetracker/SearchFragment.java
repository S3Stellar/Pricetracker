package com.naorfarag.pricetracker;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {
    View view;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        /*view = inflater.inflate(R.layout.fragment_search, container, false);
        return view;*/
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        String url = "https://www.amazon.com/";
        WebView view = rootView.findViewById(R.id.webView);

        view.setWebViewClient(new WebViewClient());
        view.getSettings().setJavaScriptEnabled(true);
        view.getSettings().setAllowContentAccess(true);
        view.getSettings().setAppCacheEnabled(true);
        view.getSettings().setDomStorageEnabled(true);
        view.getSettings().setUseWideViewPort(true);
        view.loadUrl(url);

        return rootView ;
    }
}
