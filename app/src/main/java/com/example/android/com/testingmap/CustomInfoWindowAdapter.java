package com.example.android.com.testingmap;

import android.app.Application;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by ravi on 14/3/18.
 */

public class CustomInfoWindowAdapter implements InfoWindowAdapter {
    private  Address address;
    private  Context context=null;
    CustomInfoWindowAdapter(Context context,Address address){

        this.address = address;
        this.context = context;
    }
    @Override
    public View getInfoWindow(Marker marker) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_window_info,null);
        getWindow(view,marker);

        return view;
    }

    private void getWindow(View view ,Marker marker){
        ImageView flag = view.findViewById(R.id.flag);
        TextView title = view.findViewById(R.id.title);
        TextView snippet = view.findViewById(R.id.snippet);
        flag.setImageResource(Country_Flags.getCountry_Flag(address.getCountryCode()));
        title.setText(marker.getTitle());
        snippet.setText(address.getCountryName());

    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
