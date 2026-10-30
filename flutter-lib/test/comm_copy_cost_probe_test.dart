// What a message costs on the Dart side of the comm, at growing payload sizes.
//
// A measurement, printed rather than asserted, for comparing the comm layer before and after a
// change. Each scenario runs the production code path with the socket left out, so the time is
// the comm layer's own. The slope between the two largest sizes is the part that grows with the
// payload, which is where a copy shows.

@Tags(['bench'])
library;

import 'dart:async';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireTypeNames;
import 'package:swtflutter/src/impl/utils/image_utils.dart';

const List<int> _sizes = [64 << 10, 256 << 10, 1 << 20];
const int _rounds = 5;
const Duration _warmup = Duration(milliseconds: 100);
const Duration _roundBudget = Duration(milliseconds: 150);

void main() {
  test('MEASURE: time per message by payload size, Dart side of the comm', () async {
    final comm = _LocalComm();

    await _report('send tagged values (D->J)', (chars) async {
      final payload = {'text': 'x' * chars};
      return () async {
        comm.send('Probe/1/response', payload);
        return comm.lastSent!.length;
      };
    });

    await _report('sendBytes (D->J)', (bytes) async {
      final png = _imageBytes(bytes);
      return () async {
        comm.sendBytes('Image/1/pixelsResult', png);
        return comm.lastSent!.length;
      };
    });

    await _report('receive widget frame (J->D)', (chars) async {
      final frame = _tape([_label(1, text: 'y' * chars)]);
      return () async {
        final handled = comm.nextFrame();
        comm.receiveBinary(frame);
        await handled;
        return frame.length;
      };
    });

    await _report('receive widget image (J->D)', (imageBytes) async {
      final frame = _tape([_label(4, image: _image(4004, _imageBytes(imageBytes)))]);
      return () async {
        final handled = comm.nextFrame();
        comm.receiveBinary(frame);
        await handled;
        return frame.length;
      };
    });

    await _report('build widget image, per build (size = image bytes)', (imageBytes) async {
      final image = _image(4005, _imageBytes(imageBytes));
      return () async {
        ImageUtils.buildVImage(image);
        return imageBytes;
      };
    });
  }, timeout: const Timeout(Duration(minutes: 5)));
}

/// Prints the time per message at every size, and the slope between the two largest.
Future<void> _report(String label, Future<Future<int> Function()> Function(int size) prepare) async {
  final micros = <double>[];
  final wire = <int>[];
  for (final size in _sizes) {
    final once = await prepare(size);
    wire.add(await once());
    micros.add(await _medianMicros(once));
  }
  final last = _sizes.length - 1;
  final slope = (micros[last] - micros[last - 1]) / ((wire[last] - wire[last - 1]) / (1 << 20));
  final cells = [
    for (var i = 0; i < _sizes.length; i++) '${_sizes[i] >> 10}KB=${micros[i].toStringAsFixed(1)}us',
  ];
  print('MEASURE $label: ${cells.join(' ')} slope=${slope.toStringAsFixed(1)}us/MB '
      'wire@${_sizes[last] >> 10}KB=${wire[last]}B');
}

/// Median over [_rounds] of the mean time per message within a fixed time budget.
Future<double> _medianMicros(Future<int> Function() once) async {
  final warm = Stopwatch()..start();
  while (warm.elapsed < _warmup) {
    await once();
  }
  final means = <double>[];
  for (var r = 0; r < _rounds; r++) {
    var count = 0;
    final sw = Stopwatch()..start();
    while (sw.elapsed < _roundBudget) {
      await once();
      count++;
    }
    sw.stop();
    means.add(sw.elapsedMicroseconds / count);
  }
  means.sort();
  return means[_rounds ~/ 2];
}

VLabel _label(int id, {String? text, VImage? image}) => VLabel()
  ..id = id
  ..seq = 1
  ..style = 0
  ..text = text
  ..image = image;

VImage _image(int id, Uint8List bytes) => VImage()
  ..id = id
  ..imageData = (VImageData()
    ..data = bytes
    ..width = 16
    ..height = 16
    ..depth = 32);

/// Stands in for encoded image bytes: what is measured is how they travel, not what they draw.
Uint8List _imageBytes(int n) {
  final out = Uint8List(n);
  for (var i = 0; i < n; i++) {
    out[i] = (i * 31 + 7) & 0xFF;
  }
  return out;
}

final Map<String, int> _typeNumbers = {
  for (final MapEntry(:key, :value) in wireTypeNames.entries) value: key,
};

/// A widget frame as Java writes it: an image record for each image the labels show, then a whole
/// record per label.
Uint8List _tape(List<VLabel> labels) {
  final records = <Uint8List>[];
  Uint8List record(int kind, void Function(WidgetWireWriter w) write) {
    final rest = Uint8List.fromList(WidgetWireWriter.encodeWith(write));
    final head = Uint8List.fromList(WidgetWireWriter.encodeWith((w) => w
      ..u8(kind)
      ..varint(rest.length)));
    return Uint8List.fromList([...head, ...rest]);
  }

  for (final label in labels) {
    final image = label.image;
    if (image != null) {
      records.add(record(wireImage, (w) {
        w.int64(image.id!);
        image.writeWire(w);
      }));
    }
  }
  var last = 0;
  for (final label in labels) {
    records.add(record(wireWhole, (w) {
      w
        ..varint(_typeNumbers['Label']! * 2 + 1)
        ..int64(label.id)
        ..varint(label.seq - last);
      last = label.seq;
      label.writeWire(w);
    }));
  }
  final count = ByteData(4)..setUint32(0, records.length, Endian.little);
  return ChannelHeader.frame(widgetWireChannel,
      Uint8List.fromList([...count.buffer.asUint8List(), for (final r in records) ...r]));
}

/// The comm with its socket left out: a sent frame is kept, a received one is delivered in place and
/// applied to a registry of its own.
class _LocalComm extends EquoCommBase {
  Uint8List? lastSent;
  final VRegistry _registry = VRegistry();
  Completer<void>? _applied;

  _LocalComm() {
    markOpen();
    onBytes(widgetWireChannel, (frame) {
      _registry.applyWire(frame);
      _registry.clear();
      _applied?.complete();
    });
  }

  @override
  void rawSend(Uint8List frame) => lastSent = frame;

  /// Completes once the next widget frame has been applied.
  Future<void> nextFrame() => (_applied = Completer<void>()).future;
}
