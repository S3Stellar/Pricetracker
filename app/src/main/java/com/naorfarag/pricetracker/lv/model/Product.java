package com.naorfarag.pricetracker.lv.model;

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
        return getCurrencySymbol() + String.valueOf(getCurrentPrice());
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public String getTargetStringPrice() {
        return getCurrencySymbol() + String.valueOf(getTargetPrice());
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
        map.put("productTitle", productTitle);
        map.put("mainImage", mainImage);
        map.put("soldBy", soldBy);
        map.put("currencySymbol", currencySymbol);
        map.put("originalUrl", originalUrl);
        map.put("correctUrl", correctUrl);
        map.put("rating", rating);
        map.put("currentPrice", currentPrice);
        map.put("targetPrice", targetPrice);
        map.put("asin", asin);
        return map;
    }
}