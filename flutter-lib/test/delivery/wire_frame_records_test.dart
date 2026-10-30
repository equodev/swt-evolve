// One flush's binary frame, built here byte by byte the way Java's WidgetWire writes it: a record
// count, then each record's kind, length and header, and its properties up to the length's end.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/widgets.dart';

const int _labelType = 703;
const int _labelId = 900;

/// A record: its kind, its length, then what [write] writes, header and properties.
Uint8List _record(int kind, void Function(WidgetWireWriter w) write) {
  final rest = Uint8List.fromList(WidgetWireWriter.encodeWith(write));
  final head = Uint8List.fromList(WidgetWireWriter.encodeWith((w) => w
    ..u8(kind)
    ..varint(rest.length)));
  return Uint8List.fromList([...head, ...rest]);
}

/// A frame of [records]: their count as a little-endian word, then each one.
Uint8List _frame(List<Uint8List> records) {
  final count = ByteData(4)..setUint32(0, records.length, Endian.little);
  return Uint8List.fromList([...count.buffer.asUint8List(), for (final r in records) ...r]);
}

/// A widget described whole: its type by number, id, and write stamp as a delta on the frame's last.
Uint8List _whole(int stampDelta, void Function(WidgetWireWriter w) properties, {int id = _labelId}) =>
    _record(wireWhole, (w) {
      w
        ..varint(_labelType * 2 + 1)
        ..int32(id)
        ..varint(stampDelta);
      properties(w);
    });

/// A change to a widget: as a whole record, plus how far back its base stamp is.
Uint8List _change(int stampDelta, int baseBack, void Function(WidgetWireWriter w) properties) =>
    _record(wireChange, (w) {
      w
        ..varint(_labelType * 2 + 1)
        ..int32(_labelId)
        ..varint(stampDelta)
        ..varint(baseBack);
      properties(w);
    });

/// The ids the client asked Java to describe again.
List<String> _refreshesAsked() => [
      for (final f in EquoCommService.commForTesting.sentFrames)
        if (ChannelHeader.read(f)!.$1 == widgetRefreshChannel) _payloadText(f),
    ];

String _payloadText(Uint8List frame) {
  final body = frame.sublist(ChannelHeader.read(frame)!.$2);
  return TaggedValues.isTagged(body) ? '${TaggedValues.decode(body)}' : utf8.decode(body);
}

void main() {
  late VRegistry registry;

  setUp(() {
    deliveryGate.reset();
    registry = VRegistry();
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
  });

  tearDown(() {
    registry.clear();
    EquoCommBase.recordSentFrames = false;
    EquoCommService.commForTesting.clearSentFrames();
  });

  VLabel? held() => registry.valueOn('Label/$_labelId') as VLabel?;

  test('a whole record makes the widget it describes', () {
    registry.applyWire(_frame([
      _whole(10, (w) => w
        ..key(WireKey.k_Label_text)
        ..string('first')),
    ]));

    expect(held()?.text, 'first');
    expect(held()?.seq, 10);
  });

  test('a type spelled out once is named by its place after that', () {
    registry.applyWire(_frame([
      _record(wireWhole, (w) => w
        ..varint(0)
        ..string('Label')
        ..int32(_labelId)
        ..varint(4)
        ..key(WireKey.k_Label_text)
        ..string('spelled')),
      _record(wireWhole, (w) => w
        ..varint(2)
        ..int32(_labelId + 1)
        ..varint(1)
        ..key(WireKey.k_Label_text)
        ..string('by place')),
    ]));

    expect(held()?.text, 'spelled');
    expect((registry.valueOn('Label/${_labelId + 1}') as VLabel?)?.text, 'by place');
  });

  test('a change on the held stamp applies its properties and tells the widget', () {
    registry.applyWire(_frame([
      _whole(10, (w) => w
        ..key(WireKey.k_Label_text)
        ..string('first')),
    ]));
    final changes = <VChange>[];
    registry.watch('Label/$_labelId', changes.add);

    registry.applyWire(_frame([
      _change(11, 1, (w) => w
        ..key(WireKey.k_Label_text)
        ..string('second')),
    ]));

    expect(held()?.text, 'second');
    expect(changes.single.touches(WireKey.k_Label_text), isTrue);
  });

  test('a whole record for a widget already held is copied into the one object', () {
    registry.applyWire(_frame([
      _whole(10, (w) => w
        ..key(WireKey.k_Label_text)
        ..string('first')),
    ]));
    final first = held();

    registry.applyWire(_frame([
      _whole(12, (w) => w
        ..key(WireKey.k_Label_text)
        ..string('again')),
    ]));

    expect(held(), same(first), reason: 'everything that refers to the widget refers to that object');
    expect(first?.text, 'again');
  });

  test('an image record further down fills the image a widget named before it', () {
    registry.applyWire(_frame([
      _whole(10, (w) => w
        ..key(WireKey.k_Label_image)
        ..ref(4242)),
      _record(wireImage, (w) => w
        ..int32(4242)
        ..key(74)
        ..int32(16)
        ..key(70)
        ..int32(8)),
    ]));

    final image = held()!.image!;
    expect(image.id, 4242);
    expect([image.width, image.height], [16, 8]);
    expect(image.isReference, isFalse);
  });

  test('a holder record reaches its listener, and an older one is the past arriving late', () {
    final told = <int?>[];
    final stop = registry.onHolder<VDisplay>('Display', VDisplay.new, (id, d) => told.add(d.activeShellId));
    addTearDown(stop);
    Uint8List display(int stamp, int active) => _frame([
          _record(wireHolder, (w) => w
            ..varint(741 * 2 + 1)
            ..int32(1)
            ..varint(stamp)
            ..key(WireKey.k_Display_activeShellId)
            ..int64(active)),
        ]);

    registry.applyWire(display(20, 7));
    registry.applyWire(display(19, 8));

    expect(told, [7]);
  });

  test('a record that cannot be read costs only its widget, which is asked for whole', () {
    registry.applyWire(_frame([
      _whole(10, (w) => w
        ..key(9999)
        ..int32(1)),
      _whole(1, id: _labelId + 1, (w) => w
        ..key(WireKey.k_Label_text)
        ..string('next')),
    ]));

    expect(_refreshesAsked(), contains('$_labelId'));
    expect((registry.valueOn('Label/${_labelId + 1}') as VLabel?)?.text, 'next');
  });

  test('a widget named but neither held nor described is asked for, then what named it', () {
    registry.applyWire(_frame([
      _whole(10, (w) => w
        ..key(WireKey.k_Control_menu)
        ..ref(31337)),
    ]));

    expect(_refreshesAsked(), ['31337', '$_labelId']);
  });
}
