package com.example.laptop.rideuser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Laptop on 12/27/2017.
 */

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter {

    View myview;

    public CustomInfoWindow(Context context) {
       myview = LayoutInflater.from(context)
               .inflate(R.layout.custom_rider_nfo_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView txtpickuptitle = ((TextView)myview.findViewById(R.id.txtPickup));
        txtpickuptitle.setText(marker.getTitle());

        TextView txtpickupsnippet = ((TextView)myview.findViewById(R.id.pickupSnippet));
        txtpickupsnippet.setText(marker.getSnippet());

        return myview;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
