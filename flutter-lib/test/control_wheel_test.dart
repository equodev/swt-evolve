// The wheel is reported to the control under the pointer, as three lines per event. A control that
// keeps the wheel (a Table) is the only one to report it; one that passes it on (a Button) is
// reported again by each parent on the pointer's path, as natively.

import 'package:flutter/gestures.dart' show PointerDeviceKind, PointerScrollEvent;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart' show EquoCommBase;
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/impl/widget_config.dart' show resetConfigFlags;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VButton _button(int id, VRectangle bounds) => VButton()
  ..id = id
  ..style = SWT.PUSH
  ..enabled = true
  ..text = 'OK'
  ..bounds = bounds;

VTable _table(int id, VRectangle bounds) => VTable()
  ..id = id
  ..style = SWT.SINGLE
  ..enabled = true
  ..bounds = bounds;

VComposite _composite(VControl child) => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..bounds = _rect(0, 0, 200, 120)
  ..children = [child];

/// Pumps [content], sends one wheel event at [at] (global) and returns every MouseWheel sent to Java.
Future<List<String>> _wheel(WidgetTester tester, Widget content, Offset Function() at) async {
  final sent = <String>[];
  EquoCommBase.sendObserverForTesting = (action, payload) {
    if (action.endsWith('/MouseWheel/MouseWheel')) {
      final e = payload as VEvent;
      sent.add('$action:${e.count}@${e.x},${e.y}');
    }
  };
  addTearDown(() => EquoCommBase.sendObserverForTesting = null);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(alignment: Alignment.topLeft, child: content),
  ));
  await tester.pump();
  sent.clear();
  await tester.sendEventToBinding(PointerScrollEvent(position: at(), scrollDelta: const Offset(0, 50)));
  await tester.pump();
  await tester.pump(const Duration(seconds: 2));
  return sent;
}

/// As [_wheel], for a two-finger trackpad swipe up at [at].
Future<List<String>> _swipe(WidgetTester tester, Widget content, Offset Function() at) async {
  final sent = <String>[];
  EquoCommBase.sendObserverForTesting = (action, payload) {
    if (action.endsWith('/MouseWheel/MouseWheel')) sent.add(action);
  };
  addTearDown(() => EquoCommBase.sendObserverForTesting = null);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(alignment: Alignment.topLeft, child: content),
  ));
  await tester.pump();
  sent.clear();
  final gesture = await tester.createGesture(kind: PointerDeviceKind.trackpad);
  await gesture.panZoomStart(at());
  await gesture.panZoomUpdate(at(), pan: const Offset(0, -30));
  await tester.pump();
  await gesture.panZoomEnd();
  await tester.pump(const Duration(seconds: 2));
  return sent;
}

void main() {
  setUp(resetConfigFlags);

  testWidgets('a wheel over a button is reported to it, where the pointer is, as three lines', (tester) async {
    final sent = await _wheel(
      tester,
      SizedBox(width: 160, height: 40, child: ButtonSwt<VButton>(value: _button(2, _rect(0, 0, 160, 40)))),
      () => tester.getTopLeft(find.byType(ButtonSwt<VButton>)) + const Offset(80, 20),
    );
    expect(sent, ['Button/2/MouseWheel/MouseWheel:-3@80,20']);
  });

  testWidgets('a wheel over a table in a composite is reported by the table only', (tester) async {
    final sent = await _wheel(
      tester,
      SizedBox(width: 200, height: 120, child: CompositeSwt<VComposite>(value: _composite(_table(3, _rect(10, 10, 150, 80))))),
      () => tester.getTopLeft(find.byType(CompositeSwt<VComposite>)) + const Offset(60, 50),
    );
    expect(sent.map((s) => s.split('/').take(2).join('/')), ['Table/3']);
  });

  testWidgets('a wheel over a styled text in a composite is reported by the styled text only', (tester) async {
    final text = VStyledText()
      ..id = 4
      ..style = SWT.MULTI | SWT.V_SCROLL
      ..enabled = true
      ..text = List.generate(40, (i) => 'line $i').join('\n')
      ..bounds = _rect(10, 10, 150, 80);
    final sent = await _wheel(
      tester,
      SizedBox(width: 200, height: 120, child: CompositeSwt<VComposite>(value: _composite(text))),
      () => tester.getTopLeft(find.byType(CompositeSwt<VComposite>)) + const Offset(60, 50),
    );
    expect(sent.map((s) => s.split('/').take(2).join('/')), ['StyledText/4']);
  });

  testWidgets('a swipe over a styled text in a composite is reported by the styled text only', (tester) async {
    final text = VStyledText()
      ..id = 4
      ..style = SWT.MULTI | SWT.V_SCROLL
      ..enabled = true
      ..text = List.generate(40, (i) => 'line $i').join('\n')
      ..bounds = _rect(10, 10, 150, 80);
    final sent = await _swipe(
      tester,
      SizedBox(width: 200, height: 120, child: CompositeSwt<VComposite>(value: _composite(text))),
      () => tester.getTopLeft(find.byType(CompositeSwt<VComposite>)) + const Offset(60, 50),
    );
    expect(sent.map((s) => s.split('/').take(2).join('/')).toSet(), {'StyledText/4'},
        reason: 'the StyledText scrolls by the swipe, so it keeps it, as it keeps a wheel');
  });

  testWidgets('a wheel over a button in a composite is reported by the button, then the composite', (tester) async {
    final sent = await _wheel(
      tester,
      SizedBox(width: 200, height: 120, child: CompositeSwt<VComposite>(value: _composite(_button(2, _rect(10, 10, 150, 40))))),
      () => tester.getTopLeft(find.byType(CompositeSwt<VComposite>)) + const Offset(60, 30),
    );
    expect(sent.map((s) => s.split('/').take(2).join('/')), ['Button/2', 'Composite/1']);
  });
}
