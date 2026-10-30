// ColorCachedText keeps shaped text across colour changes; it has to draw what Text draws. This
// paints both, pixel for pixel, in each configuration a control label uses, and across colour flips,
// and CachedIcon beside Icon.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart' show FontLoader, SystemChannels, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/utils/cached_text.dart';

Future<void> _loadFonts() async {
  final inter = FontLoader('Inter')..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
  await inter.load();
  final icons = FontLoader('MaterialIcons')..addFont(rootBundle.load('fonts/MaterialIcons-Regular.otf'));
  await icons.load();
}

typedef _Label = Widget Function(String text, TextStyle style, TextOverflow overflow, int? maxLines);

Widget _text(String text, TextStyle style, TextOverflow overflow, int? maxLines) =>
    Text(text, style: style, overflow: overflow, maxLines: maxLines);

Widget _cached(String text, TextStyle style, TextOverflow overflow, int? maxLines) =>
    ColorCachedText(text, style: style, overflow: overflow, maxLines: maxLines);

Future<Uint8List> _paint(WidgetTester tester, Widget child) async {
  final key = GlobalKey();
  await tester.pumpWidget(MaterialApp(
    home: Align(
      alignment: Alignment.topLeft,
      child: RepaintBoundary(
        key: key,
        child: ColoredBox(color: Colors.white, child: SizedBox(width: 120, height: 40, child: child)),
      ),
    ),
  ));
  final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  final bytes = await tester.runAsync(() async {
    final ui.Image image = await boundary.toImage();
    final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
    return data!.buffer.asUint8List();
  });
  return bytes!;
}

Widget _row(_Label label, String text, TextStyle style, TextOverflow overflow, int? maxLines) => Row(
      children: [
        const SizedBox(width: 6),
        Flexible(child: label(text, style, overflow, maxLines)),
      ],
    );

void main() {
  setUpAll(_loadFonts);

  const enabled = TextStyle(fontFamily: 'Inter', fontSize: 12, color: Color(0xFF202020));
  const disabled = TextStyle(fontFamily: 'Inter', fontSize: 12, color: Color(0xFFA0A0A0));
  const bold = TextStyle(fontFamily: 'Inter', fontSize: 13, fontWeight: FontWeight.bold, color: Colors.blue);
  const long = 'A label far too long for the room it is given here';

  final cases = <String, (String, TextStyle, TextOverflow, int?)>{
    'single line': ('Apply', enabled, TextOverflow.ellipsis, 1),
    'disabled': ('Apply', disabled, TextOverflow.ellipsis, 1),
    'bold': ('Apply', bold, TextOverflow.ellipsis, 1),
    'ellipsized': (long, enabled, TextOverflow.ellipsis, 1),
    'wrapped': (long, enabled, TextOverflow.visible, null),
    'empty-ish': (' ', enabled, TextOverflow.ellipsis, 1),
  };

  for (final MapEntry(key: name, value: (text, style, overflow, maxLines)) in cases.entries) {
    testWidgets('a cached label draws what Text draws: $name', (tester) async {
      final expected = await _paint(tester, _row(_text, text, style, overflow, maxLines));
      final actual = await _paint(tester, _row(_cached, text, style, overflow, maxLines));
      expect(actual, orderedEquals(expected));
    });
  }

  testWidgets('a label flipped between colours draws each colour as Text does', (tester) async {
    final cachedKey = GlobalKey();
    final textKey = GlobalKey();
    var style = enabled;
    late StateSetter set;
    Widget shot(GlobalKey key, _Label label) => RepaintBoundary(
          key: key,
          child: ColoredBox(
            color: Colors.white,
            child: SizedBox(width: 120, height: 40, child: _row(label, 'Apply', style, TextOverflow.ellipsis, 1)),
          ),
        );
    await tester.pumpWidget(MaterialApp(
      home: Align(
        alignment: Alignment.topLeft,
        child: StatefulBuilder(builder: (context, setState) {
          set = setState;
          return Column(mainAxisSize: MainAxisSize.min, children: [shot(cachedKey, _cached), shot(textKey, _text)]);
        }),
      ),
    ));
    Future<Uint8List> pixels(GlobalKey key) async {
      final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
      return (await tester.runAsync(() async {
        final image = await boundary.toImage();
        return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
      }))!;
    }

    for (final next in [disabled, enabled, disabled, enabled, bold, enabled]) {
      set(() => style = next);
      await tester.pump();
      expect(await pixels(cachedKey), orderedEquals(await pixels(textKey)));
    }
  });

  testWidgets('flipping between two colours keeps both shaped texts', (tester) async {
    var style = enabled;
    late StateSetter set;
    await tester.pumpWidget(MaterialApp(
      home: StatefulBuilder(builder: (context, setState) {
        set = setState;
        return Align(alignment: Alignment.topLeft, child: ColorCachedText('Apply', style: style, maxLines: 1));
      }),
    ));
    RenderCachedText render() => tester.renderObject<RenderCachedText>(
        find.descendant(of: find.byType(ColorCachedText), matching: find.byWidgetPredicate((_) => true)).last);
    for (final next in [disabled, enabled, disabled]) {
      set(() => style = next);
      await tester.pump();
    }
    expect(render().keptPainters, 2);
  });

  testWidgets('a change of the system fonts lays a cached label out again', (tester) async {
    await tester.pumpWidget(MaterialApp(
      home: Align(alignment: Alignment.topLeft, child: ColorCachedText('Apply', style: enabled, maxLines: 1)),
    ));
    final render = tester.renderObject<RenderCachedText>(
        find.descendant(of: find.byType(ColorCachedText), matching: find.byWidgetPredicate((_) => true)).last);
    expect(render.debugNeedsLayout, isFalse);

    await tester.binding.defaultBinaryMessenger.handlePlatformMessage(
      SystemChannels.system.name,
      SystemChannels.system.codec.encodeMessage({'type': 'fontsChange'}),
      (_) {},
    );

    expect(render.debugNeedsLayout, isTrue);
  });

  testWidgets('labels of the same text and style share one shaped text, each at its own width',
      (tester) async {
    final shared = RenderCachedText.sharedPainters;
    final cachedKey = GlobalKey();
    final textKey = GlobalKey();
    Widget column(_Label label) => Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            for (final width in [120.0, 60.0, 120.0])
              SizedBox(width: width, child: label(long, enabled, TextOverflow.visible, null)),
          ],
        );
    await tester.pumpWidget(MaterialApp(
      home: Align(
        alignment: Alignment.topLeft,
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            RepaintBoundary(key: cachedKey, child: ColoredBox(color: Colors.white, child: column(_cached))),
            RepaintBoundary(key: textKey, child: ColoredBox(color: Colors.white, child: column(_text))),
          ],
        ),
      ),
    ));
    expect(RenderCachedText.sharedPainters, shared + 1);

    Future<Uint8List> pixels(GlobalKey key) async {
      final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
      return (await tester.runAsync(() async {
        final image = await boundary.toImage();
        return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
      }))!;
    }

    expect(await pixels(cachedKey), orderedEquals(await pixels(textKey)));

    await tester.pumpWidget(const SizedBox());
    expect(RenderCachedText.sharedPainters, shared);
  });

  testWidgets('a cached label lends its text to the node it sits in, as Text does', (tester) async {
    final semantics = tester.ensureSemantics();
    Widget node(String id, Widget label) => Semantics(
          identifier: id,
          container: true,
          child: Row(children: [const SizedBox(width: 4), label]),
        );
    await tester.pumpWidget(MaterialApp(
      home: Column(children: [
        node('text', const Text('Apply', style: enabled)),
        node('cached', const ColorCachedText('Apply', style: enabled)),
      ]),
    ));
    SemanticsNode of(String id) => tester.getSemantics(find.bySemanticsIdentifier(id));
    expect(of('cached').label, 'Apply');
    expect(of('cached').label, of('text').label);
    expect(find.bySemanticsLabel('Apply'), findsNWidgets(2));
    semantics.dispose();
  });

  final iconCases = <String, (IconData, double, Color?, TextDirection, IconThemeData)>{
    'tick': (Icons.check, 14, const Color(0xFFFFFFFF), TextDirection.ltr, const IconThemeData()),
    'themed colour': (Icons.check, 18, null, TextDirection.ltr, const IconThemeData(color: Colors.teal)),
    'translucent theme': (Icons.check, 16, Colors.black, TextDirection.ltr, const IconThemeData(opacity: 0.4)),
    'mirrored': (Icons.arrow_back, 16, Colors.black, TextDirection.rtl, const IconThemeData()),
  };
  for (final MapEntry(key: name, value: (icon, size, color, direction, theme)) in iconCases.entries) {
    testWidgets('a cached icon draws what Icon draws: $name', (tester) async {
      Widget shot(Icon shown) => Directionality(
            textDirection: direction,
            child: IconTheme.merge(
              data: theme,
              child: ColoredBox(color: Colors.blueGrey, child: Center(child: shown)),
            ),
          );
      final expected = await _paint(tester, shot(Icon(icon, size: size, color: color)));
      final actual = await _paint(tester, shot(CachedIcon(icon, size: size, color: color)));
      // The glyph has to be drawn at all, or both would only show the background.
      expect(expected.toSet().length, greaterThan(4));
      expect(actual, orderedEquals(expected));
    });
  }
}
