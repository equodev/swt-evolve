import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

/// An application keeps its own icon for the disabled state, and the icon pack that replaced the
/// enabled one carries no counterpart for it. Drawing the application's would put its artwork next
/// to siblings that were replaced, so the replaced icon has to serve both states — the dimming for
/// disabled is the renderer's, not the asset's.
///
/// A replaced icon arrives as `svgContent` and renders an [SvgPicture]; one the pack had nothing for
/// arrives as bytes and renders an [ImageIcon]. That is what these assert on. Both states are
/// covered with at least one replaced icon on purpose: an unreplaced `filename` leaves a bundle
/// probe that never resolves under flutter_test (see icon_map_substitution_test.dart), so a case
/// with no replacement at all cannot be asserted through the widget.

const String _replacementSvg =
    '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16">'
    '<rect width="16" height="16" fill="black"/></svg>';

/// The smallest valid PNG, standing in for the application's own bytes.
const List<int> _appBytes = <int>[
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, //
  0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
  0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
  0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
  0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
  0x54, 0x78, 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
  0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4, 0x00,
  0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE,
  0x42, 0x60, 0x82,
];

/// An icon the pack replaced: Java resolved it and sent the SVG.
VImage _replaced() => VImage()..svgContent = _replacementSvg;

/// An icon the pack had nothing for: the application's own bytes came through.
VImage _appOwn(String filename) => VImage()
  ..filename = filename
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..data = _appBytes);

VToolBar _toolBar(VToolItem item) => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [item]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 40);

VToolItem _disabledItem({VImage? image, VImage? disabledImage}) => VToolItem()
  ..id = 2
  ..style = SWT.PUSH
  ..enabled = false
  ..width = 24
  ..image = image
  ..disabledImage = disabledImage;

Future<void> _pump(WidgetTester tester, VToolItem item) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 400,
      height: 40,
      child: ToolBarSwt(value: _toolBar(item)),
    ),
  ));
  await tester.pumpAndSettle();
}

void main() {
  // The widget caches are static and keyed by image content, so a scenario would otherwise
  // read back the previous one's resolved widget.
  setUp(ImageUtils.clearCache);

  testWidgets('a disabled item reuses the replaced icon when its own has no replacement',
      (tester) async {
    await _pump(
      tester,
      _disabledItem(image: _replaced(), disabledImage: _appOwn('delete_disabled_16')),
    );

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(ImageIcon), findsNothing);
  });

  testWidgets('a disabled item keeps its own icon when that is the replaced one', (tester) async {
    await _pump(
      tester,
      _disabledItem(image: _appOwn('save_24'), disabledImage: _replaced()),
    );

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(ImageIcon), findsNothing);
  });

}
