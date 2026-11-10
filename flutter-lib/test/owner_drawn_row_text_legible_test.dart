// Owner-drawn text is kept legible on the ground actually under it: the application's own fill, else
// the row Evolve paints.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/path.dart';
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

const _darkRow = Color(0xFF1F2937);
const _black = Color(0xFF000000);
const _lightBlue = Color(0xFFCCE5FF);
const _red = Color(0xFFFF0000);

VColor _v(Color c) => VColor()
  ..red = (c.r * 255).round()
  ..green = (c.g * 255).round()
  ..blue = (c.b * 255).round()
  ..alpha = 255;

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  var nextId = 1645001;

  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
    }
  }

  /// A row's GC on [ground], drawing what [draw] does with the cell's [foreground] and [background],
  /// as the listeners find them.
  Future<List<Shape>> paintRow(Color ground, Color foreground, Color background,
      void Function(GCDrawer) draw) async {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..foreground = _v(foreground)
      ..background = _v(background);
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {})
      ..rowGround = () => (ground: ground, minContrast: 4.5);
    addTearDown(drawer.dispose);
    draw(drawer);
    EquoCommService.commForTesting.receiveBinary(
        channels.frame('GC/$id/gcDispose', TaggedValues.encode({'fullRepaint': true})));
    await settle();
    return drawer.shapes;
  }

  void fillCell(GCDrawer d) => d.onFillRectangleintintintint(VGCFillRectangleintintintint()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 20);

  void drawLabel(GCDrawer d) => d.onDrawTextStringintintint(VGCDrawTextStringintintint()
    ..string = 'Result'
    ..x = 4
    ..y = 2
    ..flags = SWT.DRAW_TRANSPARENT);

  Color textOf(List<Shape> shapes) => shapes.whereType<TextShape>().single.style.color!;

  test('black text on the application\'s own light fill is kept, and so is the fill', () async {
    final shapes = await paintRow(_darkRow, _black, _lightBlue, (d) {
      fillCell(d);
      drawLabel(d);
    });

    expect(textOf(shapes), _black);
    expect(shapes.whereType<RectShape>().single.color, _lightBlue);
  });

  test('black text on no fill of its own is drawn light on the dark row Evolve paints', () async {
    final shapes = await paintRow(_darkRow, _black, Colors.white, drawLabel);

    expect(contrastRatio(textOf(shapes), _darkRow), greaterThanOrEqualTo(4.5),
        reason: 'the cell\'s black was chosen for the white Evolve does not paint');
  });

  test('an application\'s red text keeps its hue while it is made legible', () async {
    final shapes = await paintRow(_darkRow, _red, Colors.white, drawLabel);

    final text = HSLColor.fromColor(textOf(shapes));
    expect(contrastRatio(textOf(shapes), _darkRow), greaterThanOrEqualTo(4.5));
    expect(text.hue, anyOf(lessThan(15), greaterThan(345)));
    expect(text.saturation, greaterThan(0.5));
  });

  test('text that reads on the row is drawn as given', () async {
    final shapes = await paintRow(Colors.white, _black, Colors.white, drawLabel);

    expect(textOf(shapes), _black);
  });

  test('text in the next cell is read against the row, not against another cell\'s fill', () async {
    final shapes = await paintRow(_darkRow, _black, _lightBlue, (d) {
      fillCell(d);
      d.onDrawTextStringintintint(VGCDrawTextStringintintint()
        ..string = 'Other'
        ..x = 204
        ..y = 2
        ..flags = SWT.DRAW_TRANSPARENT);
    });

    expect(contrastRatio(textOf(shapes), _darkRow), greaterThanOrEqualTo(4.5));
  });

  test('a string the application adds to a path is made legible as drawn text is', () async {
    final path = VPath()
      ..textStrings = ['Result']
      ..textOrigins = [4, 2]
      ..textFonts = [
        VFont()
          ..fontData = [
            VFontData()
              ..name = 'System'
              ..height = 9
          ]
      ];
    final shapes = await paintRow(_darkRow, _black, _black,
        (d) => d.onFillPathPath(VGCFillPathPath()..path = path));

    final text = shapes.whereType<TextShape>().single.style.foreground!.color;
    expect(contrastRatio(text, _darkRow), greaterThanOrEqualTo(4.5));
  });

  group('the row the overlay reads on', () {
    VTable table({List<int>? selection}) => VTable()
      ..id = 1
      ..style = SWT.SINGLE
      ..enabled = true
      ..headerVisible = false
      ..linesVisible = false
      ..columns = []
      ..selection = selection
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

    Future<TableThemeExtension> pump(WidgetTester tester, VTable value) async {
      await tester.pumpWidget(EvolveApp(
        theme: ThemeMode.dark,
        contentWidget: SizedBox(width: 200, height: 100, child: TableSwt<VTable>(value: value)),
      ));
      await tester.pumpAndSettle();
      return Theme.of(tester.element(find.byType(OwnerDrawnRowOverlay)))
          .extension<TableThemeExtension>()!;
    }

    testWidgets('is the theme\'s row', (tester) async {
      final theme = await pump(tester, table());

      final overlay = tester.widget<OwnerDrawnRowOverlay>(find.byType(OwnerDrawnRowOverlay));
      expect(overlay.ground, theme.backgroundColor);
      expect(overlay.minContrast, theme.minTextContrast);
    });

    testWidgets('is the selection colour on a selected row', (tester) async {
      final theme = await pump(tester, table(selection: [0]));

      final overlay = tester.widget<OwnerDrawnRowOverlay>(find.byType(OwnerDrawnRowOverlay));
      expect(overlay.ground,
          Color.alphaBlend(theme.selectedBackgroundColor, theme.backgroundColor));
    });

    testWidgets('is none when the application\'s colours are painted as given', (tester) async {
      setConfigFlags(ConfigFlags()..use_swt_colors = true);
      await pump(tester, table());

      final row = tester.state<OwnerDrawnRowOverlayState>(find.byType(OwnerDrawnRowOverlay));
      expect(row.textGround, isNull);
    });
  });
}
