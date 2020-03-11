package com.naorfarag.pricetracker;

import com.google.firebase.iid.FirebaseInstanceId;

public final class Finals {


    public static final int[] TAB_TITLES = new int[]{R.string.tab_search, R.string.tab_tracklist};

    public static final int[] tabIcons = {
            R.drawable.searchicon,
            R.drawable.carticon
    };

    public static final int FIRESTORE = 1;
    public static final int WEBVIEW = 0;
    public static final int TOP_LIST_PRODUCT = 0;


    public static final String CURRENT_PRICE = "Current Price:";
    public static final String TARGET_PRICE = "Target Price:";
    public static final String RATING = " Rating";
    public static final String BY = "by:";
    public static final String BLUE_FONT_COLOR = " <font color=\"blue\">";

    // Buttons Strings
    public static final String UNDO_BT = "Undo";
    public static final String OK_BT = "Ok";
    public static final String CANCEL_BT = "Cancel";
    public static final String ACTION_FAB = "Action";

    // FireStore product's attributes
    public static final String PRODUCT_TITLE_ATTR = "productTitle";
    public static final String MAIN_IMAGE_ATTR = "mainImage";
    public static final String SOLD_BY_ATTR = "soldBy";
    public static final String CURRENCY_SYM_ATTR = "currencySymbol";
    public static final String ORIGINAL_URL_ATTR = "originalUrl";
    public static final String CORRECT_URL_ATTR = "correctUrl";
    public static final String RATING_ATTR = "rating";
    public static final String C_PRICE_ATTR = "currentPrice";
    public static final String T_PRICE_ATTR = "targetPrice";
    public static final String ASIN_ATTR = "asin";

    // RapidAPI axesso attr
    public static final String IMAGE_URL_RA_ATTR = "imageUrl";
    public static final String CURRENCY_RA_ATTR = "currency";
    public static final String SYMBOL_RA_ATTR = "symbol";
    public static final String DEAL_RA_ATTR = "dealPrice";
    public static final String SALE_RA_ATTR = "salePrice";
    public static final String RETAIL_RA_ATTR = "retailPrice";
    public static final String PRICE_RA_ATTR = "price";
    public static final String RATING_RA_ATTR = "productRating";

    // RapidAPI host, key & settings
    public static final String RAPID_API_HOST = "x-rapidapi-host";
    public static final String RAPID_API_KEY = "x-rapidapi-key";
    public static final String MY_RAPID_API_HOST = "axesso-axesso-amazon-data-service-v1.p.rapidapi.com";
    public static final String MY_RAPID_API_KEY = "163390d585mshe3218928e588b9fp1cd5c2jsn7add31f99e95";
    public static final String LOOKUP_PRODUCT_REQUEST = "https://axesso-axesso-amazon-data-service-v1.p.rapidapi.com/amz/amazon-lookup-product?url=";
    public static final int REQUEST_TIMEOUT = 6000;
    public static final int MAX_NUM_RETRIES = 2;
    public static final int BACK_OFF_MULTI = 2;


    // Amazon pages & product page settings
    public static final String AMAZON_COM_HOMEPAGE = "https://www.amazon.com/";
    public static final String AMAZON_NAME = "Amazon";
    public static final String AMAZON_PATTERN = "/([a-zA-Z0-9]{10})(?:[/?]|$)";
    public static final String PRODUCT_SLASH_TYPE = "product/";
    public static final String DP_SITE_TYPE = "/dp/";
    public static final String GP_SITE_TYPE = "/gp/";
    public static final String AW_D_TYPE = "aw/d";

    // Messages to user
    public static final String PRODUCT_ADDED_SUCC_MSG = "          The item has successfully added to tracklist!";
    public static final String ITEMS_ADDED_SUCC_MSG = "          Items loaded successfully";
    public static final String ADD_TO_TRACKLIST_FAILED_MSG = "Adding to tracklist failed";
    public static final String ALREADY_IN_TRACKLIST_MSG = "Product already in tracklist!";
    public static final String PRODUCT_REMOVED_MSG = "Product removed from tracklist";
    public static final String CHOOSE_THRESHOLD_MSG = "Choose threshold alert price";
    public static final String GO_PRODUCT_PAGE_MSG = "Go to a product page!";
    public static final String LOADING_MSG = "Loading...";

    // Logs MSGS
    public static final String LOAD_PRODUCTS_FAILED_LMSG = "Failed to load database";

}
