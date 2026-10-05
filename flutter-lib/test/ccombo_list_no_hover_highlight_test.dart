// A CCombo's list is an SWT List: it highlights the selection only, and the pointer over it moves
// nothing, so the keys go on from the selection.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/key_forwarding.dart';
import 'package:swtflutter/src/theme/theme_extensions/ccombo_theme_extension.dart';

class _CapturingCComboSwt extends CComboSwt<VCCombo> {
  const _CapturingCComboSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCCombo val, String ev, VEvent? payload) => onEvent(ev, payload);
}

VCCombo _combo(int style) => VCCombo()
  ..swt = 'CCombo'
  ..id = 11
  ..style = style
  ..enabled = true
  ..items = const ['MD', 'TVD', 'TVDSS', 'TWT']
  ..text = 'TVD'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 24);

const _items = ['MD', 'TVD', 'TVDSS', 'TWT'];

Future<(List<(String, String?)>, TestGesture)> _open(WidgetTester tester, int style) async {
  final events = <(String, String?)>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Center(
      child: SizedBox(
        width: 200,
        height: 24,
        child: _CapturingCComboSwt(
          value: _combo(style),
          onEvent: (ev, payload) => events.add((ev, payload?.text)),
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
  await tester.tap(find.byIcon(Icons.arrow_drop_down).first);
  await tester.pumpAndSettle();
  events.clear();

  final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await mouse.addPointer(location: Offset.zero);
  addTearDown(mouse.removePointer);
  return (events, mouse);
}

Future<void> _hover(WidgetTester tester, TestGesture mouse, String label) async {
  await mouse.moveTo(tester.getCenter(find.text(label).hitTestable()));
  await tester.pumpAndSettle();
}

List<String> _highlighted(WidgetTester tester) {
  final theme = Theme.of(tester.element(find.byType(EditableText).first))
      .extension<CComboThemeExtension>()!;
  return [
    for (final label in _items)
      if (tester
          .widgetList<Material>(find.ancestor(
              of: find.text(label).hitTestable(), matching: find.byType(Material)))
          .any((m) => m.color == theme.selectedItemBackgroundColor))
        label,
  ];
}

List<String?> _selections(List<(String, String?)> events) =>
    events.where((e) => e.$1 == 'Selection/Selection').map((e) => e.$2).toList();

void main() {
  for (final (name, style) in [('editable', 0), ('READ_ONLY', SWT.READ_ONLY)]) {
    testWidgets('$name: hover does not move the highlight', (tester) async {
      final (_, mouse) = await _open(tester, style);
      expect(_highlighted(tester), ['TVD']);

      await _hover(tester, mouse, 'TWT');
      expect(_highlighted(tester), ['TVD']);
    });

    testWidgets('$name: after a hover the arrow keys are still Java\'s, from the selection', (tester) async {
      final (_, mouse) = await _open(tester, style);
      await _hover(tester, mouse, 'TWT');

      expect(openListClaimsKey(const KeyDownEvent(
        physicalKey: PhysicalKeyboardKey.arrowUp,
        logicalKey: LogicalKeyboardKey.arrowUp,
        timeStamp: Duration.zero,
      )), isFalse);
    });

    testWidgets('$name: Enter after a hover does not pick the hovered item', (tester) async {
      final (events, mouse) = await _open(tester, style);
      await _hover(tester, mouse, 'MD');

      await tester.sendKeyEvent(LogicalKeyboardKey.enter);
      await tester.pumpAndSettle();
      expect(_selections(events), isNot(contains('MD')));
    });
  }
}
