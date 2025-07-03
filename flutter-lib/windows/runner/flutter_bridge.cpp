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

namespace {
//    std::unique_ptr<flutter::DartProject> project;
//    std::unique_ptr<FlutterWindow> window;

    // Window class registration
    // const wchar_t* const kWindowClassName = L"FLUTTER_WINDOW_CLASS";

    // LRESULT CALLBACK WindowProc(HWND hwnd, UINT message, WPARAM wparam, LPARAM lparam) {
    //     if (flutter_controller_) {
    //         flutter_controller_->HandleTopLevelWindowProc(hwnd, message, wparam, lparam);
    //     }

    //     switch (message) {
    //         case WM_DESTROY:
    //             window_handle_ = nullptr;
    //             return 0;
    //     }

    //     return DefWindowProc(hwnd, message, wparam, lparam);
    // }

    // void RegisterWindowClass() {
    //     WNDCLASSEX window_class{};
    //     window_class.cbSize = sizeof(WNDCLASSEX);
    //     window_class.lpszClassName = kWindowClassName;
    //     window_class.lpfnWndProc = WindowProc;
    //     window_class.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    //     window_class.hCursor = LoadCursor(nullptr, IDC_ARROW);
    //     RegisterClassEx(&window_class);
    // }
}

constexpr const wchar_t kWindowClassName[] = L"MAIN_WIN32_WINDOW";

ATOM MyRegisterClass(HINSTANCE hInstance)
{
    WNDCLASSEXW wcex;

    wcex.cbSize = sizeof(WNDCLASSEX);

    wcex.style          = CS_HREDRAW | CS_VREDRAW;
    wcex.lpfnWndProc    = Win32Window::WndProc;
    wcex.cbClsExtra     = 0;
    wcex.cbWndExtra     = 0;
    wcex.hInstance      = hInstance;
    wcex.hIcon          = LoadIcon(hInstance, MAKEINTRESOURCE(IDI_APP_ICON));
    wcex.hCursor        = LoadCursor(nullptr, IDC_ARROW);
    wcex.hbrBackground  = (HBRUSH)(COLOR_WINDOW+2);
    wcex.lpszMenuName   = nullptr;
    wcex.lpszClassName  = kWindowClassName;
    wcex.hIconSm        = LoadIcon(wcex.hInstance, MAKEINTRESOURCE(IDI_APP_ICON));

    return RegisterClassExW(&wcex);
}

//std::string GetDllDirectory() {
//    HMODULE hModule = NULL;
//    if (GetModuleHandleExA(GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS |
//                          GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
//                          (LPCSTR)GetDllDirectory, &hModule)) {
//        char path[MAX_PATH];
//        if (GetModuleFileNameA(hModule, path, MAX_PATH)) {
//            std::string fullPath(path);
//            size_t pos = fullPath.find_last_of("\\/");
//            return fullPath.substr(0, pos);
//        }
//    }
//    return "";
//}

// Or, use the address of an exported function
extern "C" __declspec(dllexport) void DummyExportedFunction()
{
    // This function exists solely to provide an address within the DLL.
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

void* initializeFlutterWindow(int port, void* parentWnd, int64_t widgetId, std::string widgetName) {
    std::cout<<"InitializeFlutterWindow"<<std::endl;
  // Attach to console when present (e.g., 'flutter run') or create a new console when running with a debugger.
  if (!::AttachConsole(ATTACH_PARENT_PROCESS) && ::IsDebuggerPresent()) {
    CreateAndAttachConsole();
  }

  // Initialize COM, so that it is available for use in the library and/or plugins.
  ::CoInitializeEx(nullptr, COINIT_APARTMENTTHREADED);

//  project = std::make_unique<flutter::DartProject>(L"C:\\Users\\Guille\\ws\\swt-flutter\\pocflutter\\build\\windows\\x64\\runner\\Release\\data");
//  project = std::make_unique<flutter::DartProject>(L"C:\\Users\\Guille\\ws\\swt-flutter\\pocflutter\\build\\windows\\x64\\runner\\Release\\data");
//  project = std::make_unique<flutter::DartProject>(GetDllPath() + L"\\data");
  flutter::DartProject project(GetDllPath() + L"\\data");

//  std::vector<std::string> command_line_arguments = GetCommandLineArguments();
  std::vector<std::string> arguments = {std::to_string(port), std::to_string(widgetId), widgetName};
//  project->set_dart_entrypoint_arguments(std::move(command_line_arguments));
//  project->set_dart_entrypoint_arguments(std::move(arguments));
  project.set_dart_entrypoint_arguments(std::move(arguments));
  // const wchar_t* window_class =
      // WindowClassRegistrar::GetInstance()->GetWindowClass();
//   MyRegisterClass(GetModuleHandle(nullptr));
  MyRegisterClass(nullptr);

  std::cout<<"InitializeFlutterWindow CreateWin"<<std::endl;

//   HWND parentWnd = CreateWindowW(kWindowClassName, L"mainWindow", WS_OVERLAPPEDWINDOW,
//       CW_USEDEFAULT, 0, CW_USEDEFAULT, 0, nullptr, nullptr, GetModuleHandle(nullptr), nullptr);

  if (!parentWnd)
  {
    std::cout<<"Main END1 "<<__FILE__<<__LINE__<<std::endl;
    return 0;
  }

//  window = std::make_unique<FlutterWindow>(*project.get());
//  FlutterWindow* window = new FlutterWindow(*project.get());
  FlutterWindow* window = new FlutterWindow(project);
  // FlutterWindow window(*project.get());
//  Win32Window::Point origin(0, 0);
//  Win32Window::Size size(600, 500);
  RECT frame;
  GetClientRect((HWND)parentWnd, &frame);
  Win32Window::Point origin(0, 0);
  Win32Window::Size size(frame.right - frame.left, frame.bottom - frame.top);
  if (!window->Create(L"swtflutter1", origin, size, (HWND)parentWnd)) {
    return 0;
  }
//  window->SetQuitOnClose(true);

//  FlutterWindow* rawPtr = window.release();
  return window;

//   FlutterWindow window2(project);
//   Win32Window::Point origin2(650, 550);
//   Win32Window::Size size2(250, 300);
//   if (!window2.Create(L"swtflutter2", origin2, size2, parentWnd)) {
//     return ;
//   }
//   window2.SetQuitOnClose(true);

  // ::MSG msg;
  // while (::GetMessage(&msg, nullptr, 0, 0)) {
    // ::TranslateMessage(&msg);
    // ::DispatchMessage(&msg);
  // }

//   ::CoUninitialize();

    // // Ensure we're not reinitializing
    // if (window_handle_ != nullptr) {
    //     return;
    // }

    // // Register window class if needed
    // static bool class_registered = false;
    // if (!class_registered) {
    //     RegisterWindowClass();
    //     class_registered = true;
    // }
    // std::cout<<"InitializeFlutterWindow1"<<std::endl;

    // // Create Flutter project
    // dart_project_ = std::make_unique<flutter::DartProject>(L"data");
    // flutter::DartProject dart_project_o = *dart_project_.get();
    // std::cout<<"InitializeFlutterWindow2"<<std::endl;

    // // Configure window
    // RECT window_bounds = {0, 0, 800, 600};  // Default size
    // AdjustWindowRect(&window_bounds, WS_OVERLAPPEDWINDOW, FALSE);

    // // Create window
    // window_handle_ = CreateWindow(
    //     kWindowClassName,
    //     L"Flutter Window",
    //     WS_OVERLAPPEDWINDOW,
    //     CW_USEDEFAULT, CW_USEDEFAULT,
    //     window_bounds.right - window_bounds.left,
    //     window_bounds.bottom - window_bounds.top,
    //     nullptr,
    //     nullptr,
    //     GetModuleHandle(nullptr),
    //     nullptr
    // );

    // if (!window_handle_) {
    //     return;
    // }

    // std::cout<<"InitializeFlutterWindow3 '"<<dart_project_o.dart_entrypoint()<<"'"<<std::endl;
    // std::cout<<"InitializeFlutterWindow3.1"<<std::endl;

    // // Create Flutter view controller
    // flutter_controller_ = std::make_unique<flutter::FlutterViewController>(
    //     window_bounds.right - window_bounds.left,
    //     window_bounds.bottom - window_bounds.top,
    //     dart_project_o
    // );
    // std::cout<<"InitializeFlutterWindow4"<<std::endl;

    // // Set window controller
    // SetWindowLongPtr(
    //     window_handle_,
    //     GWLP_USERDATA,
    //     reinterpret_cast<LONG_PTR>(flutter_controller_.get())
    // );

    // Show window
    // ShowWindow(window_handle_, SW_SHOW);
    // UpdateWindow(window_handle_);
}

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_InitializeFlutterWindow(JNIEnv* env, jclass cls, jint port, jlong parent, jlong widget_id, jstring widget_name) {
    const char* cstr = env->GetStringUTFChars(widget_name, nullptr);
    std::string widgetNameStr(cstr);
    env->ReleaseStringUTFChars(widget_name, cstr);

    return (jlong) initializeFlutterWindow(port, (void*)parent, widget_id, widgetNameStr);
}

JNIEXPORT jlong JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_GetView(JNIEnv* env, jclass cls, jlong context) {
    FlutterWindow* window = reinterpret_cast<FlutterWindow*>(context);
    return (jlong) window->GetHandle();
}

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_Dispose(JNIEnv* env, jclass cls, jlong context) {
    FlutterWindow* window = reinterpret_cast<FlutterWindow*>(context);
//    project.reset();

    ::CoUninitialize();
    window->Destroy();
    delete window;
}

JNIEXPORT void JNICALL Java_org_eclipse_swt_widgets_SwtFlutterBridgeBase_SetBounds(JNIEnv* env, jclass cls, jlong context, jint x, jint y, jint width, jint height, jint vx, jint vy, jint vwidth, jint vheight) {
    FlutterWindow* window = reinterpret_cast<FlutterWindow*>(context);
    Win32Window::Point origin(x, y);
    Win32Window::Size size(width, height);
    Win32Window::Point vorigin(vx, vy);
    Win32Window::Size vsize(vwidth, vheight);
    window->Move(origin, size, vorigin, vsize);
}