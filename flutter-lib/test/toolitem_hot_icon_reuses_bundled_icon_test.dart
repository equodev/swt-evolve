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
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

/// The by-filename half of "a hovered item keeps the replacement" — see
/// `toolitem_hot_icon_reuses_replacement_test.dart` for the `svgContent` half.
///
/// An application running without an external pack still has its icons substituted: the bundled
/// Evolve set answers for the normal name, inside Dart, leaving `svgContent` empty. Of 1252 bundled
/// icons exactly one carries "hot" in its name, so the hover state is essentially never covered —
/// which makes this the common case, not the rare one.
///
/// The substitution renders an [SvgPicture]; the application's own bytes render an [ImageIcon].
/// The bundle only resolves under [WidgetTester.runAsync], which is why these run that way.

/// A bundled icon exists for this name.
const String _coveredName = 'delete';

/// Nothing in the bundled set or the icon map answers for this one.
const String _uncoveredName = 'zz_evolve_has_no_icon_for_this';

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

/// An application icon: its own bytes, under the name Eclipse ships it as.
VImage _appIcon(String filename) => VImage()
  ..filename = filename
  ..imageData = (VImageData()..data = _appBytes);

Widget _host() => EvolveApp(
  theme: ThemeMode.light,
  contentWidget: SizedBox(
    width: 400,
    height: 40,
    child: ToolBarSwt(
      value: VToolBar()
        ..id = 1
        ..style = SWT.HORIZONTAL | SWT.FLAT
        ..enabled = true
        ..visible = true
        ..items = [
          VToolItem()
            ..id = 2
            ..style = SWT.PUSH
            ..enabled = true
            ..width = 24
            ..image = _appIcon(_coveredName)
            ..hotImage = _appIcon(_uncoveredName),
        ]
        ..bounds = (VRectangle()
          ..x = 0
          ..y = 0
          ..width = 400
          ..height = 40),
    ),
  ),
);

/// Lets real asynchronous work (the bundle lookup) finish and paints what it produced.
Future<void> _drain(WidgetTester tester) async {
  for (var i = 0; i < 25; i++) {
    await Future<void>.delayed(const Duration(milliseconds: 20));
    await tester.pump();
  }
}

void main() {
  setUp(() {
    resetConfigFlags();
    ImageUtils.clearCache();
  });
  tearDown(resetConfigFlags);

  testWidgets('a hovered item keeps the bundled icon when the hot name is not in the set',
      (tester) async {
    await tester.runAsync(() async {
      await tester.pumpWidget(_host());
      await _drain(tester);
      expect(find.byType(SvgPicture), findsOneWidget,
          reason: 'the bundled set answers for the normal name');

      final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
      await gesture.addPointer(location: Offset.zero);
      await gesture.moveTo(tester.getCenter(find.byType(ToolItemSwt)));
      await _drain(tester);

      expect(find.byType(SvgPicture), findsOneWidget);
      expect(find.byType(ImageIcon), findsNothing);
      await gesture.removePointer();
    });
  });
}
