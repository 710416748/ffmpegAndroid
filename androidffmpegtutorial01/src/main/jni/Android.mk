LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := tutorial02
LOCAL_SRC_FILES := tutorial02.c test.c
LOCAL_LDLIBS := -llog -ljnigraphics -lz -landroid
LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil libavfilter libswresample libavdevice

include $(BUILD_SHARED_LIBRARY)
$(call import-module,ffmpeg/android/)




