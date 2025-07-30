#ifndef BridgingHeader_h
#define BridgingHeader_h

#include <jni.h>

// Export macro for macOS
#define JNIEXPORT __attribute__((visibility("default")))

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_InitializeFlutterWindow(JNIEnv* env, jclass cls, jint port, jlong parent, jlong widget_id, jstring widget_name);

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_GetView(JNIEnv* env, jclass cls, jlong context);

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_Dispose(JNIEnv* env, jclass cls, jlong context);

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_SetBounds(JNIEnv* env, jclass cls, jlong context, jint x, jint y, jint width, jint height, jint vx, jint vy, jint vwidth, jint vheight);

#endif /* BridgingHeader_h */
