#ifndef FLUTTER_BRIDGE_H_
#define FLUTTER_BRIDGE_H_

#include <jni.h>

#ifdef FLUTTER_LIBRARY_EXPORTS
    #define FLUTTER_LIBRARY_API __declspec(dllexport)
#else
    #define FLUTTER_LIBRARY_API __declspec(dllimport)
#endif

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_InitializeFlutterWindow(JNIEnv* env, jclass cls, jint port, jlong parent, jlong widget_id, jstring widget_name, jstring theme, jint background_color, jint parent_background_color);

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_GetView(JNIEnv* env, jclass cls, jlong context);

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_Dispose(JNIEnv* env, jclass cls, jlong context);

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_SetBounds(JNIEnv* env, jclass cls, jlong context, jint x, jint y, jint width, jint height, jint vx, jint vy, jint vwidth, jint vheight);

#ifdef __cplusplus
}
#endif

#endif  // FLUTTER_BRIDGE_H_