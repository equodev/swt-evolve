// A DROP_DOWN ToolItem's arrow must TOGGLE its menu.
//
// Popup menus are mounted at Display level as Positioned.fill siblings of the shells
// (display_evolve.dart), so the ToolItem that opened one is never inside that MenuAnchor's
// subtree: the click that dismisses the popup is an OUTSIDE tap. Native SWT swallows that click
// in the popup's mouse grab; MenuAnchor only does so when consumeOutsideTap is set, and it
// defaults to false. Unconsumed, the same tap continues to the arrow and asks Java to open the
// menu again -- close and reopen in one gesture, so the menu can never be dismissed by its own
// arrow.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/toolbar_evolve.dart';
import 'package:swtflutter/src/theme/theme_extensions/toolbar_theme_extension.dart';

class _CapturingToolItemSwt extends ToolItemSwt<VToolItem> {
  const _CapturingToolItemSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VToolItem val, String ev, VEvent? payload) =>
      onEvent(ev, payload);
}

class _CapturingToolBarSwt extends ToolBarSwt<VToolBar> {
  const _CapturingToolBarSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  State createState() => _CapturingToolBarImpl();
}

class _CapturingToolBarImpl
    extends ToolBarImpl<_CapturingToolBarSwt, VToolBar> {
  @override
  Widget getWidgetForToolItem(
    VToolItem toolItem,
    ToolBarThemeExtension widgetTheme, {
    bool shouldLimitSize = false,
    double? maxSize,
  }) =>
      Padding(
        padding: widgetTheme.itemPadding,
        child: _CapturingToolItemSwt(value: toolItem, onEvent: widget.onEvent),
      );
}

class _CapturingMenuSwt extends MenuSwt<VMenu> {
  const _CapturingMenuSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VMenu val, String ev, VEvent? payload) => onEvent(ev, payload);
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

/// Open from the start, the way Java pushes it after handling Selection/OpenMenu.
VMenu _openPopup() => VMenu()
  ..id = 10
  ..style = SWT.POP_UP
  ..enabled = true
  ..enabledEffective = true
  ..visible = true
  ..items = [
    VMenuItem()
      ..id = 11
      ..style = SWT.PUSH
      ..enabled = true
      ..enabledEffective = true
      ..text = 'Op A',
  ];

void main() {
  testWidgets(
      'a second click on the drop-down arrow closes the menu without reopening it',
      (tester) async {
    final events = <String>[];

    await tester.pumpWidget(EvolveApp(
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
                child: _CapturingToolBarSwt(
                  value: _toolBar(),
                  onEvent: (ev, _) => events.add(ev),
                ),
              ),
            ),
          ),
          Positioned.fill(
            child: _CapturingMenuSwt(
              value: _openPopup(),
              onEvent: (ev, _) => events.add(ev),
            ),
          ),
        ]),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Op A'), findsOneWidget,
        reason: 'the popup must be open before the toggle click');
    events.clear();

    await tester.tap(find.byIcon(Icons.arrow_drop_down));
    await tester.pumpAndSettle();

    expect(events, contains('Menu/Hide'),
        reason: 'the click must dismiss the open popup');
    expect(events, isNot(contains('Selection/OpenMenu')),
        reason: 'the dismissing click must be consumed by the popup: letting it '
            'through to the arrow asks Java to reopen the menu it just closed, '
            'so the arrow can never close its own menu');
    expect(find.text('Op A'), findsNothing,
        reason: 'the menu must be left closed');
  });
}
