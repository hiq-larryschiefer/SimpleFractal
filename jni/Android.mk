#
# Copyright (c) 2013, HiQES LLC
# ALL RIGHTS RESERVED
#
# http://www.hiqes.com
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2 as
# published by the Free Software Foundation.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE	:= mand_gen_native
LOCAL_SRC_FILES	:= MandelbrotNativeGen.c

LOCAL_LDLIBS	:= -llog

include $(BUILD_SHARED_LIBRARY)

