// macOS opens the character palette on Control+Command+Space only when the page leaves that keydown
// to the browser.

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/platform_shortcuts.dart';

const _space = KeyDownEvent(
  physicalKey: PhysicalKeyboardKey.space,
  logicalKey: LogicalKeyboardKey.space,
  character: ' ',
  timeStamp: Duration.zero,
);

void main() {
  testWidgets('Control+Command+Space on macOS is left to the browser', (tester) async {
    await simulateKeyDownEvent(LogicalKeyboardKey.controlLeft);
    await simulateKeyDownEvent(LogicalKeyboardKey.metaLeft);

    expect(isPlatformTextInputShortcut(_space, platform: TargetPlatform.macOS), isTrue);
    expect(isPlatformTextInputShortcut(_space, platform: TargetPlatform.windows), isFalse,
        reason: 'the shortcut is macOS only');

    await simulateKeyUpEvent(LogicalKeyboardKey.metaLeft);
    await simulateKeyUpEvent(LogicalKeyboardKey.controlLeft);
  });

  testWidgets('other Space chords stay with the editor', (tester) async {
    expect(isPlatformTextInputShortcut(_space, platform: TargetPlatform.macOS), isFalse,
        reason: 'plain Space types a space');

    await simulateKeyDownEvent(LogicalKeyboardKey.controlLeft);
    expect(isPlatformTextInputShortcut(_space, platform: TargetPlatform.macOS), isFalse,
        reason: 'Control+Space is content assist');

    await simulateKeyDownEvent(LogicalKeyboardKey.metaLeft);
    await simulateKeyDownEvent(LogicalKeyboardKey.shiftLeft);
    expect(isPlatformTextInputShortcut(_space, platform: TargetPlatform.macOS), isFalse);

    await simulateKeyUpEvent(LogicalKeyboardKey.shiftLeft);
    await simulateKeyUpEvent(LogicalKeyboardKey.metaLeft);
    await simulateKeyUpEvent(LogicalKeyboardKey.controlLeft);
  });
}
