LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE	:= mand_gen_native
LOCAL_SRC_FILES	:= MandelbrotNativeGen.c

LOCAL_LDLIBS	:= -llog

include $(BUILD_SHARED_LIBRARY)

