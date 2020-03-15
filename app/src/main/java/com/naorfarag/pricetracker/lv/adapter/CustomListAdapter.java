package com.naorfarag.pricetracker.lv.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
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
import androidx.core.text.HtmlCompat;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SwipeLayout;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.naorfarag.pricetracker.Finals;
import com.naorfarag.pricetracker.MainActivity;
import com.naorfarag.pricetracker.R;
import com.naorfarag.pricetracker.lv.app.AppController;
import com.naorfarag.pricetracker.lv.model.Product;
import com.naorfarag.pricetracker.util.MyFireStoreHelper;
import com.willy.ratingbar.ScaleRatingBar;

import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

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
    private boolean isDialogOpen = false;
    private int position;
    private View convertView;
    private ViewGroup parent;

    @SuppressLint("HardwareIds")
    public CustomListAdapter(Activity activity, List<Product> productList) {
        this.activity = activity;
        this.productList = productList;
        this.fireStoreHelper = new MyFireStoreHelper(activity);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = Objects.requireNonNull(inflater).inflate(R.layout.list_row, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        this.position = position;
        this.convertView = convertView;
        this.parent = parent;
        updateCurrentProduct(position, convertView);
        setCardViewListener(position);
        setDeleteButtonListener(position);
        setPriceButtonListener(position);
        setBottomWrapperListener();
        setSwipeOnProducts();
        return convertView;
    }

    private void setBottomWrapperListener() {
        bottomWrapper.setOnClickListener(v -> {
            swipeLayout.close();
            notifyDataSetChanged();
        });
    }

    public void updateCurrentProduct(int position, View convertView) {
        swipeLayout = convertView.findViewById(R.id.swipe_layout1);
        cardView = convertView.findViewById(R.id.cardview_layout);
        priceButton = convertView.findViewById(R.id.priceButton);
        deleteButton = convertView.findViewById(R.id.deleteButton);
        bottomWrapper = convertView.findViewById(R.id.bottom_wrapper);

        final ScaleRatingBar scaleRatingBar = convertView.findViewById(R.id.simpleRatingBar);
        NetworkImageView thumbNail = convertView.findViewById(R.id.thumbnail);
        TextView productTitle = convertView.findViewById(R.id.title);
        TextView soldBy = convertView.findViewById(R.id.soldBy);

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

        // Set rating stars
        scaleRatingBar.setRating((float) p.getRating());
        scaleRatingBar.setClickable(false);

        // current price
        String cprice = Finals.CURRENT_PRICE + Finals.BLUE_FONT_COLOR + p.getCurrentStringPrice();
        currentPrice.setText(Html.fromHtml(cprice, HtmlCompat.FROM_HTML_MODE_LEGACY));

        // target price
        String tprice = Finals.TARGET_PRICE + Finals.BLUE_FONT_COLOR + p.getTargetStringPrice();
        targetPrice.setText(HtmlCompat.fromHtml(tprice, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    private void setCardViewListener(final int position) {
        cardView.setOnClickListener(v -> {
            YoYo.with(Techniques.Pulse).duration(200).repeat(1).playOn(v);
            if (isDialogOpen) {
                swipeLayout.close();
                threshHoldPriceDialog(position);
                isDialogOpen = false;
                notifyDataSetChanged();
            } else {
                threshHoldPriceDialog(position);
            }
        });
    }

    private void setDeleteButtonListener(final int position) {
        deleteButton.setOnClickListener(v -> {
            if (!productList.isEmpty() && position <= productList.size()) {
                if (!MainActivity.urlsInTracklist.isEmpty()) {
                    Objects.requireNonNull(MainActivity.urlsInTracklist.remove(productList.get(position).getCorrectUrl()));
                }
                Product saveProduct = productList.get(position);

                String asin = productList.get(position).getAsin();
                MainActivity.vibrate(activity);
                productList.remove(position);
                Snackbar.make(v, Finals.PRODUCT_REMOVED_MSG, BaseTransientBottomBar.LENGTH_LONG)
                        .setAction(Finals.UNDO_BT, new ProductUndoListener(saveProduct, position))
                        .show();
                notifyDataSetChanged();
                fireStoreHelper.getDb().collection(MyFireStoreHelper.getUniqueID()).document(asin).delete();
            }
        });
    }

    private void setPriceButtonListener(final int position) {
        priceButton.setOnClickListener(v -> {
            YoYo.with(Techniques.Pulse).duration(200).repeat(1).playOn(v);
            MainActivity.vibrate(activity);
            threshHoldPriceDialog(position);
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
                isDialogOpen = false;
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {
                isDialogOpen = true;
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                isDialogOpen = true;
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                isDialogOpen = false;
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
            }
        });
    }

    private void threshHoldPriceDialog(final int position) {
        final EditText edittext = new EditText(activity);
        edittext.setHint(productList.get(position).getCurrentStringPrice());
        edittext.setHapticFeedbackEnabled(true);
        edittext.setKeyListener(DigitsKeyListener.getInstance(false, true));

        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(activity, SweetAlertDialog.NORMAL_TYPE);
        sweetAlertDialog.setTitleText(Finals.TARGET_PRICE);
        sweetAlertDialog.setCustomImage(R.drawable.prices_settings_img);
        sweetAlertDialog.setConfirmButton("Ok", sweetAlertDialog1 -> {
            Product p = productList.get(position);
            String threshHold = edittext.getText().toString();
            if (!threshHold.isEmpty()) {
                MainActivity.vibrate(activity);
                p.setTargetPrice(Double.parseDouble(threshHold));
                fireStoreHelper.updateDocumentAttributeString(p.getAsin(), Finals.T_PRICE_ATTR, threshHold);
                String tprice = Finals.TARGET_PRICE + Finals.BLUE_FONT_COLOR + p.getTargetStringPrice();
                targetPrice.setText(HtmlCompat.fromHtml(tprice, HtmlCompat.FROM_HTML_MODE_LEGACY));
                notifyDataSetChanged();
            }
            sweetAlertDialog1.dismissWithAnimation();
        });
        sweetAlertDialog.setCustomView(edittext);
        sweetAlertDialog.show();
    }

    private class ProductUndoListener implements View.OnClickListener {
        private Product saveProduct;
        private int position;

        private ProductUndoListener(Product saveProduct, int position) {
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public View getConvertView() {
        return convertView;
    }

    public void setConvertView(View convertView) {
        this.convertView = convertView;
    }

    public ViewGroup getParent() {
        return parent;
    }

    public void setParent(ViewGroup parent) {
        this.parent = parent;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public Product getProductByAsin(String asin) {
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).getAsin().equalsIgnoreCase(asin))
                return productList.get(i);
        }
        return null;
    }
}
