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
#include <jni.h>
#include <pthread.h>
#include <android/log.h>
#include <sys/queue.h>


#define LOG_TAG            "*MandelbrotNativeGen"

#define LOGE(fmt, ...)     __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, (fmt), ##__VA_ARGS__)
#define LOGW(fmt, ...)     __android_log_print(ANDROID_LOG_WARN, LOG_TAG, (fmt), ##__VA_ARGS__)
#define LOGI(fmt, ...)     __android_log_print(ANDROID_LOG_INFO, LOG_TAG, (fmt), ##__VA_ARGS__)
#define LOGD(fmt, ...)     __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, (fmt), ##__VA_ARGS__)


typedef struct GenWorkObj {
    int                         y;
    TAILQ_ENTRY(GenWorkObj)     node;
} GenWork;

typedef struct
{
    int                         shutdown;

    int                         width;
    int                         height;
    int                         iter;
    jint                        *bitmap;
    int                         bitmapLen;
    jint                        *palette;
    int                         paletteLen;

    pthread_t                   *threads;
    int                         threadCount;

    pthread_mutex_t             workMtx;
    pthread_cond_t              workCond;
    TAILQ_HEAD(, GenWorkObj)    workQueue;
    TAILQ_HEAD(, GenWorkObj)    freeQueue;
    pthread_cond_t              doneCond;
    int                         done;
    int                         err;
} ThreadPool;


const static double MIN_X_VAL = -2.5;
const static double MAX_X_VAL = 1.0;
const static double MIN_Y_VAL = -1.0;
const static double MAX_Y_VAL = 1.0;
const static double MODULO_VAL = 2.0;
const static double STOP_VAL = 4.0;

static int doMandGen(int x, int y, double xScaler, double yScaler, int iter) {
    int                         i;
    double                      scaledX;
    double                      scaledY;
    double                      orbitX;
    double                      orbitY;
    double                      orbitX2;
    double                      orbitY2;
    double                      tempX;

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

    return i;
}



static void *mandGenThread(void *arg) {
    ThreadPool                  *pool = (ThreadPool *)arg;
    int                         err;
    GenWork                     *curWork;
    int                         x;
    int                         val;
    pthread_t                   self = pthread_self();
    int                         offset;
    double                      xScaler;
    double                      yScaler;

    LOGD("%s[%X]: enter\n", __FUNCTION__, self);

    xScaler = (MAX_X_VAL - MIN_X_VAL) / (double)pool->width;
    yScaler = (MAX_Y_VAL - MIN_Y_VAL) / (double)pool->height;

    pthread_mutex_lock(&pool->workMtx);
    while (!pool->shutdown) {
        //  Get any work we have to do off the queue.
        //LOGD("%s[%X]: get next work\n", __FUNCTION__, self);
        curWork = TAILQ_FIRST(&pool->workQueue);
        if (curWork != NULL) {
            int                 y;
            int                 paletteIndex;

            y = curWork->y;

            TAILQ_REMOVE(&pool->workQueue, curWork, node);
            TAILQ_INSERT_TAIL(&pool->freeQueue, curWork, node);

            pthread_mutex_unlock(&pool->workMtx);

            //LOGD("%s[%X]: gen line %d", __FUNCTION__, self, y);
            for (x = 0; x < pool->width; x++) {
                offset = x + (y * pool->width);
                val = doMandGen(x, y, xScaler, yScaler, pool->iter);

                //  Generate the color based on the generator value
                paletteIndex = (val * (pool->paletteLen - 1)) / pool->iter;
                if (paletteIndex < 0) {
                    paletteIndex = 0;
                }

                pool->bitmap[offset] = pool->palette[paletteIndex];
            }
            //LOGD("%s[%X]: line %d done", __FUNCTION__, self, y);

            pthread_mutex_lock(&pool->workMtx);

            pool->done++;

            //  Don't wait again, there could be more work!
            continue;
        }

        //  Signal that we are done with all known work
        pthread_cond_signal(&pool->doneCond);

        //  Wait for work to arrive or to be awakened to shutdown
        //LOGD("%s[%X]: wait for work\n", __FUNCTION__, self);
        err = pthread_cond_wait(&pool->workCond, &pool->workMtx);
        if (err != 0) {
            LOGE("%s[%X]: Unable to wait on cond for work (%d)\n",
                 __FUNCTION__,
                 self,
                 err);
            break;
        }

    }

    pthread_mutex_unlock(&pool->workMtx);
    LOGD("%s[%X]: exit\n", __FUNCTION__, self);
    pthread_exit(NULL);
    return NULL;
}


static void singleDoGen(ThreadPool *pool) {
    int                         x;
    int                         y;
    double                      xScaler;
    double                      yScaler;
    int                         offset;
    int                         paletteIndex;
    int                         val;

    LOGD("%s: enter\n", __FUNCTION__);

    xScaler = (MAX_X_VAL - MIN_X_VAL) / (double)pool->width;
    yScaler = (MAX_Y_VAL - MIN_Y_VAL) / (double)pool->height;

    for (y = 0; y < pool->height; y++) {
        offset = pool->width * y;
        for (x = 0; x < pool->width; x++) {
            val = doMandGen(x, y, xScaler, yScaler, pool->iter);

            paletteIndex = (val * (pool->paletteLen - 1)) / pool->iter;
            if (paletteIndex < 0) {
                paletteIndex = 0;
            }

            pool->bitmap[offset + x] = pool->palette[paletteIndex];
        }
    }

    LOGD("%s: exit\n", __FUNCTION__);
}


JNIEXPORT jint JNICALL
Java_com_hiqes_SimpleFractal_MandelbrotNativeGen_generate(JNIEnv *env,
                                                          jobject obj,
                                                          jint threads,
                                                          jint width,
                                                          jint height,
                                                          jint iter,
                                                          jintArray bitmap,
                                                          jintArray palette) {
    pthread_attr_t              attr;
    int                         done = 0;
    jint                        ret = -1;
    int                         i;
    ThreadPool                  *pool;
    GenWork                     *curWork;

    //LOGD("%s: enter\n", __FUNCTION__);

    //  Create the pool, alloc its memory and initialize it
    pool = malloc(sizeof(*pool));
    if (pool == NULL) {
        LOGE("Unable to alloc thread pool\n");
        goto _errExit;
    }

    memset(pool, 0, sizeof(*pool));

    //  Create the mutex and condition variables
    pthread_mutex_init(&pool->workMtx, NULL);
    pthread_cond_init(&pool->workCond, NULL);
    pthread_cond_init(&pool->doneCond, NULL);

    //  Init the work queue head
    TAILQ_INIT(&pool->workQueue);
    TAILQ_INIT(&pool->freeQueue);

    LOGD("%s: width %d, height %d, iterations %d\n",
         __FUNCTION__,
         width,
         height,
         iter);
    pool->width = width;
    pool->height = height;
    pool->iter = iter;

    //  Get access to the integer arrays used for the bitmap and the palette
    pool->bitmapLen = (*env)->GetArrayLength(env, bitmap);
    pool->bitmap = (*env)->GetIntArrayElements(env, bitmap, 0);

    pool->paletteLen = (*env)->GetArrayLength(env, palette);
    pool->palette = (*env)->GetIntArrayElements(env, palette, 0);

    if (threads > 1) {
        //  Get the number of CPUs and only create that many threads
        pool->threadCount = threads;
        pool->threads = malloc(sizeof(pthread_t) * pool->threadCount);
        if (pool->threads == NULL) {
            LOGE("Unable to alloc mem for threads\n");
            goto _errExit;
        }

        //LOGD("Creating %d threads...\n", pool->threadCount);
        for (i = 0; i < pool->threadCount; i++) {
            int err = pthread_create(&pool->threads[i],
                                     NULL,
                                     mandGenThread,
                                     pool);
            if (err != 0) {
                LOGE("Unable to create thread %d, err %d\n", i, err);
                pool->threadCount = i;
                goto _errExit;
            }
        }

        //  Now that all the threads are ready, load them up with work, 1 line
        //  at a time to minimize allocations.
        LOGD("Queueing work for threads...\n");
        pthread_mutex_lock(&pool->workMtx);
        for (i = 0; i < pool->height; i++) {
            curWork = malloc(sizeof(*curWork));
            if (curWork == NULL) {
                LOGE("Unable to alloc work item %d\n", i);
                goto _errExit;
            }

            curWork->y = i;

            TAILQ_INSERT_TAIL(&pool->workQueue, curWork, node);
        }
        pthread_cond_broadcast(&pool->workCond);
        pthread_mutex_unlock(&pool->workMtx);

        //  Wait for the done condition
        LOGD("Work queued, waiting for completion\n");
        pthread_mutex_lock(&pool->workMtx);
        while ((pool->done < pool->height) && (pool->err == 0)) {
            LOGD("%s: waiting for done\n", __FUNCTION__);
            int err = pthread_cond_wait(&pool->doneCond, &pool->workMtx);
            LOGD("%s: got done, checking\n", __FUNCTION__);
            if (err != 0) {
                LOGE("Done cond wait failed (%d)\n", err);
                goto _errExit;
            }
        }

        //  Done - update return value, cleanup and get out
        pthread_mutex_unlock(&pool->workMtx);
    } else {
        //  With a single thread, we just do it directly
        singleDoGen(pool);
    }

    ret = 0;
    LOGD("Done, cleanup and exit\n");

_errExit:
    if (pool != NULL) {
        if (pool->threads != NULL) {
            LOGD("%s: shutdown threads\n", __FUNCTION__);
            pthread_mutex_lock(&pool->workMtx);
            pool->shutdown = 1;
            LOGD("%s: broadcast workCond\n", __FUNCTION__);
            pthread_cond_broadcast(&pool->workCond);
            pthread_mutex_unlock(&pool->workMtx);

            for (i = 0; i < pool->threadCount; i++) {
                void *ret;
                LOGD("%s: join thread %d\n", __FUNCTION__, i);
                pthread_join(pool->threads[i], &ret);
            }

            free(pool->threads);
            pool->threads = NULL;
        }

        pthread_mutex_destroy(&pool->workMtx);
        pthread_cond_destroy(&pool->workCond);
        pthread_cond_destroy(&pool->doneCond);

        for (curWork = TAILQ_FIRST(&pool->workQueue);
             curWork != NULL;
             curWork = TAILQ_FIRST(&pool->workQueue)) {
            LOGW("%s: shutdown releasing work %X\n", __FUNCTION__, curWork);
            TAILQ_REMOVE(&pool->workQueue, curWork, node);
            free(curWork);
        }

        for (curWork = TAILQ_FIRST(&pool->freeQueue);
             curWork != NULL;
             curWork = TAILQ_FIRST(&pool->freeQueue)) {
            TAILQ_REMOVE(&pool->freeQueue, curWork, node);
            free(curWork);
        }

        //  Don't forget to cleanup our JNI arrays!
        (*env)->ReleaseIntArrayElements(env, bitmap, pool->bitmap, 0);
        (*env)->ReleaseIntArrayElements(env, palette, pool->palette, 0);

        free(pool);
    }

    return ret;
}
