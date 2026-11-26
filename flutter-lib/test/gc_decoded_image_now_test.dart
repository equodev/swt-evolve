// An image already decoded is drawn without waiting for it, and as the draw that waited for it was.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/transform.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'support/channels.dart' as channels;

/// A 9x9 RGBA bitmap.
const _pngBase64 =
    'iVBORw0KGgoAAAANSUhEUgAAAAkAAAAJCAYAAADgkQYQAAAAO0lEQVR4nGNgwA4YgbgHhxwYsALxE'
    'iD+j0sBDxDvgirAqkgMiM8gKcCqaAeaAvJNIspNMEDQdzCAEk4AKNMWZTaEExwAAAAASUVORK5CYII=';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  var nextId = 941001;

  void deliver(String actionId, Map<String, dynamic> payload) {
    EquoCommService.commForTesting
        .receiveBinary(channels.frame(actionId, TaggedValues.encode(payload)));
  }

  Future<void> settle() async {
    for (var i = 0; i < 20; i++) {
      await Future<void>.delayed(const Duration(milliseconds: 5));
    }
  }

  // Commits per drawer: a cycle's shapes are handed on when it is committed.
  final commits = <GCDrawer, int>{};

  (GCDrawer, int) drawerWith({List<double>? transform, VRectangle? clip}) {
    final id = nextId++;
    final state = VGC.empty()
      ..id = id
      ..clipping = clip
      ..transform = transform == null ? null : (VTransform()..elements = transform);
    late final GCDrawer drawer;
    drawer = GCDrawer.embedded(state,
        onShapesUpdated: (_) => commits[drawer] = (commits[drawer] ?? 0) + 1);
    addTearDown(drawer.dispose);
    return (drawer, id);
  }

  // The same bytes on every draw, as an Image the application holds sends them.
  final bytes = base64Decode(_pngBase64);
  VImage image() => VImage()
    ..imageData = (VImageData()
      ..width = 9
      ..height = 9
      ..data = bytes);

  void draw(GCDrawer drawer) {
    drawer.onDrawImageImageintint(VGCDrawImageImageintint()
      ..x = 4
      ..y = 6
      ..image = image());
    drawer.onDrawImageImageintintintintintintintint(VGCDrawImageImageintintintintintintintint()
      ..srcX = 2
      ..srcY = 1
      ..srcWidth = 5
      ..srcHeight = 6
      ..destX = 30
      ..destY = 31
      ..destWidth = 10
      ..destHeight = 12
      ..image = image());
  }

  ImageShape imageOf(Shape s) => switch (s) {
        ImageShape() => s,
        TransformShape() => imageOf(s.children.single),
        _ => throw StateError('not an image: $s'),
      };

  String describe(Shape s) {
    final i = imageOf(s);
    return '${s.runtimeType} ${i.type} src=${i.srcRect} dest=${i.destRect} clip=${i.clipRect} '
        'alpha=${i.alpha} filter=${i.colorFilter} mask=${i.inkMask} opaque=${i.opaqueSource} '
        '${s is TransformShape ? s.matrix.toList() : ''} ${s is TransformShape ? s.clipRect : ''}';
  }

  for (final (name, transform, clip) in [
    ('plain', null, null),
    ('under a clip', null, VRectangle()..x = 3..y = 3..width = 30..height = 30),
    ('under a transform', [1.5, 0.0, 0.0, 1.5, 7.0, 3.0], VRectangle()..x = 0..y = 0..width = 50..height = 40),
  ]) {
    test('$name: a decoded image commits with its cycle, as the draw that waited for it', () async {
      final (drawer, id) = drawerWith(transform: transform, clip: clip);
      draw(drawer);
      deliver('GC/$id/gcDispose', {'fullRepaint': true});
      await settle();
      final waited = drawer.shapes.map(describe).toList();
      expect(waited, hasLength(2));

      final before = commits[drawer] ?? 0;
      draw(drawer);
      deliver('GC/$id/gcDispose', {'fullRepaint': true});
      // The frame is applied in one microtask: the cycle is committed as its end is applied.
      await Future<void>.value();
      expect(commits[drawer] ?? 0, before + 1);
      expect(drawer.shapes.map(describe).toList(), waited);
      for (final s in drawer.shapes) {
        expect(identical(imageOf(s).image, null), isFalse);
      }
    });
  }

  test('an image not yet decoded still waits for its decode', () async {
    final (drawer, id) = drawerWith();
    drawer.onDrawImageImageintint(VGCDrawImageImageintint()
      ..x = 0
      ..y = 0
      ..image = (VImage()
        ..imageData = (VImageData()
          ..width = 9
          ..height = 9
          ..data = Uint8List.fromList(bytes))));
    deliver('GC/$id/gcDispose', {'fullRepaint': true});
    await Future<void>.value();
    expect(commits[drawer] ?? 0, 0);
    await settle();
    expect(commits[drawer], 1);
    expect(drawer.shapes.single, isA<ImageShape>());
  });
}
