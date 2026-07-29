// Issue #509 — SashForm half (the actual surface: the Windows Action
// Recorder is a 3-pane SashForm).
//
// Dragging a SashForm divider resizes the panels Flutter-locally
// (_SashFormLayout keeps _panelSizes and SashFormImpl.onWeightsChanged only
// does a local setState) — but the new weights are NEVER sent to Java.
// DartSashForm keeps the original weights, so the next Java-side layout (a
// window resize) recomputes the panels from stale weights and discards the
// user's drag. Verified live: after a divider drag,
// VSashForm.weights stayed [5,4,6] and maximizing the dialog snapped the
// panels back.
//
// This test drags a divider and asserts a Selection event carrying the new
// weights is sent to Java. It FAILS on current code (no event is sent).

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/sashform.dart';
import 'package:swtflutter/src/gen/swt.dart';

/// Same as the generated SashFormSwt, except sendEvent captures outgoing
/// events instead of handing them to EquoCommService (which needs a live
/// transport that doesn't exist in a widget test). The State, the real
/// GestureDetectors, and the real weight computation are untouched.
class _CapturingSashFormSwt extends SashFormSwt<VSashForm> {
  const _CapturingSashFormSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VSashForm val, String ev, VEvent? payload) =>
      onEvent(ev, payload);
}

VLabel _panel(int id) => VLabel()
  ..id = id
  ..style = SWT.NONE
  ..text = 'panel $id'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 195
    ..height = 300);

VSashForm _sashForm() => VSashForm()
  ..id = 42
  ..style = SWT.HORIZONTAL
  ..enabled = true
  ..sashWidth = 10
  ..weights = [1, 1]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 300)
  ..children = [_panel(1), _panel(2)];

void main() {
  testWidgets('dragging a SashForm divider sends the new weights to Java (#509)',
      (WidgetTester tester) async {
    final selections = <VEvent?>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: _CapturingSashFormSwt(
          value: _sashForm(),
          onEvent: (ev, payload) {
            if (ev == 'Selection/Selection') selections.add(payload);
          },
        ),
      ),
    ));
    await tester.pumpAndSettle();

    // Two equal panels in 400px minus the 10px sash → divider centered at x≈200.
    final center = tester.getCenter(find.byType(_CapturingSashFormSwt));
    final gesture = await tester.startGesture(center);
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump();
    await gesture.moveBy(const Offset(30, 0));
    await tester.pump();

    expect(selections, isNotEmpty,
        reason: 'the weights must live-sync to Java DURING the drag so the '
            'pane content reflows as the sash moves, not only at drop '
            '(issue #509)');
    final liveCount = selections.length;

    await gesture.up();
    await tester.pumpAndSettle();

    expect(selections.length, greaterThan(liveCount - 1),
        reason: 'releasing the sash must commit the final weights');
    expect(selections, isNotEmpty,
        reason: 'releasing a SashForm divider must send the new weights to '
            'Java — otherwise DartSashForm keeps the stale weights and the '
            'next Java-side layout (window resize) discards the drag '
            '(issue #509)');
    final payload = selections.last;
    expect(payload, isNotNull);
    final weights =
        payload!.text!.split(',').map(int.parse).toList(growable: false);
    expect(weights.length, 2);
    expect(weights[0], greaterThan(weights[1]),
        reason: 'the divider moved right, so the left panel must now weigh '
            'more than the right one');
  });
}
