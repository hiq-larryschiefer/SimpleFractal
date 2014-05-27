/*
 * Copyright (c) 2013, HiQES LLC
 * ALL RIGHTS RESERVED
 *
 * http://www.hiqes.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 */
package com.hiqes.SimpleFractal;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TimingLogger;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

public class FractalSurface extends SurfaceView implements SurfaceHolder.Callback {
    private static final String LOG_TAG = "FractalSurface";

    public static final int     DEFAULT_ITERATIONS = 200;
    public static final int     GEN_MAND_JAVA = 0;
    public static final int     GEN_MAND_JAVA_MULT = 1;
    public static final int     GEN_MAND_RS = 2;
    public static final int     GEN_MAND_NATIVE = 3;
    public static final int     GEN_MAND_NATIVE_MULT = 4;
    public static final int     GEN_COUNT = 5;

    private Context             mContext;
    private boolean             mSurfaceReady = false;
    private FractalGen          mGen = null;
    private FractalGen[]        mGenerators = new FractalGen[GEN_COUNT];
    private int                 mCurGen = GEN_MAND_JAVA;
    private int[]               mImage = null;
    private FractalPalette      mPalette;
    private int                 mWidth = 0;
    private int                 mHeight = 0;
    private AlertDialog.Builder mBuilder;
    private AlertDialog         mDialog;
    private ArrayList<GeneratorTask> mRunningTasks = new ArrayList<GeneratorTask>();

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
        cancelRunningTasks();
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
        //  a change which has occurred.  See if we need to create or resize our
        //  image buffer then kick off a generation.
        if (mGen == null) {
            mImage = new int[width * height];
            mWidth = width;
            mHeight = height;
            needGen = true;
        } else {
            if ((mWidth != width) || (mHeight != height)) {
                if ((width * height) > mImage.length) {
                    mImage = new int[width * height];
                }

                needGen = true;
            }
        }

        doGeneration(needGen);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (this) {
            mSurfaceReady = false;
            cancelRunningTasks();
        }
    }

    private FractalGen createGenerator(int gen) {
        FractalGen              newGen = null;

        switch (gen) {
            case GEN_MAND_JAVA:
                newGen = new MandelbrotJavaGen(mContext,
                                               mWidth,
                                               mHeight,
                                               DEFAULT_ITERATIONS,
                                               mPalette.getPalette());
                break;

            case GEN_MAND_JAVA_MULT:
                newGen = new MandelbrotJavaMultGen(mContext,
                                                   mWidth,
                                                   mHeight,
                                                   DEFAULT_ITERATIONS,
                                                   mPalette.getPalette());
                break;

            case GEN_MAND_RS:
                newGen = new MandelbrotRSGen(mContext,
                                             mWidth,
                                             mHeight,
                                             DEFAULT_ITERATIONS,
                                             mPalette.getPalette());
                break;

            case GEN_MAND_NATIVE:
                newGen = new MandelbrotNativeGen(mContext,
                                                 false,
                                                 mWidth,
                                                 mHeight,
                                                 DEFAULT_ITERATIONS,
                                                 mPalette.getPalette());
                break;

            case GEN_MAND_NATIVE_MULT:
                newGen = new MandelbrotNativeGen(mContext,
                                                 true,
                                                 mWidth,
                                                 mHeight,
                                                 DEFAULT_ITERATIONS,
                                                 mPalette.getPalette());
                break;

            default:
                Log.e(LOG_TAG,
                      "Invalid gen type provided: " + Integer.toString(gen));
                break;
        }

        return newGen;
    }

    private void doGeneration(boolean needGen) {
        if (mGen == null) {
            if (mGenerators[mCurGen] == null) {
                mGenerators[mCurGen] = createGenerator(mCurGen);
            }

            mGen = mGenerators[mCurGen];
        }

        Toast.makeText(mContext,
                       "Generating with: " + mGen.getName(),
                       Toast.LENGTH_SHORT).show();
        mDialog = mBuilder.create();
        mDialog.show();

        Log.d(LOG_TAG, "Kicking off generation");
        GeneratorTask task = new GeneratorTask();
        mRunningTasks.add(task);
        task.execute(needGen);
    }

    private void cancelRunningTasks() {
        for (Iterator<GeneratorTask> iter = mRunningTasks.iterator();
             iter.hasNext();
             /*  pulls in the loop */) {
            GeneratorTask curTask = iter.next();
            iter.remove();
            curTask.cancel(true);
        }
    }
    public void switchGenerator() {
        //  Cancel any execution already in progress
        cancelRunningTasks();

        //  Switch generator and re-generate
        mCurGen++;
        mGen = null;
        if (mCurGen >= GEN_COUNT) {
            mCurGen = GEN_MAND_JAVA;
        }

        //  Blank the screen on switch
        for (int i = 0; i < mImage.length; i++) {
            mImage[i] = Color.argb(255, 0, 0, 0);
        }

        applyImage();
        doGeneration(true);
    }

    private void applyImage() {
        synchronized (FractalSurface.this) {
            if (mSurfaceReady) {
                TimingLogger timings;

                timings =
                        new TimingLogger(LOG_TAG,
                                this.getClass().getSimpleName());

                SurfaceHolder holder = getHolder();
                Canvas can = holder.lockCanvas();
                timings.addSplit("locked canvas");
                can.drawBitmap(mImage, 0, mWidth, 0, 0, mWidth, mHeight, false, null);
                timings.addSplit("image drawn");
                holder.unlockCanvasAndPost(can);
                timings.addSplit("unlocked and posted");
                timings.dumpToLog();
            }
        }
    }

    private class GeneratorTask extends AsyncTask<Boolean, Integer, Void> {
        long         mStartTime;
        long         mEndTime;

        protected Void doInBackground(Boolean... doGen) {
            TimingLogger timings;

            //  Do timing with both the logger and our own tracking
            timings = new TimingLogger(LOG_TAG,
                                       this.getClass().getSimpleName());
            if (doGen[0]) {
                mStartTime = SystemClock.elapsedRealtime();

                mGen.setDimensions(mWidth, mHeight);
                mGen.generate(mImage);

                mEndTime = SystemClock.elapsedRealtime();

                timings.addSplit("GENERATED");
                timings.dumpToLog();
            }

            timings = null;
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

            Toast.makeText(mContext,
                           ("Generation done in " +
                            Long.toString(mEndTime - mStartTime) +
                            "ms"),
                           Toast.LENGTH_SHORT).show();
            applyImage();
        }
    }
}

