// A drag that starts on a tab's close button raises no DragDetect, as in native SWT; the side that
// draws the close button is the one that decides.

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

const _labels = ['First View', 'Second View'];

VCTabFolder _folder(int id) => VCTabFolder()
  ..id = id
  ..style = SWT.CLOSE
  ..enabled = true
  ..selection = 0
  ..items = List.generate(
    _labels.length,
    (i) => VCTabItem()
      ..id = id * 10 + i
      ..text = _labels[i]
      ..showClose = true
      ..showing = true,
  );

Future<List<VEvent>> _dragFrom(WidgetTester tester, int folderId, Finder start) async {
  final dragDetects = <VEvent>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 600,
      height: 240,
      child: _CapturingCTabFolderSwt(
        value: _folder(folderId),
        onEvent: (ev, payload) {
          if (ev.endsWith('DragDetect') && payload != null) dragDetects.add(payload);
        },
      ),
    ),
  ));
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}

  final from = tester.getCenter(start);
  final gesture = await tester.startGesture(from);
  await tester.pump(const Duration(milliseconds: 50));
  for (var i = 1; i <= 8; i++) {
    await gesture.moveTo(Offset.lerp(from, from + const Offset(0, 140), i / 8)!);
    await tester.pump(const Duration(milliseconds: 20));
  }
  await gesture.up();
  await tester.pumpAndSettle();
  await tester.pump(const Duration(milliseconds: 400));
  while (tester.takeException() != null) {}
  return dragDetects;
}

void main() {
  testWidgets('a drag from the tab label raises DragDetect', (WidgetTester tester) async {
    final dragDetects = await _dragFrom(tester, 1, find.text(_labels[0]));

    expect(dragDetects, hasLength(1));
  });

  testWidgets('a drag from the close button raises none', (WidgetTester tester) async {
    final dragDetects = await _dragFrom(tester, 2, find.byIcon(Icons.close).first);

    expect(dragDetects, isEmpty,
        reason: 'the workbench would move the view whose close button was pressed');
  });
}
