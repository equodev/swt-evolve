// A drag is announced before anything this side does about it.
//
// Both go to Java over the same channel and are handled in the order they arrive, so whatever is
// sent first is also worked on first. A tab drag selects the tab it starts from, and selecting one
// the workbench has not rendered makes it render the view - measured on a live workbench at about
// two seconds of a busy UI thread, with DragDetect waiting behind it. A drag that reaches the
// workbench after the gesture has ended is a drag that never happened.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, String ev, VEvent? payload) => onEvent(ev, payload);
}

const _labels = ['Section 3 (TVDSS)', 'Data Operations', 'Stratigraphic Modeling'];

VCTabFolder _folder() => VCTabFolder()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..selection = 0
  ..items = List.generate(
    _labels.length,
    (i) => VCTabItem()
      ..id = 100 + i
      ..text = _labels[i]
      ..showing = true,
  );

void main() {
  testWidgets('the drag is announced before the tab it started on is selected',
      (WidgetTester tester) async {
    final sent = <String>[];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 700,
        height: 240,
        child: _CapturingCTabFolderSwt(
          value: _folder(),
          onEvent: (ev, _) => sent.add(ev),
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    // A tab that is not the selected one, which is the case that makes the far side do the work.
    final start = tester.getCenter(find.text(_labels[2]));
    final gesture = await tester.startGesture(start);
    await tester.pump(const Duration(milliseconds: 50));
    for (var i = 1; i <= 8; i++) {
      await gesture.moveTo(Offset.lerp(start, start + const Offset(0, 140), i / 8)!);
      await tester.pump(const Duration(milliseconds: 20));
    }

    final drag = sent.indexWhere((e) => e.endsWith('DragDetect'));
    final selection = sent.indexWhere((e) => e.endsWith('Selection/Selection'));
    expect(drag, isNonNegative, reason: 'the workbench is told a drag started');
    expect(selection, isNonNegative, reason: 'and the tab it started on is selected');
    expect(drag, lessThan(selection),
        reason: 'the far side works on these in the order they are sent, and selecting the tab '
            'can take the workbench seconds');

    await gesture.up();
    await tester.pumpAndSettle();
    await tester.pump(const Duration(milliseconds: 400));
    while (tester.takeException() != null) {}
  });
}
