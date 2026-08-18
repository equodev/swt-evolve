// Java answers a paint by emitting the draw ops on the widget's GC channels while it dispatches
// SWT.Paint — so the GC has to be mounted and subscribed already, which is why every control that
// goes through ControlImpl.wrap() carries one offstage. buildComposite()'s children branch skipped
// it: the ops arrived with nothing listening, were dropped, and gcOverlay was never set. A
// Composite that draws its content from a PaintListener stayed permanently blank.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _bounds(int w, int h) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = w
  ..height = h;

/// The shape that motivated this: a big custom-drawn panel whose only child is the hidden inline
/// editor it pops over a value. Having a child is what routes it down the branch that was missing
/// the GC — the same composite without children was always fine.
VComposite _panel({required bool withChild}) => VComposite()
  ..id = 4242
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _bounds(419, 521)
  ..children = withChild
      ? [
          VComposite()
            ..id = 4243
            ..style = SWT.NONE
            ..visible = false
            ..bounds = _bounds(57, 22)
        ]
      : null;

Future<void> _pump(WidgetTester tester, {required bool withChild}) =>
    tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 419,
        height: 521,
        child: CompositeSwt<VComposite>(value: _panel(withChild: withChild)),
      ),
    ));

void main() {
  testWidgets('a Composite with children still mounts its GC', (tester) async {
    await _pump(tester, withChild: true);
    expect(find.byType(GCSwt<VGC>, skipOffstage: false), findsOneWidget,
        reason: 'nothing would be subscribed when Java emits the draw ops');
  });

  testWidgets('and so does one without them', (tester) async {
    await _pump(tester, withChild: false);
    expect(find.byType(GCSwt<VGC>, skipOffstage: false), findsOneWidget);
  });
}
