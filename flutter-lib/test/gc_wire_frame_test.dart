// A GC's paint as one binary frame (Java's GcWire): ops named once and then by number, the state
// whole and then by changes, and the entry that ends the paint travelling on a channel of its own.

import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/gc_wire.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey, wireChannelNames;
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/impl/gcdrawer_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

import 'support/gc_frames.dart';
import 'support/channels.dart' as channels;

/// Writes records the way Java's GcWire does: each op by its number the first time, by its place in
/// the frame after.
class _Frame {

  final _b = BytesBuilder();
  final _ops = <String, int>{};

  void _varint(int v) {
    while (v >= 0x80) {
      _b.addByte((v & 0x7F) | 0x80);
      v >>= 7;
    }
    _b.addByte(v);
  }

  void i(int v) => _varint(v >= 0 ? v * 2 : -v * 2 - 1);

  void op(String name, String signature) {
    final number = _ops[name];
    if (number != null) {
      _varint(GcWireReader.opFirst + number);
      return;
    }
    _ops[name] = _ops.length;
    _varint(GcWireReader.opNumbered);
    _varint(wireChannelNames.entries.firstWhere((e) => e.value == name).key);
  }

  void whole(VGC state) => _b.add(gcState(state));

  void backgroundChange(int argb) =>
      _b.add(gcState(VGC.empty()..background = VColor.fromArgb(argb), changed: {WireKey.k_GC_background}));

  void escape(String channel, Object payload) => _b.add(GcFrame().escape(channel, payload).bytes);

  Uint8List get bytes => _b.toBytes();
}

Uint8List _wire(String channel, List<int> body) => channels.frame(channel, Uint8List.fromList(body));

Future<void> _settle() async {
  for (var i = 0; i < 120; i++) {
    await Future<void>.delayed(Duration.zero);
  }
}

Future<ui.Color> _pixelAt(ui.Image image, int x, int y) async {
  final data = await image.toByteData(format: ui.ImageByteFormat.rawRgba);
  final bytes = data!.buffer.asUint8List();
  final i = (y * image.width + x) * 4;
  return ui.Color.fromARGB(bytes[i + 3], bytes[i], bytes[i + 1], bytes[i + 2]);
}

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  {
    test(
      'a paint in one binary frame draws its ops with the state each was drawn in',
      () async {
        const gcId = 9943;
        const ref = 9952;
        final comm = EquoCommService.commForTesting;
        GCDrawer.standalone(
          VGC()
            ..swt = 'GC'
            ..id = gcId,
        );
        comm.receiveBinary(
          _wire(
            'GC/$gcId/imageInit',
            imageInit(VImage()
              ..width = 20
              ..height = 20),
          ),
        );

        final frame = _Frame()
          ..whole(VGC.empty()
            ..alpha = 255
            ..clippingRects = []
            ..foreground = colourOf(0, 0, 0))
          ..backgroundChange(0xFFFF0000);
        frame
          ..op('fillRectangleintintintint', 'iiii')
          // Negative coordinates take the zigzag path: this one covers (0,0)-(4,4).
          ..i(-5)
          ..i(-5)
          ..i(10)
          ..i(10)
          ..backgroundChange(0xFF0000FF)
          ..op('fillRectangleintintintint', 'iiii')
          ..i(10)
          ..i(10)
          ..i(10)
          ..i(10)
          ..escape('GC/$gcId/gcDispose', {
            'ref': ref,
            'pixels': false,
            'retain': false,
          });
        comm.receiveBinary(_wire('GC/$gcId/b', frame.bytes));

        await _settle();

        final rendered = await ImageUtils.decodeVImageToUIImage(
          VImage()..remoteRef = ref,
        );
        expect(rendered, isNotNull, reason: 'the frame produced no render');
        expect(
          await _pixelAt(rendered!, 2, 2),
          const ui.Color(0xFFFF0000),
          reason: 'the first fill, in the colour set before it',
        );
        expect(
          await _pixelAt(rendered, 15, 15),
          const ui.Color(0xFF0000FF),
          reason: 'the second fill, after its own change of colour',
        );
        expect(
          await _pixelAt(rendered, 7, 7),
          const ui.Color(0xFFFFFFFF),
          reason: 'what neither fill covered',
        );
      },
    );
  }

  test(
    'a frame that arrives before its drawer is held, and drawn once the drawer exists',
    () async {
      const gcId = 9961;
      const ref = 9970;
      final comm = EquoCommService.commForTesting;
      final frame = _Frame()
        ..whole(VGC.empty()
          ..alpha = 255
          ..clippingRects = [])
        ..backgroundChange(0xFF00FF00)
        ..op('fillRectangleintintintint', 'iiii')
        ..i(0)
        ..i(0)
        ..i(4)
        ..i(4)
        ..escape('GC/$gcId/gcDispose', {
          'ref': ref,
          'pixels': false,
          'retain': false,
        });
      // Nothing listens on the frame's channel until the drawer below registers.
      comm.receiveBinary(_wire('GC/$gcId/b', frame.bytes));
      await _settle();
      GCDrawer.standalone(
        VGC()
          ..swt = 'GC'
          ..id = gcId,
      );
      comm.receiveBinary(
        _wire(
          'GC/$gcId/imageInit',
          imageInit(VImage()
            ..width = 8
            ..height = 8),
        ),
      );
      await _settle();

      final rendered = await ImageUtils.decodeVImageToUIImage(
        VImage()..remoteRef = ref,
      );
      expect(rendered, isNotNull, reason: 'the held frame was never drawn');
    },
  );

  test('a negative argument reads back as itself', () {
    final frame = _Frame()
      ..i(-1)
      ..i(0)
      ..i(-2147483648)
      ..i(2147483647);
    final r = GcWireReader(frame.bytes);
    expect([r.i(), r.i(), r.i(), r.i()], [-1, 0, -2147483648, 2147483647]);
  });
}
