// A right-click reports what a native SWT control reports for it: a Text keeps its caret, a Scale
// reports no Selection, and a Table row's press comes from the pointer, ahead of the menu request.

import 'package:flutter/gestures.dart' show kSecondaryMouseButton;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scale.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/text.dart';

VRectangle _bounds(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

Widget _app(Widget child) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(alignment: Alignment.topLeft, child: child),
    );

class _RecordingScaleSwt extends ScaleSwt<VScale> {
  final List<String> calls;
  const _RecordingScaleSwt({required VScale value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VScale val, String ev, VEvent? payload) => calls.add(ev);
}

class _RecordingTableSwt extends TableSwt<VTable> {
  final List<String> calls;
  const _RecordingTableSwt({required VTable value, required this.calls}) : super(value: value);

  @override
  void sendEvent(VTable val, String ev, VEvent? payload) =>
      calls.add('$ev:${payload?.button}@${payload?.x},${payload?.y}');
}

void main() {
  testWidgets('a right-click leaves a Text caret where it was', variant: TargetPlatformVariant.desktop(), (tester) async {
    final value = VText()
      ..swt = 'Text'
      ..id = 1
      ..style = 0
      ..enabled = true
      ..editable = true
      ..text = 'abc'
      ..bounds = _bounds(200, 30);
    await tester.pumpWidget(_app(SizedBox(width: 200, height: 30, child: TextSwt<VText>(value: value))));
    await tester.pump();

    final field = find.byType(EditableText);
    final controller = tester.widget<EditableText>(field).controller;
    expect(controller.selection.isValid, isFalse, reason: 'the field has never held a caret');

    await tester.tapAt(tester.getCenter(field), buttons: kSecondaryMouseButton);
    await tester.pumpAndSettle();

    expect(controller.selection, const TextSelection.collapsed(offset: 0));
    await tester.pump(const Duration(seconds: 2));
  });

  testWidgets('a right-click on a Scale reports no Selection', (tester) async {
    final calls = <String>[];
    final value = VScale()
      ..id = 1
      ..style = SWT.HORIZONTAL
      ..enabled = true
      ..minimum = 0
      ..maximum = 100
      ..selection = 50
      ..bounds = _bounds(300, 40);
    await tester.pumpWidget(_app(SizedBox(width: 300, height: 40, child: _RecordingScaleSwt(value: value, calls: calls))));
    await tester.pump();

    await tester.tapAt(tester.getCenter(find.byType(_RecordingScaleSwt)), buttons: kSecondaryMouseButton);
    await tester.pump();

    expect(calls.where((c) => c.startsWith('Selection/')), isEmpty);
    await tester.pump(const Duration(seconds: 2));
  });

  testWidgets('a right-click on a Table row is pressed where the pointer is, before MenuDetect',
      (tester) async {
    final calls = <String>[];
    final value = VTable()
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
      ]
      ..bounds = _bounds(800, 400);
    await tester.pumpWidget(_app(SizedBox(width: 800, height: 400, child: _RecordingTableSwt(value: value, calls: calls))));
    await tester.pump();

    final origin = tester.getTopLeft(find.byType(_RecordingTableSwt));
    final at = tester.getCenter(find.text('Row 0')) + const Offset(30, 0);
    await tester.tapAt(at, buttons: kSecondaryMouseButton);
    await tester.pump();

    final local = at - origin;
    final press = calls.indexWhere((c) => c.startsWith('Mouse/MouseDown:3'));
    final menu = calls.indexWhere((c) => c.startsWith('MenuDetect/'));
    expect(press, isNonNegative);
    expect(calls[press], 'Mouse/MouseDown:3@${local.dx.round()},${local.dy.round()}');
    expect(menu, greaterThan(press));
    await tester.pump(const Duration(seconds: 2));
  });
}
