// A keypad key carries the character it types, as every native's KeyDown does: '+' for the
// keypad's +, '7' for its 7.
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/key_mapping.dart';

KeyDownEvent _down(LogicalKeyboardKey key, PhysicalKeyboardKey physical) =>
    KeyDownEvent(physicalKey: physical, logicalKey: key, timeStamp: Duration.zero);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('the keypad\'s operators and digits carry their characters', () {
    final cases = {
      LogicalKeyboardKey.numpadAdd: (PhysicalKeyboardKey.numpadAdd, SWT.KEYPAD_ADD, '+'),
      LogicalKeyboardKey.numpadSubtract: (PhysicalKeyboardKey.numpadSubtract, SWT.KEYPAD_SUBTRACT, '-'),
      LogicalKeyboardKey.numpadMultiply: (PhysicalKeyboardKey.numpadMultiply, SWT.KEYPAD_MULTIPLY, '*'),
      LogicalKeyboardKey.numpadDivide: (PhysicalKeyboardKey.numpadDivide, SWT.KEYPAD_DIVIDE, '/'),
      LogicalKeyboardKey.numpad7: (PhysicalKeyboardKey.numpad7, SWT.KEYPAD_7, '7'),
    };
    cases.forEach((key, expected) {
      final e = mapNewKeyEventToSwt(_down(key, expected.$1));
      expect(e.keyCode, expected.$2, reason: key.debugName);
      expect(e.character, expected.$3.codeUnitAt(0), reason: key.debugName);
    });
  });
}
