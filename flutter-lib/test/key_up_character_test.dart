// A key's release carries the character its press typed, as Win32's and GTK's KeyUp do: Shift+Z
// releases 'Z', not 'z'. Flutter's KeyUpEvent has no character of its own.
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/key_mapping.dart';

const _z = PhysicalKeyboardKey.keyZ;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('the release of a shifted key carries the character its press typed', () {
    final down = mapNewKeyEventToSwt(KeyDownEvent(
        physicalKey: _z, logicalKey: LogicalKeyboardKey.keyZ, character: 'Z', timeStamp: Duration.zero));
    final up = mapNewKeyEventToSwt(
        const KeyUpEvent(physicalKey: _z, logicalKey: LogicalKeyboardKey.keyZ, timeStamp: Duration.zero));

    expect(down.character, 'Z'.codeUnitAt(0));
    expect(up.character, 'Z'.codeUnitAt(0));
    expect(up.keyCode, 'z'.codeUnitAt(0));
  });

  test('a release whose press was not seen falls back to the key itself', () {
    final up = mapNewKeyEventToSwt(const KeyUpEvent(
        physicalKey: PhysicalKeyboardKey.keyQ, logicalKey: LogicalKeyboardKey.keyQ, timeStamp: Duration.zero));

    expect(up.character, 'q'.codeUnitAt(0));
  });
}
