import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

const List<int> _pngBytes = <int>[
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, //
  0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
  0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
  0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
  0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
  0x54, 0x78, 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
  0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4, 0x00,
  0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE,
  0x42, 0x60, 0x82,
];

/// The accessible name Flutter Web gives a node, mirroring the engine's
/// `computeDomSemanticsLabel`: the tooltip, then a line break, then label and value.
/// A locator written against a visible caption matches this string, not the label alone.
String? domAccessibleName(SemanticsData d) {
  final labelValue = [d.label, d.value]
      .where((s) => s.trim().isNotEmpty)
      .join(' ');
  final tooltip = d.tooltip.isEmpty ? null : d.tooltip;
  if (tooltip == null) return labelValue.isEmpty ? null : labelValue;
  return labelValue.isEmpty ? tooltip : '$tooltip\n$labelValue';
}

SemanticsData _byIdentifier(WidgetTester tester, String identifier) {
  SemanticsData? found;
  void visit(SemanticsNode n) {
    final d = n.getSemanticsData();
    if (d.identifier == identifier) found = d;
    n.visitChildren((c) {
      visit(c);
      return true;
    });
  }

  visit(tester.binding.pipelineOwner.semanticsOwner!.rootSemanticsNode!);
  expect(found, isNotNull, reason: 'no Semantics node carries "$identifier"');
  return found!;
}

void main() {
  // An icon-only toolbar item has no caption but its tooltip, which is what the
  // Eclipse workbench toolbars are made of; the tooltip has to become its name.
  testWidgets('an icon-only ToolItem is named by its tooltip', (tester) async {
    final handle = tester.ensureSemantics();
    final item = VToolItem()
      ..id = 201
      ..style = SWT.PUSH
      ..enabled = true
      ..toolTipText = 'Show List'
      ..image = (VImage()
        ..imageData = (VImageData()
          ..width = 16
          ..height = 16
          ..data = _pngBytes));
    final bar = VToolBar()
      ..id = 200
      ..style = SWT.HORIZONTAL | SWT.FLAT
      ..enabled = true
      ..visible = true
      ..items = [item]
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 400
        ..height = 40);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget:
          SizedBox(width: 400, height: 40, child: ToolBarSwt(value: bar)),
    ));
    await tester.pumpAndSettle();

    expect(domAccessibleName(_byIdentifier(tester, 'ToolItem/201')), 'Show List');
    handle.dispose();
  });

  // A locator is written against the visible caption, so the tooltip must not
  // join the name of a control that has one.
  testWidgets('a captioned control is named by its caption, not its tooltip',
      (tester) async {
    final handle = tester.ensureSemantics();
    final button = VButton()
      ..id = 301
      ..style = SWT.CHECK
      ..enabled = true
      ..visible = true
      ..text = 'Files opened in editors'
      ..toolTipText = 'Only workspace files (in projects) are supported'
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 300
        ..height = 30);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget:
          SizedBox(width: 300, height: 30, child: ButtonSwt(value: button)),
    ));
    await tester.pumpAndSettle();

    final data = _byIdentifier(tester, 'Button/301');
    expect(domAccessibleName(data), 'Files opened in editors');
    expect(data.hint, 'Only workspace files (in projects) are supported',
        reason: 'the tooltip stays reachable as the description');
    handle.dispose();
  });

  // The path the Eclipse toolbars with a caption take: ToolItem's own Tooltip
  // must not put the tooltip back into the name.
  testWidgets('a captioned ToolItem is named by its caption, not its tooltip',
      (tester) async {
    final handle = tester.ensureSemantics();
    final item = VToolItem()
      ..id = 211
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Search'
      ..toolTipText = 'Search the workspace';
    final bar = VToolBar()
      ..id = 210
      ..style = SWT.HORIZONTAL | SWT.FLAT | SWT.RIGHT
      ..enabled = true
      ..visible = true
      ..items = [item]
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 400
        ..height = 40);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget:
          SizedBox(width: 400, height: 40, child: ToolBarSwt(value: bar)),
    ));
    await tester.pumpAndSettle();

    final data = _byIdentifier(tester, 'ToolItem/211');
    expect(domAccessibleName(data), 'Search');
    expect(data.hint, 'Search the workspace');
    handle.dispose();
  });

  // A Text's text is its value: typing must not move the tooltip out of the
  // tooltip slot, or a locator that matched the empty field stops matching.
  testWidgets('a Text keeps its tooltip whether or not it holds a value',
      (tester) async {
    for (final value in ['', 'typed']) {
      final handle = tester.ensureSemantics();
      final text = VText()
        ..id = 401
        ..style = SWT.SINGLE | SWT.BORDER
        ..enabled = true
        ..visible = true
        ..text = value
        ..toolTipText = 'Containing text'
        ..bounds = (VRectangle()
          ..x = 0
          ..y = 0
          ..width = 300
          ..height = 30);

      await tester.pumpWidget(EvolveApp(
        key: ValueKey(value),
        theme: ThemeMode.light,
        contentWidget:
            SizedBox(width: 300, height: 30, child: TextSwt(value: text)),
      ));
      await tester.pumpAndSettle();

      final data = _byIdentifier(tester, 'Text/401');
      expect(data.tooltip, 'Containing text', reason: 'value "$value"');
      expect(data.hint, isNot('Containing text'), reason: 'value "$value"');
      handle.dispose();
    }
  });
}
