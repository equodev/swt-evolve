// A CLabel's image must occupy exactly the width Java budgeted for it.
//
// CLabelSizes.computeSize sizes the widget as
//     leftMargin + rightMargin + imageData.width + IMAGE_SPACING + textWidth
// so any extra width the Dart side spends on the image is taken out of the
// text, which then ellipsizes. ImageUtils applies a toolbar-sized minimum box
// to images built without an explicit `size`, so an image with no icons_map or
// asset replacement renders wider than its imageData and clips the text.

import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/clabel.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

// chevron_right_16.png — a 16x16 icon with no icons_map entry, so it renders
// from its bytes rather than as a replacement icon.
const String _chevronRightPngBase64 =
    'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAIlJ'
    'REFUOBFjYBgFtAuBromzNvdNnaNCyAYmXAoYGRi8//z+d7V74qy27kWLuHGpw2nAfyBg'
    'YPjPBqQqGd59vwk0KAKbITgNYGRkBBoAAf8ZGb4x/md8C+Mj0yzIHGQ22AWMjD8YGf63'
    'sTPKduXle/1EloexcRoAVLCFlZWluDAr6R5M8ShNoxAAAAyMK0qpGOZNAAAAAElFTkSu'
    'QmCC';

// dev/equo/swt/size/CLabelSizes.java
const double kHorizontalPadding = 6.0; // leftMargin + rightMargin
const double kImageSpacing = 4.0; // IMAGE_SPACING
const int kImageWidth = 16;

VCLabel _titleLabel({VRectangle? bounds}) => VCLabel()
  ..id = 11
  ..style = SWT.NONE
  ..enabled = true
  ..text = 'Query Parameters'
  ..bounds = bounds
  ..image = (VImage()
    ..filename = 'chevron_right_16.png'
    ..imageData = (VImageData()
      ..width = kImageWidth
      ..height = kImageWidth
      ..data = base64Decode(_chevronRightPngBase64)));

void main() {
  testWidgets('CLabel sized by CLabelSizes shows its full text', (tester) async {
    // Measure the title with the style the CLabel actually renders it in.
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: CLabelSwt<VCLabel>(value: _titleLabel()),
      ),
    ));
    await tester.pumpAndSettle();

    final Text title = tester.widget<Text>(find.byType(Text));
    final painter = TextPainter(
      text: TextSpan(text: title.data, style: title.style),
      maxLines: 1,
      textDirection: TextDirection.ltr,
    )..layout();
    final double textWidth = painter.width;

    // The bounds Java hands back for exactly this content.
    final int width =
        (kHorizontalPadding + kImageWidth + kImageSpacing + textWidth).ceil();
    final bounds = VRectangle()
      ..x = 0
      ..y = 0
      ..width = width
      ..height = 24;

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: CLabelSwt<VCLabel>(value: _titleLabel(bounds: bounds)),
      ),
    ));
    await tester.pumpAndSettle();

    final double viewportWidth = tester.getSize(find.byType(Text)).width;
    expect(viewportWidth, greaterThanOrEqualTo(textWidth),
        reason: 'The title viewport (${viewportWidth.toStringAsFixed(2)}px) is '
            'narrower than the title (${textWidth.toStringAsFixed(2)}px): the '
            'image is spending more than the ${kImageWidth}px Java budgeted, '
            'so the text is ellipsized.');
  });
}
