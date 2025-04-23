#ifndef BridgingHeader_h
#define BridgingHeader_h

#include <jni.h>

// Export macro for macOS
#define JNIEXPORT __attribute__((visibility("default")))

// JNI function declarations
JNIEXPORT void JNICALL Java_dev_equo_Main_InitializeFlutterWindow(JNIEnv* env, jclass cls, jlong hwnd);

#endif /* BridgingHeader_h */
