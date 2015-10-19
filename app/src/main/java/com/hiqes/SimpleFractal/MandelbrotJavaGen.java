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

/**
 * Algorithm inspired by Wikipedia page: http://en.wikipedia.org/wiki/Mandlebrot_fractal
 */
public class MandelbrotJavaGen extends FractalGen {
    private static final String FRACT_GEN_NAME = "Mandelbrot Java Generator";
    private static final double MIN_X_VAL = -2.5;
    private static final double MAX_X_VAL = 1.0;
    private static final double MIN_Y_VAL = -1;
    private static final double MAX_Y_VAL = 1.0;
    private static final double MODULO_VAL = 2.0;
    private static final double STOP_VAL = MODULO_VAL * MODULO_VAL;


    public MandelbrotJavaGen(Context context, int width, int height, int iterations, int[] palette) {
        super(context, width, height, iterations, palette);
    }

    @Override
    public int generate(int[] bitmap) {
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

        //  Perform a simple loop over all pixels in the bitmap
        double xScaler = (MAX_X_VAL - MIN_X_VAL) / (double)mWidth;
        double yScaler = (MAX_Y_VAL - MIN_Y_VAL) / (double)mHeight;

        for (y = 0; y < mHeight; y++) {
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

                bitmap[(y * mWidth) + x] = mPalette[paletteIndex];
            }
        }

        return 0;
    }

    @Override
    public String getName() {
        return FRACT_GEN_NAME;
    }
}
