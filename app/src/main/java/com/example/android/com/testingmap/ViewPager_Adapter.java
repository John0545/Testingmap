package com.example.android.com.testingmap;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by ravi on 17/3/18.
 */

public class ViewPager_Adapter extends PagerAdapter {

    private final Context context;
    int wel_layout_ResID[]={R.layout.wel_slide_1,R.layout.wel_slide_2,R.layout.wel_slide_3};

   public ViewPager_Adapter(Context context){
       this.context = context;
   }
   @Override
    public int getCount() {
        return wel_layout_ResID.length;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.e("PAGER_ADAPTER","instantiateItem");

        View view = LayoutInflater.from(context).inflate(wel_layout_ResID[position],container,false);

        container.addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
       Boolean result = view==(RelativeLayout)object;
        Log.e("PAGER_ADAPTER","isViewFromObject"+result);

        return result;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.e("PAGER_ADAPTER","destroyItem");

        container.removeView((RelativeLayout)object);
    }
}
