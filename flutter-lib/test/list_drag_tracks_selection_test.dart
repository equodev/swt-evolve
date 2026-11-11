// A press on a List item dragged over the items below it: the selection follows the pointer, as a
// Win32 list box and Cocoa's table view track it. A SINGLE list ends with the item under the
// release selected; a MULTI list with the range from the pressed item to it.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

class _RecordingListSwt extends ListSwt<VList> {
  final List<String> selections;
  const _RecordingListSwt({required VList value, required this.selections})
      : super(value: value);

  @override
  void sendSelectionSelection(VList val, VEvent? payload) =>
      selections.add('${val.selection}');
}

VList _list(int style) => VList()
  ..id = 1
  ..style = style
  ..enabled = true
  ..items = ['Alpha', 'Bravo', 'Charlie', 'Delta']
  ..selection = <int>[]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 200);

Future<List<String>> _dragFromAlphaToCharlie(WidgetTester tester, int style) async {
  final selections = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 300,
      height: 200,
      child: _RecordingListSwt(value: _list(style), selections: selections),
    ),
  ));
  await tester.pump();

  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await gesture.addPointer(location: tester.getCenter(find.text('Alpha')));
  await tester.pump();
  await gesture.down(tester.getCenter(find.text('Alpha')));
  await tester.pump(Duration.zero);
  for (final item in ['Bravo', 'Charlie']) {
    await gesture.moveTo(tester.getCenter(find.text(item)));
    await tester.pump(Duration.zero);
  }
  await gesture.up();
  await tester.pump(Duration.zero);
  await gesture.removePointer();
  return selections;
}

void main() {
  testWidgets('a drag over a SINGLE list selects each item it reaches', (tester) async {
    final selections = await _dragFromAlphaToCharlie(tester, SWT.SINGLE | SWT.V_SCROLL);

    expect(selections, equals(['[0]', '[1]', '[2]']));
  });

  testWidgets('a drag over a MULTI list selects the range from the pressed item', (tester) async {
    final selections = await _dragFromAlphaToCharlie(tester, SWT.MULTI | SWT.V_SCROLL);

    expect(selections, equals(['[0]', '[0, 1]', '[0, 1, 2]']));
  });
}
