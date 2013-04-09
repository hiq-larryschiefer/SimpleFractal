package com.hiq.SimpleFractal;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

public class FractalSurface extends SurfaceView implements SurfaceHolder.Callback {
    private static final String LOG_TAG = "FractalSurface";

    public static final int     DEFAULT_ITERATIONS = 200;

    private Context             mContext;
    private boolean             mSurfaceReady = false;
    private FractalGen          mGen = null;
    private int[]               mImage = null;
    private FractalPalette      mPalette;
    private int                 mWidth = 0;
    private int                 mHeight = 0;
    private AlertDialog.Builder mBuilder;
    private AlertDialog         mDialog;

    private void init(Context context) {
        getHolder().addCallback(this);

        //  Create our palette so the generator has something to work with
        //mPalette = new SeaPalette(Bitmap.Config.ARGB_8888);
        mPalette = new PriSecPalette(Bitmap.Config.ARGB_8888);
        mContext = context;
        mBuilder = new AlertDialog.Builder(context);
        mBuilder.setMessage(R.string.generating);
    }

    public FractalSurface(Context context) {
        super(context);
        init(context);
    }

    public FractalSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FractalSurface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void shutdown() {
        getHolder().removeCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        synchronized (this) {
            mSurfaceReady = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        boolean                 needGen = false;

        //  We receive this call when the surface is created or if there is
        //  a change which has occurred.  Either create a generator or revise the one
        //  we already have, then make it draw.
        if (mGen == null) {
            mGen = new MandelbrotJavaGen(width,
                                         height,
                                         DEFAULT_ITERATIONS,
                                         mPalette.getPalette());
            mImage = new int[width * height];
            mWidth = width;
            mHeight = height;
            needGen = true;
        } else {
            if ((mWidth != width) || (mHeight != height)) {
                mGen.setDimensions(width, height);
                if ((width * height) > mImage.length) {
                    mImage = new int[width * height];
                }

                needGen = true;
            }
        }

        mDialog = mBuilder.create();
        mDialog.show();
        Log.d(LOG_TAG, "Kicking off generation");
        new GeneratorTask().execute(needGen);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (this) {
            mSurfaceReady = false;
        }
    }

    private class GeneratorTask extends AsyncTask<Boolean, Integer, Void> {
        protected Void doInBackground(Boolean... doGen) {
            if (doGen[0]) {
                mGen.generate(mImage);
            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            //  No progress indicator, maybe later
        }

        protected void onPostExecute(Void noResult) {
            Log.d(LOG_TAG, "Generation done");
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }

            synchronized (FractalSurface.this) {
                if (mSurfaceReady) {
                    Log.d(LOG_TAG, "Rendering...");
                    SurfaceHolder holder = getHolder();
                    Canvas can = holder.lockCanvas();
                    can.drawBitmap(mImage, 0, mWidth, 0, 0, mWidth, mHeight, false, null);
                    holder.unlockCanvasAndPost(can);
                    Log.d(LOG_TAG, "Rendering done.");
                }
            }
        }
    }
}

