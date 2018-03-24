package com.example.android.com.testingmap;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by ravi on 10/3/18.
 */

public class PlaceAutoCompleteAdapter  extends ArrayAdapter<AutocompletePrediction>
                        implements Filterable{
private static final String TAG = "PlaceAutoComplete";
private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

private GeoDataClient mgeoDataClient;
private AutocompleteFilter mautocompleteFilter;
private LatLngBounds mlatLngBounds;
ArrayList<AutocompletePrediction> resultList;


private ArrayList<AutocompletePrediction> mresults = new ArrayList<>();
    public PlaceAutoCompleteAdapter(@NonNull Context context, GeoDataClient geoDataClient,
                        LatLngBounds latLngBounds, AutocompleteFilter autocompleteFilter) {

        super(context, R.layout.row_layout,R.id.tv_1);
        this.mautocompleteFilter = autocompleteFilter;
        this.mgeoDataClient = geoDataClient;
        this.mlatLngBounds = latLngBounds;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row =  super.getView(position, convertView, parent);

        AutocompletePrediction item = getItem(position);
        TextView textView1 = row.findViewById(R.id.tv_1);
        TextView textView2 = row.findViewById(R.id.tv_2);
//        View view = row.findViewById
        assert item != null;
        textView1.setText(item.getPrimaryText(STYLE_BOLD));
        textView2.setText(item.getSecondaryText(STYLE_BOLD));

        return row;
    }

    @NonNull
    @Override
    public Context getContext() {
        return super.getContext();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraints) {
                FilterResults results = new FilterResults();
                ArrayList<AutocompletePrediction> result = new ArrayList<>();
                if (constraints!= null){
                    result = getAutoComplete(constraints);
                }
                results.values = result;
                if (result !=null)
                    results.count = result.size();
                else results.count = 0;
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults!=null)
                    mresults = (ArrayList<AutocompletePrediction>) filterResults.values;
                else notifyDataSetInvalidated();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof AutocompletePrediction)
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                else return super.convertResultToString(resultValue);
            }
        };


    }

    public void setBounds(LatLngBounds bounds){
        mlatLngBounds = bounds;
    }

    @Override
    public int getCount() {
        return mresults.size();
    }

    @Override
    public AutocompletePrediction getItem(int position) {
        return mresults.get(position);
    }

    private ArrayList<AutocompletePrediction> getAutoComplete(CharSequence constraints){

        Log.e(TAG,"starting Autocomplete query for "+constraints);
        Task<AutocompletePredictionBufferResponse> results =
        mgeoDataClient.getAutocompletePredictions(constraints.toString(),mlatLngBounds,mautocompleteFilter);
        AutocompletePredictionBufferResponse bufferResponse=null;
        try {
            Tasks.await(results,60, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
           Log.e(TAG,"ExecutionException");
        } catch (InterruptedException e) {
            Log.e(TAG,"InterruptedException");
        } catch (TimeoutException e) {
            Log.e(TAG,"TimeoutException");
        }

        try{

            bufferResponse = results.getResult();
            Log.e(TAG,"Querry Completed. Received "+bufferResponse.getCount()+" predictions.");

            return DataBufferUtils.freezeAndClose(bufferResponse);
        }catch (RuntimeException e){
            Log.e(TAG,"Error Connecting API : "+e.toString());
            Toast.makeText(getContext(), "Error Connecting API", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
