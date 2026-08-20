// A Composite blocks its subtree while it is effectively disabled. Java pushes the parent first
// and each leaf on its own channel afterwards, so the copy of a leaf nested in the parent's
// payload is always an OLDER snapshot than what the leaf has already applied itself.
//
// If the block is expressed by adding/removing wrapper widgets, the widget type at the root of the
// subtree changes when enablement flips: every descendant element is deactivated and rebuilt from
// that older nested copy, silently discarding the fresh value. On the Tracing preference page that
// left the whole "Tracing output" group painted enabled but disabled underneath.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/button_evolve.dart';

VButton _button(int id, {required bool enabled, required int seq}) => VButton()
  ..id = id
  ..style = SWT.CHECK
  ..seq = seq
  ..enabled = enabled
  ..enabledEffective = enabled
  ..text = "Output to file:";

VComposite _row(
  int id, {
  required bool enabledEffective,
  required int seq,
  required List<VControl> children,
}) =>
    VComposite()
      ..id = id
      ..style = SWT.NONE
      ..seq = seq
      ..enabled = true
      ..enabledEffective = enabledEffective
      ..children = children;

void main() {
  testWidgets(
      'a Composite going from effectively-disabled to enabled keeps its children mounted',
      (WidgetTester tester) async {
    final rowKey = GlobalKey();
    final buttonKey = GlobalKey<ButtonImpl>();

    Widget host(VComposite value) => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: SizedBox(
            width: 400,
            height: 120,
            child: CompositeSwt<VComposite>(key: rowKey, value: value),
          ),
        );

    // The row and its child arrive disabled, as they do while "Enable tracing" is unchecked.
    await tester.pumpWidget(host(_row(100,
        enabledEffective: false,
        seq: 10,
        children: [_button(200, enabled: false, seq: 11)])));
    await tester.pumpAndSettle();

    final ButtonImpl child = tester.state(find.byType(ButtonSwt<VButton>));
    expect(child.state.enabled, isFalse);

    // The leaf's own channel delivers the enable BEFORE the parent's rebuild — the real ordering:
    // Java flushes the parent first (seq 11 above) and each leaf afterwards, with a higher seq.
    child.setValue(_button(200, enabled: true, seq: 30));
    await tester.pumpAndSettle();
    expect(child.state.enabled, isTrue);

    // Now the parent rebuild lands, still carrying its older nested copy of the leaf (seq 11).
    await tester.pumpWidget(host(_row(100,
        enabledEffective: true,
        seq: 20,
        children: [_button(200, enabled: false, seq: 11)])));
    await tester.pumpAndSettle();

    // Same State object: the block must not have remounted the subtree...
    final ButtonImpl after = tester.state(find.byType(ButtonSwt<VButton>));
    expect(identical(after, child), isTrue,
        reason: 'the child was remounted, so it lost the value it had applied itself');

    // ...and the stale nested copy must lose to the newer one the leaf already holds.
    expect(after.state.enabled, isTrue,
        reason: 'the leaf was rebuilt from the parent\'s older snapshot');
  });
}
