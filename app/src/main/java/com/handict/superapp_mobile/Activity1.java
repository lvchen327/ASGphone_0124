package com.handict.superapp_mobile;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by LC on 2017/10/12.
 */

public class Activity1 extends Activity {
//    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        textView= (TextView) findViewById(R.id.t);
//        Log.d("1111", "StartActivity1: ----->  2");
//        textView.setText(getIntent().getStringExtra("text"));
//        Log.d("1111", "StartActivity1: ----->  3");
    }
}
