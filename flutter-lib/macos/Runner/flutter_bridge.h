#ifndef flutter_bridge_h
#define flutter_bridge_h

// Swift bridging header for the FlutterBridge dylib. It exposes the JNI types (JNIEnv, jint, jlong,
// jstring, …) to flutter_bridge.swift, and declares the JNI entry points that file defines with
// @_cdecl, so each signature is written down next to the Java native it implements.
#include <jni.h>

// dev.equo.swt.GLNative
JNIEXPORT jlong JNICALL
Java_dev_equo_swt_GLNative_Create(JNIEnv *env, jclass cls, jintArray glData, jlong share, jint width,
                                  jint height);

JNIEXPORT jboolean JNICALL
Java_dev_equo_swt_GLNative_SetSize(JNIEnv *env, jclass cls, jlong handle, jint width, jint height);

JNIEXPORT void JNICALL
Java_dev_equo_swt_GLNative_MakeCurrent(JNIEnv *env, jclass cls, jlong handle);

JNIEXPORT jboolean JNICALL
Java_dev_equo_swt_GLNative_IsCurrent(JNIEnv *env, jclass cls, jlong handle);

JNIEXPORT jboolean JNICALL
Java_dev_equo_swt_GLNative_Swap(JNIEnv *env, jclass cls, jlong handle, jintArray pixels, jint width,
                                jint height);

JNIEXPORT void JNICALL
Java_dev_equo_swt_GLNative_Destroy(JNIEnv *env, jclass cls, jlong handle);

#endif /* flutter_bridge_h */
