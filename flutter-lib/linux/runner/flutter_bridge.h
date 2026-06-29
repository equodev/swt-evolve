#ifndef FLUTTER_BRIDGE_H_
#define FLUTTER_BRIDGE_H_

#include <jni.h>

#ifdef FLUTTER_LIBRARY_EXPORTS
#define FLUTTER_LIBRARY_API __attribute__((visibility("default")))
#else
#define FLUTTER_LIBRARY_API
#endif

#ifdef __cplusplus
extern "C" {
#endif

// JNI entry points (dev.equo.swt.FlutterNative). One set of functions for both surface kinds —
// an embedded view inside a native SWT parent, or a standalone top-level Display window.
JNIEXPORT jlong JNICALL
Java_dev_equo_swt_FlutterNative_Initialize(JNIEnv *env, jclass cls, jint port, jlong parent,
                                           jlong widget_id, jstring widget_name, jstring theme,
                                           jint background_color, jint parent_background_color,
                                           jint width, jint height);

JNIEXPORT jlong JNICALL
Java_dev_equo_swt_FlutterNative_GetView(JNIEnv *env, jclass cls, jlong context);

JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_Dispose(JNIEnv *env, jclass cls, jlong context);

JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_SetBounds(JNIEnv *env, jclass cls, jlong context, jint x, jint y,
                                          jint width, jint height, jint vx, jint vy, jint vwidth,
                                          jint vheight);

JNIEXPORT jint JNICALL
Java_dev_equo_swt_FlutterNative_PumpMessages(JNIEnv *env, jclass cls, jint maxMessages);

JNIEXPORT jint JNICALL
Java_dev_equo_swt_FlutterNative_Pump(JNIEnv *env, jclass cls, jlong context);

JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_WaitEvents(JNIEnv *env, jclass cls, jlong context, jint millis);

JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_SetTitle(JNIEnv *env, jclass cls, jlong context, jstring title);

JNIEXPORT void JNICALL
Java_dev_equo_swt_FlutterNative_SetState(JNIEnv *env, jclass cls, jlong context, jint state);

#ifdef __cplusplus
}
#endif

#endif // FLUTTER_BRIDGE_H_
