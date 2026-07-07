// Regression test for issues #625 and #793.
//
// A parent Composite periodically rebuilds in Flutter due to hover or layout
// changes. Before the fix, the parent's `didUpdateWidget` would pass its own
// (potentially stale) copy of the child label's value down to the label,
// overwriting a fresher value the label had already received directly from
// Java via its own onChange channel. This caused labels inside expandable
// composites to go blank or revert to their initial text on hover.
//
// The fix: `WidgetSwtState.didUpdateWidget` prefers the value stored in
// `_latestKnownValues` (populated only by real onChange events) over the
// parent-supplied incoming value.
//
// This test reproduces the bug at the widget level:
// 1. Build a Composite containing a Label — both have text "initial".
// 2. Simulate an onChange arriving directly on the label (text "updated"),
//    which populates `_latestKnownValues` without the parent knowing.
// 3. Rebuild the parent Composite with the stale child value (text "initial").
// 4. Assert the label still shows "updated" — the onChange-received value wins.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/impl/label_evolve.dart';

const _labelId = 42;
const _compositeId = 100;

VLabel _label({required String text}) => VLabel()
  ..swt = 'Label'
  ..id = _labelId
  ..style = SWT.NONE
  ..text = text;

VComposite _composite({required String labelText}) => VComposite()
  ..swt = 'Composite'
  ..id = _compositeId
  ..style = SWT.NONE
  ..children = [_label(text: labelText)];

void main() {
  // Clear the global map between tests so they don't bleed into each other.
  tearDown(() => injectLatestKnownValue(_labelId, VLabel()
    ..swt = 'Label'
    ..id = _labelId
    ..style = SWT.NONE));

  testWidgets(
    'label keeps onChange-received text when parent rebuilds with stale value',
    (WidgetTester tester) async {
      final compositeKey = GlobalKey();

      Widget host(VComposite value) => EvolveApp(
            theme: ThemeMode.light,
            contentWidget: SizedBox(
              width: 400,
              height: 200,
              child: CompositeSwt<VComposite>(key: compositeKey, value: value),
            ),
          );

      // Step 1: render composite with label text "initial".
      await tester.pumpWidget(host(_composite(labelText: 'initial')));
      await tester.pumpAndSettle();

      final labelState =
          tester.state<LabelImpl>(find.byType(LabelSwt<VLabel>));
      expect(labelState.state.text, 'initial',
          reason: 'sanity: label starts with the initial text');

      // Step 2: simulate an onChange arriving directly on the label (text "updated").
      // This is what Java does when it calls label.setText("updated") and flushes
      // the dirty widget — the update goes directly to the label's own channel,
      // not through the parent. The parent's Flutter state still holds "initial".
      injectLatestKnownValue(_labelId, _label(text: 'updated'));

      // Step 3: parent rebuilds with its STALE copy of the child (text "initial").
      // This simulates a hover or layout change on the parent that triggers a
      // Flutter rebuild without Java having sent a new value for this child.
      await tester.pumpWidget(host(_composite(labelText: 'initial')));
      await tester.pumpAndSettle();

      // Step 4: the label must keep its onChange-received value.
      expect(
        labelState.state.text,
        'updated',
        reason: 'after a parent rebuild with stale child value, the label must '
            'keep the fresher value it received directly from Java (issue #625/#793)',
      );
    },
  );
}
