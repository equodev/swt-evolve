// What a message costs on the Dart side of the comm, at growing payload sizes.
//
// A measurement, printed rather than asserted, for comparing the comm layer before and after a
// change. Each scenario runs the production code path with the socket left out, so the time is
// the comm layer's own. The slope between the two largest sizes is the part that grows with the
// payload, which is where a copy shows.

@Tags(['bench'])
library;

import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/widgets.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

const List<int> _sizes = [64 << 10, 256 << 10, 1 << 20];
const int _rounds = 5;
const Duration _warmup = Duration(milliseconds: 100);
const Duration _roundBudget = Duration(milliseconds: 150);

void main() {
  test('MEASURE: time per message by payload size, Dart side of the comm', () async {
    final comm = _LocalComm();

    await _report('send JSON (D->J)', (chars) async {
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
      final frame = _frame('Label/1', _jsonBytes(_label(1, text: 'y' * chars)));
      return () async {
        final handled = comm.expect(['Label/1']);
        comm.receiveBinary(frame);
        await handled;
        return frame.length;
      };
    });

    await _report('receive batch of 2 (J->D)', (chars) async {
      final half = chars ~/ 2;
      final frame = _frame(EquoCommBase.batchEvent, _jsonBytes([
        ['Label/2', _label(2, text: 'y' * half)],
        ['Label/3', _label(3, text: 'z' * half)],
      ]));
      return () async {
        final handled = comm.expect(['Label/2', 'Label/3']);
        comm.receiveBinary(frame);
        await handled;
        return frame.length;
      };
    });

    await _report('receive widget image (J->D)', (imageBytes) async {
      final json = _label(4, image: _imageBytes(imageBytes));
      expect((mapWidgetValue(json) as VLabel).image?.imageData?.data?.length, imageBytes);
      final frame = _frame('Label/4', _jsonBytes(json));
      return () async {
        final handled = comm.expect(['Label/4']);
        comm.receiveBinary(frame);
        await handled;
        return frame.length;
      };
    });

    await _report('build widget image, per build (size = image bytes)', (imageBytes) async {
      final image = (mapWidgetValue(_label(5, image: _imageBytes(imageBytes))) as VLabel).image!;
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

Map<String, dynamic> _label(int id, {String? text, Uint8List? image}) => {
      'swt': 'Label',
      'id': id,
      'style': 0,
      if (text != null) 'text': text,
      if (image != null)
        'image': {
          'imageData': {'data': base64Encode(image), 'width': 16, 'height': 16, 'depth': 32},
        },
    };

/// Stands in for encoded image bytes: what is measured is how they travel, not what they draw.
Uint8List _imageBytes(int n) {
  final out = Uint8List(n);
  for (var i = 0; i < n; i++) {
    out[i] = (i * 31 + 7) & 0xFF;
  }
  return out;
}

Uint8List _jsonBytes(Object value) => utf8.encode(jsonEncode(value));

/// A frame as the Java side writes it: `[name length, 2 bytes BE][name UTF-8][payload]`.
Uint8List _frame(String name, Uint8List body) {
  final nameBytes = utf8.encode(name);
  final out = Uint8List(2 + nameBytes.length + body.length);
  out[0] = (nameBytes.length >> 8) & 0xFF;
  out[1] = nameBytes.length & 0xFF;
  out.setRange(2, 2 + nameBytes.length, nameBytes);
  out.setRange(2 + nameBytes.length, out.length, body);
  return out;
}

/// The comm with its socket left out: a sent frame is kept, a received one is delivered in place.
class _LocalComm extends EquoCommBase {
  Uint8List? lastSent;

  _LocalComm() {
    markOpen();
  }

  @override
  void rawSend(Uint8List frame) => lastSent = frame;

  /// Completes once every one of [channels] has been handled, decoded as a widget value.
  Future<void> expect(List<String> channels) {
    final waiting = channels.toSet();
    final pending = Completer<void>();
    for (final channel in channels) {
      on(channel, (payload) {
        mapWidgetValue(payload as Map<String, dynamic>);
        waiting.remove(channel);
        if (waiting.isEmpty && !pending.isCompleted) pending.complete();
      });
    }
    return pending.future;
  }
}
