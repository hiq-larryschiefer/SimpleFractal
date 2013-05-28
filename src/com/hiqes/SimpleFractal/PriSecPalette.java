package com.hiqes.SimpleFractal;

import android.graphics.Bitmap;
import android.graphics.Color;

public class PriSecPalette implements FractalPalette {
    private static final int    STEP_VAL               = 0x04;

    private static final int    RED_START              = 0x60;
    private static final int    RED_END                = 0xD8;
    private static final int    RED_TOTAL              = (RED_END - RED_START) / STEP_VAL;

    private static final int    GREEN_START            = 0x60;
    private static final int    GREEN_END              = 0xD8;
    private static final int    GREEN_TOTAL            = (GREEN_END - GREEN_START) / STEP_VAL;

    private static final int    BLUE_START             = 0x60;
    private static final int    BLUE_END               = 0xD8;
    private static final int    BLUE_TOTAL             = (BLUE_END - BLUE_START) / STEP_VAL;

    private static final int    RED_VAL_FOR_YELLOW     = 0xFF;
    private static final int    BLUE_VAL_FOR_YELLOW    = 0x00;
    private static final int    GREEN_START_FOR_YELLOW = 0x90;
    private static final int    GREEN_END_FOR_YELLOW   = 0xFF;
    private static final int    YELLOW_TOTAL           = (GREEN_END_FOR_YELLOW - GREEN_START_FOR_YELLOW) / STEP_VAL;

    private static final int    RED_VAL_FOR_ORANGE     = 0xFF;
    private static final int    BLUE_VAL_FOR_ORANGE    = 0x00;
    private static final int    GREEN_START_FOR_ORANGE = 0x30;
    private static final int    GREEN_END_FOR_ORANGE   = 0x90;
    private static final int    ORANGE_TOTAL           = (GREEN_END_FOR_ORANGE - GREEN_START_FOR_ORANGE) / STEP_VAL;

    private static final int    RED_VAL_FOR_VIOLET     = 0x60;
    private static final int    BLUE_VAL_FOR_VIOLET    = 0xFF;
    private static final int    GREEN_START_FOR_VIOLET = 0x00;
    private static final int    GREEN_END_FOR_VIOLET   =  0x60;
    private static final int    VIOLET_TOTAL           = (GREEN_END_FOR_VIOLET - GREEN_START_FOR_VIOLET) / STEP_VAL;

    private int[]               mPalette = null;

    public PriSecPalette(Bitmap.Config config) {
        mPalette = new int[RED_TOTAL +
                           ORANGE_TOTAL +
                           YELLOW_TOTAL +
                           GREEN_TOTAL +
                           BLUE_TOTAL +
                           VIOLET_TOTAL +
                           1];

        int index = 0;

        for (int i = 0; i < RED_TOTAL; i++) {
            mPalette[index + i] = Color.rgb(RED_START + (i * STEP_VAL), 0, 0);
        }

        index += RED_TOTAL;

        for (int i = 0; i < ORANGE_TOTAL; i++) {
            mPalette[index + i] = Color.rgb(RED_VAL_FOR_ORANGE,
                                            GREEN_START_FOR_ORANGE + (i * STEP_VAL),
                                            BLUE_VAL_FOR_ORANGE);
        }

        index += ORANGE_TOTAL;

        for (int i = 0; i < YELLOW_TOTAL; i++) {
            mPalette[index + i] = Color.rgb(RED_VAL_FOR_YELLOW,
                    GREEN_START_FOR_YELLOW + (i * STEP_VAL),
                    BLUE_VAL_FOR_YELLOW);
        }

        index += YELLOW_TOTAL;

        for (int i = 0; i < GREEN_TOTAL; i++) {
            mPalette[index + i] = Color.rgb(0, GREEN_START + (i * STEP_VAL), 0);
        }

        index += GREEN_TOTAL;

        for (int i = 0; i < BLUE_TOTAL; i++) {
            mPalette[index + i] = Color.rgb(0, 0, BLUE_START + (i * STEP_VAL));
        }

        index += BLUE_TOTAL;

        for (int i = 0; i < VIOLET_TOTAL; i++) {
            mPalette[index + i] = Color.rgb(RED_VAL_FOR_VIOLET,
                                            GREEN_START_FOR_VIOLET + (i * STEP_VAL),
                                            BLUE_VAL_FOR_VIOLET);
        }

        index += 1;
        mPalette[index] = Color.rgb(0, 0, 0);
    }

    public void setConfig(Bitmap.Config config) {
        //  Ignore for now
        //  TODO - Revise the palette based on the new color config
    }

    public int[] getPalette() {
        return mPalette;
    }
}
