#include "flutter_window.h"

#include <optional>
#include <string>
#include <variant>

#include "flutter/generated_plugin_registrant.h"

namespace {

// Maps a CSD edge name (see CsdResizeEdges in csd_scaffold.dart) to the Win32 hit-test code
// that starts the matching OS resize drag. Returns 0 for an unknown edge.
WPARAM HitTestForEdge(const std::string& edge) {
  if (edge == "LEFT") return HTLEFT;
  if (edge == "RIGHT") return HTRIGHT;
  if (edge == "TOP") return HTTOP;
  if (edge == "BOTTOM") return HTBOTTOM;
  if (edge == "TOP_LEFT") return HTTOPLEFT;
  if (edge == "TOP_RIGHT") return HTTOPRIGHT;
  if (edge == "BOTTOM_LEFT") return HTBOTTOMLEFT;
  if (edge == "BOTTOM_RIGHT") return HTBOTTOMRIGHT;
  return 0;
}

// Reads the "edge" string out of a method call's argument map.
std::string EdgeArgument(const flutter::MethodCall<flutter::EncodableValue>& call) {
  const auto* args = std::get_if<flutter::EncodableMap>(call.arguments());
  if (!args) return std::string();
  auto it = args->find(flutter::EncodableValue("edge"));
  if (it == args->end()) return std::string();
  const auto* edge = std::get_if<std::string>(&it->second);
  return edge ? *edge : std::string();
}

}  // namespace

FlutterWindow::FlutterWindow(const flutter::DartProject& project)
    : project_(project) {}

FlutterWindow::~FlutterWindow() {}

bool FlutterWindow::OnCreate() {
  if (!Win32Window::OnCreate()) {
    return false;
  }

  RECT frame = GetClientArea();

  // The size here must match the window dimensions to avoid unnecessary surface
  // creation / destruction in the startup path.
  flutter_controller_ = std::make_unique<flutter::FlutterViewController>(
      frame.right - frame.left, frame.bottom - frame.top, project_);
  // Ensure that basic setup of the controller was successful.
  if (!flutter_controller_->engine() || !flutter_controller_->view()) {
    return false;
  }
  RegisterPlugins(flutter_controller_->engine());
  SetupWindowChannel();
  SetChildContent(flutter_controller_->view()->GetNativeWindow());

  flutter_controller_->engine()->SetNextFrameCallback([&]() {
    this->Show();
  });

  // Flutter can complete the first frame before the "show window" callback is
  // registered. The following call ensures a frame is pending to ensure the
  // window is shown. It is a no-op if the first frame hasn't completed yet.
  flutter_controller_->ForceRedraw();

  return true;
}

void FlutterWindow::SetupWindowChannel() {
  window_channel_ =
      std::make_unique<flutter::MethodChannel<flutter::EncodableValue>>(
          flutter_controller_->engine()->messenger(), "dev.equo.swt/window",
          &flutter::StandardMethodCodec::GetInstance());
  window_channel_->SetMethodCallHandler(
      [this](const flutter::MethodCall<flutter::EncodableValue>& call,
             std::unique_ptr<flutter::MethodResult<flutter::EncodableValue>>
                 result) {
        HWND hwnd = GetHandle();
        if (!hwnd) {
          result->Success();
          return;
        }
        const std::string& method = call.method_name();
        // Posted, not called: ShowWindow runs the resize synchronously, re-entering the window
        // procedure while this method-call handler is still on the engine's stack. The child
        // Flutter view then misses the new size and the window paints its old frame over an
        // unpainted rest. WM_SYSCOMMAND off the pump does the same work at a safe point -- the
        // same reason "close" below is posted rather than run inline.
        if (method == "minimize") {
          ::PostMessage(hwnd, WM_SYSCOMMAND, SC_MINIMIZE, 0);
        } else if (method == "maximize") {
          ::PostMessage(hwnd, WM_SYSCOMMAND, SC_MAXIMIZE, 0);
        } else if (method == "restore") {
          ::PostMessage(hwnd, WM_SYSCOMMAND, SC_RESTORE, 0);
        } else if (method == "close") {
          // Runs the normal WM_CLOSE teardown, so the pump reports -1 and the SWT side shuts
          // down exactly as it does for an OS-driven close.
          ::PostMessage(hwnd, WM_CLOSE, 0, 0);
        } else if (method == "beginMove") {
          // Hands the in-flight mouse-down to the OS move loop, as dragging the system title bar
          // would. It blocks until the drag ends -- native SWT blocks in the same loop.
          ::ReleaseCapture();
          ::SendMessage(hwnd, WM_NCLBUTTONDOWN, HTCAPTION, 0);
        } else if (method == "beginResize") {
          WPARAM hit_test = HitTestForEdge(EdgeArgument(call));
          if (hit_test != 0) {
            ::ReleaseCapture();
            ::SendMessage(hwnd, WM_NCLBUTTONDOWN, hit_test, 0);
          }
        } else {
          result->NotImplemented();
          return;
        }
        result->Success();
      });
}

void FlutterWindow::OnDestroy() {
  if (window_channel_) {
    window_channel_->SetMethodCallHandler(nullptr);
    window_channel_.reset();
  }
  if (flutter_controller_) {
    flutter_controller_.reset();
  }

  Win32Window::OnDestroy();
}

LRESULT
FlutterWindow::MessageHandler(HWND hwnd, UINT const message,
                              WPARAM const wparam,
                              LPARAM const lparam) noexcept {
  // Give Flutter, including plugins, an opportunity to handle window messages.
  if (flutter_controller_) {
    std::optional<LRESULT> result =
        flutter_controller_->HandleTopLevelWindowProc(hwnd, message, wparam,
                                                      lparam);
    if (result) {
      return *result;
    }
  }

  switch (message) {
    case WM_FONTCHANGE:
      if (flutter_controller_) {
        flutter_controller_->engine()->ReloadSystemFonts();
      }
      break;
    case WM_ACTIVATE:
      // The CSD title-bar glyphs lighten when the window is inactive, mirroring the system one.
      if (window_channel_) {
        window_channel_->InvokeMethod(
            "active", std::make_unique<flutter::EncodableValue>(
                          wparam != WA_INACTIVE));
      }
      break;
  }

  return Win32Window::MessageHandler(hwnd, message, wparam, lparam);
}
