#include "flutter_bridge.h"
#include <flutter/dart_project.h>
#include <flutter/flutter_view_controller.h>
#include <windows.h>
#include <iostream>
#include <memory>

#include "flutter_window.h"
#include "utils.h"
#include "resource.h"

namespace {
    // Global variables to maintain window statew
    std::unique_ptr<flutter::DartProject> project;
    std::unique_ptr<FlutterWindow> window;
    // std::unique_ptr<flutter::FlutterViewController> flutter_controller_;
    // HWND window_handle_ = nullptr;
    
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


JNIEXPORT void JNICALL Java_dev_equo_Main_InitializeFlutterWindow(JNIEnv* env, jclass cls, void* parent) {
    InitializeFlutterWindow(parent);
}

JNIEXPORT void JNICALL Java_dev_equo_Main_CloseFlutterWindow(JNIEnv* env, jclass cls) {
    CloseFlutterWindow();
}

JNIEXPORT jboolean JNICALL Java_dev_equo_Main_IsFlutterWindowVisible(JNIEnv* env, jclass cls) {
    return static_cast<jboolean>(IsFlutterWindowVisible());
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

FLUTTER_LIBRARY_API void InitializeFlutterWindow(void* parentWnd) {
    std::cout<<"InitializeFlutterWindow"<<std::endl;
  // Attach to console when present (e.g., 'flutter run') or create a
  // new console when running with a debugger.
  if (!::AttachConsole(ATTACH_PARENT_PROCESS) && ::IsDebuggerPresent()) {
    CreateAndAttachConsole();
  }

  // Initialize COM, so that it is available for use in the library and/or
  // plugins.
  ::CoInitializeEx(nullptr, COINIT_APARTMENTTHREADED);

  // flutter::DartProject project(L"C:\\Users\\Guille\\ws\\swt-flutter\\pocflutter\\build\\windows\\x64\\runner\\Release\\data");
  project = std::make_unique<flutter::DartProject>(L"C:\\Users\\Guille\\ws\\swt-flutter\\pocflutter\\build\\windows\\x64\\runner\\Release\\data");


  std::vector<std::string> command_line_arguments =
      GetCommandLineArguments();

  project->set_dart_entrypoint_arguments(std::move(command_line_arguments));
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
    return ;
  }

  window = std::make_unique<FlutterWindow>(*project.get());
  // FlutterWindow window(*project.get());
  Win32Window::Point origin(10, 10);
  Win32Window::Size size(600, 500);
  if (!window->Create(L"swtflutter1", origin, size, (HWND)parentWnd)) {
    return ;
  }
  window->SetQuitOnClose(true);


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

FLUTTER_LIBRARY_API void CloseFlutterWindow() {
    if (window) {
        // DestroyWindow(window);
        window.reset();
    }
    // flutter_controller_.reset();
    project.reset();

    ::CoUninitialize();
}

FLUTTER_LIBRARY_API bool IsFlutterWindowVisible() {
    std::cout<<"IsFlutterWindowVisible"<<std::endl;    
    // return (window_handle_ != nullptr) && IsWindowVisible(window_handle_);
    return FALSE;
}