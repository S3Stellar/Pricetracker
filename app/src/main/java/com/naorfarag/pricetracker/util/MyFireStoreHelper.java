package com.naorfarag.pricetracker.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.naorfarag.pricetracker.lv.model.Product;

import java.util.Map;

public class MyFireStoreHelper {
    private FirebaseFirestore db;
    private static final String uniqueID = FirebaseInstanceId.getInstance().getId();

    @SuppressLint("HardwareIds")
    public MyFireStoreHelper(Context context) {
        db = FirebaseFirestore.getInstance();
    }

    public void addProductToDatabase(Product p) {
        Map<String, Object> newProduct = p.convertToMap();
        // Add a new document with a generated ID
        db.collection(uniqueID).document(p.getAsin()).set(newProduct);
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    public static String getUniqueID() {
        return uniqueID;
    }

    public void updateDocumentAttributeString(String document, String attribute, String newAttrValue) {
        db.collection(uniqueID).document(document).update(attribute, newAttrValue);
    }
    public void updateDocumentAttributeNumber(String document, String attribute, Double newAttrValue) {
        db.collection(uniqueID).document(document).update(attribute, newAttrValue);
    }
}
