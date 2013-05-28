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

public abstract class FractalGen {
    protected Context           mContext;
    protected int               mWidth;
    protected int               mHeight;
    protected int               mIter;
    protected int[]             mPalette;
    protected int               mZoom;
    protected int               mCenterX;
    protected int               mCenterY;

    public FractalGen(Context context, int width, int height, int iterations, int[] palette) {
        mContext = context;
        mWidth = width;
        mHeight = height;
        mIter = iterations;
        mPalette = palette;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getIterations() {
        return mIter;
    }

    public void setPalette(int[] palette) {
        mPalette = palette;
    }

    public void setDimensions(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void setIterations(int iterations) {
        mIter = iterations;
    }

    public void setZoom(int centerX, int centerY, int zoom) {
        mZoom = zoom;
        mCenterX = centerX;
        mCenterY = centerY;
    }

    public abstract int generate(int[] bitmap);
    public abstract String getName();
}
