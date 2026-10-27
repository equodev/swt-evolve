// A focused control inside a Group is that control's focus, not the Group's: reporting the Group
// makes it the focus holder, and the control is sent FocusOut (a cell editor closes as it opens).

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/group.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

class _RecordingGroupSwt extends GroupSwt<VGroup> {
  const _RecordingGroupSwt({required super.value, required this.sent});

  final List<String> sent;

  @override
  void sendEvent(VGroup val, String ev, VEvent? payload) => sent.add(ev);
}

void main() {
  testWidgets('focusing a control inside a Group sends the Group no FocusIn', (tester) async {
    final sent = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 200,
        child: _RecordingGroupSwt(
          sent: sent,
          value: VGroup()
            ..id = 1
            ..style = SWT.NONE
            ..text = ''
            ..visible = true
            ..enabled = true
            ..bounds = _rect(0, 0, 300, 200)
            ..children = [
              VText()
                ..id = 2
                ..style = SWT.SINGLE | SWT.BORDER
                ..text = 'inside'
                ..visible = true
                ..enabled = true
                ..bounds = _rect(10, 30, 200, 24),
            ],
        ),
      ),
    ));
    await tester.pumpAndSettle();

    await tester.showKeyboard(find.byType(EditableText));
    await tester.pumpAndSettle();

    expect(sent.where((ev) => ev.contains('FocusIn')), isEmpty);
  });
}
