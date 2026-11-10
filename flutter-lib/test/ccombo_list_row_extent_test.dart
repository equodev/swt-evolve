// Every row of an open CCombo's list is as high as ccomboItemExtent says, which is what Java answers
// from getItemHeight(): a program, or a test, that places a row from that answer finds it there.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/ccombo_theme_extension.dart';
import 'package:swtflutter/src/theme/theme_settings/ccombo_theme_settings.dart';

class _QuietCComboSwt extends CComboSwt<VCCombo> {
  const _QuietCComboSwt({required super.value});

  @override
  void sendEvent(VCCombo val, int channel, VEvent? payload) {}
}

const _items = ['alpha', 'bravo', 'charlie', 'delta'];

VCCombo _combo(int style) => VCCombo()
  ..swt = 'CCombo'
  ..id = 3
  ..style = style
  ..enabled = true
  ..items = _items
  ..text = ''
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 60);

void main() {
  for (final (name, bits) in [('editable', SWT.NONE), ('read-only', SWT.READ_ONLY)]) {
    testWidgets('an $name CCombo lays every list row out at ccomboItemExtent', (tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(
          alignment: Alignment.topCenter,
          child: SizedBox(width: 200, height: 60, child: _QuietCComboSwt(value: _combo(bits))),
        ),
      ));
      await tester.pumpAndSettle();
      await tester.tap(find.byIcon(Icons.arrow_drop_down).first);
      await tester.pumpAndSettle();

      final context = tester.element(find.byType(MenuItemButton).first);
      final theme = Theme.of(context).extension<CComboThemeExtension>()!;
      final style = tester.widget<Text>(find.text('alpha').last).style!;
      final extent = ccomboItemExtent(theme, style);
      for (final item in _items) {
        final row = tester.getRect(find.ancestor(of: find.text(item).last, matching: find.byType(MenuItemButton)).first);
        expect(row.height, extent, reason: 'row "$item"');
      }
    });
  }
}
