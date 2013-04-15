package com.hiq.SimpleFractal;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.Window;

public class MainActivity extends Activity {
    private FractalSurface      mFractSurf;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //  Get rid of our title bar so we have the maximum real estate possible
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mFractSurf = (FractalSurface)findViewById(R.id.fractSurface);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
