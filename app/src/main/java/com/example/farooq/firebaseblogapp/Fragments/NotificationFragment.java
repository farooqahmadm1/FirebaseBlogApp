package com.example.farooq.firebaseblogapp.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.farooq.firebaseblogapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {


    private static Fragment instance= null;

    public static Fragment getInstance(){
        if (instance==null){
            instance= new NotificationFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

}
