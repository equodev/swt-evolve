// Issue #509 — Dart-side half: moving the sash must fire Selection events.
//
// Native SWT fires Selection continuously while the sash is dragged (see
// SwtSash.mouseDragged on every platform; on macOS the style is forced to
// SWT.SMOOTH and the sash also moves itself). Apps — Snippet 54, SashForm,
// a recorder view — resize the neighboring composites from those
// events, so with no during-drag events nothing relayouts while the user
// drags ("Sash movement does not resize composites").
//
// SashImpl (sash_evolve.dart) currently sends a single Selection only on
// drag END, so this test's during-drag assertion FAILS on current code.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sash.dart';
import 'package:swtflutter/src/gen/swt.dart';

/// Same as the generated SashSwt, except sendEvent captures outgoing events
/// instead of handing them to EquoCommService (which needs a live transport
/// that doesn't exist in a widget test). The State, the real GestureDetector,
/// and the real coordinate computation are untouched.
class _CapturingSashSwt extends SashSwt<VSash> {
  const _CapturingSashSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VSash val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VSash _verticalSash() => VSash()
  ..id = 7
  ..style = SWT.VERTICAL
  ..enabled = true
  ..bounds = (VRectangle()
    ..x = 195
    ..y = 0
    ..width = 10
    ..height = 260);

void main() {
  testWidgets('dragging a sash fires Selection events while it moves (#509)',
      (WidgetTester tester) async {
    final selections = <VEvent>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      // Mimic the parent Composite's NoLayout: the child is placed at its
      // bounds offset inside a fixed-size area.
      contentWidget: SizedBox(
        width: 390,
        height: 260,
        child: Stack(
          children: [
            Positioned(
              left: 195,
              top: 0,
              width: 10,
              height: 260,
              child: _CapturingSashSwt(
                value: _verticalSash(),
                onEvent: (ev, payload) {
                  if (ev == 'Selection/Selection' && payload != null) {
                    selections.add(payload);
                  }
                },
              ),
            ),
          ],
        ),
      ),
    ));
    await tester.pumpAndSettle();

    final center = tester.getCenter(find.byType(_CapturingSashSwt));
    final gesture = await tester.startGesture(center);
    // Two clearly-beyond-slop moves to the right, still mid-drag.
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump();
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump();

    expect(selections, isNotEmpty,
        reason: 'native SWT fires Selection continuously during a sash drag '
            '(SwtSash.mouseDragged); without those events the app cannot '
            'resize the composites while the sash moves (issue #509)');
    expect(selections.last.x, greaterThan(195),
        reason: 'during-drag Selection must carry the new position');

    await gesture.up();
    await tester.pump();

    expect(selections, isNotEmpty,
        reason: 'releasing the sash must fire a final Selection');
    final last = selections.last;
    expect(last.x, greaterThan(195),
        reason: 'the final Selection must carry the dragged-to x, not the '
            'original position');
    expect(last.width, 10);
    expect(last.height, 260);
  });
}
