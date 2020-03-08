package com.naorfarag.pricetracker.lv.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naorfarag.pricetracker.R;
import com.naorfarag.pricetracker.lv.app.AppController;
import com.naorfarag.pricetracker.lv.model.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Product> productList;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CustomListAdapter(Activity activity, List<Product> productList) {
        this.activity = activity;
        this.productList = productList;
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
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        NetworkImageView thumbNail = convertView.findViewById(R.id.thumbnail);
        TextView productTitle = convertView.findViewById(R.id.title);
        TextView soldBy = convertView.findViewById(R.id.soldBy);
        TextView rating = convertView.findViewById(R.id.rating);
        TextView currentPrice = convertView.findViewById(R.id.currentPrice);
        final TextView targetPrice = convertView.findViewById(R.id.targetPrice);

        // getting product data for the row
        Product p = productList.get(position);

        // thumbnail image
        thumbNail.setImageUrl(p.getMainImage(), imageLoader);

        // title
        productTitle.setText(p.getProductTitle());

        // sold by
        soldBy.setText(String.format("by: %s", p.getSoldBy()));

        // rating
        rating.setText(String.format(" Rating: %s", p.getRating()));

        // current price
        String cprice = "Current Price: <font color=\"blue\">" + p.getCurrentStringPrice();
        currentPrice.setText(Html.fromHtml(cprice), TextView.BufferType.SPANNABLE);

        // target price
        String tprice = "Target Price: <font color=\"blue\">" + p.getTargetStringPrice();
        targetPrice.setText(Html.fromHtml(tprice), TextView.BufferType.SPANNABLE);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(position, targetPrice);
            }
        });
        addProductToDatabase(p, position);
        return convertView;
    }

    public void addProductToDatabase(Product p, final int position) {
        Map<String, Object> newProduct = p.convertToMap();
        // Add a new document with a generated ID
        db.collection("products").document("Product" + position).set(newProduct)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot added with ID: Product" + position);
                    }
                });
    }

    private void openDialog(final int position, final TextView targetPrice) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        final EditText edittext = new EditText(activity);
        edittext.setKeyListener(DigitsKeyListener.getInstance(false, true));
        alert.setMessage("Choose threshold alert price");
        alert.setTitle("Target price");

        alert.setView(edittext);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Product p = productList.get(position);
                String threshHold = edittext.getText().toString();
                if (!threshHold.isEmpty())
                    p.setTargetPrice(Double.parseDouble(threshHold));
                String tprice = "Target Price: <font color=\"blue\">" + p.getTargetStringPrice();
                targetPrice.setText(Html.fromHtml(tprice), TextView.BufferType.SPANNABLE);
                db.collection("products").document("Product" + position).update("targetPrice",threshHold);
            }
        });
        alert.show();
    }

    public List<Product> getProductList() {
        return productList;
    }
}