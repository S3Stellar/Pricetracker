package com.naorfarag.pricetracker.lv.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.Settings;
import android.text.Html;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.View;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.swipe.SwipeLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.naorfarag.pricetracker.MainActivity;
import com.naorfarag.pricetracker.R;
import com.naorfarag.pricetracker.lv.app.AppController;
import com.naorfarag.pricetracker.lv.model.Product;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomListAdapter extends BaseAdapter {

    private static String uniqueID = null;
    private Activity activity;
    private LayoutInflater inflater;
    private List<Product> productList;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean isDeleting = false;

    @SuppressLint("HardwareIds")
    public CustomListAdapter(Activity activity, List<Product> productList) {
        this.activity = activity;
        this.productList = productList;
        uniqueID = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
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
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = Objects.requireNonNull(inflater).inflate(R.layout.list_row, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        ImageView priceButton = convertView.findViewById(R.id.priceButton);
        ImageView deleteButton = convertView.findViewById(R.id.deleteButton);
        SwipeLayout swipeLayout = convertView.findViewById(R.id.swipe_layout1);
        LinearLayout bottomWrapper = convertView.findViewById(R.id.bottom_wrapper);
        NetworkImageView thumbNail = convertView.findViewById(R.id.thumbnail);
        TextView productTitle = convertView.findViewById(R.id.title);
        TextView soldBy = convertView.findViewById(R.id.soldBy);
        TextView rating = convertView.findViewById(R.id.rating);
        TextView currentPrice = convertView.findViewById(R.id.currentPrice);
        final TextView targetPrice = convertView.findViewById(R.id.targetPrice);

        // getting product data for the row
        final Product p = productList.get(position);

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

        // Set on card view a listener (product)
        /*convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("parentView= ", parent.toString() + " ");
                openDialog(position, targetPrice);
            }
        });*/

        Log.d("URLS", MainActivity.urlsInTracklist.toString());
        Log.d("Product P", p.toString());
        Log.d("All Products", productList.toString());
        Log.d("Position", " Position = " + position + ", Count =" + getCount());
        setPriceDeleteButtonsListener(position, deleteButton, priceButton, targetPrice);
        addProductToDatabase(p, position);
        setSwipe(swipeLayout, bottomWrapper, position, targetPrice);
        return convertView;
    }

    private void setPriceDeleteButtonsListener(final int position, ImageView deleteButton, ImageView priceButton, final TextView targetPrice) {
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDeleting = true;
                if (!productList.isEmpty() && position <= productList.size()) {
                    if (!MainActivity.urlsInTracklist.isEmpty()) {
                        Objects.requireNonNull(MainActivity.urlsInTracklist.remove(productList.get(position).getCorrectUrl()));
                    }
                    Product saveProduct = productList.get(position);

                    String asin = productList.get(position).getAsin();
                    productList.remove(position);
                    Snackbar.make(v, "Product removed from tracklist", Snackbar.LENGTH_LONG)
                            .setAction("Undo", new ProductUndoListener(saveProduct, position))
                            .show();
                    notifyDataSetChanged();
                    db.collection(uniqueID).document(asin).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("DeleteSucess", " Deleted product from database pos: " + position);
                            isDeleting = false;
                        }
                    });
                }
            }
        });

        priceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(position, targetPrice);
            }
        });
    }

    private void setSwipe(SwipeLayout swipeLayout, LinearLayout bottomWrapper, final int position, final TextView targetPrice) {
        //set show mode.
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

        //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, bottomWrapper);
        swipeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openDialog(position, targetPrice);
                return false;
            }
        });
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
                Log.d("onClose", " onClose!");
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                Log.d("onUpdate", " onUpdate!");
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {
                Log.d("onStartOpen", " onStartOpen!");
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                Log.d("onOpen", " OPEN!");
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                //isDeleteOpen = false;
                Log.d("onStartClose", " onStartClose!");
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
            }
        });
    }

    private void addProductToDatabase(Product p, final int position) {
        if (isDeleting)
            return;
        Map<String, Object> newProduct = p.convertToMap();
        // Add a new document with a generated ID
        db.collection(uniqueID).document(p.getAsin()).set(newProduct)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot added with ID: Product" + position);
                        //notifyDataSetChanged();
                    }
                });
    }


    private void openDialog(final int position, final TextView targetPrice) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        final EditText edittext = new EditText(activity);

        // Accept positive numbers only
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
                db.collection(uniqueID).document(p.getAsin()).update("targetPrice", threshHold);
            }
        });
        alert.show();
    }

    public List<Product> getProductList() {
        return productList;
    }

    private class ProductUndoListener implements View.OnClickListener {
        private Product saveProduct;
        private int position;

        public ProductUndoListener(Product saveProduct, int position) {
            this.saveProduct = saveProduct;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            productList.add(saveProduct);
            addProductToDatabase(saveProduct, position);
            Objects.requireNonNull(MainActivity.urlsInTracklist.add(productList.get(position).getCorrectUrl()));
            notifyDataSetChanged();
        }
    }
    public static String getUniqueID() {
        return uniqueID;
    }

}
