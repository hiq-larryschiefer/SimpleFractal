#pragma version(1)
#pragma rs java_package_name(com.hiq.SimpleFractal.mand_gen)

const static double MIN_X_VAL = -2.5;
const static double MAX_X_VAL = 1.0;
const static double MIN_Y_VAL = -1.0;
const static double MAX_Y_VAL = 1.0;
const static double MODULO_VAL = 2.0;
const static double STOP_VAL = MODULO_VAL * MODULO_VAL;

int32_t width;
int32_t height;
int32_t iter;
int32_t *palette;
int32_t paletteLen;

//  Unused for now
int32_t zoom;
int32_t centerX;
int32_t centerY;

void root(const uchar4 *v_in, uchar4 *v_out, uint32_t x, uint32_t y) {
    int                         i;
    double                      xScaler;
    double                      yScaler;
    double                      scaledX;
    double                      scaledY;
    double                      orbitX;
    double                      orbitY;
    double                      orbitX2;
    double                      orbitY2;
    double                      tempX;

    //rsDebug("x", x);
    //rsDebug("y", y);
    xScaler = (MAX_X_VAL - MIN_X_VAL) / (double)width;
    yScaler = (MAX_Y_VAL - MIN_Y_VAL) / (double)height;

    scaledX = MIN_X_VAL + ((double)x * xScaler);
    scaledY = MIN_Y_VAL + ((double)y * yScaler);

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

    //rsDebug("i" , i);
    //rsDebug("color palette", palette[paletteIndex]);
    //rsDebug("v_out", v_out);
    v_out->a = (palette[paletteIndex] & 0xFF000000) >> 24;
    v_out->r = (palette[paletteIndex] & 0x00FF0000) >> 16;
    v_out->g = (palette[paletteIndex] & 0x0000FF00) >> 8;
    v_out->b = (palette[paletteIndex] & 0x000000FF);
    //rsDebug("*v_out", (int)*v_out);
}
