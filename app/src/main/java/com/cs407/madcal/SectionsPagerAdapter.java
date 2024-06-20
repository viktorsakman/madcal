package com.cs407.madcal;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class SectionsPagerAdapter extends FragmentStateAdapter {

    private String wiscId; // Add this variable to store the WISC ID

    public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity, String wiscId) {
        super(fragmentActivity);
        this.wiscId = wiscId; // Assign the WISC ID
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new CalendarFragment();
                break;
            case 1:
                fragment = new TodoFragment();
                break;
            case 2:
                fragment = new MapFragment();
                break;
            case 3:
                fragment = new ScheduleFragment();
                break;
            default:
                fragment = new TodoFragment();
                break;
        }

        // Create a bundle to pass the WISC ID to the fragment
        Bundle bundle = new Bundle();
        bundle.putString("WISC_ID", wiscId);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 4;  // since you have 4 tabs
    }
}
