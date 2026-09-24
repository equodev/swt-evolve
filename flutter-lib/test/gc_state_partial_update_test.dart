// A drawing library sets one attribute between primitives — draw2d's SWTGraphics re-clips and
// re-colours per figure — so Java sends a GC's state as what changed once the batch carrying it has
// described the GC whole. The drawer therefore has to merge such a frame into the state it holds:
// taking it as a whole description would null every property the change did not name, and the ops
// that follow would be drawn in the wrong colour, with the wrong clip, or not at all.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const id = 851001;

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

  Map<String, dynamic> colour(int r, int g, int b) =>
      {'red': r, 'green': g, 'blue': b, 'alpha': 255};

  Map<String, dynamic> clip(int x, int y, int w, int h) =>
      {'x': x, 'y': y, 'width': w, 'height': h};

  /// The whole GC, as the first state push of a paint describes it.
  Map<String, dynamic> whole() => {
        'id': id,
        'swt': 'GC',
        'style': 33554432,
        'alpha': 255,
        'fillRule': 1,
        'lineStyle': 1,
        'lineCap': 1,
        'lineJoin': 1,
        'lineWidth': 3,
        'background': colour(192, 192, 192),
        'foreground': colour(0, 0, 0),
        'clipping': clip(20, 20, 96, 54),
      };

  /// A change to it, as every state push after the first does.
  Map<String, dynamic> change(Map<String, dynamic> properties) => {
        'id': id,
        'swt': 'GC',
        '_d': properties.keys.toList(),
        ...properties,
      };

  GCDrawer drawer() {
    final state = VGC.empty()..id = id;
    final d = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(d.dispose);
    return d;
  }

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(Duration.zero);
    }
  }

  test('a whole state push replaces what the drawer holds', () async {
    final d = drawer();
    deliver('GC/$id', whole());
    await settle();

    expect(d.state.lineWidth, 3);
    expect(d.state.background?.blue, 192);
    expect(d.state.clipping?.width, 96);
  });

  test('a change applies on top of the state held, leaving the rest alone', () async {
    final d = drawer();
    deliver('GC/$id', whole());
    await settle();

    deliver('GC/$id', change({'foreground': colour(128, 128, 128)}));
    await settle();

    expect(d.state.foreground?.red, 128,
        reason: 'the property the change named has to take effect');
    // The regression: taken as a whole description, this frame would null everything else and
    // every op drawn after it would use the wrong colour, clip and line width.
    expect(d.state.background?.blue, 192);
    expect(d.state.lineWidth, 3);
    expect(d.state.clipping?.width, 96);
    expect(d.state.alpha, 255);
    expect(d.state.fillRule, 1);
  });

  test('successive changes accumulate, the way a paint applies them one op at a time', () async {
    final d = drawer();
    deliver('GC/$id', whole());
    await settle();

    deliver('GC/$id', change({'clipping': clip(200, 100, 7, 7)}));
    await settle();
    deliver('GC/$id', change({'background': colour(0, 0, 0)}));
    await settle();
    deliver('GC/$id', change({'lineWidth': 1}));
    await settle();

    expect(d.state.clipping?.x, 200);
    expect(d.state.background?.red, 0);
    expect(d.state.lineWidth, 1);
    expect(d.state.foreground?.red, 0, reason: 'never named by any change, so never touched');
  });

  test('a later whole push still replaces, so a new paint starts clean', () async {
    final d = drawer();
    deliver('GC/$id', whole());
    await settle();
    deliver('GC/$id', change({'lineWidth': 9}));
    await settle();

    final next = whole()..['lineWidth'] = 1;
    deliver('GC/$id', next);
    await settle();

    expect(d.state.lineWidth, 1,
        reason: 'a whole description is the state, not something to merge into the old one');
  });

  test('the drawer reads the merged state when it turns an op into a shape', () async {
    final d = drawer();
    deliver('GC/$id', whole());
    await settle();
    deliver('GC/$id', change({'background': colour(10, 20, 30)}));
    await settle();

    expect(d.fillColor.red, 10);
    expect(d.fillColor.green, 20);
    expect(d.fillColor.blue, 30);
    expect(d.strokeColor.red, 0, reason: 'foreground was not in the change');
  });

  // Guards the assumption the merge rests on: VColor/VRectangle arrive whole inside a change, so a
  // named property is replaced outright rather than merged field by field.
  test('a change carries each named value whole', () async {
    final d = drawer();
    deliver('GC/$id', whole());
    await settle();
    deliver('GC/$id', change({'clipping': clip(1, 2, 3, 4)}));
    await settle();

    final VRectangle? c = d.state.clipping;
    expect(c, isNotNull);
    expect([c!.x, c.y, c.width, c.height], [1, 2, 3, 4]);
    final VColor? bg = d.state.background;
    expect(bg?.blue, 192, reason: 'untouched by a change that named only the clip');
  });
}
