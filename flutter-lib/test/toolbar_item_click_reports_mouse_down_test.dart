// A click that lands on a tool item still has to reach Java as SWT.MouseDown on the bar. A tool
// item is an Item, not a Control, so nothing under it forwards the press on its own -- and an
// application that dismisses a popup from a Display-level MouseDown filter, which is how SWT
// applications close one when the user clicks elsewhere, never hears about the click otherwise.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

class _RecordingToolBarSwt extends ToolBarSwt<VToolBar> {
  const _RecordingToolBarSwt({required super.value, required this.calls});

  final List<String> calls;

  @override
  void sendMouseMouseDown(VToolBar val, VEvent? payload) => calls.add('down');

  @override
  void sendMouseMouseUp(VToolBar val, VEvent? payload) => calls.add('up');
}

VToolBar _toolBar() => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 2
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Item',
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 300
    ..height = 40);

void main() {
  testWidgets('a click on a tool item reports MouseDown on the bar', (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 300,
        height: 40,
        child: _RecordingToolBarSwt(value: _toolBar(), calls: calls),
      ),
    ));
    await tester.pumpAndSettle();

    final item = find.text('Item');
    expect(item, findsOneWidget);
    final gesture = await tester.createGesture(buttons: kPrimaryButton);
    await gesture.down(tester.getCenter(item));
    await gesture.up();
    await tester.pump(Duration.zero);

    expect(calls, contains('down'));
  });
}
