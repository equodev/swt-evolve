#ifndef FLUTTER_BRIDGE_H_
#define FLUTTER_BRIDGE_H_

#include <cstdint>
#include <jni.h>

#ifdef FLUTTER_LIBRARY_EXPORTS
#define FLUTTER_LIBRARY_API __attribute__((visibility("default")))
#else
#define FLUTTER_LIBRARY_API
#endif

#ifdef __cplusplus
extern "C" {
#endif

enum ExpandPolicy {
  FOLLOW_H_PARENT,
  FOLLOW_W_PARENT,
  FOLLOW_PARENT,
  EXPAND_POLICY_SIZE
};

JNIEXPORT jlong JNICALL
Java_org_eclipse_swt_widgets_FlutterSwt_InitializeFlutterWindow(
    JNIEnv *env, jclass cls, void *parent, jint port, jlong widget_id,
    jstring widget_name, jint width, jint height, jint policy);

// Optional: Other functions following the same pattern
JNIEXPORT void JNICALL
Java_org_eclipse_swt_widgets_FlutterSwt_CloseFlutterWindow(
    JNIEnv *env, jclass cls, void *void_context);
JNIEXPORT jboolean JNICALL
Java_org_eclipse_swt_widgets_FlutterSwt_IsFlutterWindowVisible(JNIEnv *env,
                                                               jclass cls);
JNIEXPORT void JNICALL
Java_org_eclipse_swt_widgets_FlutterSwt_ResizeFlutterWindow(
    JNIEnv *env, jclass cls, void *void_context, jint width, jint height);

FLUTTER_LIBRARY_API uintptr_t InitializeFlutterWindow(
    void *parentWnd, jint port, jlong widget_id, const char *widget_name,
    int width, int height, ExpandPolicy policy);
FLUTTER_LIBRARY_API void CloseFlutterWindow(void *widget_context);
FLUTTER_LIBRARY_API bool IsFlutterWindowVisible();
FLUTTER_LIBRARY_API void ResizeFlutterWindow(void *widget_context,
                                             int32_t width, int32_t height);

#ifdef __cplusplus
}
#endif

#endif // FLUTTER_BRIDGE_H_
