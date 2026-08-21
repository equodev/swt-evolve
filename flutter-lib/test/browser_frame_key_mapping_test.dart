// Keys typed inside a Browser's iframe arrive as DOM events and must map to the same SWT keyCode /
// character / stateMask the Flutter paths produce, or command bindings will not match them.
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/key_mapping.dart';

void main() {
  group('mapDomKeyToSwt', () {
    test('Ctrl+S carries the unshifted keyCode and the CTRL state', () {
      final e = mapDomKeyToSwt('s',
          ctrl: true, shift: false, alt: false, meta: false);

      expect(e.keyCode, 's'.codeUnitAt(0));
      expect(e.stateMask! & SWT.CTRL, SWT.CTRL);
      expect(e.stateMask! & SWT.COMMAND, 0);
    });

    test('Cmd+S reports COMMAND, so a Mac browser is distinguishable', () {
      final e = mapDomKeyToSwt('s',
          ctrl: false, shift: false, alt: false, meta: true);

      expect(e.keyCode, 's'.codeUnitAt(0));
      expect(e.stateMask! & SWT.COMMAND, SWT.COMMAND);
    });

    test('a shifted letter keeps the typed character and the unshifted keyCode',
        () {
      final e = mapDomKeyToSwt('S',
          ctrl: true, shift: true, alt: false, meta: false);

      expect(e.keyCode, 's'.codeUnitAt(0), reason: 'bindings match on keyCode');
      expect(e.character, 'S'.codeUnitAt(0));
      expect(e.stateMask! & SWT.SHIFT, SWT.SHIFT);
    });

    test('named keys map to their SWT constants', () {
      int codeOf(String key) =>
          mapDomKeyToSwt(key, ctrl: false, shift: false, alt: false, meta: false)
              .keyCode!;

      expect(codeOf('F5'), SWT.F5);
      expect(codeOf('ArrowUp'), SWT.ARROW_UP);
      expect(codeOf('Escape'), SWT.ESC);
      expect(codeOf('Enter'), SWT.CR);
      expect(codeOf('Tab'), SWT.TAB);
    });

    test('a modifier pressed alone yields an empty event the caller drops', () {
      final e = mapDomKeyToSwt('Control',
          ctrl: true, shift: false, alt: false, meta: false);

      expect(e.keyCode, 0);
      expect(e.character, 0);
    });
  });
}
