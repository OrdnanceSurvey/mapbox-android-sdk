package com.mapbox.legacy.views;

import android.os.Handler;
import android.view.ScaleGestureDetector;

/**
 * https://developer.android.com/training/gestures/scale.html
 * A custom gesture detector that processes gesture events and dispatches them
 * to the map's overlay system.
 */
public class MapViewScaleGestureDetectorListener implements ScaleGestureDetector.OnScaleGestureListener {

    private static String TAG = "MapViewScaleListener";
    private static final int SCALE_END_DELAY_MS = 50;

    /**
     * When zooming, how often we should update the map's internal zoom level state.
     * This allows loading of tiles for a different zoom level, rather than merely scaling
     * what has already been drawn.
     */
    private static final int ZOOM_LEVEL_UPDATE_DELAY_MS = 300;

    /**
     * Time since the map view's internal zoom levels were last updated.
     */
    private long previousTimeMillis = 0;

    /**
     * Delay value to wait until we reset map animation state - this is to allow the map
     * to have time to make tile requests when switching internal zoom levels in onScale()
     */
    private static final int RESET_MAP_ANIMATION_STATE_DELAY_MS = 50;

    /**
     * This is the active focal point in terms of the viewport. Could be a local
     * variable but kept here to minimize per-frame allocations.
     */
    private float lastFocusX;
    private float lastFocusY;
    private float firstSpan;
    private final MapView mapView;
    private boolean scaling;
    private float currentScale;

    /**
     * Bind a new gesture detector to a map
     *
     * @param mv a map view
     */
    public MapViewScaleGestureDetectorListener(final MapView mv) {
        this.mapView = mv;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        lastFocusX = detector.getFocusX();
        lastFocusY = detector.getFocusY();
        firstSpan = detector.getCurrentSpan();
        currentScale = 1.0f;
        if (!this.mapView.isAnimating()) {
            this.mapView.setIsAnimating(true);
            this.mapView.getController().aboutToStartAnimation(lastFocusX, lastFocusY);
        }
        scaling = true;

        previousTimeMillis = System.currentTimeMillis();
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (!scaling) {
            return true;
        }

        currentScale = detector.getCurrentSpan() / firstSpan;
        long deltaTime = System.currentTimeMillis() - previousTimeMillis;
        float focusX = detector.getFocusX();
        float focusY = detector.getFocusY();

        if (deltaTime > ZOOM_LEVEL_UPDATE_DELAY_MS) {
            // This rather nasty bit of code is responsible for updating the map tile zoom level,
            // enabling async tile loading when the map changes zoom level during a pinch.
            float preZoom = mapView.getZoomLevel(false);
            float newZoom = (float) (Math.log(currentScale) / Math.log(2d) + preZoom);

            // Calling these sets (through the magic of side-effects) the state of the mapView to
            // match the newly calculated zoom level and kicks off tile loading.
            mapView.setAnimatedZoom(newZoom);
            mapView.getController().onAnimationEnd();
            mapView.getController().aboutToStartAnimation(lastFocusX, lastFocusY);

            // Dirty hack to give the mapView enough time to request tiles before we reset its
            // internal state fully. Not doing this kills all tile requests before they have a
            // chance to start.
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mapView.setIsAnimating(true);
                }
            }, RESET_MAP_ANIMATION_STATE_DELAY_MS);

            // Reset scaling & position state for next time round - needed for zoom calculations.
            firstSpan = detector.getCurrentSpan();
            currentScale = 1.0f;

            previousTimeMillis = System.currentTimeMillis();
        } else {
            // We aren't redrawing tiles for an updated zoom level - simple scaling will suffice.
            this.mapView.setScale(currentScale);
        }

        // Panning can happen independently of scaling and updating map zoom level.
        this.mapView.getController().offsetDeltaScroll(lastFocusX - focusX, lastFocusY - focusY);
        this.mapView.getController().panBy(lastFocusX - focusX, lastFocusY - focusY, true);
        lastFocusX = focusX;
        lastFocusY = focusY;

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        if (!scaling) {
            return;
        }

        //delaying the "end" will prevent some crazy scroll events when finishing
        //scaling by getting 2 fingers very close to each other
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                float preZoom = mapView.getZoomLevel(false);
                float newZoom = (float) (Math.log(currentScale) / Math.log(2d) + preZoom);
                //set animated zoom so that animationEnd will correctly set it in the mapView
                mapView.setAnimatedZoom(newZoom);
                mapView.getController().onAnimationEnd();
                mapView.setIsAnimating(false);
                scaling = false;
            }
        }, SCALE_END_DELAY_MS);

    }
}
