package com.naorfarag.pricetracker.lv.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.naorfarag.pricetracker.R;
import com.naorfarag.pricetracker.lv.app.AppController;
import com.naorfarag.pricetracker.lv.model.Product;

import java.util.ArrayList;
import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Product> productList;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public CustomListAdapter(Activity activity, List<Product> productList) {
        this.activity = activity;
        this.productList = productList;
    }

    public CustomListAdapter() {
        productList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int location) {
        return productList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        NetworkImageView thumbNail = (NetworkImageView) convertView
                .findViewById(R.id.thumbnail);

        TextView productTitle = (TextView) convertView.findViewById(R.id.title);
        TextView soldBy = (TextView) convertView.findViewById(R.id.soldBy);
        TextView rating = (TextView) convertView.findViewById(R.id.rating);
        TextView currentPrice = (TextView) convertView.findViewById(R.id.currentPrice);
        TextView targetPrice = (TextView) convertView.findViewById(R.id.targetPrice);

        // getting product data for the row
        Product p = productList.get(position);

        // thumbnail image
        thumbNail.setImageUrl(p.getMainImage(), imageLoader);

        // title
        productTitle.setText(p.getProductTitle());

        // sold by
        soldBy.setText("by: " + p.getSoldBy());

        // rating
        rating.setText(" Rating: " + p.getRating());

        // current price
        String cprice = "Current Price: <font color=\"blue\">" + p.getCurrentStringPrice();
        currentPrice.setText(Html.fromHtml(cprice), TextView.BufferType.SPANNABLE);

        // target price
        String tprice = "Target Price: <font color=\"blue\">" + p.getTargetStringPrice();
        targetPrice.setText(Html.fromHtml(tprice), TextView.BufferType.SPANNABLE);

        return convertView;
    }

    public List<Product> getProductList() {
        return productList;
    }
}