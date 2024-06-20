package com.cs407.madcal;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.gms.maps.model.Marker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private PlacesClient placesClient;
    private String wiscId;
    private static final String TAG = "MapFragment";
    private DatabaseHelper db;
    Random random;

    private List<Marker> markerList;
    private List<Integer> pinIdList;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Retrieve the WISC ID from the fragment's arguments
        if (getArguments() != null) {
            wiscId = getArguments().getString("WISC_ID");
        }

        db = new DatabaseHelper(getContext());
        markerList = new ArrayList<>();
        pinIdList = new ArrayList<>();
        random = new Random();

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), "AIzaSyDlyJg8OpAM2n5HLmuxH4pOfH_LUpMXbmc");
        }
        placesClient = Places.createClient(getContext());

        AutocompleteSupportFragment autocompleteFragment = (((AutocompleteSupportFragment) getChildFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment))
                .setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                LatLng location = place.getLatLng();
                if (location != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                    new AlertDialog.Builder(getActivity())
                            .setMessage("Would you like to add a class to this location?")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(location)
                                            .title(place.getName()));
                                    promptForInfo(marker);
                                }
                            })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            }
            @Override
            public void onError(@NonNull Status status) {Log.e(TAG, "An error occurred: " + status);}
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    private void promptForInfo(final Marker marker) {
        promptSingleInput("Enter Class Name", "", className -> {
            promptSingleInput("Enter Class Days", "", classDays -> {
                promptSingleInput("Enter Class Room #", "", classRoom -> {
                    LatLng position = marker.getPosition();
                    int pinId = db.addPin(position.latitude, position.longitude, className, classDays, classRoom, wiscId);
                    marker.setTitle(className);
                    markerList.add(marker);
                    pinIdList.add(pinId);
                    marker.showInfoWindow();
                });
            });
        });
    }

    private void promptSingleInput(String title, String initialValue, Consumer<String> onInput) {
        final EditText input = new EditText(getContext());
        input.setText(initialValue);

        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setCancelable(false)
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String inputText = input.getText().toString();
                    onInput.accept(inputText);
                })
                .show();
    }

    private void editPin(Marker marker, int pinId, DatabaseHelper.PinData pinData) {
        promptSingleInput("Edit Class Name", pinData.className, className -> {
            promptSingleInput("Edit Class Days", pinData.classDays, classDays -> {
                promptSingleInput("Edit Class Room #", pinData.classRoom, classRoom -> {
                    LatLng position = marker.getPosition();
                    db.updatePin(pinId, position.latitude, position.longitude, className, classDays, classRoom);
                    marker.setTitle(className);
                    marker.showInfoWindow();
                });
            });
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permission
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        LatLng initialLocation = new LatLng(43.074527, -89.4052735);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15));

        loadPinsFromDatabase();

        mMap.setOnMarkerClickListener(marker -> {
            showMarkerInfo(marker);
            marker.showInfoWindow();
            return false;
        });

        mMap.setOnMapClickListener(latLng -> {
            new AlertDialog.Builder(getActivity())
                    .setMessage("Would you like to add a class to this location?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("New Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(getRandomColor())));
                            promptForInfo(marker);
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, enable the location layer
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // Permission was denied
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showMarkerInfo(Marker marker) {
        int index = markerList.indexOf(marker);
        if (index != -1) {
            int pinId = pinIdList.get(index);
            DatabaseHelper.PinData pinData = db.getPinById(pinId);

            if (pinData != null) {
                String info = "Class Name: " + pinData.className + "\nDays: " + pinData.classDays + "\nRoom: " + pinData.classRoom;

                new AlertDialog.Builder(getActivity())
                        .setMessage(info)
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage("Are you sure you want to delete?")
                                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                db.deletePin(pinId);
                                                marker.remove();
                                                markerList.remove(index);
                                                pinIdList.remove(index);
                                            }
                                        })
                                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }
                        })
                        .setNeutralButton("DISMISS", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("EDIT", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                editPin(marker, pinId, pinData);
                            }
                        }).show();
            }
        }
    }
    private void loadPinsFromDatabase() {
        DatabaseHelper db = new DatabaseHelper(getContext());
        List<DatabaseHelper.PinData> pins = db.getPinsByWiscId(wiscId);
        for (DatabaseHelper.PinData pin : pins) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pin.latitude, pin.longitude))
                    .title(pin.className)
                    .icon(BitmapDescriptorFactory.defaultMarker(getRandomColor())));
            markerList.add(marker);
            pinIdList.add(pin.id);
        }
    }

    private float getRandomColor() {
        return random.nextFloat() * 360;
    }
}
