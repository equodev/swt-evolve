// A SashForm's divider is its Sash child on every SWT, so a press, drag or release on it is the
// Sash's MouseDown/MouseUp, never the SashForm's own.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sashform.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingSashFormSwt extends SashFormSwt<VSashForm> {
  const _CapturingSashFormSwt({required super.value, required this.events});

  final List<String> events;

  @override
  void sendEvent(VSashForm val, String ev, VEvent? payload) => events.add(ev);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _label(int id, int x) => VLabel()
  ..id = id
  ..style = SWT.NONE
  ..text = 'pane $id'
  ..visible = true
  ..enabled = true
  ..bounds = _rect(x, 0, 195, 300);

VSashForm _sashForm() => VSashForm()
  ..id = 42
  ..style = SWT.HORIZONTAL
  ..enabled = true
  ..visible = true
  ..sashWidth = 10
  ..weights = [1, 1]
  ..bounds = _rect(0, 0, 400, 300)
  ..children = [_label(1, 0), _label(2, 205)];

void main() {
  testWidgets('a press, drag and release on the divider is not reported as the SashForm\'s own',
      (tester) async {
    final events = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 400,
          height: 300,
          child: _CapturingSashFormSwt(value: _sashForm(), events: events),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    // Two equal panes in 400 minus the 10-wide sash: the form's centre is on the divider.
    final divider = find.byKey(const ValueKey('sashform-sash-0'));
    expect(divider, findsOneWidget);
    final gesture = await tester.startGesture(tester.getCenter(divider));
    await tester.pump();
    await gesture.moveBy(const Offset(20, 0));
    await tester.pump();
    await gesture.up();
    await tester.pumpAndSettle();

    final pointer = events.where((e) => e.startsWith('Mouse/')).toList();
    expect(pointer, isEmpty,
        reason: 'the divider is the Sash, so the SashForm must not report its press and release');
  });
}
