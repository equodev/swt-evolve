#ifndef BridgingHeader_h
#define BridgingHeader_h

#include <jni.h>

// Export macro for macOS
#define JNIEXPORT __attribute__((visibility("default")))

// JNI function declarations
JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridge_InitializeFlutterWindow(JNIEnv* env, jclass cls, jint port, jlong parent, jlong widget_id, jstring widget_name);

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridge_GetView(JNIEnv* env, jclass cls, jlong context);

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridge_Dispose(JNIEnv* env, jclass cls, jlong context);

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridge_SetBounds(JNIEnv* env, jclass cls, jlong context, jint x, jint y, jint width, jint height);

#endif /* BridgingHeader_h */
