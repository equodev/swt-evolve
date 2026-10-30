// SWT parity (Windows): Tab reaches a ToolBar, which then holds the keyboard focus, and Down on a
// focused DROP_DOWN item fires Selection with detail ARROW.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/toolbar_evolve.dart';
import 'package:swtflutter/src/theme/theme_extensions/toolbar_theme_extension.dart';
import 'package:swtflutter/src/impl/utils/force_live.dart';

final List<String> _sent = [];

class _RecordingToolItemSwt extends ToolItemSwt<VToolItem> {
  const _RecordingToolItemSwt({required super.value});

  @override
  void sendEvent(VToolItem val, int channel, VEvent? payload) => _sent.add('${val.id} ${wireChannelNames[channel]}');
}

class _RecordingToolBarSwt extends ToolBarSwt<VToolBar> {
  const _RecordingToolBarSwt({required super.value});

  @override
  void sendEvent(VToolBar val, int channel, VEvent? payload) => _sent.add('${val.id} ${wireChannelNames[channel]}');

  @override
  State createState() => _RecordingToolBarImpl();
}

class _RecordingToolBarImpl extends ToolBarImpl<_RecordingToolBarSwt, VToolBar> {
  @override
  Widget getWidgetForToolItem(
    VToolItem toolItem,
    ToolBarThemeExtension widgetTheme, {
    bool shouldLimitSize = false,
    double? maxSize,
  }) =>
      Padding(
        padding: widgetTheme.itemPadding,
        child: _RecordingToolItemSwt(value: toolItem),
      );
}

VToolItem _item(int id, int style, String text) => VToolItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..text = text;

VToolBar _toolBar({int style = 0, List<VToolItem>? items}) => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT | style
  ..enabled = true
  ..visible = true
  ..items = items ??
      [
        _item(2, SWT.PUSH, 'Push'),
        _item(3, SWT.DROP_DOWN, 'Drop'),
      ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 40);

/// The id of the tool item holding the primary focus, or null.
int? _focusedItemId() {
  final context = FocusManager.instance.primaryFocus?.context;
  if (context == null) return null;
  int? id;
  context.visitAncestorElements((element) {
    final w = element.widget;
    if (w is _RecordingToolItemSwt) {
      id = w.value.id;
      return false;
    }
    return true;
  });
  return id;
}

Future<FocusNode> _pumpFieldAndToolBar(WidgetTester tester, [VToolBar? toolBar]) async {
  final field = FocusNode(debugLabel: 'field');
  await tester.pumpWidget(ForceLive(child: EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(width: 400, child: TextField(focusNode: field)),
        SizedBox(width: 400, height: 40, child: _RecordingToolBarSwt(value: toolBar ?? _toolBar())),
      ],
    ),
  )));
  await tester.pumpAndSettle();
  field.requestFocus();
  await tester.pump();
  return field;
}

Future<void> _tabUntil(WidgetTester tester, bool Function() done, {int max = 6}) async {
  for (var i = 0; i < max && !done(); i++) {
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
  }
}

Future<void> _press(WidgetTester tester, LogicalKeyboardKey key) async {
  await tester.sendKeyEvent(key);
  await tester.pump();
}

bool _arrowFocused() {
  final focus = FocusManager.instance.primaryFocus;
  return focus is ToolItemFocusNode && focus.arrow;
}

void main() {
  setUp(_sent.clear);

  testWidgets('Tab onto a tool item reports the ToolBar focused, and leaving its items unfocused', (tester) async {
    final field = await _pumpFieldAndToolBar(tester);

    for (var i = 0; i < 3 && _focusedItemId() == null; i++) {
      await tester.sendKeyEvent(LogicalKeyboardKey.tab);
      await tester.pump();
    }
    expect(_focusedItemId(), isNotNull);
    expect(_sent, contains('1 Focus/FocusIn'));

    // Moving between the bar's own items keeps the ToolBar focused; leaving them is one FocusOut.
    for (var i = 0; i < 5 && _focusedItemId() != null; i++) {
      await tester.sendKeyEvent(LogicalKeyboardKey.tab);
      await tester.pump();
    }
    expect(_focusedItemId(), isNull);
    expect(_sent.where((e) => e.startsWith('1 Focus/')), ['1 Focus/FocusIn', '1 Focus/FocusOut']);

    field.dispose();
  });

  testWidgets('Down on a focused DROP_DOWN item opens its menu', (tester) async {
    final field = await _pumpFieldAndToolBar(tester);

    for (var i = 0; i < 5 && _focusedItemId() != 3; i++) {
      await tester.sendKeyEvent(LogicalKeyboardKey.tab);
      await tester.pump();
    }
    expect(_focusedItemId(), 3);
    _sent.clear();

    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.pump();
    expect(_sent, contains('3 Selection/OpenMenu'));

    field.dispose();
  });

  testWidgets('Right and Left move between the items, and Down on the DROP_DOWN one opens its menu',
      (tester) async {
    final field = await _pumpFieldAndToolBar(tester);

    for (var i = 0; i < 3 && _focusedItemId() == null; i++) {
      await tester.sendKeyEvent(LogicalKeyboardKey.tab);
      await tester.pump();
    }
    expect(_focusedItemId(), 2);

    await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
    await tester.pump();
    expect(_focusedItemId(), 3);

    // Past the last item, back to the first, as on Windows.
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
    await tester.pump();
    expect(_focusedItemId(), 2);
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowLeft);
    await tester.pump();
    expect(_focusedItemId(), 3);

    _sent.clear();
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
    await tester.pump();
    expect(_sent, ['3 Selection/OpenMenu']);

    field.dispose();
  });

  testWidgets('Left and Right skip a disabled item', (tester) async {
    final field = await _pumpFieldAndToolBar(
        tester,
        _toolBar(items: [
          _item(2, SWT.PUSH, 'First'),
          _item(3, SWT.PUSH, 'Disabled')..enabled = false,
          _item(4, SWT.PUSH, 'Last'),
        ]));

    await _tabUntil(tester, () => _focusedItemId() != null);
    expect(_focusedItemId(), 2);
    await _press(tester, LogicalKeyboardKey.arrowRight);
    expect(_focusedItemId(), 4);
    await _press(tester, LogicalKeyboardKey.arrowLeft);
    expect(_focusedItemId(), 2);

    field.dispose();
  });

  testWidgets('Left and Right from a DROP_DOWN arrow move to the neighbouring items', (tester) async {
    final field = await _pumpFieldAndToolBar(
        tester,
        _toolBar(items: [
          _item(2, SWT.PUSH, 'Before'),
          _item(3, SWT.DROP_DOWN, 'Drop'),
          _item(4, SWT.PUSH, 'After'),
        ]));

    await _tabUntil(tester, _arrowFocused);
    expect(_focusedItemId(), 3);
    expect(_arrowFocused(), isTrue);
    await _press(tester, LogicalKeyboardKey.arrowRight);
    expect(_focusedItemId(), 4);

    await _press(tester, LogicalKeyboardKey.arrowLeft);
    expect(_focusedItemId(), 3);
    expect(_arrowFocused(), isFalse);

    await _press(tester, LogicalKeyboardKey.tab);
    expect(_arrowFocused(), isTrue);
    await _press(tester, LogicalKeyboardKey.arrowLeft);
    expect(_focusedItemId(), 2);

    field.dispose();
  });

  testWidgets('RIGHT_TO_LEFT mirrors Left and Right', (tester) async {
    final field = await _pumpFieldAndToolBar(
        tester,
        _toolBar(style: SWT.RIGHT_TO_LEFT, items: [
          _item(2, SWT.PUSH, 'First'),
          _item(3, SWT.PUSH, 'Second'),
          _item(4, SWT.PUSH, 'Third'),
        ]));

    await _tabUntil(tester, () => _focusedItemId() == 2);
    expect(_focusedItemId(), 2);
    await _press(tester, LogicalKeyboardKey.arrowLeft);
    expect(_focusedItemId(), 3);
    await _press(tester, LogicalKeyboardKey.arrowRight);
    expect(_focusedItemId(), 2);
    await _press(tester, LogicalKeyboardKey.arrowRight);
    expect(_focusedItemId(), 4);

    field.dispose();
  });

  testWidgets('A control hosted in a separator item keeps its keys and reports no ToolBar focus', (tester) async {
    final field = await _pumpFieldAndToolBar(
        tester,
        _toolBar(items: [
          _item(2, SWT.PUSH, 'Push'),
          _item(3, SWT.SEPARATOR, '')
            ..width = 120
            ..control = (VText()
              ..swt = 'Text'
              ..id = 4
              ..style = SWT.SINGLE
              ..enabled = true
              ..visible = true
              ..text = 'hosted'
              ..bounds = (VRectangle()
                ..x = 0
                ..y = 0
                ..width = 120
                ..height = 30)),
        ]));

    final hosted = find.descendant(of: find.byType(_RecordingToolBarSwt), matching: find.byType(EditableText));
    expect(hosted, findsOneWidget);
    await tester.tap(hosted);
    await tester.pump();
    final hostedFocus = FocusManager.instance.primaryFocus;
    expect(hostedFocus?.context?.findAncestorWidgetOfExactType<EditableText>(), isNotNull);

    for (final key in [LogicalKeyboardKey.arrowLeft, LogicalKeyboardKey.arrowRight, LogicalKeyboardKey.arrowDown]) {
      await _press(tester, key);
      expect(FocusManager.instance.primaryFocus, same(hostedFocus));
    }
    expect(_sent.where((e) => e.startsWith('1 Focus/')), isEmpty);

    field.dispose();
  });
}
