// A plain (no-modifier) click on a MULTI List must select ONLY the clicked item,
// deselecting whatever was selected before — native SWT semantics. Ctrl/Cmd toggles and
// Shift extends; a bare click never accumulates. Seen on a multi-selection
// org.eclipse.swt.widgets.List used as the left-hand chooser of a preference page: clicking
// items one by one kept every previous item selected (selection grew [2] -> [0,2] -> [3,2,0])
// and the detail pane never followed the click. Before the fix the MULTI branch of
// _handleItemTap toggled on a plain click instead of replacing.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

/// Records every Selection the List impl fires, with the selection indices it carried.
class _RecordingListSwt extends ListSwt<VList> {
  final List<String> selections;
  const _RecordingListSwt({required VList value, required this.selections})
      : super(value: value);

  @override
  void sendSelectionSelection(VList val, VEvent? payload) =>
      selections.add('${val.selection}');
}

VList _multiList(List<String> items) => VList()
  ..id = 1
  ..style = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
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
    'plain clicks on a MULTI List replace the selection instead of accumulating',
    (tester) async {
      final selections = <String>[];
      final value = _multiList(['Alpha', 'Bravo', 'Charlie']);

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 300,
          height: 200,
          child: _RecordingListSwt(value: value, selections: selections),
        ),
      ));
      await tester.pump();

      // The selected/hover overlay Container covers the Text's centre, so warnIfMissed:false.
      await tester.tap(find.text('Alpha'), warnIfMissed: false);
      await tester.pump(Duration.zero);
      await tester.tap(find.text('Bravo'), warnIfMissed: false);
      await tester.pump(Duration.zero);
      await tester.tap(find.text('Charlie'), warnIfMissed: false);
      await tester.pump(Duration.zero);

      // Each plain click deselects the previous item: never an accumulating [0,1,2].
      expect(
        selections,
        equals(['[0]', '[1]', '[2]']),
        reason: 'A bare click on a MULTI list selects only the clicked item; '
            'previously the MULTI branch toggled, so selection accumulated.',
      );
    },
  );

  testWidgets(
    'Ctrl+click on a MULTI List toggles individual items (keeps multi-select usable)',
    (tester) async {
      final selections = <String>[];
      final value = _multiList(['Alpha', 'Bravo', 'Charlie']);

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 300,
          height: 200,
          child: _RecordingListSwt(value: value, selections: selections),
        ),
      ));
      await tester.pump();

      await tester.tap(find.text('Alpha'), warnIfMissed: false); // plain -> [0]
      await tester.pump(Duration.zero);

      // Ctrl+click accumulates (toggles on) rather than replacing — the opposite of a
      // plain click. Distinct items each round to avoid the double-tap detector.
      await tester.sendKeyDownEvent(LogicalKeyboardKey.controlLeft);
      await tester.tap(find.text('Bravo'), warnIfMissed: false); // ctrl -> add 1
      await tester.pump(Duration.zero);
      await tester.tap(find.text('Charlie'), warnIfMissed: false); // ctrl -> add 2
      await tester.pump(Duration.zero);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.controlLeft);

      expect(selections, equals(['[0]', '[0, 1]', '[0, 1, 2]']));
    },
  );

  testWidgets(
    'Shift+click on a MULTI List extends a contiguous range from the anchor',
    (tester) async {
      final selections = <String>[];
      final value = _multiList(['Alpha', 'Bravo', 'Charlie']);

      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: SizedBox(
          width: 300,
          height: 200,
          child: _RecordingListSwt(value: value, selections: selections),
        ),
      ));
      await tester.pump();

      await tester.tap(find.text('Alpha'), warnIfMissed: false); // plain -> anchor [0]
      await tester.pump(Duration.zero);

      await tester.sendKeyDownEvent(LogicalKeyboardKey.shiftLeft);
      await tester.tap(find.text('Charlie'), warnIfMissed: false); // shift -> [0,1,2]
      await tester.pump(Duration.zero);
      await tester.sendKeyUpEvent(LogicalKeyboardKey.shiftLeft);

      expect(selections, equals(['[0]', '[0, 1, 2]']));
    },
  );
}
