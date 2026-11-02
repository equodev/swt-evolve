import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

VLabel _title(int height) => VLabel()
  ..id = 1
  ..style = SWT.LEFT
  ..visible = true
  ..text = 'Solution - orders'
  ..font = (VFont()..fontData = [VFontData()..height = 11..name = 'System'])
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 513
    ..height = height);

void main() {
  setUp(resetConfigFlags);

  // An application may size a label by GC.textExtent of the label's font (a FormAttachment offset,
  // a heightHint). Without SWT fonts the label draws in the theme's text size, whose line can be
  // taller than that box: the whole line still has to show.
  for (final height in [12, 14]) {
    testWidgets('a label shorter than its drawn line shows the whole line (height $height)',
        (tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(width: 513, height: height.toDouble(), child: LabelSwt<VLabel>(value: _title(height))),
        ),
      ));
      await tester.pumpAndSettle();

      final text = find.text('Solution - orders');
      final line = tester.renderObject<RenderBox>(text);
      final natural = line.getMaxIntrinsicHeight(double.infinity);
      expect(line.size.height, greaterThanOrEqualTo(natural),
          reason: 'the line is laid out shorter than it is, so its glyphs are clipped');
      final label = tester.getRect(find.byType(LabelSwt<VLabel>));
      final drawn = tester.getRect(text);
      expect(drawn.top, greaterThanOrEqualTo(label.top - 0.01), reason: 'the top of the line is cut');
      expect(drawn.bottom, lessThanOrEqualTo(label.bottom + 0.01), reason: 'the bottom of the line is cut');
    });
  }
}
