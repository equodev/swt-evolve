import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/theme/theme_extensions/label_theme_extension.dart';

// Java sizes a separator from the thickness this widget gives it, and a zero-height box counts as
// "no bounds", so a separator laid out at zero thickness is never drawn at the size it was given.
VLabel _separator(int style) => VLabel()
  ..id = 1
  ..style = style
  ..text = '';

// How long the widget makes a separator that has no bounds yet.
const _unboundedLength = 7.0;

double _themeThickness(WidgetTester tester) =>
    Theme.of(tester.element(find.byType(LabelSwt<VLabel>)))
        .extension<LabelThemeExtension>()!
        .separatorThickness;

Widget _host(VLabel value) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: LabelSwt<VLabel>(value: value),
      ),
    );

void main() {
  testWidgets('a horizontal separator without bounds is a line with a thickness',
      (tester) async {
    await tester.pumpWidget(_host(_separator(SWT.SEPARATOR | SWT.HORIZONTAL)));
    await tester.pumpAndSettle();

    final thickness = _themeThickness(tester);
    expect(thickness, greaterThan(0));
    expect(tester.getSize(find.byType(LabelSwt<VLabel>)), Size(_unboundedLength, thickness));
  });

  testWidgets('a vertical separator without bounds is a line with a thickness',
      (tester) async {
    await tester.pumpWidget(_host(_separator(SWT.SEPARATOR | SWT.VERTICAL)));
    await tester.pumpAndSettle();

    final thickness = _themeThickness(tester);
    expect(thickness, greaterThan(0));
    expect(tester.getSize(find.byType(LabelSwt<VLabel>)), Size(thickness, _unboundedLength));
  });
}
