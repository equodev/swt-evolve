// A drag has to select the tab it starts from, the way a native CTabFolder does on mouse down.
//
// A workbench renders only the selected part of a stack, so a drag from a tab that is not selected
// hands it a part with no widget: the gesture is dropped, or it fails on the null. Measured on a
// live workbench - dragging the drawn tab of a stack whose selected tab was scrolled out moved
// nothing three times over, and the same drag worked immediately after a click selected that tab.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';

/// Captures what would go to Java instead of needing a live transport.
class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VCTabFolder _folder({required bool dragSource}) => VCTabFolder()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..selection = 0
  ..dragSource = dragSource
  ..items = List.generate(
    3,
    (i) => VCTabItem()
      ..id = 100 + i
      ..text = 'Tab $i'
      ..showing = true,
  );

Widget _host(Widget folder) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 600, height: 240, child: folder),
    );

Future<List<VEvent>> selectionsWhileDragging(
  WidgetTester tester, {
  required bool dragSource,
}) async {
  final events = <MapEntry<String, VEvent?>>[];
  await tester.pumpWidget(_host(_CapturingCTabFolderSwt(
    value: _folder(dragSource: dragSource),
    onEvent: (ev, payload) => events.add(MapEntry(ev, payload)),
  )));
  await tester.pump();
  while (tester.takeException() != null) {}

  final start = tester.getCenter(find.text('Tab 2'));
  final gesture = await tester.startGesture(start);
  await tester.pump(const Duration(milliseconds: 50));
  for (var i = 1; i <= 8; i++) {
    await gesture.moveTo(Offset.lerp(start, start - const Offset(160, 0), i / 8)!);
    await tester.pump(const Duration(milliseconds: 20));
  }
  await gesture.up();
  await tester.pumpAndSettle();
  await tester.pump(const Duration(milliseconds: 400));
  while (tester.takeException() != null) {}

  return events
      .where((e) => e.key == 'Selection/Selection')
      .map((e) => e.value!)
      .toList();
}

void main() {
  testWidgets('a tab drag selects the tab it starts from', (WidgetTester tester) async {
    final selections = await selectionsWhileDragging(tester, dragSource: false);

    expect(selections.map((e) => e.index), contains(2),
        reason: 'the workbench renders the selected part, and it is the one being dragged');
  });

  testWidgets('it does so with an application DragSource too', (WidgetTester tester) async {
    final selections = await selectionsWhileDragging(tester, dragSource: true);

    expect(selections.map((e) => e.index), contains(2));
  });
}
