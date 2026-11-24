#ifndef RUNNER_FLUTTER_WINDOW_H_
#define RUNNER_FLUTTER_WINDOW_H_

#include <flutter/dart_project.h>
#include <flutter/flutter_view_controller.h>
#include <flutter/method_channel.h>
#include <flutter/standard_method_codec.h>

#include <memory>

#include "win32_window.h"

// A window that does nothing but host a Flutter view.
class FlutterWindow : public Win32Window {
 public:
  // Creates a new FlutterWindow hosting a Flutter view running |project|.
  explicit FlutterWindow(const flutter::DartProject& project);
  virtual ~FlutterWindow();

  // Whether Dart started without the semantics tree (a11y=off) and so has to hear when a screen
  // reader starts or stops. Set before Create.
  void SetReportsAssistiveTechnology(bool reports) { reports_assistive_technology_ = reports; }

 protected:
  // Win32Window:
  bool OnCreate() override;
  void OnDestroy() override;
  LRESULT MessageHandler(HWND window, UINT const message, WPARAM const wparam,
                         LPARAM const lparam) noexcept override;

 private:
  // Registers the Client-Side-Decorations window channel (dev.equo.swt/window) that the
  // Flutter-drawn title bar drives -- its minimize/maximize/close buttons and its drag and
  // resize handles. The desktop counterpart of the browser's injected `window.equo.*`.
  void SetupWindowChannel();

  // The project to run.
  flutter::DartProject project_;

  // The Flutter instance hosted by this window.
  std::unique_ptr<flutter::FlutterViewController> flutter_controller_;

  // The CSD window channel; null for embedded/headless surfaces that have no window chrome.
  std::unique_ptr<flutter::MethodChannel<flutter::EncodableValue>> window_channel_;

  bool reports_assistive_technology_ = false;

  // Reports to Dart a screen reader that starts or stops after the engine started
  // (dev.equo.swt/accessibility, `assistiveTechnology`); null unless reports_assistive_technology_.
  std::unique_ptr<flutter::MethodChannel<flutter::EncodableValue>> a11y_channel_;
};

#endif  // RUNNER_FLUTTER_WINDOW_H_
