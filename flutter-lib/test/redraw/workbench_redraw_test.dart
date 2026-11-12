// What each everyday operation in a workbench window rebuilds and repaints. A change that touches
// one control should repaint that control, not the window around it.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/styledtext.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/gen/tree.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import '../delivery/support/deliver.dart';
import 'support/budget.dart';
import 'support/redraw_probe.dart';
import 'support/workbench.dart';

Future<void> _mount(WidgetTester tester) async {
  tester.view.physicalSize = const Size(windowWidth, windowHeight);
  tester.view.devicePixelRatio = 1;
  addTearDown(tester.view.reset);
  freshClient();
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        // A Shell's own layer, as the Display gives every shell it shows.
        child: RepaintBoundary(
          child: SizedBox(
            width: windowWidth,
            height: windowHeight,
            child: CompositeSwt<VComposite>(value: workbench()),
          ),
        ),
      ),
    ),
  );
  await tester.pumpAndSettle();
  // The tab strip trips a debug-only paint assertion in this harness (a hairline border with a
  // radius), unrelated to what is measured.
  while (tester.takeException() != null) {}
}

const budgets = <String, Budget>{
  'idle': Budget(0, 0, 0.01),
  'sash.drag (20 steps)': Budget(9880, 11452, 63.40),
  'window.resize (10 steps)': Budget(6114, 18706, 45.60),
  'status.label.text': Budget(2, 73, 0.03),
  'toolitem.disable': Budget(29, 287, 0.05),
  'table.row.text': Budget(53, 1469, 0.61),
  'table.selection': Budget(65, 1469, 0.61),
  'tree.selection': Budget(14, 35, 0.02),
  'tree.item.text': Budget(173, 12, 0.01),
  'editor.caret': Budget(9, 26, 0.49),
  'editor.type (Java echo)': Budget(9, 28, 0.98),
  'editor.scroll': Budget(9, 26, 0.49),
  'hover.toolitem enter+leave': Budget(56, 300, 0.05),
  'hover.tree rows': Budget(151, 639, 0.22),
  'hover.table rows': Budget(281, 15910, 12.82),
  'hover.sash': Budget(15, 1421, 1.16),
  'editor.click': Budget(120, 148, 0.03),
  'editor.key (before echo)': Budget(120, 148, 0.03),
};

void main() {
  testWidgets('workbench operations', (tester) async {
    await _mount(tester);
    final probe = RedrawProbe(tester, const Size(windowWidth, windowHeight));
    final java = JavaSide();
    final costs = <RedrawCost>[];
    Future<void> measure(String name, Future<void> Function() op) async {
      costs.add(await probe.measure(name, op));
      while (tester.takeException() != null) {}
    }

    await measure('idle', () async {});

    await measure('sash.drag (20 steps)', () async {
      for (var i = 1; i <= 20; i++) {
        java.sashAt(300 + i * 6);
        await tester.pump(const Duration(milliseconds: 16));
      }
    });

    await measure('window.resize (10 steps)', () async {
      for (var i = 1; i <= 10; i++) {
        java.windowSize(1200 - i * 10, 800 - i * 6, left: 420);
        await tester.pump(const Duration(milliseconds: 16));
      }
    });
    java.windowSize(1200, 800);
    java.sashAt(300);
    await tester.pumpAndSettle();

    await measure('status.label.text', () async {
      java.flush([
        (
          VLabel()
            ..id = statusLabelId(1)
            ..text = 'Writable',
          [WireKey.k_Label_text],
        ),
      ]);
    });

    await measure('toolitem.disable', () async {
      java.flush([
        (
          VToolItem()
            ..id = toolItemId(3)
            ..enabled = false,
          [WireKey.k_ToolItem_enabled],
        ),
      ]);
    });

    await measure('table.row.text', () async {
      java.flush([
        (tableRow(7, text: 'Changed problem'), [WireKey.k_TableItem_texts]),
      ]);
    });

    await measure('table.selection', () async {
      java.flush([
        (
          VTable()
            ..id = tableId
            ..selection = [5],
          [WireKey.k_Table_selection],
        ),
      ]);
    });

    await measure('tree.selection', () async {
      java.flush([
        (
          VTree()
            ..id = treeId
            ..selection = [VTreeItem()..id = treeItemId(1, 2)],
          [WireKey.k_Tree_selection],
        ),
      ]);
    });

    await measure('tree.item.text', () async {
      java.flush([
        (
          VTreeItem()
            ..id = treeItemId(5)
            ..text = 'package.renamed',
          [WireKey.k_Item_text],
        ),
      ]);
    });

    await measure('editor.caret', () async {
      java.flush([
        (
          VStyledText()
            ..id = editorId
            ..caretOffset = 120,
          [WireKey.k_StyledText_caretOffset],
        ),
      ]);
    });

    await measure('editor.type (Java echo)', () async {
      final text = editorText(400);
      java.flush([
        (
          VStyledText()
            ..id = editorId
            ..text = '${text.substring(0, 120)}x${text.substring(120)}'
            ..caretOffset = 121,
          [WireKey.k_StyledText_text, WireKey.k_StyledText_caretOffset],
        ),
      ]);
    });

    await measure('editor.scroll', () async {
      java.flush([
        (
          VStyledText()
            ..id = editorId
            ..topPixel = 160,
          [WireKey.k_StyledText_topPixel],
        ),
      ]);
    });

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(1190, 790));
    addTearDown(mouse.removePointer);
    await tester.pumpAndSettle();

    await measure('hover.toolitem enter+leave', () async {
      await mouse.moveTo(tester.getCenter(find.text('Tool 6')));
      await tester.pump(const Duration(milliseconds: 300));
      await mouse.moveTo(const Offset(1190, 790));
    });

    await measure('hover.tree rows', () async {
      for (var y = 60; y < 400; y += 10) {
        await mouse.moveTo(Offset(100, y.toDouble()));
        await tester.pump(const Duration(milliseconds: 8));
      }
    });

    await measure('hover.table rows', () async {
      for (var y = 620; y < 760; y += 6) {
        await mouse.moveTo(Offset(500, y.toDouble()));
        await tester.pump(const Duration(milliseconds: 8));
      }
    });

    await measure('hover.sash', () async {
      await mouse.moveTo(const Offset(302, 300));
      await tester.pump(const Duration(milliseconds: 100));
      await mouse.moveTo(const Offset(1190, 790));
    });

    await measure('editor.click', () async {
      await tester.tapAt(const Offset(700, 200));
    });

    await measure('editor.key (before echo)', () async {
      await tester.sendKeyEvent(LogicalKeyboardKey.keyA);
    });

    probe.uninstall();
    // ignore: avoid_print
    print('\n[redraw] summary\n${costs.join('\n')}');
    expectWithinBudgets(costs, budgets);
  });
}
