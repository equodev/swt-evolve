// A click that switches a TabFolder's tab takes the focus, as on GTK; a click on the selected tab
// does not.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';

class _RecordingTabFolderSwt extends TabFolderSwt<VTabFolder> {
  final List<String> calls;
  const _RecordingTabFolderSwt({required VTabFolder value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VTabFolder val, String ev, VEvent? payload) => calls.add(ev);
}

VTabFolder _folder() {
  final one = VTabItem()
    ..id = 11
    ..text = 'One';
  return VTabFolder()
    ..id = 10
    ..style = SWT.TOP
    ..enabled = true
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 300
      ..height = 30)
    ..items = [
      one,
      VTabItem()
        ..id = 12
        ..text = 'Two',
    ]
    ..selection = [one];
}

Future<List<String>> _tap(WidgetTester tester, String label) async {
  final calls = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
          width: 300,
          height: 30,
          child: KeyedSubtree(key: UniqueKey(), child: _RecordingTabFolderSwt(value: _folder(), calls: calls))),
    ),
  ));
  await tester.pump();
  while (tester.takeException() != null) {}
  calls.clear();

  await tester.tap(find.text(label));
  await tester.pump(const Duration(seconds: 1));
  return calls;
}

void main() {
  testWidgets('a click that switches tabs reports the focus before the selection', (tester) async {
    final calls = await _tap(tester, 'Two');
    final focus = calls.indexOf('Focus/FocusIn');
    expect(focus, isNonNegative, reason: '$calls');
    expect(calls.indexWhere((c) => c == 'Selection/Selection'), greaterThan(focus), reason: '$calls');
  });

  testWidgets('after a click that switches tabs, keys and Tab start from the pressed tab', (tester) async {
    final calls = await _tap(tester, 'Two');
    final pressed = Focus.of(tester.element(find.text('Two')));
    expect(pressed.hasPrimaryFocus, isTrue, reason: '${FocusManager.instance.primaryFocus}');

    await tester.sendKeyEvent(LogicalKeyboardKey.keyA);
    await tester.pump();
    expect(pressed.hasPrimaryFocus, isTrue, reason: '${FocusManager.instance.primaryFocus}');

    calls.clear();
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
    expect(calls.where((c) => c.startsWith('Focus/')), isEmpty, reason: '$calls');
    expect(Focus.of(tester.element(find.text('One'))).hasPrimaryFocus, isTrue,
        reason: '${FocusManager.instance.primaryFocus}');
  });

  testWidgets('a click on the selected tab takes no focus', (tester) async {
    final calls = await _tap(tester, 'One');
    expect(calls, isNot(contains('Focus/FocusIn')));
  });

  testWidgets('an arrow key from outside the folder does not walk into its tabs', (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, height: 30, child: _RecordingTabFolderSwt(value: _folder(), calls: calls)),
      ),
    ));
    await tester.pump();
    while (tester.takeException() != null) {}
    calls.clear();

    for (final key in [LogicalKeyboardKey.arrowRight, LogicalKeyboardKey.arrowUp, LogicalKeyboardKey.arrowDown]) {
      await tester.sendKeyEvent(key);
      await tester.pump();
    }
    expect(calls.where((c) => c.startsWith('Focus/')), isEmpty, reason: '$calls');
  });

  testWidgets('Tab onto a tab focuses the folder once; moving between its tabs is no focus change',
      (tester) async {
    final calls = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 300, height: 30, child: _RecordingTabFolderSwt(value: _folder(), calls: calls)),
      ),
    ));
    await tester.pump();
    while (tester.takeException() != null) {}
    calls.clear();

    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
    expect(calls.where((c) => c == 'Focus/FocusIn'), hasLength(1), reason: '$calls');

    calls.clear();
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
    expect(calls.where((c) => c.startsWith('Focus/')), isEmpty, reason: '$calls');
  });
}
