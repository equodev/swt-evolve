// QuietAnimatedContainer follows a change of what it paints by repainting instead of rebuilding;
// every step of it has to draw what Flutter's AnimatedContainer draws, whatever changes.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/utils/quiet_animations.dart';

const _duration = Duration(milliseconds: 150);

typedef _Look = ({
  Color fill,
  Color border,
  double borderWidth,
  EdgeInsets padding,
  Color? foreground,
});

const _Look _rest = (
  fill: Color(0xFFFFFFFF),
  border: Color(0xFF888888),
  borderWidth: 1.0,
  padding: EdgeInsets.all(4),
  foreground: null,
);

Widget _box(_Look look, {required bool flutters, Widget? child}) {
  final decoration = BoxDecoration(
    color: look.fill,
    borderRadius: BorderRadius.circular(4),
    border: Border.all(color: look.border, width: look.borderWidth),
  );
  final foreground = look.foreground == null ? null : BoxDecoration(color: look.foreground);
  const constraints = BoxConstraints(minWidth: 60, minHeight: 24);
  return flutters
      ? AnimatedContainer(
          duration: _duration,
          decoration: decoration,
          foregroundDecoration: foreground,
          padding: look.padding,
          constraints: constraints,
          alignment: Alignment.center,
          child: child,
        )
      : QuietAnimatedContainer(
          duration: _duration,
          decoration: decoration,
          foregroundDecoration: foreground,
          padding: look.padding,
          constraints: constraints,
          alignment: Alignment.center,
          child: child,
        );
}

class _Kept extends StatefulWidget {
  const _Kept({super.key});

  @override
  State<_Kept> createState() => _KeptState();
}

class _KeptState extends State<_Kept> {
  @override
  Widget build(BuildContext context) => const SizedBox(width: 20, height: 10, child: ColoredBox(color: Colors.teal));
}

void main() {
  final cases = <String, List<_Look>>{
    'fill': [
      (fill: const Color(0xFF3366CC), border: _rest.border, borderWidth: 1, padding: _rest.padding, foreground: null),
      _rest,
    ],
    'border colour': [
      (fill: _rest.fill, border: const Color(0xFFCC3333), borderWidth: 1, padding: _rest.padding, foreground: null),
      _rest,
    ],
    'foreground': [
      (fill: _rest.fill, border: _rest.border, borderWidth: 1, padding: _rest.padding, foreground: const Color(0x4400FF00)),
      (fill: _rest.fill, border: _rest.border, borderWidth: 1, padding: _rest.padding, foreground: const Color(0x8800FF00)),
    ],
    'border width': [
      (fill: _rest.fill, border: _rest.border, borderWidth: 3, padding: _rest.padding, foreground: null),
      _rest,
    ],
    'padding': [
      (fill: _rest.fill, border: _rest.border, borderWidth: 1, padding: const EdgeInsets.all(9), foreground: null),
      _rest,
    ],
    // Mid-way through a colour change the padding changes, then the colour alone again.
    'switching path': [
      (fill: const Color(0xFF3366CC), border: _rest.border, borderWidth: 1, padding: _rest.padding, foreground: null),
      (fill: const Color(0xFF3366CC), border: _rest.border, borderWidth: 1, padding: const EdgeInsets.all(9), foreground: null),
      (fill: const Color(0xFFCC9933), border: _rest.border, borderWidth: 1, padding: const EdgeInsets.all(9), foreground: null),
    ],
  };

  for (final MapEntry(key: name, value: looks) in cases.entries) {
    testWidgets('each step draws what AnimatedContainer draws: $name', (tester) async {
      final oursKey = GlobalKey();
      final fluttersKey = GlobalKey();
      final keptKey = GlobalKey();
      _Look look = _rest;
      late StateSetter set;
      Widget shot(GlobalKey key, bool flutters) => RepaintBoundary(
            key: key,
            child: ColoredBox(
              color: Colors.white,
              child: SizedBox(
                width: 100,
                height: 50,
                child: Center(
                  child: _box(look, flutters: flutters, child: _Kept(key: flutters ? null : keptKey)),
                ),
              ),
            ),
          );
      await tester.pumpWidget(MaterialApp(
        home: Align(
          alignment: Alignment.topLeft,
          child: StatefulBuilder(builder: (context, setState) {
            set = setState;
            return Column(mainAxisSize: MainAxisSize.min, children: [shot(oursKey, false), shot(fluttersKey, true)]);
          }),
        ),
      ));
      final kept = keptKey.currentState;
      Future<Uint8List> pixels(GlobalKey key) async {
        final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
        return (await tester.runAsync(() async {
          final image = await boundary.toImage();
          return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
        }))!;
      }

      for (final next in looks) {
        set(() => look = next);
        for (var step = 0; step < 5; step++) {
          await tester.pump(const Duration(milliseconds: 16));
          expect(await pixels(oursKey), orderedEquals(await pixels(fluttersKey)), reason: 'step $step');
        }
      }
      await tester.pumpAndSettle();
      expect(await pixels(oursKey), orderedEquals(await pixels(fluttersKey)));
      expect(keptKey.currentState, same(kept));
    });
  }

  testWidgets('a change of colour alone is followed without rebuilding', (tester) async {
    _Look look = _rest;
    late StateSetter set;
    await tester.pumpWidget(Directionality(
      textDirection: TextDirection.ltr,
      child: StatefulBuilder(builder: (context, setState) {
        set = setState;
        return Center(child: _box(look, flutters: false));
      }),
    ));
    set(() => look = (fill: const Color(0xFF3366CC), border: _rest.border, borderWidth: 1, padding: _rest.padding, foreground: null));
    await tester.pump();

    var rebuilds = 0;
    debugOnRebuildDirtyWidget = (element, _) {
      if (element.widget is QuietAnimatedContainer) rebuilds++;
    };
    addTearDown(() => debugOnRebuildDirtyWidget = null);
    await tester.pump(const Duration(milliseconds: 16));
    await tester.pump(const Duration(milliseconds: 16));
    await tester.pumpAndSettle();
    expect(rebuilds, 0);
  });
}
