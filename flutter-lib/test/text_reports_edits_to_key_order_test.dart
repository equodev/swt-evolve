// A Text registers with KeyEditOrder as a field that reports its edits, so the key events after a
// character typed into it wait for its edit. Whichever editor draws it, it has to report that edit,
// or every key after a typed character waits out the timeout.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/utils/key_edit_order.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _shell(int style, int id) => VShell()
  ..id = 900
  ..style = SWT.SHELL_TRIM
  ..text = 'shell'
  ..visible = true
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    VText()
      ..id = id
      ..style = style | SWT.BORDER
      ..text = ''
      ..enabled = true
      ..editable = true
      ..visible = true
      ..bounds = _rect(10, 10, 300, style & SWT.MULTI != 0 ? 80 : 24),
  ];

void main() {
  setUp(() {
    resetConfigFlags();
    FocusRequests.instance.reset();
  });
  tearDown(() {
    resetConfigFlags();
    FocusRequests.instance.reset();
  });

  for (final (name, style, id) in [('single-line', SWT.SINGLE, 901), ('multi-line', SWT.MULTI | SWT.V_SCROLL, 902)]) {
    // Semantics off, as in production: with them on, a multi-line Text is a TextField.
    testWidgets('a $name Text reports the edit a typed character makes', semanticsEnabled: false, (tester) async {
      freshClient();
      await tester.pumpWidget(
        EvolveApp(
          theme: ThemeMode.light,
          contentWidget: DisplaySwt(value: VDisplay()..shells = [_shell(style, id)]),
        ),
      );
      await tester.pumpAndSettle();
      FocusRequests.instance.request(id);
      await tester.pumpAndSettle();

      final order = KeyEditOrder.instance;
      expect(order.editorFocused, isTrue, reason: 'the focused field takes typing and reports its edits');
      final sent = <String>[];
      order.forward(typed: true, send: () => sent.add('KeyDown a'));
      tester.testTextInput.updateEditingValue(
        const TextEditingValue(text: 'a', selection: TextSelection.collapsed(offset: 1)),
      );
      await tester.pump();
      order.forward(typed: false, send: () => sent.add('KeyUp a'));

      expect(sent, ['KeyDown a', 'KeyUp a'], reason: 'the KeyUp goes once the edit landed, not after a timeout');
      await tester.pumpAndSettle(const Duration(milliseconds: 300));
    });
  }
}
