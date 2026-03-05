#include "flutter_bridge.h"
#include <flutter/dart_project.h>
#include <flutter/flutter_view_controller.h>
#include <windows.h>
#include <string>
#include <iostream>
#include <memory>
#include <filesystem> // For std::filesystem

#include "flutter_window.h"
#include "utils.h"
#include "resource.h"

// Or, use the address of an exported function
extern "C" __declspec(dllexport) void DummyExportedFunction()
{
    // This function exists solely to provide an address within the DLL.
}

static FlutterWindow* s_first_engine = nullptr;

std::wstring GetDllPath() {
    HMODULE hModule = NULL;

    void* address_in_dll = &DummyExportedFunction;

    if (GetModuleHandleExW( // Use the Wide version of the function
        GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
        (LPCWSTR)address_in_dll, // Cast to LPCWSTR for wide char
        &hModule
    ))
    {
        // Use a wide character buffer and GetModuleFileNameW
        wchar_t path[MAX_PATH];
        DWORD result = GetModuleFileNameW(hModule, path, MAX_PATH);
        if (result != 0 && result < MAX_PATH)
        {
            std::filesystem::path dll_path(path);
            return dll_path.parent_path().wstring();
            //            return path; // Returns std::wstring directly from wchar_t array
        }
        else
        {
            // Use std::wcerr for wide string error output
            std::wcerr << L"Error getting DLL path with GetModuleFileNameW: " << GetLastError() << std::endl;
        }
    }
    else
    {
        // Use std::wcerr for wide string error output
        std::wcerr << L"Error getting module handle with GetModuleHandleExW: " << GetLastError() << std::endl;
    }
    return L""; // Return an empty wide string on failure
}

int pumpMessages(int maxMessages) {
    ::MSG msg;
    int count = 0;
    for (int i = 0; i < maxMessages; i++) {
        if (::PeekMessage(&msg, nullptr, 0, 0, PM_REMOVE)) {
            ::TranslateMessage(&msg);
            ::DispatchMessage(&msg);
            count++;
        } else {
            break;
        }
    }
//    std::cout<<"Processed "<<count<<" messages"<<std::endl;
    return count;
}

void* initializeFlutterWindow(int port, void* parentWnd, int64_t widgetId, std::string widgetName, std::string theme, int backgroundColor, int parentBackgroundColor) {
    std::cout<<"InitializeFlutterWindow"<<std::endl;
  // Attach to console when present (e.g., 'flutter run') or create a new console when running with a debugger.
  if (!::AttachConsole(ATTACH_PARENT_PROCESS) && ::IsDebuggerPresent()) {
    CreateAndAttachConsole();
  }

  flutter::DartProject project(GetDllPath() + L"\\data");

  std::vector<std::string> arguments = {std::to_string(port), std::to_string(widgetId), widgetName, theme, std::to_string(backgroundColor), std::to_string(parentBackgroundColor)};
  project.set_dart_entrypoint_arguments(std::move(arguments));

  std::cout<<"InitializeFlutterWindow CreateWin"<<std::endl;

//   HWND parentWnd = CreateWindowW(kWindowClassName, L"mainWindow", WS_OVERLAPPEDWINDOW,
//       CW_USEDEFAULT, 0, CW_USEDEFAULT, 0, nullptr, nullptr, GetModuleHandle(nullptr), nullptr);

  FlutterWindow* window = new FlutterWindow(project);
  if (!s_first_engine) {
    s_first_engine = window;
  }

  if (!parentWnd)
  {
    std::cout<<"InitializeFlutterWindow - Headless mode (offscreen window)"<<__FILE__<<__LINE__<<std::endl;
    Win32Window::Point origin(10, 10);
    Win32Window::Size size(1280, 720);
    if (!window->Create(L"swtflutter", origin, size, 0, true)) {
      std::cout<<"InitializeFlutterWindow - Error1"<<__FILE__<<__LINE__<<std::endl;
      return 0;
    }
    window->SetQuitOnClose(true);
    pumpMessages(1);
    return window;
  }

  RECT frame;
  GetClientRect((HWND)parentWnd, &frame);
  Win32Window::Point origin(0, 0);
  Win32Window::Size size(frame.right - frame.left, frame.bottom - frame.top);
  if (!window->Create(L"swtflutter1", origin, size, (HWND)parentWnd), false) {
    return 0;
  }
//  window->SetQuitOnClose(true);

  return window;
}

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_InitializeFlutterWindow(JNIEnv* env, jclass cls, jint port, jlong parent, jlong widget_id, jstring widget_name, jstring theme, jint background_color, jint parent_background_color) {
    const char* cstr = env->GetStringUTFChars(widget_name, nullptr);
    std::string widgetNameStr(cstr);
    env->ReleaseStringUTFChars(widget_name, cstr);
    
    const char* theme_cstr = env->GetStringUTFChars(theme, nullptr);
    std::string themeStr(theme_cstr);
    env->ReleaseStringUTFChars(theme, theme_cstr);
    
    return (jlong) initializeFlutterWindow(port, (void*)parent, widget_id, widgetNameStr, themeStr, background_color, parent_background_color);
}

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_GetView(JNIEnv* env, jclass cls, jlong context) {
    FlutterWindow* window = reinterpret_cast<FlutterWindow*>(context);
    return (jlong) window->GetHandle();
}

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_Dispose(JNIEnv* env, jclass cls, jlong context) {
    FlutterWindow* window = reinterpret_cast<FlutterWindow*>(context);

    if (window == s_first_engine) {
        // NOTE(elias): Never destroy the first engine: Destroying it while
        // any other engine is alive causes PostMessage failures and a CFG crash.
        // Hide it and reparent to HWND_MESSAGE so the SWT parent's
        // DestroyWindow does not cascade into this window.
        window->Leak();
    } else {
        window->Destroy();
    }
}

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_SetBounds(JNIEnv* env, jclass cls, jlong context, jint x, jint y, jint width, jint height, jint vx, jint vy, jint vwidth, jint vheight) {
    FlutterWindow* window = reinterpret_cast<FlutterWindow*>(context);
    Win32Window::Point origin(x, y);
    Win32Window::Size size(width, height);
    Win32Window::Point vorigin(vx, vy);
    Win32Window::Size vsize(vwidth, vheight);
    window->Move(origin, size, vorigin, vsize);
}

// Pump Windows messages to allow Flutter/Dart async operations to progress
// Java should call this periodically in headless mode
JNIEXPORT jint JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_PumpMessages(JNIEnv* env, jclass cls, jint maxMessages) {
    return pumpMessages(maxMessages);
}

