// A channel answered per kind of widget (EquoCommService.onWidgetOp) is reached by the numbers its
// frame carries: the kind, the channel and the widget's id, read where they are. These pin what that
// route keeps from the one by channel name it replaces: arrival order, frames held for a widget not
// listening yet, and kinds kept apart.

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireChannel;
import 'support/channels.dart' as channels;
import 'support/channels.dart' show NamedChannels;
import 'package:swtflutter/src/comm/widget_key.dart';

EquoCommBase get _comm => EquoCommService.commForTesting;

void _receive(String channel, Map<String, Object?> body) =>
    _comm.receiveBinary(channels.frame(channel, TaggedValues.encode(body)));

int _type(String swt) => WidgetKey.typeOf(swt);

void main() {
  final valueKey = TaggedValues.keyNumber('value');

  test('frames on numbered and named channels apply in the order they arrived', () async {
    final seen = <String>[];
    _comm.onWidgetOp(_type('Text'), WireChannel.keyVetoable, (id, frame, start, end) {
      seen.add('op:${TaggedValues.boolField(frame, start, valueKey)}');
      return true;
    });
    EquoCommService.commForTesting.onNamed('swt.evolve.timing.ping', (_) => seen.add('named'));

    _receive('Text/9101/key/vetoable', {'value': true});
    _receive('swt.evolve.timing.ping', {});
    _receive('Text/9101/key/vetoable', {'value': false});
    await Future<void>.delayed(Duration.zero);

    expect(seen, ['op:true', 'named', 'op:false']);
  });

  test('a frame for a widget that does not listen yet is applied once it does', () async {
    final listening = <int>{};
    final seen = <String>[];
    _comm.onWidgetOp(_type('Spinner'), WireChannel.keyVerdict, (id, frame, start, end) {
      if (!listening.contains(id)) return false;
      seen.add('verdict:$id');
      return true;
    });

    _receive('Spinner/9201/key/verdict', {'doit': true});
    _receive('Spinner/9201/key/verdict', {'doit': false});
    await Future<void>.delayed(Duration.zero);
    expect(seen, isEmpty);

    listening.add(9201);
    _comm.replayWidgetOps(9201);
    await Future<void>.delayed(Duration.zero);

    expect(seen, ['verdict:9201', 'verdict:9201']);
  });

  test('two kinds answering the same channel each get only their own frames', () async {
    final seen = <String>[];
    _comm.onWidgetOp(_type('Combo'), WireChannel.modifyVerdict, (id, frame, start, end) {
      seen.add('combo:$id');
      return true;
    });
    _comm.onWidgetOp(_type('Text'), WireChannel.modifyVerdict, (id, frame, start, end) {
      seen.add('text:$id');
      return true;
    });

    _receive('Text/9301/modify/verdict', {'doit': true});
    _receive('Combo/9301/modify/verdict', {'doit': true});
    await Future<void>.delayed(Duration.zero);

    expect(seen, ['text:9301', 'combo:9301']);
  });

  test('a frame in a batch reaches its handler with only its own payload', () async {
    final seen = <bool?>[];
    final payloads = <List<int>>[];
    _comm.onWidgetOp(_type('Table'), WireChannel.traverseVetoable, (id, frame, start, end) {
      seen.add(TaggedValues.boolField(frame, start, valueKey));
      payloads.add(frame.sublist(start, end));
      return true;
    });
    final entries = BytesBuilder();
    final sent = <List<int>>[];
    for (final value in [true, false]) {
      final body = TaggedValues.encode({'value': value});
      sent.add(body);
      final frame = channels.frame('Table/9401/traverse/vetoable', body);
      final header = ChannelHeader.skip(frame, 0);
      entries.add(Uint8List.sublistView(frame, 0, header));
      entries.addByte(frame.length - header);
      entries.add(Uint8List.sublistView(frame, header));
    }
    _comm.receiveBinary(channels.frame('swt.evolve.batch', entries.toBytes()));
    await Future<void>.delayed(Duration.zero);

    expect(seen, [true, false]);
    expect(payloads, sent, reason: 'a handler reads its entry in place, up to its end and no further');
  });

  test('more frames than the queue first holds still apply in order', () async {
    final seen = <int>[];
    _comm.onWidgetOp(_type('Tree'), WireChannel.keyVetoable, (id, frame, start, end) {
      seen.add(id);
      return true;
    });

    for (var id = 1; id <= 200; id++) {
      _receive('Tree/$id/key/vetoable', {'value': true});
    }
    await Future<void>.delayed(Duration.zero);

    expect(seen, [for (var id = 1; id <= 200; id++) id]);
  });

  test('frames held for a widget that is gone are let go', () async {
    final listening = <int>{};
    final seen = <int>[];
    _comm.onWidgetOp(_type('Spinner'), WireChannel.modifyVerdict, (id, frame, start, end) {
      if (!listening.contains(id)) return false;
      seen.add(id);
      return true;
    });

    _receive('Spinner/9301/modify/verdict', {'doit': true});
    await Future<void>.delayed(Duration.zero);
    _comm.forgetWidgetOps(9301);
    listening.add(9301);
    _comm.replayWidgetOps(9301);
    await Future<void>.delayed(Duration.zero);

    expect(seen, isEmpty, reason: 'nothing is held for a widget once it is gone');
  });

  test('frames are held for so many widgets at most, the oldest let go first', () async {
    final listening = <int>{};
    final seen = <int>[];
    _comm.onWidgetOp(_type('Spinner'), WireChannel.modifyVerdict, (id, frame, start, end) {
      if (!listening.contains(id)) return false;
      seen.add(id);
      return true;
    });

    const first = 100000;
    for (var i = 0; i <= EquoCommBase.maxHeldWidgets; i++) {
      _receive('Spinner/${first + i}/modify/verdict', {'doit': true});
    }
    await Future<void>.delayed(Duration.zero);
    listening.addAll([first, first + EquoCommBase.maxHeldWidgets]);
    _comm.replayWidgetOps(first);
    _comm.replayWidgetOps(first + EquoCommBase.maxHeldWidgets);
    await Future<void>.delayed(Duration.zero);

    expect(seen, [first + EquoCommBase.maxHeldWidgets], reason: 'the oldest widget held for was let go');
    for (var i = 1; i < EquoCommBase.maxHeldWidgets; i++) {
      _comm.forgetWidgetOps(first + i);
    }
  });
}
