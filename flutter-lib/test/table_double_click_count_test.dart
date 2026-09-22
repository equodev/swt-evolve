// A Table cell emits its own MouseDown instead of the one ControlImpl.wrap()
// sends, and it must carry the same truthful click count: 1 for the first
// click of a double-click, 2 for the second. JFace's ColumnViewer installs a
// mouse listener whose mouseDown handler is skipped when count == 2, so that a
// double-click activates a cell editor exactly once — through mouseDoubleClick.
// A cell reporting every down as count 1 defeats that guard and activates the
// editor twice, which opens two dialogs for one double-click.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/double_tap_detector.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

class _RecordingTableSwt extends TableSwt<VTable> {
  final List<String> calls;
  const _RecordingTableSwt({required VTable value, required this.calls})
      : super(value: value);

  @override
  void sendEvent(VTable val, String ev, VEvent? payload) =>
      calls.add('$ev:${payload?.count}');
}

VTable _table() => VTable()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..headerVisible = false
  ..linesVisible = false
  ..columns = [
    VTableColumn()
      ..id = 2
      ..width = 400
      ..alignment = SWT.LEFT
      ..text = 'Col'
  ]
  ..items = [
    VTableItem()
      ..id = 10
      ..texts = ['Row 0'],
    VTableItem()
      ..id = 11
      ..texts = ['Row 1'],
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 800
    ..height = 400);

List<int?> _countsOf(List<String> calls, String event) => calls
    .where((c) => c.startsWith('$event:'))
    .map((c) => int.tryParse(c.substring(event.length + 1)))
    .toList();

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);
  tearDown(() => DoubleTapDetector.clock = DateTime.now);

  testWidgets('the second click of a row double-click reports count=2',
      (tester) async {
    // What is under test is how SWT numbers the clicks, not how fast they arrive.
    // DoubleTapDetector.registerTap() compares DateTime.now() against a real-time
    // window while testWidgets drives a fake clock, so the default 300ms couples
    // this assertion to how loaded the machine is: on a busy runner the two taps
    // below stop pairing and the second reports 1. Widen the window instead.
    setConfigFlags(ConfigFlags()..double_click_timeout_ms = 60000);

    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 800,
        height: 400,
        child: _RecordingTableSwt(value: _table(), calls: calls),
      ),
    ));
    await tester.pump();

    // Step the detector's clock by a fixed amount per tap. Left on the wall clock, the pairing
    // measures how long the harness took to run the two taps, so on a loaded machine the gap
    // exceeds the double-click window and the second click starts a fresh sequence.
    var fake = DateTime(2020);
    DoubleTapDetector.clock = () => fake;

    await tester.tap(find.text('Row 1'));
    await tester.pump(Duration.zero);
    fake = fake.add(const Duration(milliseconds: 20));
    await tester.tap(find.text('Row 1'));
    await tester.pump(Duration.zero);

    expect(_countsOf(calls, 'Mouse/MouseDown'), equals([1, 2]),
        reason: 'SWT numbers the clicks of a double-click on MouseDown; '
            'JFace ColumnViewer skips its editor activation on count == 2');
    expect(_countsOf(calls, 'Mouse/MouseDoubleClick'), equals([2]),
        reason: 'a double-click sends exactly one MouseDoubleClick');
    expect(calls.where((c) => c.startsWith('Selection/DefaultSelection:')),
        hasLength(1),
        reason: 'a double-click sends exactly one DefaultSelection');
  });
}
