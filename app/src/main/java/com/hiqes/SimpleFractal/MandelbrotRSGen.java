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

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Type;
import android.util.Log;

import com.hiqes.SimpleFractal.mand_gen.ScriptC_mand_gen;

public class MandelbrotRSGen extends FractalGen {
    private static final String LOG_TAG = MandelbrotRSGen.class.getSimpleName();
    private static final String FRACT_GEN_NAME = "Mandelbrot Renderscript Generator";

    RenderScript                mRSCtx;
    ScriptC_mand_gen            mMandGen;

    public MandelbrotRSGen(Context context, int width, int height, int iterations, int[] palette) {
        super(context, width, height, iterations, palette);

        //  Create the RenderScript context used to communicate with our RS.
        //  Then create our actual script which will do the real work
        mRSCtx = RenderScript.create(mContext);
        mMandGen = new ScriptC_mand_gen(mRSCtx,
                                        mContext.getResources(),
                                        R.raw.mand_gen);

        //  Set the initial parameters for the generator.
        //  TODO: ADD SUPPORT FOR RE-CENTERING AND ZOOM
        mMandGen.set_width(width);
        mMandGen.set_height(height);
        mMandGen.set_iter(iterations);
        mMandGen.set_paletteLen(mPalette.length);
        Type.Builder intArrayBuilder = new Type.Builder(mRSCtx,
                                                        Element.I32(mRSCtx));
        intArrayBuilder.setX(mPalette.length);
        Allocation allocPalette =
            Allocation.createTyped(mRSCtx,
                                   intArrayBuilder.create());
        allocPalette.copyFrom(mPalette);
        mMandGen.bind_palette(allocPalette);
    }

    @Override
    public int generate(int[] bitmap) {
        Bitmap                  outBitmap;
        Allocation              outAlloc;

        //  Create an output bitmap for our script and an Allocation to send
        //  it to the RenderScript
        outBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        outAlloc = Allocation.createFromBitmap(mRSCtx, outBitmap);

        //  Call the RS kernel to do the computation and get back the data.
        mMandGen.forEach_root(outAlloc);
        outAlloc.copyTo(outBitmap);
        outBitmap.getPixels(bitmap, 0, mWidth, 0, 0, mWidth, mHeight);
        return 0;
    }

    @Override
    public void setDimensions(int width, int height) {
        super.setDimensions(width, height);
        mMandGen.set_width(width);
        mMandGen.set_height(height);
    }

    @Override
    public void setIterations(int iterations) {
        super.setIterations(iterations);
        mMandGen.set_iter(iterations);
    }

    @Override
    public void setZoom(int centerX, int centerY, int zoom) {
        super.setZoom(centerX, centerY, zoom);
        mMandGen.set_centerX(centerX);
        mMandGen.set_centerY(centerY);
        mMandGen.set_zoom(zoom);
    }

    @Override
    public String getName() {
        return FRACT_GEN_NAME;
    }
}


