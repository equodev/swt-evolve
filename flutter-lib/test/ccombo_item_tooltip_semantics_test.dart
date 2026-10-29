// A CCombo entry is named by its text: its per-item tooltip must not join that name, or a locator
// written against the visible entry stops matching.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/custom/html_tooltip.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/rectangle.dart';

VCCombo _combo(List<String> tooltips) => VCCombo()
  ..swt = 'CCombo'
  ..id = 31
  ..style = 0
  ..enabled = true
  ..items = const ['MD', 'TVD']
  ..itemTooltips = tooltips
  ..text = 'MD'
  ..listVisible = false
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 24);

List<SemanticsData> _nodesShowing(WidgetTester tester, String tip) {
  final found = <SemanticsData>[];
  void visit(SemanticsNode n) {
    final d = n.getSemanticsData();
    if (d.tooltip.contains(tip) || d.label.contains(tip)) found.add(d);
    n.visitChildren((c) {
      visit(c);
      return true;
    });
  }

  visit(tester.binding.pipelineOwner.semanticsOwner!.rootSemanticsNode!);
  return found;
}

void main() {
  testWidgets('a plain-text HtmlTooltip leaves the name of what it wraps alone', (tester) async {
    final handle = tester.ensureSemantics();
    await tester.pumpWidget(const MaterialApp(
      home: Center(child: HtmlTooltip(text: 'Measured depth', child: Text('MD'))),
    ));
    await tester.pumpAndSettle();

    expect(_nodesShowing(tester, 'MD'), isNotEmpty, reason: 'sanity: the child is in semantics');
    expect(_nodesShowing(tester, 'Measured'), isEmpty);
    handle.dispose();
  });

  // CCombo hands every per-item tooltip to HtmlTooltip as html, plain text included.
  for (final tip in ['Measured depth', '<b>Measured</b> depth']) {
    testWidgets('an open CCombo entry with tooltip "$tip" is named by its text only',
        (tester) async {
      final handle = tester.ensureSemantics();
      VRegistry.instance.clear();
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Center(
          child: SizedBox(
              width: 200, height: 24, child: CComboSwt(value: _combo([tip, '']))),
        ),
      ));
      await tester.pumpAndSettle();
      await tester.tap(find.byIcon(Icons.arrow_drop_down).first);
      await tester.pumpAndSettle();
      expect(find.text('TVD').hitTestable(), findsOneWidget, reason: 'sanity: the list is open');
      expect(_nodesShowing(tester, 'TVD'), isNotEmpty, reason: 'sanity: the entries are in semantics');

      expect(_nodesShowing(tester, 'Measured'), isEmpty,
          reason: 'the per-item tooltip must not reach the entry name');
      handle.dispose();
    });
  }
}
