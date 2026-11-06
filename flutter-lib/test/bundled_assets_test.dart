// This package's fonts and icons resolve whether it runs as the app or as a dependency of another
// app, where Flutter keeps them under packages/swtflutter/.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/impl/utils/bundled_assets.dart';
import 'package:swtflutter/src/impl/utils/font_utils.dart';

class _FontManifestBundle extends CachingAssetBundle {
  _FontManifestBundle(this.interFamily);

  final String interFamily;

  @override
  Future<ByteData> load(String key) async {
    if (key != 'FontManifest.json') throw StateError('no asset $key');
    final manifest = jsonEncode([
      {'family': 'MaterialIcons', 'fonts': []},
      {'family': interFamily, 'fonts': []},
    ]);
    return ByteData.sublistView(Uint8List.fromList(utf8.encode(manifest)));
  }
}

VFont _font(String name) => VFont()..fontData = [VFontData()..name = name..height = 12..style = 0];

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  tearDown(() => BundledAssets.resolve(_FontManifestBundle('Inter')));

  test('as the app, the assets and families keep the names pubspec.yaml gives them', () async {
    await BundledAssets.resolve(_FontManifestBundle('Inter'));

    expect(BundledAssets.path('assets/icons/add.svg'), 'assets/icons/add.svg');
    expect(FontUtils.textStyleFromVFont(_font('Liberation Sans'), null).fontFamily, 'Liberation Sans');
    expect(FontUtils.textStyleFromVFont(_font('Liberation Sans'), null).fontFamilyFallback, ['Inter']);
  });

  test('as a dependency, they are read under the package', () async {
    await BundledAssets.resolve(_FontManifestBundle('packages/swtflutter/Inter'));

    expect(BundledAssets.path('assets/icons/add.svg'), 'packages/swtflutter/assets/icons/add.svg');
    final style = FontUtils.textStyleFromVFont(_font('Liberation Sans'), null);
    expect(style.fontFamily, 'packages/swtflutter/Liberation Sans');
    expect(style.fontFamilyFallback, ['packages/swtflutter/Inter'],
        reason: 'the symbols still fall back to the bundled Inter');
    expect(FontUtils.fallbackFor(style.fontFamily), ['packages/swtflutter/Inter'],
        reason: 'a family already resolved, as a StyledText range carries it, falls back the same');
  });

  test('a family the package does not bundle is left as Java names it', () async {
    await BundledAssets.resolve(_FontManifestBundle('packages/swtflutter/Inter'));

    expect(FontUtils.textStyleFromVFont(_font('Menlo'), null).fontFamily, 'Menlo');
    expect(BundledAssets.bundledFamily('Menlo'), isNull);
    expect(BundledAssets.bundledFamily('packages/swtflutter/Inter'), 'Inter');
  });
}
