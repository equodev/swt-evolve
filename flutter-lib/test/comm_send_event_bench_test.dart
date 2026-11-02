// What sending a widget event costs on the Dart side, from the widget's sendEvent to the frame the
// transport writes: time, and bytes allocated. A widget sends by its type's and the channel's numbers
// (sendTo); the channel spelled out as `Type/id/Listener/Event` and parsed back by ChannelHeader is
// measured alongside, as is the event's writeWire alone.
//
// A measurement, printed rather than asserted. Bytes allocated need the VM service:
//   flutter test --enable-vmservice --tags bench test/comm_send_event_bench_test.dart

@Tags(['bench'])
library;

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames, wireTypeNames;

import 'support/heap_bytes.dart';
import 'support/channels.dart' as channels;
import 'support/channels.dart' show NamedChannels;

const int _events = 200000;
const int _allocEvents = 4000;
const int _rounds = 5;

class _NullComm extends EquoCommBase {
  _NullComm() {
    markOpen();
  }

  int sent = 0;

  @override
  void rawSend(Uint8List frame) => sent += frame.length;

  // As a transport that copies the frame before the call returns, as a browser socket does.
  // ignore: override_on_non_overriding_member
  @override
  void rawSendRange(Uint8List buffer, int length) => sent += length;
}

final int _button = wireTypeNames.entries.firstWhere((e) => e.value == 'Button').key;
final int _selection = wireChannelNames.entries.firstWhere((e) => e.value == 'Selection/Selection').key;

/// The header of `Type/id/name`, written from its numbers, then [body], in one buffer.
Uint8List _numbered(int type, int id, int name, Uint8List body) {
  final out = Uint8List(15 + body.length);
  var n = 0;
  void varint(int v) {
    while (v >= 0x80) {
      out[n++] = (v % 0x80) + 0x80;
      v = v ~/ 0x80;
    }
    out[n++] = v;
  }

  varint(name * 2 + 1);
  varint(type);
  varint(id >= 0 ? id * 2 : -id * 2 - 1);
  out.setRange(n, n + body.length, body);
  return Uint8List.sublistView(out, 0, n + body.length);
}

void main() {
  test('a widget event, spelled out and parsed back, against written from its numbers', () async {
    final comm = _NullComm();
    final heap = await NewSpace.connect();
    final event = VEvent()
      ..x = 10
      ..y = 4
      ..stateMask = 0;
    const swt = 'Button';
    const ev = 'Selection/Selection';
    final ids = [for (var i = 0; i < 200; i++) 700001 + i * 7];

    void spelled(int n) {
      for (var i = 0; i < n; i++) {
        final id = ids[i % ids.length];
        EquoCommBase.observeSend('$swt/$id/$ev', event);
        comm.sendNamed('$swt/$id/$ev', WidgetWireWriter.encode(event));
      }
    }

    // The same frame and the same write as [numbered]: only how the header is made differs.
    void spelledDirect(int n) {
      for (var i = 0; i < n; i++) {
        final id = ids[i % ids.length];
        EquoCommBase.observeSend('$swt/$id/$ev', event);
        comm.rawSend(channels.frame('$swt/$id/$ev', WidgetWireWriter.encode(event)));
      }
    }

    void numbered(int n) {
      for (var i = 0; i < n; i++) {
        final id = ids[i % ids.length];
        comm.rawSend(_numbered(_button, id, _selection, WidgetWireWriter.encode(event)));
      }
    }

    // What a widget's sendEvent does now.
    void sendTo(int n) {
      for (var i = 0; i < n; i++) {
        comm.sendTo(_button, ids[i % ids.length], _selection, event);
      }
    }

    // The event's fields alone, into the shared writer.
    var written = 0;
    void writeWire(int n) {
      for (var i = 0; i < n; i++) {
        written += WidgetWireWriter.encode(event).length;
      }
    }

    // Both write the same bytes: the alternative changes the cost, not the frame.
    final a = channels.frame('$swt/${ids[0]}/$ev', Uint8List.fromList(WidgetWireWriter.encode(event)));
    final b = _numbered(_button, ids[0], _selection, Uint8List.fromList(WidgetWireWriter.encode(event)));
    expect(b, a);

    Future<void> report(String name, void Function(int) run) async {
      run(20000);
      final times = <double>[];
      for (var r = 0; r < _rounds; r++) {
        final sw = Stopwatch()..start();
        run(_events);
        times.add(sw.elapsedMicroseconds * 1000 / _events);
      }
      times.sort();
      double? bytes;
      if (heap != null) {
        final samples = <double>[];
        for (var r = 0; r < _rounds; r++) {
          final quiet = await heap.bytesOf(() async {});
          final used = await heap.bytesOf(() async => run(_allocEvents));
          if (quiet != null && used != null) samples.add((used - quiet) / _allocEvents);
        }
        samples.sort();
        if (samples.isNotEmpty) bytes = samples[samples.length ~/ 2];
      }
      // ignore: avoid_print
      print('[send-event-bench] $name: ${times[_rounds ~/ 2].toStringAsFixed(0)} ns'
          '${bytes == null ? '' : ', ${bytes.toStringAsFixed(1)} B'} per event');
    }

    await report('spelled out, parsed back, through sendBytes', spelled);
    await report('spelled out, parsed back, written directly', spelledDirect);
    await report('written from numbers', numbered);
    await report('sendTo', sendTo);
    await report('VEvent writeWire', writeWire);
    expect(written, greaterThan(0));
    // What sendBytes adds around the write: the Future it returns, measured in bursts with the event
    // loop turning between them, so its microtask runs as it would in the app.
    Future<void> bursts(String name, void Function(int) run) async {
      Future<void> go(int n) async {
        for (var done = 0; done < n; done += burst) {
          run(burst);
          await Future<void>.delayed(Duration.zero);
        }
      }

      await go(20000);
      final times = <double>[];
      for (var r = 0; r < _rounds; r++) {
        final sw = Stopwatch()..start();
        await go(_events);
        times.add(sw.elapsedMicroseconds * 1000 / _events);
      }
      times.sort();
      double? bytes;
      if (heap != null) {
        final samples = <double>[];
        for (var r = 0; r < _rounds; r++) {
          final quiet = await heap.bytesOf(() => go(0));
          final used = await heap.bytesOf(() => go(_allocEvents));
          if (quiet != null && used != null) samples.add((used - quiet) / _allocEvents);
        }
        samples.sort();
        if (samples.isNotEmpty) bytes = samples[samples.length ~/ 2];
      }
      // ignore: avoid_print
      print('[send-event-bench] $name, in bursts of $burst: ${times[_rounds ~/ 2].toStringAsFixed(0)} ns'
          '${bytes == null ? '' : ', ${bytes.toStringAsFixed(1)} B'} per event');
    }

    await bursts('spelled out, through sendBytes', spelled);
    await bursts('spelled out, written directly', spelledDirect);
    await bursts('written from numbers', numbered);
    await bursts('sendTo', sendTo);
  });
}
