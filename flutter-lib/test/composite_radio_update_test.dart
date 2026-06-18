// Issue #597 — web delivery path.
//
// On web, DartButton.selectRadio() additionally dirties the PARENT Composite.
// The bridge's dirty-ancestor filter then drops the deselected sibling from
// individual sends and delivers it inside the parent (Composite) update. So on
// web, a radio's deselection reaches Flutter as a *parent rebuild* that carries
// the child with selection=false, NOT as an update on the child's own channel.
//
// This test verifies that rebuilding a Composite with a child's selection
// flipped actually updates the rendered radio. If it does not, two radios stay
// visibly selected even though Java only has one selected.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/button_evolve.dart';

VButton _radio(int id, {required bool selected}) => VButton()
  ..id = id
  ..style = SWT.RADIO
  ..enabled = true
  ..selection = selected
  ..text = "Option $id";

VComposite _composite(List<VControl> children) => VComposite()
  ..id = 1000
  ..style = SWT.NONE
  ..children = children;

void main() {
  testWidgets('rebuilding a Composite with a flipped child selection updates the radio',
      (WidgetTester tester) async {
    // Stable key so the Composite State (and its children) is preserved across
    // rebuilds — exactly what happens when Java pushes a parent update.
    final compositeKey = GlobalKey();
    final r1Key = GlobalKey<ButtonImpl>();
    final r2Key = GlobalKey<ButtonImpl>();

    Widget host(VComposite value) => EvolveApp(
          theme: ThemeMode.light,
          contentWidget: SizedBox(
            width: 300,
            height: 120,
            child: CompositeSwt<VComposite>(key: compositeKey, value: value),
          ),
        );

    // r1 selected, r2 not.
    await tester.pumpWidget(host(_composite([
      _radio(1, selected: true),
      _radio(2, selected: false),
    ])));
    await tester.pumpAndSettle();

    // Parent update: r1 deselected, r2 selected (what Java sends after the user
    // picks r2, carried via the Composite payload on web).
    await tester.pumpWidget(host(_composite([
      _radio(1, selected: false),
      _radio(2, selected: true),
    ])));
    await tester.pumpAndSettle();

    final r1 = find.descendant(
        of: find.byKey(compositeKey), matching: find.text('Option 1'));
    expect(r1, findsOneWidget);

    // Find the ButtonImpl states by walking the tree for the radios.
    final states = tester
        .stateList<ButtonImpl>(find.byType(ButtonSwt<VButton>))
        .toList();
    final byText = {for (final s in states) s.state.text: s.state.selection ?? false};

    expect(byText['Option 1'], isFalse,
        reason: 'After the parent update, radio Option 1 must be deselected. '
            'If it stays selected, two radios appear checked (issue #597).');
    expect(byText['Option 2'], isTrue,
        reason: 'Radio Option 2 must be selected after the parent update.');
  });
}
