#ifndef BridgingHeader_h
#define BridgingHeader_h

#include <jni.h>

// Export macro for macOS
#define JNIEXPORT __attribute__((visibility("default")))

// JNI function declarations
JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridge_InitializeFlutterWindow(JNIEnv* env, jclass cls, jint port, jlong parent, jlong widget_id, jstring widget_name);

#endif /* BridgingHeader_h */
