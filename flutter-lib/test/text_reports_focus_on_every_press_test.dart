// Java can move its focus control away from a Text without the client's focus moving with it: a part
// activated from a tab click focuses its own default control. A press on the field then changes no
// focus on the client, but natively it focuses the field again, so the Text has to report FocusIn on
// the press itself; otherwise Java keeps routing the field's keys to the other control.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';

class _CapturingTextSwt extends TextSwt<VText> {
  const _CapturingTextSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VText val, String ev, VEvent? payload) => onEvent(ev);
}

VText _search() => VText()
  ..swt = 'Text'
  ..id = 1
  ..style = SWT.SINGLE | SWT.SEARCH
  ..enabled = true
  ..editable = true
  ..visible = true
  ..text = ''
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 24);

void main() {
  testWidgets('a press on a Text that already holds the client focus reports FocusIn again', (tester) async {
    final events = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Center(
        child: SizedBox(
          width: 160,
          height: 24,
          child: _CapturingTextSwt(value: _search(), onEvent: events.add),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.byType(TextField));
    await tester.pumpAndSettle();
    expect(events, contains('Focus/FocusIn'));

    events.clear();
    await tester.tap(find.byType(TextField));
    await tester.pumpAndSettle();
    expect(events, contains('Focus/FocusIn'),
        reason: 'the second press focuses the field again natively; without the report Java keeps '
            'whatever control it moved its focus to in between');
    expect(events, isNot(contains('Focus/FocusOut')));
  });
}
