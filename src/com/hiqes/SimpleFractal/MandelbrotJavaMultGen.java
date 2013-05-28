package com.hiqes.SimpleFractal;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

public class MandelbrotJavaMultGen extends FractalGen {
    private static String          LOG_TAG = "MandelbrotJavaMultGen";
    private static final String    FRACT_GEN_NAME = "Mandelbrot Multi-thread Java Generator";
    private static long            THREAD_KEEP_ALIVE_TIME_MS = 1000;
    private static final double    MIN_X_VAL = -2.5;
    private static final double    MAX_X_VAL = 1.0;
    private static final double    MIN_Y_VAL = -1;
    private static final double    MAX_Y_VAL = 1.0;
    private static final double    MODULO_VAL = 2.0;
    private static final double    STOP_VAL = MODULO_VAL * MODULO_VAL;

    private ThreadPoolExecutor     mThreadPool;
    private int                    mDoneLines;
    private Semaphore              mDoneSem = new Semaphore(1);

    public MandelbrotJavaMultGen(Context context,
                                 int     width,
                                 int     height,
                                 int     iterations,
                                 int[]   palette) {
        super(context, width, height, iterations, palette);

        //  Create a thread pool for parallel operations based on the number
        //  of available processors since the threads should not block on any
        //  of their operations.
        Runtime runtime = Runtime.getRuntime();

        Log.d(LOG_TAG,
              "Creating thread pool with " +
              Integer.toString(runtime.availableProcessors()) +
              " threads.");

        mThreadPool = new ThreadPoolExecutor(runtime.availableProcessors(),
                                             Integer.MAX_VALUE,
                                             THREAD_KEEP_ALIVE_TIME_MS,
                                             TimeUnit.MILLISECONDS,
                                             new LinkedBlockingQueue<Runnable>());
        mThreadPool.prestartAllCoreThreads();

        //  Prep the semphore for blocking
        mDoneSem.drainPermits();
    }

    @Override
    public void finalize() {
        mThreadPool.shutdownNow();
        try {
            super.finalize();
        } catch (Throwable t) {
            Log.e(LOG_TAG, "Caught throwable on finalize: " + t.getMessage());
        }
    }

    @Override
    public int generate(int[] bitmap) {
        boolean done = false;

        //  Queue each line to the thread pool then wait for them all to finish
        Log.d(LOG_TAG, "Creating line gen tasks");
        mDoneLines = 0;
        for (int y = 0; y < mHeight; y++) {
            mThreadPool.execute(new MandelBrotLineGenTask(y, bitmap));
        }

        Log.d(LOG_TAG, "Gen tasks spawed, wait for done");

        while (!done) {
            try {
                mDoneSem.acquire();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG,
                      "Interrupted while waiting for sem: " + e.getMessage());
                break;
            } 

            synchronized(this) {
                if (mDoneLines >= mHeight) {
                    done = true;
                    continue;
                }
            }        
        }

        return 0;
    }

    @Override
    public String getName() {
        return FRACT_GEN_NAME;
    }

    private class MandelBrotLineGenTask implements Runnable {
        private int             mLine;
        private int[]           mBitmap;

        public MandelBrotLineGenTask(int line, int[] bitmap) {
            mLine = line;
            mBitmap = bitmap;
        }

        @Override
        public void run() {
            int                     x;
            int                     y;
            double                  scaledX;
            double                  scaledY;
            double                  orbitX;
            double                  orbitX2;
            double                  orbitY;
            double                  orbitY2;
            double                  tempX;
            int                     i;

            //  Perform a simple loop over all pixels in the line
            double xScaler = (MAX_X_VAL - MIN_X_VAL) / (double)mWidth;
            double yScaler = (MAX_Y_VAL - MIN_Y_VAL) / (double)mHeight;

            y = mLine;
            scaledY = MIN_Y_VAL + ((double)y * yScaler);

            for (x = 0; x < mWidth; x++) {
                scaledX = MIN_X_VAL + ((double)x * xScaler);

                orbitX = 0;
                orbitY = 0;
                orbitX2 = 0;
                orbitY2 = 0;
                for (i = 0; (i < mIter) && ((orbitX2 + orbitY2) < STOP_VAL); i++) {
                    tempX = orbitX2 - orbitY2 + scaledX;
                    orbitY = MODULO_VAL * orbitX * orbitY + scaledY;
                    orbitX = tempX;

                    orbitX2 = orbitX * orbitX;
                    orbitY2 = orbitY * orbitY;
                }

                //  The iteration value is what we use to generate the correct color
                int paletteIndex = (i * (mPalette.length - 1)) / mIter;
                if (paletteIndex < 0) {
                    paletteIndex = 0;
                }

                mBitmap[(y * mWidth) + x] = mPalette[paletteIndex];
            }

            //  Safely add this line to the list
            synchronized(MandelbrotJavaMultGen.this) {
                mDoneLines++;
                mDoneSem.release();
            }
        }
    }
}
