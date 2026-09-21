// A GC paints point-sized text at the host's own point-to-pixel scale, the one the Java side
// measured its extents at (ConfigFlags.font_point_scale) -- 72-dpi points on macOS, 96 elsewhere.
// The browser's platform says nothing about the host, so assuming 96 dpi paints a macOS
// application's labels a third too large, overflowing the figures it sized for its own metrics.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/painting.dart' show TextStyle;
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/font.dart';
import 'package:swtflutter/src/gen/fontdata.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const fontHeightPoints = 12;
  var nextId = 1516001;

  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  void hostReports(double pointScale) {
    setConfigFlags(ConfigFlags()..font_point_scale = pointScale);
  }

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
    }
  }

  /// Paints one string through the real draw path and returns the style it was painted with.
  Future<TextStyle> paintedStyle() async {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..font = (VFont()
        ..fontData = [
          VFontData()
            ..name = 'System'
            ..height = fontHeightPoints
            ..style = 0
        ]);
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);

    drawer.onDrawTextStringintint(VGCDrawTextStringintint()
      ..string = 'Constant'
      ..x = 10
      ..y = 10);

    final action = utf8.encode('GC/$id/gcDispose');
    final body = utf8.encode(jsonEncode({'fullRepaint': true}));
    final frame = Uint8List(2 + action.length + body.length);
    frame[0] = (action.length >> 8) & 0xFF;
    frame[1] = action.length & 0xFF;
    frame.setRange(2, 2 + action.length, action);
    frame.setRange(2 + action.length, frame.length, body);
    EquoCommService.commForTesting.receiveBinary(frame);
    await settle();

    return drawer.shapes.whereType<TextShape>().single.style;
  }

  test('a 72-dpi host paints a 12-point font at 12 logical pixels', () async {
    hostReports(1.0);
    expect((await paintedStyle()).fontSize, closeTo(12.0, 0.001));
  });

  test('a 96-dpi host still converts the same font to 16 logical pixels', () async {
    hostReports(96 / 72);
    expect((await paintedStyle()).fontSize, closeTo(16.0, 0.001));
  });
}
