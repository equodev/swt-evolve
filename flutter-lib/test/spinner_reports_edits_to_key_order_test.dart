// A Spinner's field registers with KeyEditOrder as a Text's does, so the KeyUp of a typed character
// waits for the edit it makes: SWT reports KeyDown, Verify, KeyUp. A character that leaves no number
// is held off the field and offered to Java, and that offer is the edit the KeyUp waits for.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/swt.dart';
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

const _spinnerId = 901;

VShell _shell() => VShell()
  ..id = 900
  ..style = SWT.SHELL_TRIM
  ..text = 'shell'
  ..visible = true
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [
    VSpinner()
      ..id = _spinnerId
      ..style = SWT.BORDER
      ..minimum = 0
      ..maximum = 100
      ..selection = 3
      ..digits = 0
      ..enabled = true
      ..visible = true
      ..bounds = _rect(10, 10, 160, 30),
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

  for (final (name, typed) in [('a letter', '3a'), ('a digit', '35')]) {
    testWidgets('a Spinner reports the edit $name typed into it makes', (tester) async {
      freshClient();
      await tester.pumpWidget(
        EvolveApp(
          theme: ThemeMode.light,
          contentWidget: DisplaySwt(value: VDisplay()..shells = [_shell()]),
        ),
      );
      await tester.pumpAndSettle();
      await tester.tap(find.byType(TextField));
      await tester.pumpAndSettle();

      final order = KeyEditOrder.instance;
      expect(order.editorFocused, isTrue, reason: 'the focused Spinner field takes typing and reports its edits');
      final sent = <String>[];
      order.forward(typed: true, send: () => sent.add('KeyDown'));
      tester.testTextInput.updateEditingValue(
        TextEditingValue(text: typed, selection: TextSelection.collapsed(offset: typed.length)),
      );
      await tester.pump();
      order.forward(typed: false, send: () => sent.add('KeyUp'));

      expect(sent, ['KeyDown', 'KeyUp'], reason: 'the KeyUp goes once the edit is reported, not after a timeout');
      await tester.pumpAndSettle(const Duration(milliseconds: 300));
    });
  }
}
