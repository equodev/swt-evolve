// #465 — an Rcp App's Command Palette opens the selected item from a MouseListener on the
// underlying SWT List (mouseUp -> performCommand()), not from Selection/DefaultSelection.
// A List item click must therefore emit SWT MouseDown/MouseUp, in native order around the
// Selection, or the item never opens. Before the fix the item's tap detector consumed the
// pointer and only Selection was sent.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

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
  void sendSelectionSelection(VList val, VEvent? payload) =>
      calls.add('Selection:${val.selection}');
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
    'clicking a List item emits MouseDown, then Selection, then MouseUp (#465)',
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
        reason: 'App MouseListeners (e.g. a Command Palette) open the item on the '
            "List's mouseUp, with the selection already applied.",
      );
    },
  );
}
