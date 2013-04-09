package com.hiq.SimpleFractal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

public class FractalSurface extends SurfaceView implements SurfaceHolder.Callback {
    public static final int     DEFAULT_ITERATIONS = 200;

    private boolean             mSurfaceReady = false;
    private FractalGen          mGen = null;
    private int[]               mImage = null;
    private FractalPalette      mPalette;

    private void init() {
        getHolder().addCallback(this);

        //  Create our palette so the generator has something to work with
        //mPalette = new SeaPalette(Bitmap.Config.ARGB_8888);
        mPalette = new PriSecPalette(Bitmap.Config.ARGB_8888);
    }

    public FractalSurface(Context context) {
        super(context);
        init();
    }

    public FractalSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FractalSurface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void shutdown() {
        getHolder().removeCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //  We receive this call when the surface is created or if there is
        //  a change which has occurred.  Either create a generator or revise the one
        //  we already have, then make it draw.
        if (mGen == null) {
            mGen = new MandelbrotJavaGen(width,
                                         height,
                                         DEFAULT_ITERATIONS,
                                         mPalette.getPalette());
            mImage = new int[width * height];
        } else {
            mGen.setDimensions(width, height);
            if ((width * height) > mImage.length) {
                mImage = new int[width * height];
            }
        }

        mGen.generate(mImage);
        Canvas can = holder.lockCanvas();
        can.drawBitmap(mImage, 0, width, 0, 0, width, height, false, null);
        holder.unlockCanvasAndPost(can);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceReady = false;
    }
}

