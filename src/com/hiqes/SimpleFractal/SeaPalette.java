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

import android.graphics.Bitmap;

public class SeaPalette implements FractalPalette {
    private static final int    R_START = 0x10;
    private static final int    R_END = 0x40;
    private static final int    R_STEP = 0x2;
    private static final int    R_TOTAL = (R_END - R_START) / R_STEP;
    private static final int    G_START = 0x40;
    private static final int    G_END = 0x70;
    private static final int    G_STEP = 0x4;
    private static final int    G_TOTAL = (G_END - G_START) / G_STEP;
    private static final int    B_START = 0xA0;
    private static final int    B_END = 0xEE;
    private static final int    B_STEP = 0x4;
    private static final int    B_TOTAL = (B_END - B_START) / B_STEP;
    private static final int    ALPHA_FULL = (0xFF << 24);

    private Bitmap.Config       mConfig;
    int[]                       mPalette;

    public SeaPalette(Bitmap.Config config) {
        //  Ignore bitmap config now, just assume ARGB_8888 for now
        mConfig = config;

        //  Simple palette generation of blue-ish/green colors
        mPalette = new int[R_TOTAL * G_TOTAL * B_TOTAL];
        for (int i = 0; i < R_TOTAL; i ++) {
            int redVal = ALPHA_FULL | (((R_END - (R_STEP * i)) << 16) & 0xFFFF0000);

            for (int j = 0; j < G_TOTAL; j++) {
                int greenVal = ((G_END - (G_STEP * j)) << 8) & 0x0000FF00;

                for (int k = 0; k < B_TOTAL; k++) {
                    int blueVal = (B_END - (B_STEP * k)) & 0x000000FF;
                    int color = redVal | greenVal | blueVal;

                    int index = (i * G_TOTAL * B_TOTAL) + (j * B_TOTAL) + k;

                    mPalette[index] = color;
                }
            }
        }
    }

    public void setConfig(Bitmap.Config config) {
        //  Ignore for now
        //  TODO - Revise the palette based on the new color config
    }

    public int[] getPalette() {
        return mPalette;
    }
}
