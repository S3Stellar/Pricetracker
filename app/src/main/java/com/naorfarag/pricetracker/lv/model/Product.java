package com.naorfarag.pricetracker.lv.model;

import com.naorfarag.pricetracker.Finals;

import java.util.HashMap;
import java.util.Map;

public class Product {
    private String productTitle;
    private String mainImage;
    private String soldBy;
    private String currencySymbol;
    private String originalUrl;
    private String correctUrl;
    private String asin;
    private double rating;
    private double currentPrice;
    private double targetPrice;

    public Product() {
    }

    public Product(String productTitle, String mainImage, String soldBy, String currencySymbol, double rating,
                   double price, String asin) {
        this.productTitle = productTitle;
        this.mainImage = mainImage;
        this.soldBy = soldBy;
        this.currencySymbol = currencySymbol;
        this.rating = rating;
        this.currentPrice = price;
        this.targetPrice = price;
        this.asin = asin;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getSoldBy() {
        return soldBy;
    }

    public void setSoldBy(String soldBy) {
        this.soldBy = soldBy;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }


    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getCurrentStringPrice() {
        return getCurrencySymbol() + getCurrentPrice();
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public String getTargetStringPrice() {
        return getCurrencySymbol() + getTargetPrice();
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getCorrectUrl() {
        return correctUrl;
    }

    public void setCorrectUrl(String correctUrl) {
        this.correctUrl = correctUrl;
    }

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public Map<String, Object> convertToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(Finals.PRODUCT_TITLE_ATTR, productTitle);
        map.put(Finals.MAIN_IMAGE_ATTR, mainImage);
        map.put(Finals.SOLD_BY_ATTR, soldBy);
        map.put(Finals.CURRENCY_SYM_ATTR, currencySymbol);
        map.put(Finals.ORIGINAL_URL_ATTR, originalUrl);
        map.put(Finals.CORRECT_URL_ATTR, correctUrl);
        map.put(Finals.RATING_ATTR, rating);
        map.put(Finals.C_PRICE_ATTR, currentPrice);
        map.put(Finals.T_PRICE_ATTR, targetPrice);
        map.put(Finals.ASIN_ATTR, asin);
        return map;
    }
}