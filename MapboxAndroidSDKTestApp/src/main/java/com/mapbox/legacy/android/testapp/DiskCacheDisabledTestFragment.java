package com.mapbox.legacy.android.testapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mapbox.legacy.geometry.LatLng;
import com.mapbox.legacy.views.MapView;

public class DiskCacheDisabledTestFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MapView.setDebugMode(true);

        View view = inflater.inflate(R.layout.fragment_diskcachedisabled, container, false);

        // Setup Map
        MapView mapView = (MapView) view.findViewById(R.id.diskCacheDisableMapView);
        mapView.getTileProvider().setDiskCacheEnabled(false);
        mapView.setCenter(new LatLng(-22.95903, -43.17970));
        mapView.setZoom(14);

        return view;
    }
}
