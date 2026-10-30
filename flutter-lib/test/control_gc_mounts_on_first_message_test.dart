// A control's GC is mounted when Java first sends it something, not with the control: most controls
// are never drawn through. A control still asks Java for its first paint, which Java answers only
// for one that paints, and what that answer sends mounts the GC, which draws it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'delivery/support/deliver.dart';
import 'support/gc_frames.dart';

const int _id = 7;

class _CapturingButtonSwt extends ButtonSwt<VButton> {
  const _CapturingButtonSwt({required super.value, required this.onEvent});

  final void Function(String ev) onEvent;

  @override
  void sendEvent(VButton val, String ev, VEvent? payload) => onEvent(ev);
}

VButton _button() => VButton()
  ..id = _id
  ..style = SWT.PUSH
  ..enabled = true
  ..visible = true
  ..text = 'Push'
  ..bounds = (VRectangle()
    ..width = 120
    ..height = 30);

Future<void> _mount(WidgetTester tester, List<String> events) async {
  freshClient();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(width: 120, height: 30, child: _CapturingButtonSwt(value: _button(), onEvent: events.add)),
  ));
  await tester.pumpAndSettle();
}

int _paints(List<String> events) => events.where((e) => e == 'Paint/Paint').length;

Finder get _gc => find.byWidgetPredicate((w) => w is GCSwt, skipOffstage: false);

void main() {
  testWidgets('a control nobody draws through mounts no GC, and asks Java for its paint once', (tester) async {
    final events = <String>[];
    await _mount(tester, events);

    expect(_gc, findsNothing);
    expect(_paints(events), 1);
  });

  testWidgets('what Java draws through it mounts the GC, which draws it and asks for a paint of its own',
      (tester) async {
    final events = <String>[];
    await _mount(tester, events);

    // One paint, as Java sends it: the GC's state, the op, and the entry that ends the paint.
    receive(
        'GC/$_id/b',
        GcFrame()
            .whole(VGC.empty()
              ..id = _id
              ..foreground = colourOf(200, 0, 0))
            .opNamed('drawLineintintintint', [0, 0, 100, 20])
            .escape('GC/$_id/gcDispose', {'fullRepaint': true})
            .bytes);
    await tester.pumpAndSettle();

    expect(_gc, findsOneWidget);
    expect((tester.state(_gc) as dynamic).paintOps as List, isNotEmpty,
        reason: 'what arrived before the GC listened is drawn');
    expect(_paints(events), 2, reason: 'a GC mounted late asks for a paint it listened to from the start');
  });
}
