import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

// Java sizes a separator from the thickness this widget gives it, and a zero-height box counts as
// "no bounds", so a separator laid out at zero thickness is never drawn at the size it was given.
VLabel _separator(int style, {VRectangle? bounds}) => VLabel()
  ..id = 1
  ..style = style
  ..text = ''
  ..bounds = bounds;

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

    expect(tester.getSize(find.byType(LabelSwt<VLabel>)), const Size(7, 2));
  });

  testWidgets('a vertical separator without bounds is a line with a thickness',
      (tester) async {
    await tester.pumpWidget(_host(_separator(SWT.SEPARATOR | SWT.VERTICAL)));
    await tester.pumpAndSettle();

    expect(tester.getSize(find.byType(LabelSwt<VLabel>)), const Size(2, 7));
  });

  testWidgets('a separator laid out at its natural thickness takes the width it was given',
      (tester) async {
    await tester.pumpWidget(_host(_separator(
      SWT.SEPARATOR | SWT.HORIZONTAL,
      bounds: VRectangle()
        ..x = 0
        ..y = 0
        ..width = 200
        ..height = 2,
    )));
    await tester.pumpAndSettle();

    expect(tester.getSize(find.byType(LabelSwt<VLabel>)), const Size(200, 2));
    expect(find.byType(Divider), findsOneWidget);
  });
}
