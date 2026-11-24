// An arrow button draws its whole arrow, centred, whatever its bounds: as native SWT, the arrow
// grows with the button, it is never drawn below the theme's size unless its ink would not fit
// inside the border, and the corners stay within a quarter of the shorter side, so a narrow button
// still reads as a rectangle. A button with no bounds keeps the size of its arrow.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/quiet_animations.dart';

VRectangle _bounds(int width, int height) => VRectangle()
  ..x = 0
  ..y = 0
  ..width = width
  ..height = height;

Future<void> _pump(WidgetTester tester, int alignment, VRectangle? bounds) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: ButtonSwt<VButton>(
        value: VButton()
          ..id = 1
          ..style = SWT.ARROW | alignment
          ..alignment = alignment
          ..enabled = true
          ..visible = true
          ..bounds = bounds,
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  // The default theme: a 15 px arrow icon, a 1 px border and a 4 px corner radius.
  for (final (name, alignment, width, height, iconSize, radius) in [
    ('left', SWT.LEFT, 10, 20, 15.0, 2.5),
    ('right', SWT.RIGHT, 10, 20, 15.0, 2.5),
    ('up', SWT.UP, 20, 10, 15.0, 2.5),
    ('down', SWT.DOWN, 20, 10, 15.0, 2.5),
    // The ink, 7.41/24 of the icon wide, fits the 3 px inside the border at 3 * 24 / 7.41.
    ('tiny left', SWT.LEFT, 5, 8, 9.7166, 1.25),
    // The arrow's long side, 12/24 of the icon, is half the 40 px side.
    ('large right', SWT.RIGHT, 40, 40, 40.0, 4.0),
  ]) {
    testWidgets('a $name arrow button of ${width}x$height draws its whole arrow centred in it', (tester) async {
      await _pump(tester, alignment, _bounds(width, height));

      final surface = find.byType(QuietAnimatedContainer).first;
      final glyph = find.descendant(of: find.byType(Icon), matching: find.byType(RichText));
      final paragraph = tester.renderObject<RenderParagraph>(glyph);

      expect(tester.widget<Icon>(find.byType(Icon)).size, moreOrLessEquals(iconSize, epsilon: 0.001));
      // Within a pixel: the line box rounds up a fractional glyph size.
      expect(paragraph.getMaxIntrinsicWidth(double.infinity), lessThanOrEqualTo(paragraph.size.width + 1),
          reason: 'the glyph is laid out narrower than it is, so part of the arrow is cut off');
      expect(paragraph.getMaxIntrinsicHeight(double.infinity), lessThanOrEqualTo(paragraph.size.height + 1),
          reason: 'the glyph is laid out shorter than it is, so part of the arrow is cut off');
      expect((tester.getCenter(find.byType(Icon)) - tester.getCenter(surface)).distance, lessThan(0.5));

      final decoration = tester.widget<QuietAnimatedContainer>(surface).decoration! as BoxDecoration;
      expect((decoration.borderRadius! as BorderRadius).topLeft.x, radius);
    });
  }

  testWidgets('an arrow button with no bounds is as large as its arrow and its border', (tester) async {
    await _pump(tester, SWT.LEFT, null);

    final surface = find.byType(QuietAnimatedContainer).first;
    expect(tester.getSize(surface), const Size(17, 17));
    expect(tester.widget<Icon>(find.byType(Icon)).size, 15.0);
    final decoration = tester.widget<QuietAnimatedContainer>(surface).decoration! as BoxDecoration;
    expect((decoration.borderRadius! as BorderRadius).topLeft.x, 4.0);
  });
}
