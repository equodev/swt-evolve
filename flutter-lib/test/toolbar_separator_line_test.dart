// A SWT.SEPARATOR ToolItem draws a visible line between its neighbours, across the bar but short of
// its edges, when icons are drawn at their own size. Under `bigger_icon_size` it stays a plain gap.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/theme/theme_extensions/toolitem_theme_extension.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

final _shot = GlobalKey();

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VToolItem _push(int id, String text) => VToolItem()
  ..id = id
  ..style = SWT.PUSH
  ..enabled = true
  ..text = text;

/// With [squeezed], a box too small for the items at their own size, which scales them down to fit.
VToolBar _bar({bool vertical = false, bool squeezed = false}) => VToolBar()
  ..id = 1
  ..style = (vertical ? SWT.VERTICAL : SWT.HORIZONTAL) | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..bounds = vertical
      ? _rect(0, 0, 100, squeezed ? 41 : 200)
      : _rect(0, 0, squeezed ? 61 : 200, 30)
  ..items = [
    _push(2, 'One'),
    VToolItem()
      ..id = 3
      ..style = SWT.SEPARATOR
      ..enabled = true,
    _push(4, 'Two'),
  ];

Future<void> _pumpBar(WidgetTester tester, VToolBar bar) async {
  // One device pixel per logical pixel, as the shot is taken.
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.resetDevicePixelRatio);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: RepaintBoundary(
        key: _shot,
        child: SizedBox(
          width: bar.bounds!.width.toDouble(),
          height: bar.bounds!.height.toDouble(),
          child: ToolBarSwt<VToolBar>(value: bar),
        ),
      ),
    ),
  ));
  await tester.pumpAndSettle();
}

/// The bar's pixels, and the gap between its two items: the rect the separator owns.
Future<(ByteData, int, Rect)> _shotAndGap(WidgetTester tester, {required bool vertical}) async {
  final items = find.byType(ToolItemSwt<VToolItem>).evaluate().toList();
  expect(items, hasLength(2));
  final origin = tester.getTopLeft(find.byKey(_shot));
  final first = tester.getRect(find.byWidget(items[0].widget)).shift(-origin);
  final second = tester.getRect(find.byWidget(items[1].widget)).shift(-origin);
  final gap = vertical
      ? Rect.fromLTRB(first.left, first.bottom, first.right, second.top)
      : Rect.fromLTRB(first.right, first.top, second.left, first.bottom);
  final boundary = tester.renderObject<RenderRepaintBoundary>(find.byKey(_shot));
  final image = (await tester.runAsync(() => boundary.toImage()))!;
  final bytes = (await tester.runAsync(() => image.toByteData(format: ui.ImageByteFormat.rawRgba)))!;
  return (bytes, image.width, gap);
}

/// SWT.COLOR_WIDGET_NORMAL_SHADOW as `Display.getSystemColor` answers it.
const _widgetNormalShadow = Color(0xFF808080);

int _pixel(ByteData bytes, int width, double x, double y) =>
    bytes.getUint32((y.floor() * width + x.floor()) * 4);

/// An opaque pixel read by [_pixel] (RGBA) as a [Color].
Color _color(int rgba) => Color.fromARGB(255, rgba >> 24 & 0xFF, rgba >> 16 & 0xFF, rgba >> 8 & 0xFF);

/// The largest channel difference between two colours, in 0..255.
int _distance(Color a, Color b) => [
      ((a.r - b.r) * 255).round().abs(),
      ((a.g - b.g) * 255).round().abs(),
      ((a.b - b.b) * 255).round().abs(),
    ].reduce((x, y) => x > y ? x : y);

void main() {
  tearDown(resetConfigFlags);

  for (final vertical in [false, true]) {
    final orientation = vertical ? 'vertical' : 'horizontal';

    testWidgets('without bigger_icon_size a $orientation bar draws a separator line short of its edges',
        (tester) async {
      setConfigFlags(ConfigFlags()..bigger_icon_size = false);
      await _pumpBar(tester, _bar(vertical: vertical));
      final (bytes, width, gap) = await _shotAndGap(tester, vertical: vertical);

      expect(vertical ? gap.height : gap.width, greaterThan(4),
          reason: 'the separator must keep its neighbours apart');
      final c = gap.center;
      final ground = vertical ? _pixel(bytes, width, c.dx, gap.top + 1) : _pixel(bytes, width, gap.left + 1, c.dy);
      Offset across(double f) => vertical
          ? Offset(gap.left + f * gap.width, c.dy)
          : Offset(c.dx, gap.top + f * gap.height);
      for (final p in [across(0.25), across(0.5), across(0.75)]) {
        expect(_pixel(bytes, width, p.dx, p.dy), isNot(ground),
            reason: 'the separator must paint a line across the bar at $p, not an empty gap');
      }
      // One pixel in from the bar's edges, the line leaves the ground showing.
      final edges = vertical
          ? [Offset(gap.left + 1, c.dy), Offset(gap.right - 1, c.dy)]
          : [Offset(c.dx, gap.top + 1), Offset(c.dx, gap.bottom - 1)];
      for (final p in edges) {
        expect(_pixel(bytes, width, p.dx, p.dy), ground,
            reason: 'the separator line must stop short of the bar\'s edge at $p');
      }
    });

    for (final (swtColors, squeezed) in [(false, false), (true, false), (false, true)]) {
      final source = swtColors ? 'SWT.COLOR_WIDGET_NORMAL_SHADOW under use_swt_colors' : "the theme's separator colour";
      final bar = squeezed ? ' scaled down into a smaller bar' : '';
      testWidgets('a $orientation separator line$bar is drawn in $source', (tester) async {
        setConfigFlags(ConfigFlags()
          ..bigger_icon_size = false
          ..use_swt_colors = swtColors);
        await _pumpBar(tester, _bar(vertical: vertical, squeezed: squeezed));
        final (bytes, width, gap) = await _shotAndGap(tester, vertical: vertical);

        final c = gap.center;
        final ground = _color(_pixel(bytes, width, vertical ? c.dx : gap.left + 1, vertical ? gap.top + 1 : c.dy));
        final itemTheme = Theme.of(tester.element(find.byType(ToolBarSwt<VToolBar>)))
            .extension<ToolItemThemeExtension>()!;
        final expected =
            swtColors ? _widgetNormalShadow : Color.alphaBlend(itemTheme.separatorColor, ground);
        expect(expected, isNot(ground), reason: 'the line must be visible on the bar');

        // The line's core: the pixel across the gap that stands out most from the ground.
        Color core = ground;
        final steps = (vertical ? gap.height : gap.width).floor();
        for (var i = 0; i < steps; i++) {
          final p = _color(vertical
              ? _pixel(bytes, width, c.dx, gap.top + i)
              : _pixel(bytes, width, gap.left + i, c.dy));
          if (_distance(p, ground) > _distance(core, ground)) core = p;
        }
        expect(_distance(core, expected), lessThanOrEqualTo(3),
            reason: 'line core $core, expected $expected');
      });
    }

    testWidgets('with bigger_icon_size a $orientation bar keeps the separator a plain gap',
        (tester) async {
      setConfigFlags(ConfigFlags()..bigger_icon_size = true);
      await _pumpBar(tester, _bar(vertical: vertical));
      final (bytes, width, gap) = await _shotAndGap(tester, vertical: vertical);

      final c = gap.center;
      final ground = _pixel(bytes, width, gap.left + 1, c.dy);
      expect(_pixel(bytes, width, c.dx, c.dy), ground,
          reason: 'with enlarged icons the groups are split by a gap with no line');
    });
  }
}
