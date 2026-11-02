// An image a GC drew on is a size and a remote ref: its pixels are a render the client keeps by
// that ref. Java sends its record once, ahead of the first paint that blits it, and every paint
// after names it by id alone, in a frame of its own. The client has to turn that id back into the
// ref, or the blit finds nothing and draws a placeholder: an editor's rulers, painted from buffers
// they draw once, showed nothing.

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/image.dart';

/// A widget frame of one image record, as Java's WidgetWire writes it.
Uint8List _imageRecord(int id, VImage image) {
  final record = Uint8List.fromList(WidgetWireWriter.encodeWith((w) {
    w.varint(id * 2);
    image.writeWire(w);
  }));
  final out = BytesBuilder()
    ..add((ByteData(4)..setUint32(0, 1, Endian.little)).buffer.asUint8List())
    ..addByte(wireImage);
  var length = record.length;
  while (length >= 0x80) {
    out.addByte((length & 0x7F) | 0x80);
    length >>= 7;
  }
  out
    ..addByte(length)
    ..add(record);
  return out.toBytes();
}

/// An image a GC drew on: its size and the render it is kept as.
VImage _render(int remoteRef, int width, int height) => VImage()
  ..remoteRef = remoteRef
  ..width = width
  ..height = height;

/// The image a later frame names by [id], read as a GC paint's own reader reads it.
VImage? _namedInALaterFrame(int id) {
  final bytes = Uint8List.fromList(WidgetWireWriter.encodeWith((w) => w.varint(id * 2 + 1)));
  return WidgetWireReader(bytes).image();
}

void main() {
  test('an image described by its render is found by the id a later paint names it by', () {
    VRegistry.instance.applyWire(_imageRecord(9101, _render(55501, 22, 360)));

    final named = _namedInALaterFrame(9101);

    expect(named?.remoteRef, 55501, reason: 'without the ref the blit draws a placeholder');
    expect(named?.width, 22);
  });

  test('a render the image is drawn again into replaces the one it was named by', () {
    VRegistry.instance.applyWire(_imageRecord(9102, _render(55601, 22, 360)));
    VRegistry.instance.applyWire(_imageRecord(9102, _render(55602, 22, 360)));

    expect(_namedInALaterFrame(9102)?.remoteRef, 55602);
  });
}
