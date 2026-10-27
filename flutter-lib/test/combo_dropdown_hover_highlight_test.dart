// A drop-down Combo's open list has one highlighted item: hover moves it and the arrow keys go on from it.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/combo_theme_extension.dart';

class _CapturingComboSwt extends ComboSwt<VCombo> {
  const _CapturingComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCombo val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VCombo _combo(int style) => VCombo()
  ..swt = 'Combo'
  ..id = 7
  ..style = style
  ..enabled = true
  ..items = const ['MD', 'TVD', 'TVDSS', 'TWT']
  ..text = 'TVD'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 120
    ..height = 30);

Future<List<(String, VEvent?)>> _pump(WidgetTester tester, int style) async {
  final events = <(String, VEvent?)>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: 120,
        height: 30,
        child: _CapturingComboSwt(
          value: _combo(style),
          onEvent: (ev, payload) => events.add((ev, payload)),
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return events;
}

Future<void> _openWithArrow(WidgetTester tester) async {
  await tester.tap(find.byIcon(Icons.arrow_drop_down));
  await tester.pumpAndSettle();
  expect(find.text('TWT'), findsOneWidget, reason: 'sanity: the arrow opens the list');
}

List<String> _highlighted(WidgetTester tester) {
  final theme = Theme.of(tester.element(find.text('TWT'))).extension<ComboThemeExtension>()!;
  final painted = {theme.selectedItemBackgroundColor, theme.hoverBackgroundColor};
  return [
    for (final label in const ['MD', 'TVD', 'TVDSS', 'TWT'])
      if (tester
          .widgetList<AnimatedContainer>(
              find.ancestor(of: find.text(label), matching: find.byType(AnimatedContainer)))
          .any((c) => painted.contains((c.decoration as BoxDecoration?)?.color)))
        label,
  ];
}

void main() {
  for (final (name, style) in const [('editable', SWT.DROP_DOWN), ('READ_ONLY', SWT.READ_ONLY)]) {
    testWidgets('$name: hover moves the highlight and ArrowDown goes on from it', (tester) async {
      final events = await _pump(tester, style);
      await _openWithArrow(tester);
      final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
      await mouse.addPointer(location: Offset.zero);
      addTearDown(mouse.removePointer);

      await mouse.moveTo(tester.getCenter(find.text('MD')));
      await tester.pumpAndSettle();
      expect(_highlighted(tester), ['MD']);

      await tester.sendKeyEvent(LogicalKeyboardKey.arrowDown);
      await tester.pumpAndSettle();
      expect(_highlighted(tester), ['TVD'], reason: 'the pointer still over MD must not keep it lit');

      await tester.sendKeyEvent(LogicalKeyboardKey.enter);
      await tester.pumpAndSettle();
      expect(events.where((e) => e.$1 == 'Selection/Selection').map((e) => e.$2?.text), ['TVD']);
    });
  }
}
