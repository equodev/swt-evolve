// A string added to a Path has no outline on this backend: its runs travel beside the path data and
// are drawn from their font. The run has to keep that font's weight, and be stroked the way the GC
// strokes any path - a line width of 0 is a hairline, not a thick pen.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' show FontWeight, PaintingStyle;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/path.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 135101;

  void deliver(String actionId, Map<String, dynamic> json) {
    final action = utf8.encode(actionId);
    final body = utf8.encode(jsonEncode(json));
    final out = Uint8List(2 + action.length + body.length);
    out[0] = (action.length >> 8) & 0xFF;
    out[1] = action.length & 0xFF;
    out.setRange(2, 2 + action.length, action);
    out.setRange(2 + action.length, out.length, body);
    EquoCommService.commForTesting.receiveBinary(out);
  }

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
    }
  }

  (GCDrawer, int) drawerWith({int lineWidth = 0}) {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..lineWidth = lineWidth;
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);
    return (drawer, id);
  }

  Future<List<Shape>> committed(GCDrawer drawer, int id) async {
    deliver('GC/$id/gcDispose', {'fullRepaint': true});
    await settle();
    return drawer.shapes;
  }

  VPath textPath() => VPath()
    ..textStrings = ['SWT']
    ..textOrigins = [10, 20]
    ..textFonts = [
      VFont()
        ..fontData = [
          VFontData()
            ..name = 'System'
            ..height = 40
            ..style = SWT.BOLD
        ]
    ];

  TextShape onlyText(List<Shape> shapes) => shapes.whereType<TextShape>().single;

  test('a string in a drawn path keeps its font weight and is outlined with a hairline', () async {
    final (drawer, id) = drawerWith(lineWidth: 0);
    drawer.onDrawPathPath(VGCDrawPathPath()..path = textPath());

    final text = onlyText(await committed(drawer, id));
    expect(text.text, 'SWT');
    expect(text.style.fontWeight, FontWeight.bold);
    expect(text.style.foreground!.style, PaintingStyle.stroke);
    expect(text.style.foreground!.strokeWidth, 1);
  });

  test('a string in a filled path is filled', () async {
    final (drawer, id) = drawerWith();
    drawer.onFillPathPath(VGCFillPathPath()..path = textPath());

    final text = onlyText(await committed(drawer, id));
    expect(text.style.fontWeight, FontWeight.bold);
    expect(text.style.foreground!.style, PaintingStyle.fill);
  });

  test('a clip set from a path holding text masks what is drawn', () async {
    final (drawer, id) = drawerWith();
    drawer.state.clippingText = textPath();
    drawer.onFillRectangleintintintint(VGCFillRectangleintintintint()
      ..x = 0
      ..y = 0
      ..width = 100
      ..height = 100);

    final shapes = await committed(drawer, id);
    expect(shapes.single, isA<MaskShape>());
  });
}
