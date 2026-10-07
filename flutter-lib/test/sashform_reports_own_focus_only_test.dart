// A focused control inside a SashForm is that control's focus, not the SashForm's: reporting the
// SashForm makes it the focus holder, and the control is sent FocusOut (a cell editor closes as it opens).

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sashform.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

class _RecordingSashFormSwt extends SashFormSwt<VSashForm> {
  const _RecordingSashFormSwt({required super.value, required this.sent});

  final List<String> sent;

  @override
  void sendEvent(VSashForm val, String ev, VEvent? payload) => sent.add(ev);
}

VText _text(int id, int x) => VText()
  ..id = id
  ..style = SWT.SINGLE | SWT.BORDER
  ..text = 'inside'
  ..visible = true
  ..enabled = true
  ..bounds = _rect(x, 0, 140, 24);

void main() {
  testWidgets('focusing a control inside a SashForm sends the SashForm no FocusIn', (tester) async {
    final sent = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 200,
        child: _RecordingSashFormSwt(
          sent: sent,
          value: VSashForm()
            ..id = 1
            ..style = SWT.HORIZONTAL
            ..visible = true
            ..enabled = true
            ..bounds = _rect(0, 0, 300, 200)
            ..weights = [1, 1]
            ..children = [_text(2, 0), _text(3, 150)],
        ),
      ),
    ));
    await tester.pumpAndSettle();

    await tester.showKeyboard(find.byType(EditableText).first);
    await tester.pumpAndSettle();

    expect(sent.where((ev) => ev.contains('FocusIn')), isEmpty);
  });
}
