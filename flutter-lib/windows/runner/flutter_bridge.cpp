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

// Converts a UTF-8 std::string to a UTF-16 std::wstring for the Win32 *W APIs.
static std::wstring Utf16FromUtf8(const std::string& utf8) {
    if (utf8.empty()) {
        return std::wstring();
    }
    int len = ::MultiByteToWideChar(CP_UTF8, 0, utf8.c_str(), (int)utf8.size(), nullptr, 0);
    if (len <= 0) {
        return std::wstring();
    }
    std::wstring wstr(len, L'\0');
    ::MultiByteToWideChar(CP_UTF8, 0, utf8.c_str(), (int)utf8.size(), &wstr[0], len);
    return wstr;
}

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

// Creates a visible top-level window hosting the whole Display (desktop-native, 100% Flutter).
FlutterWindow* createDisplayWindow(int port, int64_t displayId, std::string widgetName, std::string theme, int backgroundColor, int width, int height) {
    std::cout << "FlutterNative.createDisplayWindow port:" << port << " id:" << displayId
              << " name:" << widgetName << " " << width << "x" << height << std::endl;

    if (!::AttachConsole(ATTACH_PARENT_PROCESS) && ::IsDebuggerPresent()) {
        CreateAndAttachConsole();
    }
    // COM is required by the Flutter Windows embedder/plugins. Harmless if already initialized.
    ::CoInitializeEx(nullptr, COINIT_APARTMENTTHREADED);

    flutter::DartProject project(GetDllPath() + L"\\data");

    // Same arg layout as the embedded path: [port, id, name, theme, bg, parentBg]. Passing the real
    // widget background (not 0) avoids the Flutter theme rendering a pure-black Display background.
    std::string bg = std::to_string(backgroundColor);
    std::vector<std::string> arguments = {std::to_string(port), std::to_string(displayId), widgetName, theme, bg, bg};
    project.set_dart_entrypoint_arguments(std::move(arguments));

    FlutterWindow* window = new FlutterWindow(project);
    if (!s_first_engine) {
        s_first_engine = window;
    }

    Win32Window::Point origin(10, 10);
    Win32Window::Size size(width, height);
    // parentWnd = nullptr, headless = false -> a visible top-level window (WS_OVERLAPPEDWINDOW). The
    // window shows itself once Flutter's first frame is ready (FlutterWindow::OnCreate -> Show()).
    if (!window->Create(Utf16FromUtf8(widgetName), origin, size, nullptr, false)) {
        std::cout << "createDisplayWindow - failed to create window" << std::endl;
        return nullptr;
    }
    // Closing the window posts WM_QUIT, which the pump reports back as -1 so the SWT side can match.
    window->SetQuitOnClose(true);
    return window;
}

// A Flutter "surface": either an embedded view (isWindow=false) or a standalone top-level window.
// On Windows both are a FlutterWindow; the tag selects the right disposal behaviour.
struct Surface {
    FlutterWindow* window;
    bool isWindow;
};

// =================================================================================================
// JNI entry points (dev.equo.swt.FlutterNative). One set of functions for both surface kinds.
// =================================================================================================

JNIEXPORT jlong JNICALL Java_dev_equo_swt_FlutterNative_Initialize(JNIEnv* env, jclass cls, jint port, jlong parent, jlong widget_id, jstring widget_name, jstring theme, jint background_color, jint parent_background_color, jint width, jint height) {
    const char* name_c = env->GetStringUTFChars(widget_name, nullptr);
    std::string nameStr(name_c);
    env->ReleaseStringUTFChars(widget_name, name_c);

    const char* theme_c = env->GetStringUTFChars(theme, nullptr);
    std::string themeStr(theme_c);
    env->ReleaseStringUTFChars(theme, theme_c);

    FlutterWindow* window;
    bool isWindow;
    if (width > 0 && height > 0) {
        window = createDisplayWindow(port, widget_id, nameStr, themeStr, background_color, width, height);
        isWindow = true;
    } else {
        window = reinterpret_cast<FlutterWindow*>(initializeFlutterWindow(port, (void*)parent, widget_id, nameStr, themeStr, background_color, parent_background_color));
        isWindow = false;
    }
    if (!window) {
        return 0;
    }
    return (jlong) new Surface{window, isWindow};
}

JNIEXPORT jlong JNICALL Java_dev_equo_swt_FlutterNative_GetView(JNIEnv* env, jclass cls, jlong context) {
    Surface* s = reinterpret_cast<Surface*>(context);
    return (jlong)((s && s->window) ? s->window->GetHandle() : nullptr);
}

JNIEXPORT void JNICALL Java_dev_equo_swt_FlutterNative_Dispose(JNIEnv* env, jclass cls, jlong context) {
    Surface* s = reinterpret_cast<Surface*>(context);
    if (!s) return;
    FlutterWindow* window = s->window;
    if (window) {
        if (!s->isWindow && window == s_first_engine) {
            // NOTE(elias): never destroy the first embedded engine — destroying it while another
            // engine is alive causes PostMessage failures and a CFG crash. Hide + reparent instead.
            window->Leak();
        } else {
            if (window == s_first_engine) {
                s_first_engine = nullptr;
            }
            window->Destroy();
        }
    }
    delete s;
}

JNIEXPORT void JNICALL Java_dev_equo_swt_FlutterNative_SetBounds(JNIEnv* env, jclass cls, jlong context, jint x, jint y, jint width, jint height, jint vx, jint vy, jint vwidth, jint vheight) {
    Surface* s = reinterpret_cast<Surface*>(context);
    if (!s || !s->window) return;
    // For a window surface Java passes the window rect as both rects, so Win32Window::Move moves the
    // window and lets WM_SIZE resize the Flutter child. For an embedded surface the two rects differ.
    s->window->Move(Win32Window::Point(x, y), Win32Window::Size(width, height),
                    Win32Window::Point(vx, vy), Win32Window::Size(vwidth, vheight));
}

// Pumps up to maxMessages from the thread queue (headless measurement path).
JNIEXPORT jint JNICALL Java_dev_equo_swt_FlutterNative_PumpMessages(JNIEnv* env, jclass cls, jint maxMessages) {
    return pumpMessages(maxMessages);
}

// Pumps a window surface's event loop; returns -1 once the window has been closed (WM_QUIT seen).
JNIEXPORT jint JNICALL Java_dev_equo_swt_FlutterNative_Pump(JNIEnv* env, jclass cls, jlong context) {
    MSG msg;
    int count = 0;
    while (::PeekMessage(&msg, nullptr, 0, 0, PM_REMOVE)) {
        if (msg.message == WM_QUIT) {
            return -1;
        }
        ::TranslateMessage(&msg);
        ::DispatchMessage(&msg);
        count++;
    }
    return count;
}

// Blocks until a message/input is available or up to |millis| ms, WITHOUT removing it (the idle sleep).
JNIEXPORT void JNICALL Java_dev_equo_swt_FlutterNative_WaitEvents(JNIEnv* env, jclass cls, jlong context, jint millis) {
    DWORD timeout = millis < 0 ? 0 : (DWORD)millis;
    ::MsgWaitForMultipleObjectsEx(0, nullptr, timeout, QS_ALLINPUT, MWMO_INPUTAVAILABLE);
}

JNIEXPORT void JNICALL Java_dev_equo_swt_FlutterNative_SetTitle(JNIEnv* env, jclass cls, jlong context, jstring title) {
    Surface* s = reinterpret_cast<Surface*>(context);
    const char* t = env->GetStringUTFChars(title, nullptr);
    std::string ts(t);
    env->ReleaseStringUTFChars(title, t);
    if (s && s->window) {
        HWND hwnd = s->window->GetHandle();
        if (hwnd) ::SetWindowTextW(hwnd, Utf16FromUtf8(ts).c_str());
    }
}

// state: 0 = restore/normal, 1 = maximized, 2 = minimized, 3 = fullscreen (approximated as maximized).
JNIEXPORT void JNICALL Java_dev_equo_swt_FlutterNative_SetState(JNIEnv* env, jclass cls, jlong context, jint state) {
    Surface* s = reinterpret_cast<Surface*>(context);
    if (!s || !s->window) return;
    HWND hwnd = s->window->GetHandle();
    if (!hwnd) return;
    switch (state) {
        case 1: ::ShowWindow(hwnd, SW_MAXIMIZE); break;
        case 2: ::ShowWindow(hwnd, SW_MINIMIZE); break;
        case 3: ::ShowWindow(hwnd, SW_MAXIMIZE); break;
        default: ::ShowWindow(hwnd, SW_RESTORE); break;
    }
}
