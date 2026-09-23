import 'package:flutter/gestures.dart' show PointerDeviceKind;
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

/// The hover state of a ToolItem is the disabled state's problem again: an application names its
/// hot icon as a second file (Eclipse fills it from `IAction.getHoverImageDescriptor()`), which the
/// icon pack often has no counterpart for. Drawing it would make the hovered item the only one in
/// the bar in the application's own artwork, so the replaced icon has to serve both states — hover
/// feedback is the zoom and the highlight, never a different picture.
///
/// A replaced icon arrives as `svgContent` and renders an [SvgPicture]; one the pack had nothing
/// for arrives as bytes and renders an [ImageIcon] over a [MemoryImage] of those bytes. That is
/// what these assert on.

const String _replacementSvg =
    '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16">'
    '<rect width="16" height="16" fill="black"/></svg>';

/// Two distinct valid PNGs, standing in for the application's own artwork. Their lengths differ on
/// purpose: [ImageUtils]'s binary-image cache is keyed by byte length, so same-length bytes would
/// resolve to each other's widget.
const List<int> _normalBytes = <int>[
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, //
  0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
  0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
  0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
  0x89, 0x00, 0x00, 0x00, 0x0D, 0x49, 0x44, 0x41,
  0x54, 0x78, 0xDA, 0x63, 0xF8, 0xCF, 0xC0, 0xF0,
  0x1F, 0x00, 0x05, 0x00, 0x01, 0xFF, 0x56, 0xC7,
  0x2F, 0x0D, 0x00, 0x00, 0x00, 0x00, 0x49, 0x45,
  0x4E, 0x44, 0xAE, 0x42, 0x60, 0x82,
];

const List<int> _hotBytes = <int>[
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, //
  0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
  0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x02,
  0x08, 0x06, 0x00, 0x00, 0x00, 0x72, 0xB6, 0x0D,
  0x24, 0x00, 0x00, 0x00, 0x10, 0x49, 0x44, 0x41,
  0x54, 0x78, 0xDA, 0x63, 0x60, 0x60, 0xF8, 0xFF,
  0x1F, 0x82, 0xA1, 0x0C, 0x00, 0x3F, 0xD2, 0x07,
  0xF9, 0x5C, 0x13, 0xE0, 0x42, 0x00, 0x00, 0x00,
  0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE, 0x42, 0x60,
  0x82,
];

/// An icon the pack replaced: Java resolved it and sent the SVG.
VImage _replaced() => VImage()..svgContent = _replacementSvg;

/// An icon the pack had nothing for: the application's own bytes came through. [filename] is left
/// off when *both* of an item's icons are unreplaced — two unresolved bundle probes never settle
/// under flutter_test (see icon_map_substitution_test.dart), and the branch under test reads
/// `svgContent` alone, so the filename adds nothing to it.
VImage _appOwn(List<int> bytes, {String? filename}) => VImage()
  ..filename = filename
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..data = bytes);

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

VToolItem _item({VImage? image, VImage? hotImage}) => VToolItem()
  ..id = 2
  ..style = SWT.PUSH
  ..enabled = true
  ..width = 24
  ..image = image
  ..hotImage = hotImage;

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

/// Moves the pointer onto the item and lets the hover animation finish.
Future<void> _hover(WidgetTester tester) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await gesture.addPointer(location: Offset.zero);
  addTearDown(gesture.removePointer);
  await gesture.moveTo(tester.getCenter(find.byType(ToolItemSwt)));
  await tester.pumpAndSettle();
}

/// The bytes the drawn [ImageIcon] is painting.
List<int> _drawnBytes(WidgetTester tester) =>
    (tester.widget<ImageIcon>(find.byType(ImageIcon)).image as MemoryImage).bytes;

void main() {
  // The widget caches are static and keyed by image content, so a scenario would otherwise
  // read back the previous one's resolved widget.
  setUp(ImageUtils.clearCache);

  testWidgets('a hovered item keeps the replaced icon when its hot icon has no replacement',
      (tester) async {
    await _pump(
      tester,
      _item(image: _replaced(), hotImage: _appOwn(_hotBytes, filename: 'save_hot_16')),
    );
    expect(find.byType(SvgPicture), findsOneWidget);

    await _hover(tester);

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(ImageIcon), findsNothing);
  });

  testWidgets('a hovered item takes its hot icon when that is the replaced one', (tester) async {
    await _pump(
      tester,
      _item(image: _appOwn(_normalBytes, filename: 'save_24'), hotImage: _replaced()),
    );

    await _hover(tester);

    expect(find.byType(SvgPicture), findsOneWidget);
    expect(find.byType(ImageIcon), findsNothing);
  });

  testWidgets('with nothing replaced the hot icon is still what hovering draws', (tester) async {
    await _pump(
      tester,
      _item(
        image: _appOwn(_normalBytes),
        hotImage: _appOwn(_hotBytes),
      ),
    );
    expect(_drawnBytes(tester), _normalBytes);

    await _hover(tester);

    expect(_drawnBytes(tester), _hotBytes);
  });
}
