// SlidingBox and AnimatedStyledText animate by repainting instead of rebuilding; every step of
// either has to draw what Flutter's AnimatedPositioned and AnimatedDefaultTextStyle draw.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart' show FontLoader, rootBundle;
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/utils/quiet_animations.dart';

const _duration = Duration(milliseconds: 200);
const _selected = TextStyle(fontFamily: 'Inter', fontSize: 12, color: Color(0xFF1A1A1A));
const _unselected = TextStyle(fontFamily: 'Inter', fontSize: 12, color: Color(0xFF8A8A8A));

/// A two-part segmented control whose highlight slides under the selected part.
Widget _segment({required bool first, required bool flutters}) {
  final width = first ? 60.0 : 44.4;
  final decoration = BoxDecoration(color: const Color(0xFFCCDDEE), borderRadius: BorderRadius.circular(5));
  Widget label(String text, bool selected) {
    final style = selected ? _selected : _unselected;
    return SizedBox(
      width: text == 'Keyword' ? 60 : 44,
      child: Center(
        child: flutters
            ? AnimatedDefaultTextStyle(
                style: style,
                duration: _duration,
                child: Text(text, textAlign: TextAlign.center),
              )
            : AnimatedStyledText(text, style: style, duration: _duration, textAlign: TextAlign.center),
      ),
    );
  }

  return SizedBox(
    width: 120,
    height: 24,
    child: Stack(
      children: [
        if (flutters)
          AnimatedPositioned(
            duration: _duration,
            curve: Curves.easeInOut,
            left: first ? 0.0 : 60.3,
            top: 0,
            bottom: 0,
            child: Container(width: width, decoration: decoration),
          )
        else
          Positioned.fill(
            child: SlidingBox(
              duration: _duration,
              curve: Curves.easeInOut,
              left: first ? 0.0 : 60.3,
              width: width,
              decoration: decoration,
            ),
          ),
        Row(children: [label('Keyword', first), label('Debug', !first)]),
      ],
    ),
  );
}

void main() {
  setUpAll(() async {
    final inter = FontLoader('Inter')..addFont(rootBundle.load('assets/fonts/Inter_18pt-Medium.ttf'));
    await inter.load();
  });

  testWidgets('each step of a sliding, recolouring segment draws what Flutter draws', (tester) async {
    final oursKey = GlobalKey();
    final fluttersKey = GlobalKey();
    var first = true;
    late StateSetter set;
    Widget shot(GlobalKey key, bool flutters) => RepaintBoundary(
          key: key,
          child: ColoredBox(color: Colors.white, child: _segment(first: first, flutters: flutters)),
        );
    await tester.pumpWidget(MaterialApp(
      home: Align(
        alignment: Alignment.topLeft,
        child: StatefulBuilder(builder: (context, setState) {
          set = setState;
          return Column(
            mainAxisSize: MainAxisSize.min,
            children: [shot(oursKey, false), shot(fluttersKey, true)],
          );
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

    var steps = 0;
    for (final interruptAfter in [20, 7, 3]) {
      set(() => first = !first);
      for (var step = 0; step < interruptAfter; step++) {
        await tester.pump(const Duration(milliseconds: 16));
        expect(await pixels(oursKey), orderedEquals(await pixels(fluttersKey)), reason: 'step $step');
        steps++;
      }
    }
    expect(steps, 30);
  });
}
