LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_LDLIBS := -llog
LOCAL_MODULE    := sudoku
LOCAL_SRC_FILES := sudoku.c
include $(BUILD_SHARED_LIBRARY)