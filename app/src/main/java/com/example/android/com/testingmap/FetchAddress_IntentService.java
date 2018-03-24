package com.example.android.com.testingmap;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by ravi on 6/3/18.
 */

public class FetchAddress_IntentService extends IntentService {

    private ResultReceiver mresultReceiver;
    static final String TAG = "Intent Service";
    public FetchAddress_IntentService() {
        super("Fetch Address intent");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String error_message = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        assert intent != null;
        Location location = intent.getParcelableExtra(Constant.LOCATION_DATA_EXTRA);
        mresultReceiver = intent.getParcelableExtra(Constant.RECEIVER);

        List<Address> addressList  = null;

        try {
            addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
        } catch (IOException e) {
            error_message = getString(R.string.service_not_Avaliable);
            Log.e(TAG,error_message,e);
        }catch (IllegalArgumentException e){
            error_message = getString(R.string.Invalid_Lat_Long_used);
            Log.e(TAG,error_message+"."+"Latitude"+location.getLatitude()+", Longitude"+location.getLongitude(),e);
        }

        if (addressList==null||addressList.size() ==0){
            if (error_message.isEmpty()){
                error_message = getString(R.string.No_Address_Found);
                Log.e(TAG,error_message);
            }
            DeleiverResult_to_Receiver(Constant.FAILURE_RESULT,error_message);
        }else {
            Address address = addressList.get(0);
            ArrayList<String> addressFragment = new ArrayList<>();

            for (int i=0;i<=address.getMaxAddressLineIndex();i++){
                addressFragment.add(address.getAddressLine(i));
            }
            Log.e(TAG,"Address Found");
            DeleiverResult_to_Receiver(Constant.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),addressFragment));

        }
    }


    private void DeleiverResult_to_Receiver(int ResultCode,String message){
        Bundle bundle = new Bundle();
        bundle.putString(Constant.RESULT_DATA_KEY,message);
        mresultReceiver.send(ResultCode,bundle);
    }
}
