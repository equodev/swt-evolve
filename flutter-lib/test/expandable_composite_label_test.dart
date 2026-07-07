// Regression test for issues #625 and #793.
//
// Composite contains a header label and a details panel. When the user
// expands or collapses the panel, the parent Composite rebuilds in Flutter
// and calls didUpdateWidget on its children with its own (potentially stale)
// copy of the child values.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/impl/label_evolve.dart';

const _idExecution = 20;
const _idLabel = 50;
const _idDetails = 60;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VLabel _label({String text = ''}) => VLabel()
  ..swt = 'Label'
  ..id = _idLabel
  ..style = SWT.NONE
  ..bounds = _rect(0, 0, 300, 20)
  ..text = text;

VComposite _details({bool visible = true}) => VComposite()
  ..swt = 'Composite'
  ..id = _idDetails
  ..style = SWT.NONE
  ..bounds = _rect(0, 20, 300, visible ? 200 : 0)
  ..visible = visible
  ..children = [];

/// Builds compositeExecution with a specific expand state.
/// [labelText] is what the PARENT knows about the label — this can be stale.
VComposite _execution({required bool expanded, String labelText = ''}) =>
    VComposite()
      ..swt = 'Composite'
      ..id = _idExecution
      ..style = SWT.NONE
      ..bounds = _rect(0, 0, 420, 320)
      ..children = [
        _label(text: labelText),
        _details(visible: expanded),
      ];

void main() {
  tearDown(() {
    injectLatestKnownValue(_idLabel, _label(text: ''));
  });

  testWidgets(
    'label text survives parent rebuild on expand and collapse',
    (WidgetTester tester) async {
      final executionKey = GlobalKey();

      Widget host(VComposite value) => EvolveApp(
            theme: ThemeMode.light,
            contentWidget: SizedBox(
              width: 420,
              height: 320,
              child: CompositeSwt<VComposite>(key: executionKey, value: value),
            ),
          );

      // Step 1: initial state — panel collapsed, parent has empty label text
      // (label text hasn't been set by Java yet from the parent's perspective).
      await tester.pumpWidget(host(_execution(expanded: false, labelText: '')));
      await tester.pumpAndSettle();

      // Step 2: Java sends "Information" directly to the label's own
      // onChange channel. The parent composite does NOT get a new state — its
      // cached copy of the label still has text = ''.
      injectLatestKnownValue(_idLabel, _label(text: 'Information'));

      // Step 3: user expands the panel. The parent composite rebuilds in Flutter
      // passing its STALE child values (label text = '') to didUpdateWidget.
      await tester.pumpWidget(
          host(_execution(expanded: true, labelText: '')));
      await tester.pumpAndSettle();

      final labelState =
          tester.state<LabelImpl>(find.byType(LabelSwt<VLabel>));

      expect(
        labelState.state.text,
        'Information',
        reason: 'after expand the label must keep the text received from Java; '
            'if it reverts to empty the header goes blank (issue #625)',
      );

      // Step 4: user collapses. Parent rebuilds again with stale label text.
      await tester.pumpWidget(
          host(_execution(expanded: false, labelText: '')));
      await tester.pumpAndSettle();

      expect(
        labelState.state.text,
        'Information',
        reason: 'after collapse the label must still keep its text (issue #625)',
      );

      // Step 5: expand again — a second cycle must also hold.
      await tester.pumpWidget(
          host(_execution(expanded: true, labelText: '')));
      await tester.pumpAndSettle();

      expect(
        labelState.state.text,
        'Information',
        reason: 'second expand must not wipe the label text (issue #625)',
      );
    },
  );
}
