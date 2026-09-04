// GC.setTransform(t) applies t to everything drawn afterwards, not to text alone. A backend that
// transforms only text puts a canvas' geometry in one coordinate space and its labels in another:
// at a GEF zoom of 1.5 the figures render at 1x with 1.5x labels overflowing them.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' show Color, Offset, Rect;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/transform.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';

/// A 9x9 RGBA bitmap, the smallest thing ImageShape will decode.
const _pngBase64 =
    'iVBORw0KGgoAAAANSUhEUgAAAAkAAAAJCAYAAADgkQYQAAAAO0lEQVR4nGNgwA4YgbgHhxwYsALxE'
    'iD+j0sBDxDvgirAqkgMiM8gKcCqaAeaAvJNIspNMEDQdzCAEk4AKNMWZTaEExwAAAAASUVORK5CYII=';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 940001;

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

  /// A drawer whose GC state carries [elements] as its transform, plus [clip] if given.
  (GCDrawer, int) drawerWith(List<double>? elements, {VRectangle? clip}) {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..clipping = clip
      ..transform = elements == null ? null : (VTransform()..elements = elements);
    final drawer = GCDrawer.embedded(state, onShapesUpdated: (_) {});
    addTearDown(drawer.dispose);
    return (drawer, id);
  }

  VRectangle rect(int x, int y, int w, int h) => VRectangle()
    ..x = x
    ..y = y
    ..width = w
    ..height = h;

  Future<List<Shape>> committed(GCDrawer drawer, int id) async {
    deliver('GC/$id/gcDispose', {'fullRepaint': true});
    await settle();
    return drawer.shapes;
  }

  const scale = [1.5, 0.0, 0.0, 1.5, 0.0, 0.0];

  test('a scaling transform reaches every primitive, not only text', () async {
    final (drawer, id) = drawerWith(scale);

    drawer.onDrawRectangleintintintint(VGCDrawRectangleintintintint()
      ..x = 21
      ..y = 21
      ..width = 98
      ..height = 38);
    drawer.onDrawLineintintintint(VGCDrawLineintintintint()
      ..x1 = 0
      ..y1 = 0
      ..x2 = 40
      ..y2 = 40);
    drawer.onDrawOvalintintintint(VGCDrawOvalintintintint()
      ..x = 10
      ..y = 10
      ..width = 20
      ..height = 20);
    drawer.onDrawPointintint(VGCDrawPointintint()
      ..x = 5
      ..y = 5);
    drawer.onDrawTextStringintintboolean(VGCDrawTextStringintintboolean()
      ..string = 'label'
      ..x = 25
      ..y = 25
      ..isTransparent = true);

    final shapes = await committed(drawer, id);

    expect(shapes, hasLength(5));
    expect(shapes.whereType<TransformShape>(), hasLength(5),
        reason: 'geometry drawn under a transform must be transformed like the text is');
    for (final s in shapes.cast<TransformShape>()) {
      expect(s.matrix[0], 1.5);
      expect(s.matrix[5], 1.5);
    }
  });

  test('an identity transform leaves the shapes unwrapped', () async {
    final (drawer, id) = drawerWith(const [1.0, 0.0, 0.0, 1.0, 0.0, 0.0]);

    drawer.onDrawRectangleintintintint(VGCDrawRectangleintintintint()
      ..x = 1
      ..y = 1
      ..width = 2
      ..height = 2);

    expect(await committed(drawer, id), [isA<RectShape>()]);
  });

  test('no transform leaves the shapes unwrapped', () async {
    final (drawer, id) = drawerWith(null);

    drawer.onDrawRectangleintintintint(VGCDrawRectangleintintintint()
      ..x = 1
      ..y = 1
      ..width = 2
      ..height = 2);

    expect(await committed(drawer, id), [isA<RectShape>()]);
  });

  test('an image staged before it decodes is transformed too', () async {
    final (drawer, id) = drawerWith(scale);

    drawer.onDrawImageImageintint(VGCDrawImageImageintint()
      ..x = 4
      ..y = 4
      ..image = (VImage()
        ..filename = 'dot'
        ..imageData = (VImageData()
          ..width = 9
          ..height = 9
          ..depth = 24
          ..data = base64Decode(_pngBase64))));

    final shapes = await committed(drawer, id);

    expect(shapes, hasLength(1));
    final wrapper = shapes.single as TransformShape;
    expect(wrapper.children.single, isA<ImageShape>(),
        reason: 'an image bypasses _addShape, so it must wrap itself once decoded');
    expect(wrapper.matrix[0], 1.5);
  });

  test('copyArea shifts a transformed shape by the device offset', () {
    final wrapper = TransformShape(
        Float64List.fromList(
            [1.5, 0, 0, 0, 0, 1.5, 0, 0, 0, 0, 1, 0, 10, 20, 0, 1]),
        [RectShape(const Rect.fromLTWH(0, 0, 10, 10), const Color(0xFF000000), 1,
            0, 0)]);

    final copied = GCDrawer.copyAreaShapes(
      baseImage: null,
      painted: [wrapper],
      srcRect: const Rect.fromLTWH(0, 0, 100, 100),
      destOffset: const Offset(40, 5),
    );

    final shifted = copied.single as TransformShape;
    expect([shifted.matrix[12], shifted.matrix[13]], [50.0, 25.0]);
    expect(shifted.matrix[0], 1.5, reason: 'only the translation moves');
    expect(wrapper.matrix[12], 10.0, reason: 'the source shape is left alone');
  });

  test('the clip is applied in device space, on the wrapper', () async {
    final (drawer, id) = drawerWith(scale, clip: rect(0, 0, 60, 60));

    drawer.onDrawRectangleintintintint(VGCDrawRectangleintintintint()
      ..x = 10
      ..y = 10
      ..width = 20
      ..height = 20);

    final shapes = await committed(drawer, id);
    final wrapper = shapes.single as TransformShape;

    expect(wrapper.clipRect, const Rect.fromLTWH(0, 0, 60, 60),
        reason: 'the clip SWT reports is in device space, so it belongs outside the matrix');
    expect(wrapper.children.single.clipRect, isNull,
        reason: 'clipping again inside the matrix would scale the clip rectangle too');
  });
}
