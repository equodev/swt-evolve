// SWT puts a real control inside a toolbar by giving a SEPARATOR ToolItem a control via
// ToolItem.setControl() — that is how combos, text fields and nested toolbars end up in a
// toolbar. Rendering every separator as a divider throws that control away together with its
// whole subtree, leaving a blank stretch of toolbar where the app drew a combo.
//
// CoolItem already renders its hosted control this way; ToolItem must do the same.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/toolbar_theme_extension.dart';

const double _viewportWidth = 400;
const double _viewportHeight = 120;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

/// The control an app hands to ToolItem.setControl(): a composite holding real widgets, sized by
/// the layout, the way an RCP workbench fills its main toolbar.
VComposite _hostedControl() => VComposite()
  ..id = 500
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 200, 28)
  ..children = [
    VLabel()
      ..id = 501
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 4, 90, 20)
      ..text = 'Z Scale',
    VButton()
      ..id = 502
      ..style = SWT.PUSH
      ..enabled = true
      ..visible = true
      ..bounds = _rect(95, 0, 105, 28)
      ..text = 'Apply',
  ];

VToolBar _toolBar({required List<VToolItem> items}) => VToolBar()
  ..id = 100
  ..style = SWT.FLAT | SWT.HORIZONTAL
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, 300, 28)
  ..items = items;

VToolItem _separatorHosting(VComposite control) => VToolItem()
  ..id = 200
  ..style = SWT.SEPARATOR
  ..enabled = true
  ..width = 200
  ..control = control;

VToolItem _plainSeparator() => VToolItem()
  ..id = 201
  ..style = SWT.SEPARATOR
  ..enabled = true;

VToolItem _pushItem({required int id, required String text}) => VToolItem()
  ..id = id
  ..style = SWT.PUSH
  ..enabled = true
  ..text = text;

Future<void> _pump(
  WidgetTester tester,
  VToolBar toolBar, {
  bool inToolbarArea = false,
}) async {
  Widget bar = ToolBarSwt<VToolBar>(value: toolBar);
  if (inToolbarArea) {
    bar = ToolbarAreaMarker(active: true, child: bar);
  }
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: _viewportWidth,
      height: _viewportHeight,
      child: bar,
    ),
  ));
  await tester.pump();
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a separator ToolItem renders the control it hosts', (tester) async {
    await _pump(tester, _toolBar(items: [_separatorHosting(_hostedControl())]));

    expect(find.text('Z Scale'), findsOneWidget,
        reason: 'the hosted control must be built, not replaced by a divider');
    expect(find.text('Apply'), findsOneWidget);
  });

  testWidgets('the hosted control keeps the width the item reserves for it', (tester) async {
    await _pump(tester, _toolBar(items: [_separatorHosting(_hostedControl())]));

    // ToolItem.width is what Java computed from the control, and what the trim layout budgeted.
    final hosted = tester.getRect(find.byKey(const ValueKey(500)).first);
    expect(hosted.width, closeTo(200, 1));
  });

  testWidgets('a separator with no control is still just a divider', (tester) async {
    await _pump(tester, _toolBar(items: [
      _pushItem(id: 300, text: 'Left'),
      _plainSeparator(),
      _pushItem(id: 301, text: 'Right'),
    ]));

    expect(find.text('Left'), findsOneWidget);
    expect(find.text('Right'), findsOneWidget);
    // Nothing hosted, so no control subtree should have appeared between them.
    expect(find.byKey(const ValueKey(500)), findsNothing);
  });

  testWidgets('a hosted control paints on the toolbar band, not on the default surface',
      (tester) async {
    // Inside a trim area the toolbar has its own background. A hosted Composite defaults to the
    // theme surface, which shows up as a white slab sitting on the toolbar.
    await _pump(tester, _toolBar(items: [_separatorHosting(_hostedControl())]),
        inToolbarArea: true);

    final barTheme = Theme.of(tester.element(find.byType(ToolBarSwt<VToolBar>).first))
        .extension<ToolBarThemeExtension>()!;
    final hostedFill = tester.widget<ColoredBox>(
      find
          .descendant(
            of: find.byKey(const ValueKey(500)),
            matching: find.byType(ColoredBox),
          )
          .first,
    );

    expect(hostedFill.color, barTheme.toolbarBackgroundColor);
  });

  testWidgets('hosted controls and ordinary items coexist in one toolbar', (tester) async {
    await _pump(tester, _toolBar(items: [
      _separatorHosting(_hostedControl()),
      _pushItem(id: 302, text: 'Chevron'),
    ]));

    expect(find.text('Z Scale'), findsOneWidget);
    expect(find.text('Chevron'), findsOneWidget);
  });
}
