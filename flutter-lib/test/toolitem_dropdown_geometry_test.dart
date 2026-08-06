import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
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
  }) {
    Widget result = Padding(
      padding: widgetTheme.itemPadding,
      child: _CapturingToolItemSwt(value: toolItem, onEvent: widget.onEvent),
    );
    if (shouldLimitSize && maxSize != null) {
      result = ConstrainedBox(
        constraints: BoxConstraints(maxWidth: maxSize, maxHeight: maxSize),
        child: result,
      );
    }
    return result;
  }
}

VToolItem _item(int id, int style, String text) => VToolItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text;

VToolBar _toolBar(List<VToolItem> items) => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..items = items
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 40);

void main() {
  testWidgets(
      'clicking a drop-down arrow reports the item geometry relative to the ToolBar',
      (tester) async {
    final events = <(String, VEvent?)>[];
    final bar = _toolBar([
      _item(2, SWT.PUSH, 'First'),
      _item(3, SWT.DROP_DOWN, 'Menu'),
    ]);

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 400,
        height: 40,
        child: _CapturingToolBarSwt(
          value: bar,
          onEvent: (ev, payload) => events.add((ev, payload)),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.byIcon(Icons.arrow_drop_down));
    await tester.pump();

    final openMenu =
        events.where((e) => e.$1 == 'Selection/OpenMenu').toList();
    expect(openMenu, hasLength(1),
        reason: 'the arrow tap must fire exactly one Selection/OpenMenu');

    final payload = openMenu.single.$2;
    expect(payload, isNotNull,
        reason: 'OpenMenu must carry the item geometry — a null payload leaves '
            'Java\'s ToolItem.getBounds() at the hardcoded (0,0) and the menu '
            'opens at the toolbar\'s left edge');
    final toolBarTopLeft =
        tester.getTopLeft(find.byType(_CapturingToolBarSwt));
    final dropDownFinder = find.byType(_CapturingToolItemSwt).last;
    final itemTopLeft = tester.getTopLeft(dropDownFinder);
    final itemSize = tester.getSize(dropDownFinder);

    expect(payload!.x, (itemTopLeft.dx - toolBarTopLeft.dx).round());
    expect(payload.y, (itemTopLeft.dy - toolBarTopLeft.dy).round());
    expect(payload.width, itemSize.width.round());
    expect(payload.height, itemSize.height.round());

    expect(payload.x! > 0, isTrue,
        reason: 'the second item cannot sit at x=0 — that was the bug');
  });
}
