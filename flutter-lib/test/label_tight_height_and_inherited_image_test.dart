import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart';

import 'delivery/support/deliver.dart';

VLabel _label({required int height, bool ownBackground = false}) => VLabel()
  ..id = 1
  ..style = SWT.NONE
  ..text = 'Discharge'
  ..background = (VColor()
    ..red = 240
    ..green = 240
    ..blue = 240
    ..alpha = 255)
  ..hasOwnBackground = ownBackground ? true : null
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = height);

Widget _wrap(Widget child) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(width: 300, height: 60, child: child),
    );

Color? _paintedBackground(WidgetTester tester) {
  for (final container in tester.widgetList<Container>(find.byType(Container))) {
    final decoration = container.decoration;
    if (decoration is BoxDecoration && decoration.color != null) return decoration.color;
  }
  return null;
}

void main() {
  // Applications commonly size a label with GridData.heightHint = GC.getFontMetrics().getHeight():
  // exactly one line, with no room for the theme's padding.
  testWidgets('a label as tall as its line draws the whole line', (tester) async {
    await tester.pumpWidget(_wrap(LabelSwt<VLabel>(value: _label(height: 40))));
    await tester.pumpAndSettle();
    final line = tester.getSize(find.byType(RichText).first).height;

    final height = line.ceil();
    freshClient();
    await tester.pumpWidget(_wrap(LabelSwt<VLabel>(value: _label(height: height))));
    await tester.pumpAndSettle();

    final bounds = tester.getRect(find.byWidgetPredicate(
        (w) => w is Container && w.constraints?.maxHeight == height.toDouble()));
    final drawn = tester.getRect(find.byType(RichText).first);
    expect(drawn.height, greaterThanOrEqualTo(line),
        reason: 'the vertical padding leaves the line less room than it needs, clipping the glyphs');
    expect(drawn.top, greaterThanOrEqualTo(bounds.top),
        reason: 'the top of the line falls outside the label and is clipped');
    expect(drawn.bottom, lessThanOrEqualTo(bounds.bottom),
        reason: 'the bottom of the line falls outside the label and is clipped');
  });

  // Eclipse Forms' Section paints its title-bar gradient as its background image and sets
  // INHERIT_DEFAULT, so its title Label shows the gradient behind the text.
  testWidgets('a label paints no inherited colour over an ancestor background image',
      (tester) async {
    await tester.pumpWidget(_wrap(ParentBackgroundScope(
      background: null,
      backgroundImage: VImage(),
      child: SwtColorScope(child: LabelSwt<VLabel>(value: _label(height: 20))),
    )));
    await tester.pumpAndSettle();

    expect(_paintedBackground(tester), isNull);
  });

  testWidgets('a label keeps its own colour over an ancestor background image', (tester) async {
    await tester.pumpWidget(_wrap(ParentBackgroundScope(
      background: null,
      backgroundImage: VImage(),
      child: SwtColorScope(
          child: LabelSwt<VLabel>(value: _label(height: 20, ownBackground: true))),
    )));
    await tester.pumpAndSettle();

    expect(_paintedBackground(tester), const Color(0xFFF0F0F0));
  });
}
