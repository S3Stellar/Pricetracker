package com.naorfarag.pricetracker.lv.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SwipeLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.naorfarag.pricetracker.Finals;
import com.naorfarag.pricetracker.MainActivity;
import com.naorfarag.pricetracker.R;
import com.naorfarag.pricetracker.lv.app.AppController;
import com.naorfarag.pricetracker.lv.model.Product;
import com.naorfarag.pricetracker.util.MyFireStoreHelper;

import java.util.List;
import java.util.Objects;

public class CustomListAdapter extends BaseAdapter {

    private MyFireStoreHelper fireStoreHelper;
    private Activity activity;
    private LayoutInflater inflater;
    private List<Product> productList;
    private ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    private SwipeLayout swipeLayout;
    private CardView cardView;
    private ImageView priceButton;
    private ImageView deleteButton;
    private LinearLayout bottomWrapper;
    private TextView targetPrice;

    @SuppressLint("HardwareIds")
    public CustomListAdapter(Activity activity, List<Product> productList) {
        this.activity = activity;
        this.productList = productList;
        this.fireStoreHelper = new MyFireStoreHelper(activity);
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

        updateCurrentProduct(position, convertView);
        setCardViewListener(position);
        setDeleteButtonListener(position);
        setPriceButtonListener(position);
        setSwipeOnProducts();
        return convertView;
    }

    private void updateCurrentProduct(int position, View convertView) {
        swipeLayout = convertView.findViewById(R.id.swipe_layout1);
        cardView = convertView.findViewById(R.id.cardview_layout);
        priceButton = convertView.findViewById(R.id.priceButton);
        deleteButton = convertView.findViewById(R.id.deleteButton);
        bottomWrapper = convertView.findViewById(R.id.bottom_wrapper);

        NetworkImageView thumbNail = convertView.findViewById(R.id.thumbnail);
        TextView productTitle = convertView.findViewById(R.id.title);
        TextView soldBy = convertView.findViewById(R.id.soldBy);
        TextView rating = convertView.findViewById(R.id.rating);
        TextView currentPrice = convertView.findViewById(R.id.currentPrice);
        targetPrice = convertView.findViewById(R.id.targetPrice);

        // getting product data for the row
        Product p = productList.get(position);

        // thumbnail image
        thumbNail.setImageUrl(p.getMainImage(), imageLoader);

        // title
        productTitle.setText(p.getProductTitle());

        // sold by
        soldBy.setText(String.format(Finals.BY + " %s", p.getSoldBy()));

        // rating
        rating.setText(String.format(" %s", p.getRating()));

        // current price
        String cprice = Finals.CURRENT_PRICE + Finals.BLUE_FONT_COLOR + p.getCurrentStringPrice();
        currentPrice.setText(Html.fromHtml(cprice), TextView.BufferType.SPANNABLE);

        // target price
        String tprice = Finals.TARGET_PRICE + Finals.BLUE_FONT_COLOR + p.getTargetStringPrice();
        targetPrice.setText(Html.fromHtml(tprice), TextView.BufferType.SPANNABLE);
    }

    private void setCardViewListener(final int position) {
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YoYo.with(Techniques.Pulse).duration(200).repeat(1).playOn(v);
                threshHoldPriceDialog(position);
            }
        });
    }

    private void setDeleteButtonListener(final int position) {
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!productList.isEmpty() && position <= productList.size()) {
                    if (!MainActivity.urlsInTracklist.isEmpty()) {
                        Objects.requireNonNull(MainActivity.urlsInTracklist.remove(productList.get(position).getCorrectUrl()));
                    }
                    Product saveProduct = productList.get(position);

                    String asin = productList.get(position).getAsin();
                    MainActivity.vibrate(activity);
                    productList.remove(position);
                    Snackbar.make(v, Finals.PRODUCT_REMOVED_MSG, Snackbar.LENGTH_LONG)
                            .setAction(Finals.UNDO_BT, new ProductUndoListener(saveProduct, position))
                            .show();
                    notifyDataSetChanged();
                    fireStoreHelper.getDb().collection(MyFireStoreHelper.getUniqueID()).document(asin).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
                }
            }
        });
    }

    private void setPriceButtonListener(final int position) {
        priceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YoYo.with(Techniques.Pulse).duration(200).repeat(1).playOn(v);
                MainActivity.vibrate(activity);
                threshHoldPriceDialog(position);
            }
        });
    }

    private void setSwipeOnProducts() {
        //set show mode.
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

        //add drag edge.(If the BottomView has 'layout_gravity' attribute, this line is unnecessary)
        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, bottomWrapper);
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onClose(SwipeLayout layout) {
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {
            }

            @Override
            public void onOpen(SwipeLayout layout) {
            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
            }
        });
    }

    private void threshHoldPriceDialog(final int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        final EditText edittext = new EditText(activity);

        // Accept positive numbers only
        edittext.setKeyListener(DigitsKeyListener.getInstance(false, true));
        alert.setMessage(Finals.CHOOSE_THRESHOLD_MSG);
        alert.setTitle(Finals.TARGET_PRICE);

        alert.setView(edittext);

        alert.setPositiveButton(Finals.OK_BT, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Product p = productList.get(position);
                String threshHold = edittext.getText().toString();
                if (!threshHold.isEmpty()) {
                    MainActivity.vibrate(activity);
                    p.setTargetPrice(Double.parseDouble(threshHold));
                }
                String tprice = Finals.TARGET_PRICE + Finals.BLUE_FONT_COLOR + p.getTargetStringPrice();
                targetPrice.setText(Html.fromHtml(tprice), TextView.BufferType.SPANNABLE);
                fireStoreHelper.updateDocumentAttribute(p.getAsin(), Finals.T_PRICE_ATTR, threshHold);
                notifyDataSetChanged();
            }
        });
        alert.show();
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
            productList.add(0, saveProduct);
            fireStoreHelper.addProductToDatabase(saveProduct);
            Objects.requireNonNull(MainActivity.urlsInTracklist.add(productList.get(position).getCorrectUrl()));
            notifyDataSetChanged();
        }
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
}
