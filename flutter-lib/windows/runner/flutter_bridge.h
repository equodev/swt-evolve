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

JNIEXPORT void JNICALL Java_dev_equo_Main_InitializeFlutterWindow(JNIEnv* env, jclass cls, void* parent);

// Optional: Other functions following the same pattern
JNIEXPORT void JNICALL Java_dev_equo_Main_CloseFlutterWindow(JNIEnv* env, jclass cls);
JNIEXPORT jboolean JNICALL Java_dev_equo_Main_IsFlutterWindowVisible(JNIEnv* env, jclass cls);

FLUTTER_LIBRARY_API void InitializeFlutterWindow(void* parentWnd);
FLUTTER_LIBRARY_API void CloseFlutterWindow();
FLUTTER_LIBRARY_API bool IsFlutterWindowVisible();

#ifdef __cplusplus
}
#endif

#endif  // FLUTTER_BRIDGE_H_