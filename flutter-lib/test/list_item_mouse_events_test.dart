// An app's own MouseListener on the underlying SWT List can open the selected item directly
// on mouseUp (mouseUp -> performCommand()), not via Selection/DefaultSelection.
// A List item click must therefore emit SWT MouseDown/MouseUp, in native order around the
// Selection, or the item never opens. Before the fix the item's tap detector consumed the
// pointer and only Selection was sent.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/double_tap_detector.dart';

/// Records the events the List impl fires on `widget`, so the test can assert both that
/// MouseUp is emitted and that it comes after the selection is applied.
class _RecordingListSwt extends ListSwt<VList> {
  final List<String> calls;
  const _RecordingListSwt({required VList value, required this.calls})
      : super(value: value);

  @override
  void sendMouseMouseDown(VList val, VEvent? payload) => calls.add('MouseDown');

  @override
  void sendMouseMouseUp(VList val, VEvent? payload) => calls.add('MouseUp');

  @override
  void sendMouseMouseDoubleClick(VList val, VEvent? payload) => calls.add('MouseDoubleClick');

  @override
  void sendSelectionSelection(VList val, VEvent? payload) =>
      calls.add('Selection:${val.selection}');

  @override
  void sendSelectionDefaultSelection(VList val, VEvent? payload) =>
      calls.add('DefaultSelection:${payload?.segments}:${payload?.stateMask}');
}

/// One press and release with explicit timestamps: the click count chains on PointerEvent.timeStamp,
/// which tester.tap leaves at a constant.
Future<void> _click(WidgetTester tester, Offset at, Duration when) async {
  final gesture = await tester.createGesture(buttons: kPrimaryButton);
  await gesture.down(at, timeStamp: when);
  await gesture.up(timeStamp: when + const Duration(milliseconds: 20));
}

VList _list(List<String> items) => VList()
  ..id = 1
  ..style = SWT.SINGLE
  ..enabled = true
  ..items = items
  ..selection = <int>[]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 200);

void main() {
  testWidgets(
    'clicking a List item emits MouseDown, then Selection, then MouseUp',
    (tester) async {
      final calls = <String>[];
      final value = _list(['Alpha', 'Bravo', 'Charlie']);

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 300,
          height: 200,
          child: _RecordingListSwt(value: value, calls: calls),
        ),
      ));
      await tester.pump();

      // The selected/hover overlay Container sits above the Text in the item's Stack, so the
      // Text's exact centre is covered; the tap still lands on the item's tap detector.
      await tester.tap(find.text('Bravo'), warnIfMissed: false);
      await tester.pump(Duration.zero);

      expect(
        calls,
        equals(['MouseDown', 'Selection:[1]', 'MouseUp']),
        reason: "App MouseListeners open the item on the List's mouseUp, with the "
            'selection already applied.',
      );
    },
  );

  testWidgets(
    'the second press of a double click reports DefaultSelection, with the button held, and no '
    'Selection',
    (tester) async {
      final calls = <String>[];
      final value = _list(['Alpha', 'Bravo', 'Charlie']);

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 300,
          height: 200,
          child: _RecordingListSwt(value: value, calls: calls),
        ),
      ));
      await tester.pump();

      final bravo = tester.getCenter(find.text('Bravo'));
      await _click(tester, bravo, const Duration(seconds: 1));
      await _click(tester, bravo, const Duration(seconds: 1, milliseconds: 150));
      await tester.pump(Duration.zero);

      expect(
        calls,
        equals([
          'MouseDown', 'Selection:[1]', 'MouseUp',
          'MouseDown', 'MouseDoubleClick', 'DefaultSelection:[1]:${SWT.BUTTON1}', 'MouseUp',
        ]),
        reason: 'Win32 and GTK report the double click before its release, with the button held, '
            'and the second press selects nothing new',
      );
    },
  );

  testWidgets(
    'a tap through the semantics tree selects the item, once',
    (tester) async {
      final handle = tester.ensureSemantics();
      final calls = <String>[];
      final value = _list(['Alpha', 'Bravo', 'Charlie']);

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 300,
          height: 200,
          child: _RecordingListSwt(value: value, calls: calls),
        ),
      ));
      await tester.pump();

      final node = tester.getSemantics(find.text('Bravo'));
      tester.binding.pipelineOwner.semanticsOwner!.performAction(node.id, SemanticsAction.tap);
      await tester.pump(Duration.zero);

      expect(calls.where((c) => c.startsWith('Selection')).toList(), equals(['Selection:[1]']),
          reason: 'accessibility and test drivers tap through the semantics tree, with no pointer '
              'press before the tap');
      handle.dispose();
    },
  );

  testWidgets(
    'two quick taps on an item through the semantics tree report DefaultSelection, with no button',
    (tester) async {
      final handle = tester.ensureSemantics();
      var now = DateTime(2026);
      DoubleTapDetector.clock = () => now;
      addTearDown(() => DoubleTapDetector.clock = DateTime.now);
      final calls = <String>[];
      final value = _list(['Alpha', 'Bravo', 'Charlie']);

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 300,
          height: 200,
          child: _RecordingListSwt(value: value, calls: calls),
        ),
      ));
      await tester.pump();

      final semantics = tester.binding.pipelineOwner.semanticsOwner!;
      semantics.performAction(tester.getSemantics(find.text('Bravo')).id, SemanticsAction.tap);
      await tester.pump(Duration.zero);
      now = now.add(const Duration(milliseconds: 100));
      semantics.performAction(tester.getSemantics(find.text('Bravo')).id, SemanticsAction.tap);
      await tester.pump(Duration.zero);

      expect(calls, equals(['Selection:[1]', 'DefaultSelection:[1]:0']),
          reason: 'with semantics on, the web engine turns a pointer click on an item into a '
              'semantics tap, so a double click arrives as two of them');
      handle.dispose();
    },
  );
}
