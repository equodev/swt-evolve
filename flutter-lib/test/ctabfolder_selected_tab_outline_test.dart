// Native SWT outlines a bordered CTabFolder as one shape: up both folder sides past the strip, over
// the selected tab, and along the strip bottom on either side of it, open towards the page.
// Each check compares against the page's own side-frame ink, so a theme colour moves both halves.
// Inside the outline, native fills a 2px band with the folder's ground, or with the selection
// background while the folder is highlighted.
// The outline is opt-in (`swt.use_swt_border`); left off, the folder keeps the theme's flat frame.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/quiet_animations.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const double _folderWidth = 360;
const double _folderHeight = 160;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VCTabFolder _folder({int tabPosition = SWT.TOP, bool highlighted = false}) {
  final names = ['View A', 'View B', 'View C'];
  return VCTabFolder()
    ..highlightEnabled = highlighted
    ..highlight = highlighted
    ..selectionBackground = VColor.fromArgb(0xFFC8DCF0)
    ..id = 10
    ..style = SWT.BORDER | SWT.CLOSE
    ..enabled = true
    ..visible = true
    ..tabHeight = 24
    ..tabPosition = tabPosition
    ..selection = 0
    ..items = [
      for (var i = 0; i < names.length; i++)
        VCTabItem()
          ..id = 11 + i
          ..text = names[i],
    ]
    ..children = [
      VLabel()
        ..id = 20
        ..style = SWT.NONE
        ..enabled = true
        ..visible = true
        ..text = '${names[0]} content'
        ..bounds = _rect(0, 0, 300, 100),
    ];
}

class _Shot {
  final ByteData bytes;
  final int width;
  _Shot(this.bytes, this.width);

  Color at(double x, double y) {
    final o = (y.floor() * width + x.floor()) * 4;
    return Color.fromARGB(
      bytes.getUint8(o + 3),
      bytes.getUint8(o),
      bytes.getUint8(o + 1),
      bytes.getUint8(o + 2),
    );
  }
}

final _boundary = GlobalKey();

Future<_Shot> _render(WidgetTester tester, VCTabFolder folder) async {
  tester.view.devicePixelRatio = 1;
  addTearDown(tester.view.resetDevicePixelRatio);
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: RepaintBoundary(
          key: _boundary,
          // An opaque ground, so a translucent frame ink composites as it does in a window.
          child: ColoredBox(
            color: Colors.white,
            child: SizedBox(
              width: _folderWidth,
              height: _folderHeight,
              child: CTabFolderSwt<VCTabFolder>(value: folder),
            ),
          ),
        ),
      ),
    ),
  );
  await tester.pumpAndSettle();
  while (tester.takeException() != null) {}
  late _Shot shot;
  await tester.runAsync(() async {
    final boundary = tester.renderObject(find.byKey(_boundary)) as RenderRepaintBoundary;
    final image = await boundary.toImage();
    shot = _Shot((await image.toByteData(format: ui.ImageByteFormat.rawRgba))!, image.width);
  });
  return shot;
}

Rect _inFolder(WidgetTester tester, Finder finder) {
  final folder = tester.getRect(find.byType(CTabFolderSwt<VCTabFolder>));
  return tester.getRect(finder).translate(-folder.left, -folder.top);
}

Matcher _sameInk(Color expected) => predicate<Color>(
  (c) =>
      (c.red - expected.red).abs() <= 4 &&
      (c.green - expected.green).abs() <= 4 &&
      (c.blue - expected.blue).abs() <= 4,
  'the frame ink $expected',
);

Rect _selectedTab(WidgetTester tester) => _inFolder(
  tester,
  find.ancestor(of: find.text('View A'), matching: find.byType(QuietAnimatedContainer)).first,
);

void main() {
  testWidgets('with use_swt_border off, a bordered folder draws no outline', (tester) async {
    resetConfigFlags();
    final shot = await _render(tester, _folder());
    final page = _inFolder(tester, find.byType(IndexedStack).first);
    final tab = _selectedTab(tester);
    final frameInk = shot.at(0, page.center.dy);

    expect(
      shot.at(0, tab.center.dy),
      isNot(_sameInk(frameInk)),
      reason: 'the frame stops at the tab strip',
    );
    expect(
      shot.at(tab.center.dx, tab.top),
      isNot(_sameInk(frameInk)),
      reason: 'no outline runs over the selected tab',
    );
  });

  group('with use_swt_border on', () {
    setUp(() => setConfigFlags(ConfigFlags()..use_swt_border = true));
    tearDown(resetConfigFlags);

    testWidgets('the selected tab is outlined and joined to the folder frame', (tester) async {
      final shot = await _render(tester, _folder());
      final page = _inFolder(tester, find.byType(IndexedStack).first);
      final tab = _selectedTab(tester);
      final frameInk = shot.at(0, page.center.dy);
      final stripBottom = page.top - 1;

      expect(
        shot.at(0, tab.center.dy),
        _sameInk(frameInk),
        reason: 'the frame climbs the folder\'s side past the tab strip',
      );
      expect(
        shot.at(tab.center.dx, tab.top),
        _sameInk(frameInk),
        reason: 'the outline runs over the selected tab\'s top',
      );
      expect(
        shot.at(tab.right + 4, stripBottom),
        _sameInk(frameInk),
        reason: 'the outline carries on along the strip\'s bottom past the selected tab',
      );
      expect(
        shot.at(tab.center.dx, stripBottom),
        isNot(_sameInk(frameInk)),
        reason: 'the selected tab is open towards its page',
      );
    });

    testWidgets('with the tabs at the bottom, the outline is mirrored', (tester) async {
      final shot = await _render(tester, _folder(tabPosition: SWT.BOTTOM));
      final page = _inFolder(tester, find.byType(IndexedStack).first);
      final tab = _selectedTab(tester);
      final frameInk = shot.at(0, page.center.dy);
      final stripTop = page.bottom;

      expect(
        shot.at(0, tab.center.dy),
        _sameInk(frameInk),
        reason: 'the frame climbs the folder\'s side past the tab strip',
      );
      expect(
        shot.at(page.center.dx, page.top - 1),
        isNot(_sameInk(frameInk)),
        reason: 'the page edge away from the strip is the band inside the outline',
      );
      expect(
        shot.at(page.center.dx, 0),
        _sameInk(frameInk),
        reason: 'the outline closes across the page edge away from the strip',
      );
      expect(
        shot.at(tab.right + 4, stripTop),
        _sameInk(frameInk),
        reason: 'the outline carries on along the strip\'s top past the selected tab',
      );
      expect(
        shot.at(tab.center.dx, stripTop),
        isNot(_sameInk(frameInk)),
        reason: 'the selected tab is open towards its page',
      );
    });

    testWidgets('the band inside the outline is the folder\'s ground', (tester) async {
      final shot = await _render(tester, _folder());
      final page = _inFolder(tester, find.byType(IndexedStack).first);
      final stripGround = shot.at(_folderWidth - 8, page.top / 2);

      expect(shot.at(page.left - 1, page.center.dy), _sameInk(stripGround));
      expect(shot.at(page.right, page.center.dy), _sameInk(stripGround));
      expect(shot.at(page.center.dx, page.bottom), _sameInk(stripGround));
    });

    testWidgets('a highlighted folder fills the band with the selection background', (
      tester,
    ) async {
      final shot = await _render(tester, _folder(highlighted: true));
      final page = _inFolder(tester, find.byType(IndexedStack).first);
      final tab = _selectedTab(tester);
      final selectionGround = shot.at(tab.left + 3, tab.center.dy);
      final stripGround = shot.at(_folderWidth - 8, page.top / 2);

      expect(shot.at(page.left - 1, page.center.dy), _sameInk(selectionGround));
      expect(shot.at(page.center.dx, page.bottom), _sameInk(selectionGround));
      expect(
        shot.at(page.center.dx, page.center.dy),
        _sameInk(stripGround),
        reason: 'only the band takes the selection background; the page keeps the folder\'s ground',
      );
    });
  });
}
