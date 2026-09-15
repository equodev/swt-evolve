// A Shell takes its initial focus once, when it is shown. Any later update it receives must leave
// the keyboard where it is: a Shell is re-described on every state push, and moving focus for each
// one walks it round the Shell's controls, so a field being typed into loses the keyboard between
// keystrokes.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
// The io transport directly: `flutter test` runs on the VM, where comm.dart resolves to it —
// but the analyzer resolves the conditional export to the web variant, which has no test hook.
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

const int _base = 8400;
const int _shellId = _base + 1;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// Two of them: without somewhere else for the keyboard to land, focus moving off the first field
/// would wrap straight back to it and the defect would not show.
VText _field(int index) => VText()
  ..swt = 'Text'
  ..id = _base + 2 + index
  ..style = SWT.BORDER
  ..enabled = true
  ..editable = true
  ..visible = true
  ..text = ''
  ..bounds = _rect(10, 10 + index * 30, 200, 24);

VButton _okButton() => VButton()
  ..swt = 'Button'
  ..id = _base + 9
  ..style = SWT.PUSH
  ..enabled = true
  ..visible = true
  ..text = 'OK'
  ..bounds = _rect(10, 80, 60, 24);

VShell _dialogShell() => VShell()
  ..swt = 'Shell'
  ..id = _shellId
  ..style = SWT.DIALOG_TRIM
  ..text = 'dialog'
  ..visible = true
  ..bounds = _rect(0, 0, 400, 200)
  ..children = [_field(0), _field(1), _okButton()];

VDisplay _display() => VDisplay()
  ..swt = 'Display'
  ..id = _base
  ..shells = [_dialogShell()]
  ..activeShellId = _shellId;

/// Delivers an inbound frame exactly as the transport would (2-byte name length,
/// name, JSON body) so the channel subscription receives it.
void _receiveJson(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

bool _fieldHasFocus(WidgetTester tester, int index) =>
    tester.widget<TextField>(find.byType(TextField).at(index)).focusNode!.hasFocus;

void main() {
  testWidgets('a Shell re-described while a Text is focused leaves the keyboard in the field',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: _display()),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.byType(TextField).first);
    await tester.pumpAndSettle();
    expect(_fieldHasFocus(tester, 0), isTrue,
        reason: 'sanity: clicking the field gives it the keyboard');

    // What Java sends whenever anything about the Shell changes — here, twice, because one push
    // per keystroke is exactly the traffic a field being typed into produces.
    for (var i = 0; i < 2; i++) {
      _receiveJson('Shell/$_shellId', _dialogShell().toJson());
      await tester.pumpAndSettle();
    }

    expect(_fieldHasFocus(tester, 0), isTrue,
        reason: 'the Shell was already visible, so the update is not an opening and must not '
            'move focus off the field');
  });
}
