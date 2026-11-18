import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

/// Whether [event] is a system shortcut the browser must still see after the editor has it.
///
/// A key the editor reports as handled has its browser default prevented, and on macOS Chrome then
/// skips its menu key equivalents. Control+Command+Space is "Emoji & Symbols": prevented, the
/// character palette never opens.
bool isPlatformTextInputShortcut(KeyEvent event, {TargetPlatform? platform}) {
  if ((platform ?? defaultTargetPlatform) != TargetPlatform.macOS) return false;
  if (event is! KeyDownEvent || event.logicalKey != LogicalKeyboardKey.space) return false;
  final keys = HardwareKeyboard.instance;
  return keys.isControlPressed && keys.isMetaPressed && !keys.isAltPressed && !keys.isShiftPressed;
}
