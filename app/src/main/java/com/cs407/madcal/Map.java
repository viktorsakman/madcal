package com.cs407.madcal;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Map extends Fragment {

    private static final String ARG_WISC_ID = "wisc_id";
    private String wiscId;

    public Map() {
        // Required empty public constructor
    }

    public static Map newInstance(String wiscId) {
        Map fragment = new Map();
        Bundle args = new Bundle();
        args.putString(ARG_WISC_ID, wiscId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            wiscId = getArguments().getString(ARG_WISC_ID);
        }
        // Here you can use wiscId as needed
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }
}
