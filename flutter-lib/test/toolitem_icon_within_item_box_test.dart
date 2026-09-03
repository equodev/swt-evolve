// A toolbar icon must be drawn whole and vertically centred inside its item.
//
// The icon assets are rendered by flutter_svg, which clips to the SVG's own viewBox: any ink
// an asset draws outside that box is cut off, and what survives is no longer centred on the
// box it was measured into. refresh.svg drew its arc around a centre 5 units above the middle
// of its 24x24 viewBox, so the top of the circle fell outside it -- the icon rendered sliced
// along its top edge and sitting high in the toolbar row.


import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

const double _barWidth = 60;
const double _barHeight = 60;

final _boundaryKey = GlobalKey();

VToolBar _bar(String filename) => VToolBar()
  ..id = 1
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 2
      ..style = SWT.PUSH
      ..enabled = true
      ..toolTipText = filename
      ..image = (VImage()..filename = filename)
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _barWidth.toInt()
    ..height = _barHeight.toInt());

/// Bounding box of everything painted inside [box] that is not the toolbar background.
Future<Rect> _inkBounds(WidgetTester tester, Rect box) async {
  final boundary =
      _boundaryKey.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  late ByteData data;
  await tester.runAsync(() async {
    final ui.Image image = await boundary.toImage();
    data = (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!;
    image.dispose();
  });
  final int w = boundary.size.width.round();
  // The item is padded around the icon, so its own top-left pixel is toolbar background.
  final background =
      data.getUint32((box.top.round() * w + box.left.round()) * 4);

  double? left, top, right, bottom;
  for (var y = box.top.round(); y < box.bottom.round(); y++) {
    for (var x = box.left.round(); x < box.right.round(); x++) {
      if (data.getUint32((y * w + x) * 4) == background) continue;
      left = (left == null || x < left) ? x.toDouble() : left;
      right = (right == null || x > right) ? x.toDouble() : right;
      top = (top == null || y < top) ? y.toDouble() : top;
      bottom = (bottom == null || y > bottom) ? y.toDouble() : bottom;
    }
  }
  expect(left, isNotNull, reason: 'the toolbar painted no icon at all');
  return Rect.fromLTRB(left!, top!, right! + 1, bottom! + 1);
}

Future<void> _pumpBar(WidgetTester tester, String filename) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: RepaintBoundary(
      key: _boundaryKey,
      child: SizedBox(
        width: _barWidth,
        height: _barHeight,
        child: ToolBarSwt(value: _bar(filename)),
      ),
    ),
  ));
  for (var i = 0; i < 8; i++) {
    await tester.pump(const Duration(milliseconds: 50));
  }
  while (tester.takeException() != null) {}
}

void main() {
  // Every asset that shares refresh.svg's circular-arrow construction: they all drew the arc
  // on a circle whose centre had drifted off the middle of the viewBox.
  const arcFamily = <String>[
    'refresh.png',
    'refresh_remote.png',
    'update_ans.png',
    'synch_co.png',
    'synch_synch.png',
    'sync_impl.png',
    'sync_over.png',
    'sync_broken.png',
    'synced_co.png',
    'unsynced_co.png',
    'conflicting_co.png',
    'filter_history.png',
  ];

  for (final filename in arcFamily) {
    testWidgets('$filename is painted whole and centred in its toolbar item',
        (tester) async {
      await _pumpBar(tester, filename);

      final item = tester.getRect(find.byType(ToolItemSwt));
      final ink = await _inkBounds(tester, item);

      expect(ink.top, greaterThan(item.top), reason: '$filename is clipped at the top');
      expect(ink.bottom, lessThan(item.bottom), reason: '$filename is clipped at the bottom');
      expect(ink.left, greaterThan(item.left), reason: '$filename is clipped on the left');
      expect(ink.right, lessThan(item.right), reason: '$filename is clipped on the right');
      expect((ink.center.dy - item.center.dy).abs(), lessThanOrEqualTo(1.5),
          reason: '$filename does not sit on the item\'s vertical centre');
    });
  }
}
