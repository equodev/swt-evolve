// A theme can declare the colour an owner-drawn Table row's own fill takes: the colour the
// application fills most of the Table's rows with is painted in it, every other fill as given.
// A theme that declares none leaves the fills as the application paints them.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/owner_draw_overlay.dart';
import 'package:swtflutter/src/impl/utils/widget_utils.dart' show contrastRatio;
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/table_theme_extension.dart';
import 'support/channels.dart' as channels;

const _themeRow = Color(0xFF1A2A1F);
const _white = Color(0xFFFFFFFF);
const _black = Color(0xFF000000);
const _selectionTint = Color(0xFF73B4E6);
const _hover = Color(0xFFE5E5E5);

VColor _v(Color c) => VColor()
  ..red = (c.r * 255).round()
  ..green = (c.g * 255).round()
  ..blue = (c.b * 255).round()
  ..alpha = 255;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  var nextId = 1977001;

  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
    }
  }

  /// One row's GC of a Table whose rows share [fill], drawing what [draw] does with the cell's
  /// [background] and black text.
  Future<List<Shape>> paintRow(
      OwnerDrawnRowFill? fill, Color background, void Function(GCDrawer) draw) async {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..foreground = _v(_black)
      ..background = _v(background);
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {})
      ..rowFill = (() => fill)
      ..rowGround = () => (ground: _themeRow, chosenOn: Colors.white, minContrast: 4.5);
    addTearDown(drawer.dispose);
    draw(drawer);
    EquoCommService.commForTesting.receiveBinary(
        channels.frame('GC/$id/gcDispose', TaggedValues.encode({'fullRepaint': true})));
    await settle();
    return drawer.shapes;
  }

  /// The GC of one row of a Table whose rows share [fill]; each call paints the row again with
  /// the cell's background and what [draw] does.
  Future<List<Shape>> Function(Color, void Function(GCDrawer)) rowGC(OwnerDrawnRowFill fill,
      {bool selected = false}) {
    final id = nextId++;
    final drawer = GCDrawer.embedded(VGC.empty()..id = id, onShapesUpdated: (_) {})
      ..rowFill = (() => fill)
      ..rowSelected = (() => selected)
      ..rowGround = () => (ground: _themeRow, chosenOn: Colors.white, minContrast: 4.5);
    addTearDown(drawer.dispose);
    return (background, draw) async {
      drawer.state
        ..foreground = _v(_black)
        ..background = _v(background);
      draw(drawer);
      EquoCommService.commForTesting.receiveBinary(
          channels.frame('GC/$id/gcDispose', TaggedValues.encode({'fullRepaint': true})));
      await settle();
      return drawer.shapes;
    };
  }

  void fillCell(GCDrawer d) => d.onFillRectangleintintintint(VGCFillRectangleintintintint()
    ..x = 8
    ..y = 0
    ..width = 400
    ..height = 18);

  void fillDot(GCDrawer d) {
    d.state.background = _v(_black);
    d.onFillRectangleintintintint(VGCFillRectangleintintintint()
      ..x = 13
      ..y = 5
      ..width = 8
      ..height = 8);
  }

  void drawLabel(GCDrawer d) => d.onDrawTextStringintintint(VGCDrawTextStringintintint()
    ..string = '[local]/the_model.slx'
    ..x = 26
    ..y = 2
    ..flags = SWT.DRAW_TRANSPARENT);

  List<Color> fillsOf(List<Shape> shapes) =>
      shapes.whereType<RectShape>().map((s) => s.color).toList();

  test('the application\'s row fill takes the colour the theme declares', () async {
    final shapes = await paintRow(OwnerDrawnRowFill(_themeRow), _white, (d) {
      fillCell(d);
      fillDot(d);
      drawLabel(d);
    });

    expect(fillsOf(shapes), [_themeRow, _black],
        reason: 'only the row fill is the theme\'s; the dot keeps the application\'s colour');
    expect(contrastRatio(shapes.whereType<TextShape>().single.style.color!, _themeRow),
        greaterThanOrEqualTo(4.5),
        reason: 'the text is read on the fill now painted under it');
  });

  test('a row filled in another colour than the Table\'s other rows keeps it', () async {
    final fill = OwnerDrawnRowFill(_themeRow);
    await paintRow(fill, _white, fillCell);
    await paintRow(fill, _white, fillCell);

    final selected = await paintRow(fill, _selectionTint, fillCell);

    expect(fillsOf(selected), [_selectionTint]);
  });

  test('a row hovered and repainted many times keeps the theme colour once it rests', () async {
    final row = rowGC(OwnerDrawnRowFill(_themeRow));
    expect(fillsOf(await row(_white, fillCell)), [_themeRow]);

    for (var i = 0; i < 3; i++) {
      expect(fillsOf(await row(_hover, fillCell)), [_hover],
          reason: 'the hover fill stays the application\'s on repaint ${i + 1}');
    }

    expect(fillsOf(await row(_white, fillCell)), [_themeRow]);
  });

  test('the rows\' own fill does not depend on which row paints first', () async {
    final fill = OwnerDrawnRowFill(_themeRow);
    final rows = [rowGC(fill), rowGC(fill), rowGC(fill)];
    await rows[0](_selectionTint, fillCell);
    await rows[1](_white, fillCell);
    await rows[2](_white, fillCell);

    expect(fillsOf(await rows[0](_selectionTint, fillCell)), [_selectionTint]);
    expect(fillsOf(await rows[1](_white, fillCell)), [_themeRow]);
  });

  test('the rows are told to repaint when their own fill changes', () async {
    final fill = OwnerDrawnRowFill(_themeRow);
    var changes = 0;
    fill.addListener(() => changes++);
    final first = rowGC(fill), second = rowGC(fill), third = rowGC(fill);

    await first(_selectionTint, fillCell);
    final before = changes;
    await second(_white, fillCell);
    await third(_white, fillCell);

    expect(changes, greaterThan(before),
        reason: 'the selected row painted first was the own fill until the others painted');
    final settled = changes;
    await second(_white, fillCell);
    await third(_white, fillCell);
    expect(changes, settled, reason: 'repainting the same rows changes nothing');
  });

  group('a theme that declares a hover fill', () {
    const themeHover = Color(0xFF2E3B33);

    test('paints the application\'s hover fill of a row in it', () async {
      final row = rowGC(OwnerDrawnRowFill(_themeRow, hoverColor: themeHover));
      expect(fillsOf(await row(_white, fillCell)), [_themeRow]);

      for (var i = 0; i < 3; i++) {
        final hovered = await row(_hover, (d) {
          fillCell(d);
          drawLabel(d);
        });
        expect(fillsOf(hovered), [themeHover], reason: 'hover repaint ${i + 1}');
        expect(contrastRatio(hovered.whereType<TextShape>().single.style.color!, themeHover),
            greaterThanOrEqualTo(4.5),
            reason: 'the text is read on the theme\'s hover fill');
      }

      expect(fillsOf(await row(_white, fillCell)), [_themeRow]);
      expect(fillsOf(await row(_hover, fillCell)), [themeHover]);
    });

    test('paints the hover fill in it on every row once one row was hovered', () async {
      final fill = OwnerDrawnRowFill(_themeRow, hoverColor: themeHover);
      final first = rowGC(fill), second = rowGC(fill);
      await first(_white, fillCell);
      await second(_white, fillCell);
      await first(_hover, fillCell);
      await first(_white, fillCell);

      expect(fillsOf(await second(_hover, fillCell)), [themeHover]);
    });

    test('keeps the fill of a selected row', () async {
      final fill = OwnerDrawnRowFill(_themeRow, hoverColor: themeHover);
      final selected = rowGC(fill, selected: true), plain = rowGC(fill), other = rowGC(fill);
      await selected(_white, fillCell);
      await plain(_white, fillCell);
      await other(_white, fillCell);

      expect(fillsOf(await selected(_selectionTint, fillCell)), [_selectionTint]);
      expect(fillsOf(await plain(_hover, fillCell)), [themeHover],
          reason: 'the selection fill was not taken for the hover fill');
      expect(fillsOf(await selected(_selectionTint, fillCell)), [_selectionTint]);
    });

    test('keeps the small fills on a row', () async {
      final row = rowGC(OwnerDrawnRowFill(_themeRow, hoverColor: themeHover));
      await row(_white, fillCell);

      expect(fillsOf(await row(_white, (d) {
        fillCell(d);
        fillDot(d);
      })), [_themeRow, _black]);
    });
  });

  test('a theme that declares no hover fill leaves the application\'s hover fill', () async {
    final row = rowGC(OwnerDrawnRowFill(_themeRow));
    await row(_white, fillCell);

    expect(fillsOf(await row(_hover, fillCell)), [_hover]);
  });

  test('a theme that declares no row fill leaves the application\'s fills', () async {
    final shapes = await paintRow(null, _white, (d) {
      fillCell(d);
      fillDot(d);
    });

    expect(fillsOf(shapes), [_white, _black]);
  });

  group('the Table hands its rows', () {
    VTable table() => VTable()
      ..id = 1
      ..style = SWT.SINGLE
      ..enabled = true
      ..headerVisible = false
      ..linesVisible = false
      ..columns = []
      ..items = [
        VTableItem()
          ..id = 10
          ..texts = ['owner-drawn']
          ..paintedTexts = [0],
      ]
      ..bounds = (VRectangle()
        ..x = 0
        ..y = 0
        ..width = 200
        ..height = 100);

    Future<OwnerDrawnRowOverlayState> pump(WidgetTester tester,
        {Color? declared, Color? hover}) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.dark,
        contentWidget: Builder(builder: (context) {
          final theme = Theme.of(context);
          final rows = theme.extension<TableThemeExtension>()!;
          return Theme(
            data: theme.copyWith(extensions: [
              ...theme.extensions.values.where((e) => e is! TableThemeExtension),
              rows.copyWith(ownerDrawnRowFillColor: declared, ownerDrawnRowHoverFillColor: hover),
            ]),
            child: SizedBox(width: 200, height: 100, child: TableSwt<VTable>(value: table())),
          );
        }),
      ));
      await tester.pumpAndSettle();
      return tester.state<OwnerDrawnRowOverlayState>(find.byType(OwnerDrawnRowOverlay));
    }

    testWidgets('the row fill its theme declares', (tester) async {
      final row = await pump(tester, declared: _themeRow);

      expect(row.rowFill?.color, _themeRow);
      expect(row.rowFill?.hoverColor, isNull);
    });

    testWidgets('the hover fill its theme declares', (tester) async {
      final row = await pump(tester, declared: _themeRow, hover: _selectionTint);

      expect(row.rowFill?.hoverColor, _selectionTint);
    });

    testWidgets('no row fill when its theme declares none', (tester) async {
      final row = await pump(tester);

      expect(row.rowFill, isNull);
    });

    testWidgets('no row fill when the application\'s colours are painted as given', (tester) async {
      setConfigFlags(ConfigFlags()..use_swt_colors = true);
      final row = await pump(tester, declared: _themeRow);

      expect(row.rowFill, isNull);
    });
  });
}
