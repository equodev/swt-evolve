#ifndef bridge_h
#define bridge_h

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

// Export macro for macOS
#define JNIEXPORT __attribute__((visibility("default")))

//// JNI function declaration matching your Java native method
//JNIEXPORT void JNICALL Java_dev_equo_Main_InitializeFlutterWindow(JNIEnv* env, jclass cls, void* parent);

// Optional: Other functions following the same pattern
JNIEXPORT void JNICALL Java_dev_equo_Main_CloseFlutterWindow(JNIEnv* env, jclass cls);

#ifdef __cplusplus
}
#endif

#endif /* bridge_h */