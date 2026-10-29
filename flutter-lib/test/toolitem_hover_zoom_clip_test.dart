// The hover zoom grows an icon past its item's box, and every control is clipped to its own bounds.
// A grown icon must still be drawn whole wherever its ToolBar sits -- including in a Composite sized
// to the bar, whose own clip leaves no room past the bar's edge.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
// ignore: unused_import
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'support/visible_region.dart';

const List<int> _pngBytes = <int>[
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

const _pageWidth = 420;
const _pageHeight = 320;
const _itemCount = 3;

/// A 16 px image item as Sizes.computeSize(DartToolItem) measures it.
const _itemWidth = 23;
const _itemHeight = 22;
const _barWidth = _itemCount * _itemWidth;

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VToolBar _toolBar(int id, {int x = 0, int itemCount = _itemCount}) => VToolBar()
  ..id = id
  ..style = SWT.FLAT | SWT.RIGHT
  ..enabled = true
  ..visible = true
  ..bounds = _rect(x, 0, itemCount * _itemWidth, _itemHeight)
  ..items = [
    for (var i = 1; i <= itemCount; i++)
      VToolItem()
        ..id = id + i
        ..style = SWT.PUSH
        ..enabled = true
        ..width = _itemWidth
        ..image = (VImage()
          ..imageData = (VImageData()
            ..width = 16
            ..height = 16
            ..data = _pngBytes)),
  ];

/// A bordered Composite whose RowLayout puts a ToolBar flush against the top-left corner
/// (`marginTop = marginLeft = 0`, `spacing = 0`) and a one-item ToolBar right after it, leaving
/// only the default 3 px on the right and bottom.
VComposite _borderedToolBarRow() => VComposite()
  ..id = 2000
  ..style = SWT.BORDER
  ..enabled = true
  ..visible = true
  ..bounds = _rect(20, 20, _barWidth + _itemWidth + 3 + 2, _itemHeight + 3 + 2)
  ..children = [_toolBar(2001), _toolBar(2101, x: _barWidth, itemCount: 1)];

/// A folder whose topRight control is a Composite holding the ToolBar, sized to exactly the bar.
VCTabFolder _folderWithTopRightToolBar() => VCTabFolder()
  ..id = 3000
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..selection = 0
  ..bounds = _rect(10, 10, _pageWidth - 20, _pageHeight - 20)
  ..topRightAlignment = SWT.RIGHT | SWT.WRAP
  ..topRight = (VComposite()
    ..swt = 'Composite'
    ..id = 3100
    ..visible = true
    ..bounds = _rect(_pageWidth - 20 - _barWidth - 4, 2, _barWidth, _itemHeight)
    ..children = [_toolBar(3101)])
  ..items = [
    VCTabItem()
      ..id = 3200
      ..text = 'Search'
      ..control = (VLabel()
        ..id = 3300
        ..visible = true
        ..text = 'Page body'
        ..bounds = _rect(3, 32, _pageWidth - 26, 250)),
  ];

VComposite _page(VControl child) => VComposite()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _rect(0, 0, _pageWidth, _pageHeight)
  ..children = [child];

final _icons = find.byType(Image);

void _expectEveryItemDrawnWholeWhileHovered(String shape, int iconCount, VControl Function() build) {
  for (var hoveredIndex = 0; hoveredIndex < iconCount; hoveredIndex++) {
    testWidgets('$shape: item $hoveredIndex is drawn whole while hovered', (tester) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(
            width: _pageWidth.toDouble(),
            height: _pageHeight.toDouble(),
            child: CompositeSwt<VComposite>(value: _page(build())),
          ),
        ),
      ));
      await tester.pumpAndSettle();
      while (tester.takeException() != null) {}
      expect(_icons, findsNWidgets(iconCount), reason: 'every item must render its image');

      final target = _icons.at(hoveredIndex);
      final atRest = tester.getRect(target);
      final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
      await gesture.addPointer(location: const Offset(400, 300));
      addTearDown(gesture.removePointer);
      await gesture.moveTo(tester.getCenter(target));
      await tester.pumpAndSettle();
      final hovered = tester.getRect(target);
      expect(hovered.height, greaterThan(atRest.height), reason: 'the zoom did not apply');
      expect(hovered.center.dx, closeTo(atRest.center.dx, 0.01),
          reason: 'the icon must grow around its rest spot, not drift');
      expect(hovered.center.dy, closeTo(atRest.center.dy, 0.01),
          reason: 'the icon must grow around its rest spot, not drift');

      final visible = visibleRegion(tester.renderObject<RenderBox>(target));
      final cut = <String, double>{
        'left': visible.left - hovered.left,
        'right': hovered.right - visible.right,
        'top': visible.top - hovered.top,
        'bottom': hovered.bottom - visible.bottom,
      }..removeWhere((_, px) => px <= 0.01);
      expect(cut, isEmpty, reason: 'the zoomed icon is cut off (visible=$visible hovered=$hovered)');
    });
  }
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a grown icon is still cut where a scroll viewport ends', (tester) async {
    const viewportTop = 100.0;
    const viewportHeight = 40.0;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: Padding(
          padding: const EdgeInsets.only(top: viewportTop),
          child: SizedBox(
            width: _pageWidth.toDouble(),
            height: viewportHeight,
            child: SingleChildScrollView(
              // Scrolls the bar's top few pixels out of the viewport.
              controller: ScrollController(initialScrollOffset: 26),
              child: SizedBox(
                width: _pageWidth.toDouble(),
                height: _pageHeight.toDouble(),
                child: CompositeSwt<VComposite>(value: _page(_borderedToolBarRow())),
              ),
            ),
          ),
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    final target = _icons.first;
    final atRest = tester.getRect(target);
    expect(atRest.top, lessThan(viewportTop), reason: 'the icon must start above the viewport');
    final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await gesture.addPointer(location: const Offset(400, 300));
    addTearDown(gesture.removePointer);
    await gesture.moveTo(Offset(atRest.center.dx, (viewportTop + atRest.bottom) / 2));
    await tester.pumpAndSettle();

    final hovered = tester.getRect(target);
    expect(hovered.height, greaterThan(atRest.height), reason: 'the zoom did not apply');
    final visible = visibleRegion(tester.renderObject<RenderBox>(target));
    expect(visible.top, greaterThanOrEqualTo(viewportTop - 0.01),
        reason: 'the grown icon paints over what the viewport scrolled away (visible=$visible)');
  });

  _expectEveryItemDrawnWholeWhileHovered(
      'ToolBars in a bordered Composite', _itemCount + 1, _borderedToolBarRow);

  for (final autoHide in [true, false]) {
    group('ctabfolder_topright_auto_hide=$autoHide', () {
      setUp(() => setConfigFlags(ConfigFlags()..ctabfolder_topright_auto_hide = autoHide));

      _expectEveryItemDrawnWholeWhileHovered(
          'ToolBar in a topRight Composite', _itemCount, _folderWithTopRightToolBar);
    });
  }
}
