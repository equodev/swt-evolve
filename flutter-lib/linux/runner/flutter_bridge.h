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

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_FlutterSwt_InitializeFlutterWindow(JNIEnv* env, jclass cls, void* parent, jint port, jlong widget_id, jstring widget_name);

// Optional: Other functions following the same pattern
JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_FlutterSwt_Main_CloseFlutterWindow(JNIEnv* env, jclass cls);
JNIEXPORT jboolean JNICALL Java_org_eclipse_swt_widgets_FlutterSwt_IsFlutterWindowVisible(JNIEnv* env, jclass cls);

FLUTTER_LIBRARY_API uintptr_t InitializeFlutterWindow(void* parentWnd, jint port, jlong widget_id, const char *widget_name);
FLUTTER_LIBRARY_API void CloseFlutterWindow();
FLUTTER_LIBRARY_API bool IsFlutterWindowVisible();

#ifdef __cplusplus
}
#endif

#endif  // FLUTTER_BRIDGE_H_
