package com.example.android.com.testingmap;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.IntentService;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.nfc.Tag;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "A1";
    private static final int ERROR_DIALOG_REQUEST = 1001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String FINE_LOCAION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static boolean isPermissionGranted = false;
    GoogleMap mgoogleMap = null;
    boolean Google_Services_Status = false;
    private static final int REQUEST_CHECK_SETTING = 10045;
    private Location mCurrent_location;
    FusedLocationProviderClient mfusedLocationProviderClient;
    LocationCallback mlocationCallback;
    Boolean mRequesting_updates = false;
    AddressResultReceiver mresultReceiver;
    private static final float DEFAULT_ZOOM = 15f;
    private GeoDataClient mgeoDataClient;
    private LatLngBounds bounds;
    Address address;


    //widget
    AutoCompleteTextView et_search;
    ImageView iv_clear;

//   onCreate of Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et_search = findViewById(R.id.et_search);
        iv_clear = findViewById(R.id.iv_clear);
        iv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
            }
        });
        mresultReceiver = new AddressResultReceiver(new Handler());
        mlocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.e("LocationCallback", "called");
                for (Location location : locationResult.getLocations())
                    Log.e("Location Result", location.toString());
                startIntentService();
            }

        };

    }

//    Initializing AutoComplete Place
    private void init(){

        mgeoDataClient = Places.getGeoDataClient(this,null);

        bounds = new LatLngBounds( new  LatLng(-85, 180),new LatLng(85, -180));

        final PlaceAutoCompleteAdapter autoCompleteAdapter = new PlaceAutoCompleteAdapter(getApplicationContext()
                        ,mgeoDataClient,bounds,null);

        et_search.setAdapter(autoCompleteAdapter);
        et_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String placeId = autoCompleteAdapter.getItem(i).getPlaceId();
                Log.e(TAG,"Place Id :"+placeId);
                Task<PlaceBufferResponse>  responseTasks  = mgeoDataClient.getPlaceById(placeId);
                responseTasks.addOnSuccessListener(new OnSuccessListener<PlaceBufferResponse>() {
                    @Override
                    public void onSuccess(PlaceBufferResponse places) {
                        geoLocate();
                        moveCamera(places.get(0).getLatLng(),DEFAULT_ZOOM,places.get(0).getName().toString());

//                       places.get(0)
                       }
                });

            }
        });
        Log.e(TAG,"init()");
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int action_id, KeyEvent keyEvent) {
                Log.e(TAG,"Editor Action 1");
                if (action_id == EditorInfo.IME_ACTION_DONE || action_id == EditorInfo.IME_ACTION_GO
                        || action_id == EditorInfo.IME_ACTION_SEARCH ||keyEvent.getAction()== KeyEvent.ACTION_DOWN
                        || keyEvent.getAction()==KeyEvent.KEYCODE_ENTER){
                    Log.e(TAG,"Editor Action");
                    geoLocate();

                }
                return false;
            }
        });
    }

    private void geoLocate(){
        Log.e(TAG,"GeoLocate");
        String searchString = et_search.getText().toString();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList = new ArrayList<>();
        try {

            addressList = geocoder.getFromLocationName(searchString,1);
        } catch (IOException e) {
            Log.e(TAG,"IOException Occurs"+e.getMessage());
        }

        if (addressList.size()>0){
            address = addressList.get(0);
            Log.e(TAG,"Address Found"+addressList.get(0).getCountryName()+"code:"+addressList.get(0).getCountryCode()+"\n"+address.toString());
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,searchString);
           }
    }

//   onPause of Activity
    @Override
    protected void onPause() {
        super.onPause();
        Stop_Location_updates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Google_Services_Status = isServicesOk();
        getLocationPermission();
        SettingClient_API();
        getDevice_Current_Location();
        Log.e("STATUS", mRequesting_updates + "");


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            isPermissionGranted = false;
                            Log.e(TAG, "Permission granted failed");
                            return;
                        }
                        Log.e(TAG, "inside for");
                        isPermissionGranted = true;
                    }
                    Log.e(TAG, "CHECK ===" + isPermissionGranted);
                }
                break;
        }
    }

//    Getting Device's Current Location
    public void getDevice_Current_Location() {
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (isPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "getDeviceLocation Permission Check");
                return;
            }
            mfusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.e("Location", "Success");

                    if (location != null) {
                        mCurrent_location = location;
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                             return;
                        }
                        mgoogleMap.setMyLocationEnabled(true);
                        LatLng latLng = new LatLng(mCurrent_location.getLatitude(),mCurrent_location.getLongitude());
                        mgoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
                        moveCamera(latLng,DEFAULT_ZOOM,"My Location");
                        Log.e("Location",location.toString());
                        mRequesting_updates = true;
//                            Start_Location_Updates();

                    }
                }
            });


        }

    }

    public void moveCamera(LatLng latLng,float zoom,String title){
        Log.e(TAG,"Moving the camera to "+"Lat:"+latLng.latitude+",Long "+latLng.longitude);

        mgoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        AddMarker(latLng,title);
    }

//    Start And Stop Location Updates
    public void Start_Location_Updates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        Log.e("Error","Not Enough Permission");
            return;
        }


        mfusedLocationProviderClient.requestLocationUpdates(get_LocationRequest(),mlocationCallback , null);

    }

    public void Stop_Location_updates(){
        mfusedLocationProviderClient.removeLocationUpdates(mlocationCallback);
        mRequesting_updates = false;
    }

//    initializing Map
    public void initMap() {
        if (isPermissionGranted) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mgoogleMap = googleMap;
                    mgoogleMap.getUiSettings().setMapToolbarEnabled(false);

                    mgoogleMap.getUiSettings().setCompassEnabled(true);
                    init();

                    Toast.makeText(MainActivity.this, "Map is Ready", Toast.LENGTH_SHORT).show();

                }
            });

        }else
            Toast.makeText(this, "not Init()" +
                    "", Toast.LENGTH_SHORT).show();
    }

//    Adding Marker to Map
    private void AddMarker(LatLng latLng,String title){
       mgoogleMap.clear();
        MarkerOptions options = new MarkerOptions().position(latLng).title(title).icon(BitmapDescriptorFactory.fromResource(R.mipmap.black_map_pin));
        options.draggable(true);
        if (!title.equals("My Location"))
            mgoogleMap.addMarker(options);
        if (address!=null)
            mgoogleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this,address));

    }

//    Getting Permission for Location
    public void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, FINE_LOCAION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, COARSE_LOCATION)) {
                Log.e(TAG, "should show details");
                isPermissionGranted = false;

            } else
                ActivityCompat.requestPermissions(this, new String[]{COARSE_LOCATION, FINE_LOCAION}, LOCATION_PERMISSION_REQUEST_CODE);

        } else {
            isPermissionGranted = true;
            initMap();
            Log.e(TAG,"All permission Ok");
        }
        }


    public LocationRequest get_LocationRequest(){

            return new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(5000);
        }

//        Setting Api for Map
    public void SettingClient_API(){
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(get_LocationRequest());

            SettingsClient client = LocationServices.getSettingsClient(getApplicationContext());

            Task<LocationSettingsResponse> task =  client.checkLocationSettings(builder.build());

            task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        Log.e(TAG+"===","All settings Satisfied");
                    } catch (ApiException e) {
                        Log.e("Error","ApiException Occured");
                        switch (e.getStatusCode()){

                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                                try {
                                    resolvableApiException.startResolutionForResult(MainActivity.this,REQUEST_CHECK_SETTING);

                                } catch (IntentSender.SendIntentException e1) {
                                    Log.e(TAG,"IntentSender.SendIntentException ");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Log.e(TAG,"can't Fix");
                                break;
                        }
                    }
                }
            });


    }

//    checking is google service of device is working
    public boolean isServicesOk() {

        int isavailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (isavailable == ConnectionResult.SUCCESS) {
            //Result ok
            Log.e(TAG, "Api Availablity Ok");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(isavailable)) {
            Log.e(TAG, "Error But can be Fixed");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, isavailable, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Log.e(TAG, "Error and can't be Fixed");
        }
        return false;
    }

    protected void startIntentService(){
            Intent intent = new Intent(this,FetchAddress_IntentService.class);
            intent.putExtra(Constant.RECEIVER,mresultReceiver);
            intent.putExtra(Constant.LOCATION_DATA_EXTRA,mCurrent_location);
            startService(intent);

    }

    class AddressResultReceiver extends ResultReceiver{

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
           String address = (String) resultData.get(Constant.RESULT_DATA_KEY);
           Log.e("RECEIVER",address);
           if (resultCode ==Constant.SUCCESS_RESULT)
               Toast.makeText(MainActivity.this, "address Found", Toast.LENGTH_SHORT).show();
        }
    }
}

