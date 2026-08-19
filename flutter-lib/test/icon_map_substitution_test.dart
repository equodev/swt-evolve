import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

/// The icon-map half of `disable_evolve_icons` — see `icon_substitution_flag_test.dart` for the
/// bundled half and the flag defaults.
///
/// Every icon-map key is also a bundled asset and the bundle is consulted first, so the map only
/// answers for a name the bundle misses and the map's normalizer catches (`delete_enabled` ->
/// `delete`). That miss leaves a bundle probe that never resolves under flutter_test and would
/// block any later `runAsync` in the same file, which is why these live apart.
void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  VImage anAppIcon() => VImage()
    ..filename = 'delete_enabled'
    ..imageData = (VImageData()..data = _onePixelPng);

  testWidgets('the icon map substitutes by default', (tester) async {
    await _pump(tester, anAppIcon());

    expect(find.byIcon(Icons.delete), findsOneWidget);
    expect(find.byType(ImageIcon), findsNothing);
  });

  testWidgets('the application bytes render once disabled', (
    tester,
  ) async {
    applyConfigFlags(ConfigFlags()..disable_evolve_icons = true);

    await _pump(tester, anAppIcon());

    expect(find.byIcon(Icons.delete), findsNothing);
    expect(find.byType(ImageIcon), findsOneWidget);
  });
}

Future<void> _pump(WidgetTester tester, VImage image) async {
  final built = ImageUtils.buildVImage(image, size: 16);
  expect(built, isNotNull);
  await tester.pumpWidget(
    MaterialApp(home: Scaffold(body: Center(child: built))),
  );
  await tester.pump();
}

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
