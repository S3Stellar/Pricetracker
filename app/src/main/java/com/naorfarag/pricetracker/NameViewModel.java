package com.naorfarag.pricetracker;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NameViewModel extends ViewModel {

    // Create a LiveData with a String
    private MutableLiveData<Integer> currentName;

    public MutableLiveData<Integer> getCurrentName() {
        if (currentName == null) {
            currentName = new MutableLiveData<Integer>();
        }
        return currentName;
    }

}