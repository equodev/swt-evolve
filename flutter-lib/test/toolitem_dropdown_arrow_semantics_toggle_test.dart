// A DROP_DOWN ToolItem's arrow must TOGGLE its menu even when the tap is delivered as a
// SemanticsAction instead of a real PointerEvent -- the path Flutter Web takes for a tap on a
// semantics-tagged control while accessibility is forced on (every E2E run).
// MenuAnchor.consumeOutsideTap's dismissal is built on TapRegion, which only listens to real
// pointer routing, so such a tap never sees this popup as "outside" -- the arrow's own onTap fires
// again and asks Java to re-show an already-visible menu, which is a no-op: the popup never closes.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter/semantics.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/toolbar_evolve.dart';
import 'package:swtflutter/src/theme/theme_extensions/toolbar_theme_extension.dart';

import 'support/menu_shown_ack.dart';

/// Delivers an inbound frame exactly as the transport would (2-byte name length, name, JSON body).
void _receive(String channel, Map<String, dynamic> payload) {
  final channelBytes = utf8.encode(channel);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + channelBytes.length + body.length);
  frame[0] = (channelBytes.length >> 8) & 0xFF;
  frame[1] = channelBytes.length & 0xFF;
  frame.setRange(2, 2 + channelBytes.length, channelBytes);
  frame.setRange(2 + channelBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

class _CapturingToolItemSwt extends ToolItemSwt<VToolItem> {
  const _CapturingToolItemSwt({required super.value, required this.onOpenMenu});

  final VoidCallback onOpenMenu;

  @override
  void sendEvent(VToolItem val, String ev, VEvent? payload) {
    if (ev == "Selection/OpenMenu") onOpenMenu();
  }
}

class _CapturingToolBarSwt extends ToolBarSwt<VToolBar> {
  const _CapturingToolBarSwt({required super.value, required this.onOpenMenu});

  final VoidCallback onOpenMenu;

  @override
  State createState() => _CapturingToolBarImpl();
}

class _CapturingToolBarImpl extends ToolBarImpl<_CapturingToolBarSwt, VToolBar> {
  @override
  Widget getWidgetForToolItem(
    VToolItem toolItem,
    ToolBarThemeExtension widgetTheme, {
    bool shouldLimitSize = false,
    double? maxSize,
  }) =>
      Padding(
        padding: widgetTheme.itemPadding,
        child: _CapturingToolItemSwt(value: toolItem, onOpenMenu: widget.onOpenMenu),
      );
}

VToolBar _toolBar() => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 2
      ..style = SWT.DROP_DOWN
      ..enabled = true
      ..text = 'More',
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 40);

VMenu _popup({required int seq, required bool visible}) => VMenu()
  ..id = 10
  ..seq = seq
  ..style = SWT.POP_UP
  ..enabled = true
  ..visible = visible
  ..items = [
    VMenuItem()
      ..id = 11
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Op A',
  ];

/// Standalone popups mount at Display level, outside the ToolBar's own subtree (see
/// display_evolve.dart). A widget's state now lives in VRegistry, addressed by channel and kept
/// current by write-stamp order (VRegistry.register/_takeNewerOf) -- rebuilding a parent with a
/// freshly-built value carrying the same or an unset seq is not a state push, and is silently
/// dropped as "not newer". So "Java" pushing a state change has to go over the wire, the same as
/// the real transport, each push's seq higher than the last.
Widget _harness({required VoidCallback onOpenMenu}) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 300,
        child: Stack(children: [
          Positioned.fill(
            child: Align(
              alignment: Alignment.topLeft,
              child: SizedBox(
                width: 400,
                height: 40,
                child: _CapturingToolBarSwt(value: _toolBar(), onOpenMenu: onOpenMenu),
              ),
            ),
          ),
          Positioned.fill(
            child: MenuSwt<VMenu>(value: _popup(seq: 1, visible: false)),
          ),
        ]),
      ),
    );

/// The DROP_DOWN ToolItem's arrow, among the semantics tree's tap-actionable nodes: unlike the
/// main-button node (labelled with the item's text, e.g. "More") and the outer node that merely
/// carries the `flt-semantics-identifier` (no tap action of its own), the icon-only arrow is the
/// sole tap-actionable node with no label. Scoped to the ToolItem's own tagged subtree (id 2, from
/// [_toolBar]) rather than the whole tree, so an unrelated icon-only control elsewhere in the app
/// (e.g. window chrome) can never be picked up as a false match.
SemanticsNode _arrowTapNode(WidgetTester tester) {
  SemanticsNode? scope;
  void findScope(SemanticsNode node) {
    if (scope != null) return;
    if (node.getSemanticsData().identifier == 'ToolItem/2') {
      scope = node;
      return;
    }
    node.visitChildren((child) {
      findScope(child);
      return true;
    });
  }

  final root = tester.binding.pipelineOwner.semanticsOwner!.rootSemanticsNode;
  if (root != null) findScope(root);
  final toolItemNode = scope ?? (throw StateError('ToolItem/2 semantics node not found'));

  SemanticsNode? found;
  void visit(SemanticsNode node) {
    final data = node.getSemanticsData();
    if (data.hasAction(SemanticsAction.tap) && data.label.isEmpty) {
      found = node;
    }
    node.visitChildren((child) {
      visit(child);
      return true;
    });
  }
  visit(toolItemNode);
  return found ?? (throw StateError('no unlabelled tap-actionable semantics node found under ToolItem/2'));
}

void main() {
  testWidgets(
      'a second SemanticsAction tap on the drop-down arrow closes the menu without reopening it',
      (tester) async {
    final handle = tester.ensureSemantics();

    // Mirrors Java's own Menu.visible field and write-stamp counter: a redundant "open" request
    // while already visible is silently dropped, exactly like the real setVisible(true) no-op.
    var javaVisible = false;
    var seq = 1;
    void onOpenMenu() {
      if (javaVisible) return;
      javaVisible = true;
      seq++;
      _receive('Menu/10', {..._popup(seq: seq, visible: true).toJson(), '_s': seq});
    }

    await tester.pumpWidget(_harness(onOpenMenu: onOpenMenu));
    await tester.pumpAndSettle();

    final arrowNode = _arrowTapNode(tester);
    final owner = tester.binding.pipelineOwner.semanticsOwner!;

    owner.performAction(arrowNode.id, SemanticsAction.tap);
    await tester.pump();
    // The popup no longer opens straight off the visible flag -- it waits for the "shown" ack
    // Java sends once it has finished (re)filling the menu's content, same as a context menu.
    await ackMenuShown(tester, 10);
    expect(find.text('Op A'), findsOneWidget,
        reason: 'the first tap must open the popup');

    owner.performAction(arrowNode.id, SemanticsAction.tap);
    await tester.pumpAndSettle();
    expect(find.text('Op A'), findsNothing,
        reason: 'the second tap on the same arrow must close the popup it just opened, even '
            'though it arrives as a SemanticsAction rather than a PointerEvent');

    handle.dispose();
  });
}
