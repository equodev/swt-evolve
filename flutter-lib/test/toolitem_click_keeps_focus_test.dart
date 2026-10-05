// SWT parity: clicking a ToolItem does not move the keyboard focus. With the semantics tree on,
// Flutter Web delivers that click to a focusable node as SemanticsAction.focus as well. Tab still
// reaches the items, as it does the items of a native ToolBar.

import 'package:flutter/material.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/toolbar_evolve.dart';
import 'package:swtflutter/src/theme/theme_extensions/toolbar_theme_extension.dart';

class _SilentToolItemSwt extends ToolItemSwt<VToolItem> {
  const _SilentToolItemSwt({required super.value});

  @override
  void sendEvent(VToolItem val, String ev, VEvent? payload) {}
}

class _SilentToolBarSwt extends ToolBarSwt<VToolBar> {
  const _SilentToolBarSwt({required super.value});

  @override
  State createState() => _SilentToolBarImpl();
}

class _SilentToolBarImpl extends ToolBarImpl<_SilentToolBarSwt, VToolBar> {
  @override
  Widget getWidgetForToolItem(
    VToolItem toolItem,
    ToolBarThemeExtension widgetTheme, {
    bool shouldLimitSize = false,
    double? maxSize,
  }) =>
      Padding(
        padding: widgetTheme.itemPadding,
        child: _SilentToolItemSwt(value: toolItem),
      );
}

VToolItem _item(int id, int style, String text) => VToolItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text;

VToolBar _toolBar() => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    _item(2, SWT.PUSH, 'Push'),
    _item(3, SWT.CHECK, 'Check'),
    _item(4, SWT.DROP_DOWN, 'Run'),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 40);

/// Every semantics node inside a ToolItem's tagged subtree.
List<SemanticsNode> _toolItemNodes(WidgetTester tester) {
  final nodes = <SemanticsNode>[];
  void collect(SemanticsNode node, bool inItem) {
    final here = inItem || node.getSemanticsData().identifier.startsWith('ToolItem/');
    if (here) nodes.add(node);
    node.visitChildren((child) {
      collect(child, here);
      return true;
    });
  }

  final root = tester.binding.pipelineOwner.semanticsOwner!.rootSemanticsNode;
  if (root != null) collect(root, false);
  return nodes;
}

void main() {
  testWidgets('a focus request on a ToolItem leaves the focused field focused', (tester) async {
    final handle = tester.ensureSemantics();
    final field = FocusNode(debugLabel: 'field');

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(width: 400, child: TextField(focusNode: field)),
          SizedBox(width: 400, height: 40, child: _SilentToolBarSwt(value: _toolBar())),
        ],
      ),
    ));
    await tester.pumpAndSettle();
    field.requestFocus();
    await tester.pump();
    expect(field.hasPrimaryFocus, isTrue);

    final nodes = _toolItemNodes(tester);
    expect(nodes.where((n) => n.getSemanticsData().hasAction(SemanticsAction.tap)), isNotEmpty);

    final owner = tester.binding.pipelineOwner.semanticsOwner!;
    for (final node in nodes) {
      if (!node.getSemanticsData().hasAction(SemanticsAction.focus)) continue;
      owner.performAction(node.id, SemanticsAction.focus);
      await tester.pump();
      expect(field.hasPrimaryFocus, isTrue,
          reason: 'focus moved to ${FocusManager.instance.primaryFocus}');
    }

    field.dispose();
    handle.dispose();
  });

  testWidgets('Tab reaches the tool items', (tester) async {
    final field = FocusNode(debugLabel: 'field');

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(width: 400, child: TextField(focusNode: field)),
          SizedBox(width: 400, height: 40, child: _SilentToolBarSwt(value: _toolBar())),
        ],
      ),
    ));
    await tester.pumpAndSettle();
    field.requestFocus();
    await tester.pump();

    // The first Tab stops on the ToolBar itself, the next on its first item.
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
    expect(FocusManager.instance.primaryFocus?.debugLabel, 'ToolItem');

    field.dispose();
  });
}
