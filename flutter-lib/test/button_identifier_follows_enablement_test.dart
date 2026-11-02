// A control's semantics identifier is how anything outside the canvas finds it. Enablement decides
// whether a control's node is tappable, and with that its role on the web, where the engine rebuilds
// a node's element for a new role without the identifier it already had. So with semantics on, a
// control whose enablement changes gets a new node, created with its identifier and its text. The
// control itself is not mounted again: what it holds, as its focus or a running animation, stays.

import 'package:flutter/material.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VButton _button({required bool enabled, int seq = 10}) => VButton()
  ..id = 7
  ..seq = seq
  ..style = SWT.PUSH
  ..text = '&Next >'
  ..enabled = enabled
  ..visible = true
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 92
    ..height = 26);

Widget _app(VButton button) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 92, height: 26, child: ButtonSwt<VButton>(value: button)),
      ),
    );

/// The button's own focus, deepest in what it builds.
final _innermostFocus = find.descendant(of: find.byType(ButtonSwt<VButton>), matching: find.byType(Focus)).last;

/// The semantics node identified as [identifier], or null.
SemanticsNode? _identified(WidgetTester tester, String identifier) {
  SemanticsNode? found;
  bool visit(SemanticsNode node) {
    if (node.identifier == identifier) {
      found = node;
      return false;
    }
    node.visitChildren(visit);
    return found == null;
  }

  visit(tester.binding.pipelineOwner.semanticsOwner!.rootSemanticsNode!);
  return found;
}

void main() {
  setUp(freshClient);

  for (final (from, to) in [(false, true), (true, false)]) {
    testWidgets('a button going from ${from ? 'enabled' : 'disabled'} to ${to ? 'enabled' : 'disabled'} '
        'gets a new node, identified and labelled', (tester) async {
      final handle = tester.ensureSemantics();
      final button = _button(enabled: from);
      await tester.pumpWidget(_app(button));
      await tester.pumpAndSettle();
      final before = _identified(tester, 'Button/7');
      final focus = tester.state(_innermostFocus);
      expect(before?.label, 'Next >');
      expect(before!.getSemanticsData().hasAction(SemanticsAction.tap), from);

      button
        ..enabled = to
        ..seq = 20;
      await deliverChange(button, keys: [WireKey.k_Control_enabled], base: 10);
      await tester.pumpAndSettle();

      final after = _identified(tester, 'Button/7');
      expect(after?.label, 'Next >', reason: 'the button is found by its identifier, and read by its text');
      expect(after!.getSemanticsData().hasAction(SemanticsAction.tap), to);
      expect(after.id, isNot(before.id), reason: 'a node whose tap comes or goes has to be a new one');
      expect(tester.state(_innermostFocus), same(focus), reason: 'the button is rebuilt, not mounted again');
      handle.dispose();
    });
  }

  testWidgets('a button changing anything but its enablement keeps its node', (tester) async {
    final handle = tester.ensureSemantics();
    final button = _button(enabled: true)..primary = true;
    await tester.pumpWidget(_app(button));
    await tester.pumpAndSettle();
    final before = _identified(tester, 'Button/7');

    button
      ..primary = false
      ..seq = 20;
    await deliverChange(button, keys: [WireKey.k_Button_primary], base: 10);
    await tester.pumpAndSettle();

    expect(_identified(tester, 'Button/7')?.id, before!.id);
    handle.dispose();
  });

  testWidgets('a control built before semantics are turned on is identified once they are', (tester) async {
    await tester.pumpWidget(_app(_button(enabled: true)));
    await tester.pumpAndSettle();
    final handle = tester.ensureSemantics();
    await tester.pump();
    await tester.pump();

    expect(_identified(tester, 'Button/7')?.label, 'Next >');
    handle.dispose();
  }, semanticsEnabled: false);
}
