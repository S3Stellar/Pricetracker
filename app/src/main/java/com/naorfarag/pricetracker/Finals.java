package com.naorfarag.pricetracker;

import android.view.View;

//import okhttp3.MediaType;

public final class Finals {

    public static final int UI_FLAGS =
            /*View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | --- disables navigation bar*/
            View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;

    public static final int[] tabIcons = {
            R.drawable.searchicon,
            R.drawable.carticon
    };

    /*public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");*/

    public enum HOF_RANKS {
        FIRST("First : "), SECOND("Second : "), THIRD("Third : "), FOURTH("Fourth : ");
        private String rank;

        public String getRank() {
            return this.rank;
        }

        HOF_RANKS(String rank) {
            this.rank = rank;
        }
    }
}
