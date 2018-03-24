package com.example.android.com.testingmap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by ravi on 3/3/18.
 */

public class Welcome_Screen extends AppCompatActivity {
    ViewPager viewPager;
    LinearLayout dot;
    Button skip,done;
    ImageButton next;
    static final int LAST_PAGE=2;
    SharedPreferences mpreference;
    Boolean launcherTest = false;
    private static final String LAUNCH_TEST = "LaunchTest";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mpreference = PreferenceManager.getDefaultSharedPreferences(this);
        if (!mpreference.contains(LAUNCH_TEST)){
            Log.e("TAG","not Contains");
            mpreference.edit().putBoolean(LAUNCH_TEST,true).apply();
        }

        else if (!mpreference.getBoolean(LAUNCH_TEST,false))
        {
            launchHomeScreen();
        }
        setContentView(R.layout.welcome_screen);
        viewPager = findViewById(R.id.mview_pager);
        dot = findViewById(R.id.ll_dot);
        skip = findViewById(R.id.btn_skip);
        done = findViewById(R.id.btn_Done);
        next = findViewById(R.id.btn_next);

        mpreference.edit().putBoolean("LaunchTest",false).apply();


        Window window = this.getWindow();

        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {



            }

            @Override
            public void onPageSelected(int position) {

                Log.e("===",position+"");
                setDot(dot,viewPager.getAdapter().getCount(),position);
                if (position!=0)
                    skip.setVisibility(View.VISIBLE);
                else skip.setVisibility(View.INVISIBLE);

                if (position==LAST_PAGE) {
                    next.setVisibility(View.INVISIBLE);
                    done.setVisibility(View.VISIBLE);
                }  else {
                    next.setVisibility(View.VISIBLE);
                    done.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setAdapter(new ViewPager_Adapter(this));
        setDot(dot,viewPager.getAdapter().getCount(),0);

    }
    private void setDot(ViewGroup viewGroup ,int count,int position){
        viewGroup.removeAllViews();
        TextView dot[] = new TextView[count];

        for (int i=0;i<count;i++){
            dot[i]= new TextView(this);
            dot[i].setText(Html.fromHtml("&#8226;"));
            dot[i].setTextSize(50);
            dot[i].setTextColor(Color.BLACK);
            viewGroup.addView(dot[i]);
        }
        dot[position].setTextColor(Color.WHITE);
    }
public void onClick(View view) {
    switch (view.getId()){

        case R.id.btn_Done:
        launchHomeScreen();
            break;
        case R.id.btn_next:
            viewPager.arrowScroll(View.FOCUS_RIGHT);
            break;
        case R.id.btn_skip:
            launchHomeScreen();
            break;
    }

}

private void launchHomeScreen(){
mpreference.edit().putBoolean(LAUNCH_TEST,false).apply();
    startActivity(new Intent(this,MainActivity.class));
    finish();

}
}
