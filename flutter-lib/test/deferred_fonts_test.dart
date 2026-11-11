// The web page keeps the faces named fonts are substituted with out of the engine's start
// (web/flutter_bootstrap.js). A family is registered when text first asks for it, and what lays text
// out before it arrives lays it out again.

import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/deferred_fonts.dart';
import 'package:swtflutter/src/impl/utils/font_utils.dart';

final _manifest = jsonEncode([
  {
    'family': 'Liberation Sans',
    'fonts': [
      {'asset': 'assets/fonts/LiberationSans-Regular.ttf'},
      {'asset': 'assets/fonts/LiberationSans-Bold.ttf', 'weight': 700},
    ],
  },
  {
    'family': 'Liberation Mono',
    'fonts': [
      {'asset': 'assets/fonts/LiberationMono-Regular.ttf'},
    ],
  },
]);

VFont _font(String name) => VFont()
  ..fontData = [
    VFontData()
      ..name = name
      ..height = 12
      ..style = 0,
  ];

void main() {
  final registered = <String, List<String>>{};
  Future<void> record(String family, List<String> assets) async => registered[family] = assets;

  setUp(registered.clear);
  tearDown(() => DeferredFonts.configure(null));

  test('nothing is registered until text asks for a family', () async {
    DeferredFonts.configure(_manifest, register: record);
    await pumpEventQueue();
    expect(registered, isEmpty);
  });

  test('a text style in a deferred family registers that family, with all of its faces, once', () async {
    DeferredFonts.configure(_manifest, register: record);

    FontUtils.textStyleFromVFont(_font('Liberation Sans'), null);
    FontUtils.textStyleFromVFont(_font('Liberation Sans'), null);
    await pumpEventQueue();

    expect(registered, {
      'Liberation Sans': ['assets/fonts/LiberationSans-Regular.ttf', 'assets/fonts/LiberationSans-Bold.ttf'],
    });
  });

  test('a family the engine already has registers nothing', () async {
    DeferredFonts.configure(_manifest, register: record);
    await DeferredFonts.request('Inter');
    await DeferredFonts.request('System Web');
    expect(registered, isEmpty);
  });

  test('all() registers every family and waits for them', () async {
    final pending = <String, Completer<void>>{};
    DeferredFonts.configure(_manifest, register: (family, _) => (pending[family] = Completer<void>()).future);
    var done = false;
    unawaited(DeferredFonts.all().then((_) => done = true));
    await pumpEventQueue();
    expect(pending.keys, unorderedEquals(['Liberation Sans', 'Liberation Mono']));

    pending['Liberation Mono']!.complete();
    await pumpEventQueue();
    expect(done, isFalse, reason: 'Liberation Sans is still loading');

    pending['Liberation Sans']!.complete();
    await pumpEventQueue();
    expect(done, isTrue);
  });

  test('a family that fails to load does not hold all() back', () async {
    DeferredFonts.configure(_manifest, register: (family, assets) async {
      if (family == 'Liberation Mono') throw StateError('not found');
      registered[family] = assets;
    });
    await DeferredFonts.all();
    expect(registered.keys, ['Liberation Sans']);
  });

  test('without a manifest, as off the web, nothing is deferred', () async {
    DeferredFonts.configure(null, register: record);
    await DeferredFonts.all();
    await DeferredFonts.request('Liberation Sans');
    expect(registered, isEmpty);
  });

  testWidgets('a GC scene is painted again when a font arrives', (tester) async {
    await tester.pumpWidget(Directionality(
      textDirection: TextDirection.ltr,
      child: CustomPaint(size: const Size(40, 20), painter: ScenePainter(Colors.white, const [])),
    ));
    final scene = tester.renderObject<RenderCustomPaint>(
      find.byWidgetPredicate((w) => w is CustomPaint && w.painter is ScenePainter),
    );
    expect(scene.debugNeedsPaint, isFalse);

    await PaintingBinding.instance.handleSystemMessage(<String, dynamic>{'type': 'fontsChange'});

    expect(scene.debugNeedsPaint, isTrue, reason: 'its text would stay drawn in the fallback face');
  });
}
