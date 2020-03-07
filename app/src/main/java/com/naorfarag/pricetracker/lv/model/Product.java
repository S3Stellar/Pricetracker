package com.naorfarag.pricetracker.lv.model;

public class Product {
    private String productTitle;
    private String mainImage;
    private String soldBy;
    private String currencySymbol;
    private String url;
    private double rating;
    private double currentPrice;
    private double targetPrice;

    public Product() {
    }

    public Product(String productTitle, String mainImage, String soldBy, String currencySymbol, double rating,
                   double price) {
        this.productTitle = productTitle;
        this.mainImage = mainImage;
        this.soldBy = soldBy;
        this.currencySymbol = currencySymbol;
        this.rating = rating;
        this.currentPrice = price;
        this.targetPrice = price;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}