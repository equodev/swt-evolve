// A child's State must survive its parent rebuilding: every guard that lives in didUpdateWidget
// (stale-snapshot rejection, echo baselines) is skipped when Flutter remounts instead of updating.
// Deciding where to put the GC Stack from gcOverlay broke that — mounting the overlay sets the
// field, so the Stack moved across the interaction chrome on the second build and the whole
// subtree was rebuilt from scratch.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/label_evolve.dart';

VLabel _label(String text) => VLabel()
  ..id = 77
  ..style = SWT.NONE
  ..text = text
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 100
    ..height = 20);

VComposite _parent(List<VControl> children) => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..children = children;

void main() {
  testWidgets('a Composite rebuild updates its children instead of remounting them',
      (WidgetTester tester) async {
    Widget host(VComposite value) => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: SizedBox(
            width: 200,
            height: 100,
            child: CompositeSwt<VComposite>(value: value),
          ),
        );

    await tester.pumpWidget(host(_parent([_label('first')])));
    await tester.pumpAndSettle();
    final before = tester.state<LabelImpl>(find.byType(LabelSwt<VLabel>));

    await tester.pumpWidget(host(_parent([_label('second')])));
    await tester.pumpAndSettle();
    final after = tester.state<LabelImpl>(find.byType(LabelSwt<VLabel>));

    expect(identical(before, after), isTrue,
        reason: 'a new State means didUpdateWidget never ran and its guards were skipped');
  });
}
