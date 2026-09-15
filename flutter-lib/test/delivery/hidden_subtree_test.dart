// A hidden subtree costs nothing and misses nothing.
//
// SWT hides a control by flipping `visible`, and Java keeps describing what is underneath it -- an
// invisible page of a preference dialog is still being filled in. The client used to have to keep
// that subtree mounted (`maintainState: true`) for its updates to land, because a widget's value
// died with the State that rendered it: dropping the subtree meant unsubscribing from it, and
// showing it again would have rendered whatever it held when it went away.
//
// It does not any more. The value lives in the registry and keeps up on its own, so a hidden
// subtree can be dropped entirely -- no build, no layout, nothing held -- and rebuilt from current
// state when it comes back.

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

VLabel _label(String text, {required int seq}) => VLabel()
  ..id = 9
  ..seq = seq
  ..style = SWT.NONE
  ..text = text
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 120
    ..height = 20);

VComposite _page({required bool visible, required int seq, required List<VControl> children}) =>
    VComposite()
      ..id = 1
      ..seq = seq
      ..style = SWT.NONE
      ..visible = visible
      ..children = children
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 200
        ..height = 100);

Future<void> _mount(WidgetTester tester, VComposite value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 200,
      height: 100,
      child: CompositeSwt<VComposite>(value: value),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('a hidden subtree is not kept mounted', (tester) async {
    await _mount(tester, _page(visible: true, seq: 1, children: [_label('before', seq: 1)]));
    expect(find.byType(LabelSwt<VLabel>), findsOneWidget);

    await deliverWhole(_page(visible: false, seq: 10, children: [_label('before', seq: 10)]));
    await tester.pumpAndSettle();

    // skipOffstage: false, or a subtree kept alive but not painted would read as gone.
    expect(find.byType(LabelSwt<VLabel>, skipOffstage: false), findsNothing,
        reason: 'nothing is drawing it, so nothing should be building or laying it out either');
  });

  testWidgets('a hidden subtree keeps up while it is gone', (tester) async {
    await _mount(tester, _page(visible: true, seq: 1, children: [_label('before', seq: 1)]));

    await deliverWhole(_page(visible: false, seq: 10, children: [_label('before', seq: 10)]));
    await tester.pumpAndSettle();

    // Java goes on describing what is underneath: a preference page being filled in behind the one
    // on screen, a tab whose content changed while another tab was selected.
    await deliverWhole(_label('told while hidden', seq: 20));
    await tester.pumpAndSettle();

    expect((VRegistry.instance.valueOn('Label/9') as VLabel).text, 'told while hidden',
        reason: 'the value keeps receiving with nothing rendering it, which is the whole reason it '
            'does not live in the State');
  });

  testWidgets('and shows what it was told when it comes back', (tester) async {
    await _mount(tester, _page(visible: true, seq: 1, children: [_label('before', seq: 1)]));

    await deliverWhole(_page(visible: false, seq: 10, children: [_label('before', seq: 10)]));
    await tester.pumpAndSettle();
    await deliverWhole(_label('told while hidden', seq: 20));
    await tester.pumpAndSettle();

    await deliverWhole(_page(visible: true, seq: 30, children: [_label('before', seq: 10)]));
    await tester.pumpAndSettle();

    expect(find.text('told while hidden'), findsOneWidget,
        reason: 'revealing rebuilds from the state the widget actually holds - not from the copy '
            'the parent was carrying, which is older than the update that arrived while hidden');
    expect(find.text('before'), findsNothing);
  });
}
