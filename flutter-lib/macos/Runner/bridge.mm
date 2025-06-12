#import "bridge.h"
#import <FlutterMacOS/FlutterMacOS.h>
#import <Cocoa/Cocoa.h>

// JNIEXPORT void JNICALL Java_dev_equo_Main_InitializeFlutterWindow(JNIEnv* env, jclass cls, void* hwnd) {
//     printf("Java_dev_equo_Main_InitializeFlutterWindow\n");
//
// }

// Optional: Other functions following the same pattern
JNIEXPORT void JNICALL Java_dev_equo_Main_CloseFlutterWindow(JNIEnv* env, jclass cls) {
    printf("Java_dev_equo_Main_CloseFlutterWindow\n");
}
