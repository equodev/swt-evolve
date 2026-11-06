// A TabFolder switches tabs on the press, as GTK and Win32 do: the Selection comes between the
// MouseDown and the MouseUp and carries the button held.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

class _RecordingTabFolderSwt extends TabFolderSwt<VTabFolder> {
  final void Function(String ev, VEvent? payload) onEvent;
  const _RecordingTabFolderSwt({required VTabFolder value, required this.onEvent}) : super(value: value);

  @override
  void sendEvent(VTabFolder val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VTabFolder _folder(int style) {
  final one = VTabItem()
    ..id = 11
    ..text = 'One';
  return VTabFolder()
    ..id = 10
    ..style = style
    ..enabled = true
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 300
      ..height = 60)
    ..items = [
      one,
      VTabItem()
        ..id = 12
        ..text = 'Two',
      VTabItem()
        ..id = 13
        ..text = 'Three',
    ]
    ..selection = [one];
}

Future<void> _pump(WidgetTester tester, int style, void Function(String ev, VEvent? payload) onEvent) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(width: 300, height: 60, child: _RecordingTabFolderSwt(value: _folder(style), onEvent: onEvent)),
    ),
  ));
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}
}

void main() {
  testWidgets('a press on another tab selects it before the release, with the button held', (tester) async {
    final calls = <String>[];
    VEvent? selection;
    await _pump(tester, SWT.TOP, (ev, payload) {
      calls.add(ev);
      if (ev == 'Selection/Selection') selection = payload;
    });
    calls.clear();

    final gesture = await tester.startGesture(tester.getCenter(find.text('Two')), kind: PointerDeviceKind.mouse);
    await tester.pump();
    expect(calls, contains('Selection/Selection'), reason: 'the press switches the tab: $calls');
    expect(selection!.index, 1);
    expect(selection!.stateMask, SWT.BUTTON1);
    expect(calls.indexOf('Selection/Selection'), greaterThan(calls.indexOf('Mouse/MouseDown')), reason: '$calls');

    await gesture.up();
    await tester.pump(const Duration(seconds: 1));
    expect(calls.where((c) => c == 'Selection/Selection'), hasLength(1), reason: 'the release selects nothing again: $calls');
  });

  testWidgets('a tap that comes with no pointer press, as an accessibility action, still selects the tab', (tester) async {
    final calls = <String>[];
    await _pump(tester, SWT.TOP, (ev, payload) => calls.add(ev));
    calls.clear();

    final semantics = tester.ensureSemantics();
    tester.semantics.tap(find.semantics.byLabel('Two'));
    await tester.pump(const Duration(seconds: 1));
    semantics.dispose();

    expect(calls, contains('Selection/Selection'), reason: '$calls');
  });
}
