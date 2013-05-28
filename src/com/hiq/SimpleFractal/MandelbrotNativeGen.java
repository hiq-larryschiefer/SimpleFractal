package com.hiq.SimpleFractal;

import android.content.Context;

public class MandelbrotNativeGen extends FractalGen
{
    private static final String    FRACT_GEN_NAME = "Mandelbrot Native Generator";
    private static final String    FRACT_GEN_MULTI_NAME = "Mandelbrot Native Multi-thread Generator";

    private boolean             mMulti = false;

    static {
        //  Load our native shared library when we are first created
        System.loadLibrary("mand_gen_native");
    }

    public MandelbrotNativeGen(Context context,
                               boolean multi,
                               int width,
                               int height,
                               int iterations,
                               int[] palette) {
        super(context, width, height, iterations, palette);
        mMulti = multi;
    }

    @Override
    public int generate(int[] bitmap) {
        int                     threads;

        if (mMulti) {
            threads = Runtime.getRuntime().availableProcessors();
        } else {
            threads = 1;
        }

        return generate(threads,
                        mWidth,
                        mHeight,
                        mIter,
                        bitmap,
                        mPalette);
    }

    @Override
    public String getName() {
        if (mMulti) {
            return FRACT_GEN_MULTI_NAME;
        } else {
            return FRACT_GEN_NAME;
        }
    }

    public native int generate(int threads,
                               int width,
                               int height,
                               int iter,
                               int[] bitmap,
                               int[] palette);
}
