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
#pragma version(1)
#pragma rs java_package_name(com.hiqes.SimpleFractal)

#pragma rs_fp_relaxed

const static float MIN_X_VAL = -2.5;
const static float MAX_X_VAL = 1.0;
const static float MIN_Y_VAL = -1.0;
const static float MAX_Y_VAL = 1.0;
const static float MODULO_VAL = 2.0;
const static float STOP_VAL = MODULO_VAL * MODULO_VAL;

#define PALETTE_MAX 1025

int32_t width;
int32_t height;
int32_t iter;
int32_t palette[PALETTE_MAX];
int32_t paletteLen;

//  Unused for now
int32_t zoom;
int32_t centerX;
int32_t centerY;

void init(void) {
    zoom = 1;
}

rs_allocation alloc_palette;

void setPalette() {
    for (int i = 0; i < paletteLen; i++) {
        palette[i] = rsGetElementAt_int(alloc_palette, i);
    }
}

void root(uchar4 *v_out, uint32_t x, uint32_t y) {
    int                         i;
    float                      xScaler;
    float                      yScaler;
    float                      scaledX;
    float                      scaledY;
    float                      orbitX;
    float                      orbitY;
    float                      orbitX2;
    float                      orbitY2;
    float                      tempX;

    xScaler = (MAX_X_VAL - MIN_X_VAL) / (float)width;
    yScaler = (MAX_Y_VAL - MIN_Y_VAL) / (float)height;

    scaledX = MIN_X_VAL + ((float)x * xScaler);
    scaledY = MIN_Y_VAL + ((float)y * yScaler);

    orbitX = 0;
    orbitY = 0;
    orbitX2 = 0;
    orbitY2 = 0;

    for (i = 0; (i < iter) && ((orbitX2 + orbitY2) < STOP_VAL); i++) {
        tempX = orbitX2 - orbitY2 + scaledX;
        orbitY = MODULO_VAL * orbitX * orbitY + scaledY;
        orbitX = tempX;

        orbitX2 = orbitX * orbitX;
        orbitY2 = orbitY * orbitY;
    }

    int paletteIndex = (i * (paletteLen - 1)) / iter;
    if (paletteIndex < 0) {
        paletteIndex = 0;
    }

    v_out->a = (palette[paletteIndex] & 0xFF000000) >> 24;
    v_out->r = (palette[paletteIndex] & 0x00FF0000) >> 16;
    v_out->g = (palette[paletteIndex] & 0x0000FF00) >> 8;
    v_out->b = (palette[paletteIndex] & 0x000000FF);
}
