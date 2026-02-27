#include "win32_window.h"

#include <dwmapi.h>
#include <flutter_windows.h>
#include <iostream>

#include "resource.h"

namespace {

/// Window attribute that enables dark mode window decorations.
///
/// Redefined in case the developer's machine has a Windows SDK older than
/// version 10.0.22000.0.
/// See: https://docs.microsoft.com/windows/win32/api/dwmapi/ne-dwmapi-dwmwindowattribute
#ifndef DWMWA_USE_IMMERSIVE_DARK_MODE
#define DWMWA_USE_IMMERSIVE_DARK_MODE 20
#endif

constexpr const wchar_t kWindowClassName[] = L"FLUTTER_RUNNER_WIN32_WINDOW";

/// Registry key for app theme preference.
///
/// A value of 0 indicates apps should use dark mode. A non-zero or missing
/// value indicates apps should use light mode.
constexpr const wchar_t kGetPreferredBrightnessRegKey[] =
  L"Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
constexpr const wchar_t kGetPreferredBrightnessRegValue[] = L"AppsUseLightTheme";

// The number of Win32Window objects that currently exist.
static int g_active_window_count = 0;

using EnableNonClientDpiScaling = BOOL __stdcall(HWND hwnd);

// Scale helper to convert logical scaler values to physical using passed in
// scale factor
int Scale(int source, double scale_factor) {
  return static_cast<int>(source * scale_factor);
}

// Dynamically loads the |EnableNonClientDpiScaling| from the User32 module.
// This API is only needed for PerMonitor V1 awareness mode.
void EnableFullDpiSupportIfAvailable(HWND hwnd) {
  HMODULE user32_module = LoadLibraryA("User32.dll");
  if (!user32_module) {
    return;
  }
  auto enable_non_client_dpi_scaling =
      reinterpret_cast<EnableNonClientDpiScaling*>(
          GetProcAddress(user32_module, "EnableNonClientDpiScaling"));
  if (enable_non_client_dpi_scaling != nullptr) {
    enable_non_client_dpi_scaling(hwnd);
  }
  FreeLibrary(user32_module);
}

}  // namespace

// Manages the Win32Window's window class registration.
class WindowClassRegistrar {
 public:
  ~WindowClassRegistrar() = default;

  // Returns the singleton registrar instance.
  static WindowClassRegistrar* GetInstance() {
    if (!instance_) {
      instance_ = new WindowClassRegistrar();
    }
    return instance_;
  }

  // Returns the name of the window class, registering the class if it hasn't
  // previously been registered.
  const wchar_t* GetWindowClass();

  // Unregisters the window class. Should only be called if there are no
  // instances of the window.
  void UnregisterWindowClass();

 private:
  WindowClassRegistrar() = default;

  static WindowClassRegistrar* instance_;

  bool class_registered_ = false;
};

WindowClassRegistrar* WindowClassRegistrar::instance_ = nullptr;

const wchar_t* WindowClassRegistrar::GetWindowClass() {
  if (!class_registered_) {
    WNDCLASS window_class{};
    window_class.hCursor = LoadCursor(nullptr, IDC_ARROW);
    window_class.lpszClassName = kWindowClassName;
    window_class.style = CS_HREDRAW | CS_VREDRAW;
    window_class.cbClsExtra = 0;
    window_class.cbWndExtra = 0;
    window_class.hInstance = GetModuleHandle(nullptr);
    window_class.hIcon =
        LoadIcon(window_class.hInstance, MAKEINTRESOURCE(IDI_APP_ICON));
    window_class.hbrBackground = (HBRUSH)(COLOR_WINDOW+1);
    window_class.lpszMenuName = nullptr;
    window_class.lpfnWndProc = Win32Window::WndProc;
    RegisterClass(&window_class);
    class_registered_ = true;
  }
  return kWindowClassName;
}

void WindowClassRegistrar::UnregisterWindowClass() {
  UnregisterClass(kWindowClassName, nullptr);
  class_registered_ = false;
}

Win32Window::Win32Window() {
}

Win32Window::~Win32Window() {
  --g_active_window_count;
  if (g_active_window_count == 0) {
    std::cout << "Win32Window::~Win32Window unregistering window class" << std::endl;
    WindowClassRegistrar::GetInstance()->UnregisterWindowClass();
  }
}

bool Win32Window::Create(const std::wstring& title,
                         const Point& origin,
                         const Size& size,
                         const HWND parentWnd,
                         bool headless) {
  const wchar_t* window_class =
      WindowClassRegistrar::GetInstance()->GetWindowClass();

  const POINT target_point = {static_cast<LONG>(origin.x),
                              static_cast<LONG>(origin.y)};
  HMONITOR monitor = MonitorFromPoint(target_point, MONITOR_DEFAULTTONEAREST);
  UINT dpi = FlutterDesktopGetDpiForMonitor(monitor);
  double scale_factor = dpi / 96.0;
  scale_factor_ = scale_factor;

  // Store headless flag
  headless_ = headless;

  // Determine window style based on whether we have a parent
  int style;
  int showCmd;
  if (parentWnd) {
    // Embedded mode: child window
    style = WS_CHILD;
    showCmd = SW_SHOW;
  } else if (headless) {
    // Headless mode: borderless window (not shown)
    style = WS_POPUP;
    showCmd = SW_SHOWNOACTIVATE;
  } else {
    // Standalone mode: visible window for development
    style = WS_OVERLAPPEDWINDOW;
    showCmd = SW_SHOW;
  }

  HWND window = CreateWindow(
      window_class, title.c_str(), style,
      Scale(origin.x, scale_factor), Scale(origin.y, scale_factor),
      Scale(size.width, scale_factor), Scale(size.height, scale_factor),
      parentWnd, nullptr, GetModuleHandle(nullptr), this);

  std::cout << "Win32Window::Create - Created window=" << window << " headless=" << (parentWnd ? "false" : "true") << std::endl;

  if (!window) {
    DWORD error = GetLastError();
    std::cout << "Win32Window::Create - Failed to create window, error=" << error << std::endl;
    return false;
  }

  ++g_active_window_count;

  UpdateTheme(window);

  if (parentWnd)
      ShowWindow(window, showCmd);

  return OnCreate();
}

bool Win32Window::Show() {
  // Don't show window in headless mode
  if (headless_) {
    return true;
  }
  return ShowWindow(window_handle_, SW_SHOWNORMAL);
}

// static
LRESULT CALLBACK Win32Window::WndProc(HWND const window,
                                      UINT const message,
                                      WPARAM const wparam,
                                      LPARAM const lparam) noexcept {

  if (message == WM_NCCREATE) {
    auto window_struct = reinterpret_cast<CREATESTRUCT*>(lparam);
    if (window_struct->lpCreateParams) {
      std::cout << "Win32Window::WndProc WM_NCCREATE - Setting up Flutter window" << std::endl;
      SetWindowLongPtr(window, GWLP_USERDATA,
                      reinterpret_cast<LONG_PTR>(window_struct->lpCreateParams));

      auto that = static_cast<Win32Window*>(window_struct->lpCreateParams);
      EnableFullDpiSupportIfAvailable(window);
      that->window_handle_ = window;
    } else {
      std::cout << "Win32Window::WndProc WM_NCCREATE - Not a Flutter window" << std::endl;
      ShowWindow(window, SW_SHOWNORMAL);
    }
  } else if (Win32Window* that = GetThisFromHandle(window)) {
    return that->MessageHandler(window, message, wparam, lparam);
  }

  return DefWindowProc(window, message, wparam, lparam);
}

LRESULT
Win32Window::MessageHandler(HWND hwnd,
                            UINT const message,
                            WPARAM const wparam,
                            LPARAM const lparam) noexcept {
    switch (message) {
        case WM_DESTROY: {
            std::cout << "Win32Window: WM_DESTROY" << std::endl;
            if (quit_on_close_) {
                PostQuitMessage(0);
            }
            return 0;
        }
        case WM_NCDESTROY: {
            std::cout << "Win32Window: WM_NCDESTROY" << std::endl;
            OnDestroy();
            auto self = GetThisFromHandle(hwnd);
            SetWindowLongPtr(hwnd, GWLP_USERDATA, 0);
            delete self;
            return 0;
        }
        case WM_KEYDOWN:
        case WM_KEYUP:
        case WM_CHAR:
        case WM_SYSKEYDOWN:
        case WM_SYSKEYUP:
        case WM_DPICHANGED: {
            UINT new_dpi = HIWORD(wparam);
            scale_factor_ = new_dpi / 96.0;
            std::cout << "Win32Window: WM_DPICHANGED dpi=" << new_dpi << " scale=" << scale_factor_ << std::endl;
            auto newRectSize = reinterpret_cast<RECT*>(lparam);
            LONG newWidth = newRectSize->right - newRectSize->left;
            LONG newHeight = newRectSize->bottom - newRectSize->top;

            SetWindowPos(hwnd, nullptr, newRectSize->left, newRectSize->top, newWidth,
                       newHeight, SWP_NOZORDER | SWP_NOACTIVATE);
            return 0;
        }

        case WM_SIZE: {
            std::cout << "Win32Window: WM_SIZE" << std::endl;
            RECT rect = GetClientArea();
            if (child_content_ != nullptr) {
                MoveWindow(child_content_, rect.left, rect.top, rect.right - rect.left,
                         rect.bottom - rect.top, TRUE);
            }
            return 0;
        }

        case WM_ACTIVATE: {
            std::cout << "Win32Window: WM_ACTIVATE - setting focus to Flutter" << std::endl;
            if (child_content_ != nullptr) {
                SetFocus(child_content_);
            }
            HWND shellWindow = GetAncestor(hwnd, GA_ROOT);
            if (shellWindow != nullptr && shellWindow != hwnd) {
                SendMessage(shellWindow, WM_ACTIVATE, wparam, lparam);
            }
            return 0;
        }

        case WM_LBUTTONDOWN:
        case WM_RBUTTONDOWN:
        case WM_MBUTTONDOWN:
        {
            std::cout << "Win32Window: Mouse click - ensuring Flutter has focus" << std::endl;
            if (child_content_ != nullptr) {
                SetFocus(child_content_);
                SendMessage(child_content_, message, wparam, lparam);
            }
            return 0;
        }

        case WM_SETFOCUS:
            std::cout << "Win32Window: WM_SETFOCUS - delegating to Flutter child" << std::endl;
            if (child_content_ != nullptr) {
                SetFocus(child_content_);
            }
            return 0;

        case WM_DWMCOLORIZATIONCOLORCHANGED:
            std::cout << "Win32Window: WM_DWMCOLORIZATIONCOLORCHANGED" << std::endl;
            UpdateTheme(hwnd);
            return 0;
    }

    return DefWindowProc(hwnd, message, wparam, lparam);
}

void Win32Window::Destroy() {
  if (window_handle_) {
    DestroyWindow(window_handle_);
    window_handle_ = nullptr;
  }
}

Win32Window* Win32Window::GetThisFromHandle(HWND const window) noexcept {
  return reinterpret_cast<Win32Window*>(
      GetWindowLongPtr(window, GWLP_USERDATA));
}

void Win32Window::SetChildContent(HWND content) {
  //std::cout << "Win32Window::SetChildContent - content=" << content << std::endl;
  child_content_ = content;
  SetParent(content, window_handle_);
  RECT frame = GetClientArea();

  MoveWindow(content, frame.left, frame.top, frame.right - frame.left,
             frame.bottom - frame.top, true);

  SetFocus(child_content_);
  std::cout << "Win32Window::SetChildContent - Complete, child focus set" << std::endl;
}

void Win32Window::Move(const Point& origin, const Size& size, const Point& vorigin, const Size& vsize) {
  // Values from Java are in SWT points (logical pixels). Convert to physical
  // pixels for MoveWindow, which expects physical pixels in a DPI-aware process.
  // scale_factor_ is set in Create() and updated on WM_DPICHANGED.
  int px = Scale(origin.x, scale_factor_);
  int py = Scale(origin.y, scale_factor_);
  int pw = Scale(size.width, scale_factor_);
  int ph = Scale(size.height, scale_factor_);
  std::cout << "Win32Window::Move - origin=(" << origin.x << "," << origin.y
            << ") size=(" << size.width << "x" << size.height << ")"
            << " scale=" << scale_factor_ << " scaled=(" << px << "," << py << " " << pw << "x" << ph << ")" << std::endl;
  MoveWindow(window_handle_, px, py, pw, ph, true);
  if (origin.x == vorigin.x && origin.y == vorigin.y && size.width == vsize.width && size.height == vsize.height) {
    return;
  }
  int vpx = Scale(vorigin.x, scale_factor_);
  int vpy = Scale(vorigin.y, scale_factor_);
  int vpw = Scale(vsize.width, scale_factor_);
  int vph = Scale(vsize.height, scale_factor_);
  MoveWindow(child_content_, vpx, vpy, vpw, vph, true);
}

RECT Win32Window::GetClientArea() {
  RECT frame;
  GetClientRect(window_handle_, &frame);
  return frame;
}

HWND Win32Window::GetHandle() {
  return window_handle_;
}

void Win32Window::SetQuitOnClose(bool quit_on_close) {
  quit_on_close_ = quit_on_close;
}

bool Win32Window::OnCreate() {
  // No-op; provided for subclasses.
  return true;
}

void Win32Window::OnDestroy() {
  // No-op; provided for subclasses.
}

void Win32Window::UpdateTheme(HWND const window) {
  DWORD light_mode;
  DWORD light_mode_size = sizeof(light_mode);
  LSTATUS result = RegGetValue(HKEY_CURRENT_USER, kGetPreferredBrightnessRegKey,
                               kGetPreferredBrightnessRegValue,
                               RRF_RT_REG_DWORD, nullptr, &light_mode,
                               &light_mode_size);

  if (result == ERROR_SUCCESS) {
    BOOL enable_dark_mode = light_mode == 0;
    DwmSetWindowAttribute(window, DWMWA_USE_IMMERSIVE_DARK_MODE,
                          &enable_dark_mode, sizeof(enable_dark_mode));
  }
}
