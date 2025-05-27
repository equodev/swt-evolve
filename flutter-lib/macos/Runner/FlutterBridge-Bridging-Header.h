#ifndef BridgingHeader_h
#define BridgingHeader_h

#include <jni.h>

// Export macro for macOS
#define JNIEXPORT __attribute__((visibility("default")))

// JNI function declarations
JNIEXPORT jlong JNICALL Java_dev_equo_swt_FlutterBridge_InitializeFlutterWindow(JNIEnv* env, jclass cls, jint port, jlong parent, jlong widget_id, jstring widget_name);

#endif /* BridgingHeader_h */
