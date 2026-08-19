import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// `disable_evolve_icons` governs every substitution Evolve makes for an application icon — the
/// bundled icon set and the name-to-Material-icon map alike. With it on, the application's own icon
/// draws.
///
/// The bundled set is exercised through the async builder, which resolves the whole chain before
/// returning. The icon-map half lives in `icon_map_substitution_test.dart`: every icon-map key is
/// also a bundled asset and the bundle answers first, so the map only ever serves a name the bundle
/// misses and the map's normalizer catches (`delete_enabled` -> `delete`).
void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  /// An application icon carrying real bytes plus a [filename] Eclipse would ship.
  VImage anAppIcon(String filename) => VImage()
    ..filename = filename
    ..imageData = (VImageData()..data = _onePixelPng);

  group('flag defaults', () {
    test('before any config arrives they match what Java sends', () {
      expect(useEvolveIcons, isTrue);
      expect(preserveIconColors, isFalse);
    });

    test('an explicit disable is honoured', () {
      applyConfigFlags(ConfigFlags()..disable_evolve_icons = true);
      expect(useEvolveIcons, isFalse);
    });
  });

  group('the icon map', () {
    test('substitutes by default', () {
      expect(ImageUtils.buildIconWidget('delete_enabled', size: 16), isNotNull);
    });

    test('does not substitute once disabled', () {
      applyConfigFlags(ConfigFlags()..disable_evolve_icons = true);
      expect(ImageUtils.buildIconWidget('delete_enabled', size: 16), isNull);
    });
  });

  group('the bundled icon set', () {
    testWidgets('substitutes by default', (tester) async {
      await _pumpAsync(tester, anAppIcon('delete'));

      expect(find.byType(SvgPicture), findsOneWidget);
      expect(find.byType(ImageIcon), findsNothing);
    });

    testWidgets('renders the application bytes once disabled', (
      tester,
    ) async {
      applyConfigFlags(ConfigFlags()..disable_evolve_icons = true);

      await _pumpAsync(tester, anAppIcon('delete'));

      expect(find.byType(SvgPicture), findsNothing);
      expect(find.byType(ImageIcon), findsOneWidget);
    });
  });
}

/// The async builder resolves the whole chain before returning, so its result is already final.
Future<void> _pumpAsync(WidgetTester tester, VImage image) async {
  Widget? built;
  await tester.runAsync(() async {
    built = await ImageUtils.buildVImageAsync(image, size: 16);
  });
  expect(built, isNotNull);
  await tester.pumpWidget(_host(built));
  await tester.pump();
}

Widget _host(Widget? built) =>
    MaterialApp(home: Scaffold(body: Center(child: built)));

/// The smallest valid PNG, standing in for a real application icon's bytes.
final List<int> _onePixelPng = <int>[
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
