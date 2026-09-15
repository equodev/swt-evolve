// What the client is holding, measured through the real tree.
//
// The registry keeps a widget's state alive independently of whether anything renders it, which is
// what makes a hidden subtree keep up and a widget referenced twice stay one widget. The standing
// question that buys is whether anything is ever let go of - so this drives the mounted path, the
// one production uses, and watches the count rather than any single widget.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/deliver.dart';

const int _rootId = 1;

VLabel _label(int id, {required int seq}) => VLabel()
  ..id = id
  ..seq = seq
  ..style = SWT.NONE
  ..text = 'row $id'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 80
    ..height = 20);

VComposite _root(List<VControl> children, {required int seq}) => VComposite()
  ..id = _rootId
  ..seq = seq
  ..style = SWT.NONE
  ..children = children
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 200);

Future<void> _mount(WidgetTester tester, VComposite value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 200,
      child: CompositeSwt<VComposite>(value: value),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('a subtree that closes is let go of', (tester) async {
    await _mount(
        tester,
        _root([
          _label(10, seq: 1),
          _label(11, seq: 1),
        ], seq: 1));
    final withBoth = VRegistry.instance.size;
    expect(VRegistry.instance.valueOn('Label/11'), isNotNull);

    await deliverWhole(_root([_label(10, seq: 20)], seq: 20));
    await tester.pumpAndSettle();

    expect(find.text('row 11'), findsNothing);
    expect(VRegistry.instance.valueOn('Label/11'), isNull,
        reason: 'the widget Java stopped carrying is the widget Java disposed');
    expect(VRegistry.instance.size, lessThan(withBoth),
        reason: 'and what it cost is given back - a control also drags the overlay it is drawn on');
  });

  testWidgets('opening and closing the same thing over and over settles', (tester) async {
    // The shape that matters in a long session: a dialog, a tooltip, a tab body - opened and
    // closed again and again. A client that keeps the state of each one holds the whole session's
    // history of a screen that never grew.
    await _mount(tester, _root([_label(10, seq: 1)], seq: 1));
    final baseline = VRegistry.instance.size;

    var seq = 10;
    for (var round = 0; round < 20; round++) {
      await deliverWhole(_root([
        _label(10, seq: ++seq),
        _label(1000 + round, seq: seq),
      ], seq: seq));
      await tester.pumpAndSettle();

      await deliverWhole(_root([_label(10, seq: ++seq)], seq: seq));
      await tester.pumpAndSettle();
    }

    expect(VRegistry.instance.size, baseline,
        reason: 'twenty widgets came and went, and what is held is what is on screen');
  });

  testWidgets('a widget still on screen is never let go of', (tester) async {
    await _mount(tester, _root([_label(10, seq: 1)], seq: 1));
    final baseline = VRegistry.instance.size;

    for (var seq = 10; seq <= 50; seq += 10) {
      await deliverWhole(_root([_label(10, seq: seq)], seq: seq));
      await tester.pumpAndSettle();
    }

    expect(find.text('row 10'), findsOneWidget);
    expect(VRegistry.instance.valueOn('Label/10'), isNotNull);
    expect(VRegistry.instance.size, baseline,
        reason: 'the same tree described five times is still one tree');
  });
}
