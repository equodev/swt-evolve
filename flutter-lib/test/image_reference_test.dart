import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';

import 'support/gc_frames.dart';

/// The image a frame names by [id], as a reader of that frame resolves it.
VImage _named(int id) =>
    WidgetWireReader(Uint8List.fromList(WidgetWireWriter.encodeWith((w) => w.ref(id)))).image()!;

/// An image arrives once and is named after that, so the client has to answer a name with the
/// image it already holds. A name it cannot answer would render as nothing at all.
void main() {
  test('an image named after being described resolves to the one held', () {
    describeImage(VImage()
      ..id = 4242
      ..width = 16
      ..height = 16
      ..imageData = (VImageData()
        ..width = 16
        ..height = 16
        ..depth = 24));

    final named = _named(4242);

    expect(identical(named, _named(4242)), isTrue,
        reason: 'a named image must resolve to the one already held, or it draws nothing');
    expect(named.isReference, isFalse);
    expect(named.imageData, isNotNull);
  });

  test('an image drawn by its own GC resolves to its render when named', () {
    // Its pixels are a render the client keeps by remote ref, and a frame names the image by id:
    // only the held image turns that id back into the ref.
    describeImage(VImage()
      ..id = 99
      ..remoteRef = 7);

    final named = _named(99);
    expect(named.imageData, isNull);
    expect(named.remoteRef, 7);
  });

  test('a name nothing answers stays a name', () {
    final named = _named(98);
    expect(named.isReference, isTrue,
        reason: 'a name nothing answers stays a name, so a caller can tell it apart from an image');
  });
}
