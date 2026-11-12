// An arrow moves no focus between controls on the client: Java traverses a radio group or a tool bar
// by its own rules, and the control Flutter's directional traversal finds in that direction (a
// button to the group's left) is not the radio SWT moves to.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';

class _RecordingButtonSwt extends ButtonSwt<VButton> {
  final List<String> focusIns;
  const _RecordingButtonSwt({required super.value, required this.focusIns});

  @override
  void sendFocusFocusIn(VButton val, VEvent? payload) => focusIns.add(val.text ?? '');
}

VButton _radio(int id, String text) => VButton()
  ..id = id
  ..style = SWT.RADIO
  ..enabled = true
  ..visible = true
  ..text = text
  ..bounds = (VRectangle()
    ..width = 100
    ..height = 30);

void main() {
  tearDown(FocusRequests.instance.reset);

  testWidgets('an arrow in a focused radio leaves the focus where it is', (tester) async {
    final focusIns = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          for (final (id, text) in [(1, 'Left'), (2, 'Right')])
            SizedBox(width: 100, height: 30, child: _RecordingButtonSwt(value: _radio(id, text), focusIns: focusIns)),
        ]),
      ),
    ));
    FocusRequests.instance.request(2);
    await tester.pump();
    expect(focusIns, ['Right']);

    for (final key in [LogicalKeyboardKey.arrowLeft, LogicalKeyboardKey.arrowUp, LogicalKeyboardKey.arrowRight]) {
      await tester.sendKeyEvent(key);
      await tester.pump();
    }
    expect(focusIns, ['Right'], reason: 'only Java moves the focus to another control on an arrow');
  });
}
