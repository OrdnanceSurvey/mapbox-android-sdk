package com.mapbox.legacy.clustering;


import com.mapbox.legacy.geometry.LatLng;

import java.util.Collection;

/**
 * A collection of ClusterItems that are nearby each other.
 */
public interface Cluster<T extends ClusterItem> {
    public LatLng getPosition();

    Collection<T> getItems();

    int getSize();
}