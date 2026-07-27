import 'package:flutter_driver/driver_extension.dart';

import 'main.dart' as app;

/// Debug-only entrypoint that scripts the app via the `ext.flutter.driver` VM-Service
/// extension — finder-based tap/scroll/enter_text/get_text, real gestures through the
/// framework, no coordinate math (see docs/design/dtd-helpers/driver_cmd.dart).
///
/// Launched instead of `main.dart` (`-t lib/main_driver.dart`) only when a Java-side debug
/// launcher opts in — see `WebDisplayBridge.launchFlutterRunDev`'s `dev.equo.swt.dartDriver`
/// flag. `main.dart` never imports this file, so it is absent from every production build.
void main(List<String> args) {
  enableFlutterDriverExtension();
  app.main(args);
}
