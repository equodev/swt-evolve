#ifndef bridge_h
#define bridge_h

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

// Export macro for macOS
#define JNIEXPORT __attribute__((visibility("default")))

// JNI function declarations matching Java native methods
JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_InitializeFlutterWindow(JNIEnv* env, jclass cls, jint port, jlong parent, jlong widget_id, jstring widget_name, jstring theme, jint background_color, jint parent_background_color);

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_GetView(JNIEnv* env, jclass cls, jlong context);

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_Dispose(JNIEnv* env, jclass cls, jlong context);

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_SetBounds(JNIEnv* env, jclass cls, jlong context, jint x, jint y, jint width, jint height, jint vx, jint vy, jint vwidth, jint vheight);

JNIEXPORT jint JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_PumpMessages(JNIEnv* env, jclass cls, jint maxMessages);

// Optional: Other functions following the same pattern
JNIEXPORT void JNICALL Java_dev_equo_Main_CloseFlutterWindow(JNIEnv* env, jclass cls);

#ifdef __cplusplus
}
#endif

#endif /* bridge_h */