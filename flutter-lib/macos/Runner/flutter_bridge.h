#ifndef flutter_bridge_h
#define flutter_bridge_h

// Swift bridging header for the FlutterBridge dylib: its only job is to expose the JNI types
// (JNIEnv, jint, jlong, jstring, …) to flutter_bridge.swift, which exports the JNI entry points
// via @_cdecl (so no C function declarations are needed here).
#include <jni.h>

#endif /* flutter_bridge_h */
