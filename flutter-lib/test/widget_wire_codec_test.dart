// The widget wire carries no types: each side reads a value knowing from its property what it is.
// So the two ends agree on every encoding byte for byte, the client's writer and reader with each
// other, and the reader with what Java's WidgetWire writes, built here by hand where the client
// never writes that form.

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/widget_key.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireChannel, WireType;
import 'support/channels.dart' as channels;
import 'support/channels.dart' show NamedChannels;

/// Reads the bytes [write] wrote.
WidgetWireReader _readBack(void Function(WidgetWireWriter w) write) =>
    WidgetWireReader(Uint8List.fromList(WidgetWireWriter.encodeWith(write)));

/// An object of three properties, numbered as a generated value class numbers its own.
class _Probe implements WireReadable, WireWritable {
  int? count;
  String? name;
  _Probe? child;

  @override
  bool readWire(int key, WidgetWireReader r) {
    switch (key) {
      case 3:
        count = r.intBoxed();
        return true;
      case 4:
        name = r.string();
        return true;
      case 9:
        child = r.object(_Probe.new);
        return true;
    }
    return false;
  }

  @override
  void writeWire(WidgetWireWriter w) {
    if (count != null) {
      w.key(3);
      w.intBoxed(count!);
    }
    if (name != null) {
      w.key(4);
      w.string(name!);
    }
    if (child != null) {
      w.key(9);
      w.object(child!.writeWire);
    }
  }
}

void main() {
  test('numbers read back as written, the large and the negative included', () {
    final r = _readBack((w) => w
      ..varint(0)
      ..varint(300)
      ..varint(1 << 40)
      ..int32(-1)
      ..int64(-(1 << 40))
      ..float32(1.5)
      ..float64(-0.1)
      ..u8(255));

    expect(r.varint(), 0);
    expect(r.varint(), 300);
    expect(r.varint(), 1 << 40, reason: 'read by arithmetic, so the same on the web');
    expect(r.zigzag(), -1);
    expect(r.zigzag(), -(1 << 40));
    expect(r.float32(), 1.5);
    expect(r.float64(), -0.1);
    expect(r.u8(), 255);
    expect(r.hasMore, isFalse);
  });

  test('a boxed value is a presence byte and the value; absent reads as null', () {
    final r = _readBack((w) => w
      ..boolBoxed(true)
      ..boolBoxed(false)
      ..u8(0)
      ..intBoxed(-7)
      ..longBoxed(1 << 33)
      ..u8(0)
      ..floatBoxed(2.25)
      ..doubleBoxed(3.5)
      ..u8(0)
      ..boolean(true));

    expect(r.boolBoxed(), isTrue);
    expect(r.boolBoxed(), isFalse);
    expect(r.boolBoxed(), isNull);
    expect(r.intBoxed(), -7);
    expect(r.longBoxed(), 1 << 33);
    expect(r.intBoxed(), isNull);
    expect(r.floatBoxed(), 2.25);
    expect(r.doubleBoxed(), 3.5);
    expect(r.doubleBoxed(), isNull);
    expect(r.boolean(), isTrue);
  });

  test('a string is ASCII when it fits and UTF-16 otherwise, and null is a 0', () {
    final long = 'line of plain text\n' * 40;
    final r = _readBack((w) => w
      ..string('')
      ..string(long)
      ..string('plain ñ')
      ..string('€ 日本')
      ..u8(0)
      ..strings(['a', null, 'ü€']));

    expect(r.string(), '');
    expect(r.string(), long);
    expect(r.string(), 'plain ñ');
    expect(r.string(), '€ 日本');
    expect(r.string(), isNull);
    expect(r.strings(), ['a', null, 'ü€']);
  });

  test('lists and byte arrays are a length plus one, null a 0', () {
    final r = _readBack((w) => w
      ..ints([0, -1, 1 << 31, -(1 << 31)])
      ..longs([1 << 40])
      ..chars([65, 0x20AC])
      ..bytes([1, 2, 255])
      ..bytes([])
      ..bytes(null)
      ..varint(0));

    expect(r.ints(), [0, -1, 1 << 31, -(1 << 31)]);
    expect(r.longs(), [1 << 40]);
    expect(r.chars(), [65, 0x20AC]);
    expect(r.bytes(), Uint8List.fromList([1, 2, 255]));
    expect(r.bytes(), Uint8List(0));
    expect(r.bytes(), isNull);
    expect(r.ints(), isNull);
  });

  test('a point and a rectangle are present then their numbers', () {
    final r = _readBack((w) => w
      ..point(-3, 4)
      ..rect(1, -2, 300, 40)
      ..present(false)
      ..present(false));

    final point = r.point()!;
    expect([point.x, point.y], [-3, 4]);
    final rect = r.rect()!;
    expect([rect.x, rect.y, rect.width, rect.height], [1, -2, 300, 40]);
    expect(r.point(), isNull);
    expect(r.rect(), isNull);
  });

  test('a colour is present then one ARGB word', () {
    final r = _readBack((w) => w
      ..color(0x80FF2010)
      ..color(null));

    final color = r.color()!;
    expect([color.alpha, color.red, color.green, color.blue], [0x80, 0xFF, 0x20, 0x10]);
    expect(r.color(), isNull);
  });

  test('an object is its fields by key delta, closed by a 0, nested ones too', () {
    final probe = _Probe()
      ..count = 12
      ..name = 'outer'
      ..child = (_Probe()..name = 'inner');
    final read = WidgetWireReader(Uint8List.fromList(WidgetWireWriter.encode(probe))).fields(_Probe());

    expect(read.count, 12);
    expect(read.name, 'outer');
    expect(read.child?.name, 'inner');
    expect(read.child?.count, isNull);
  });

  test('a property number the value does not know abandons the read', () {
    final r = _readBack((w) => w
      ..key(5)
      ..intBoxed(1)
      ..u8(0));

    expect(() => r.fields(_Probe()), throwsA(isA<WireUnknownProperty>()
        .having((e) => e.key, 'key', 5)
        .having((e) => e.toString(), 'message', contains('has no property 5'))));
  });

  test('a widget named by id resolves to the one held, and one not held is noted', () {
    final held = VLabel()..id = 70;
    final r = _readBack((w) => w
      ..ref(70)
      ..ref(-71)
      ..ref(null)
      ..varint(4)
      ..ref(70)
      ..ref(72)
      ..ref(null))
      ..resolve = (id, _) => id == 70 ? held : null;

    expect(r.ref<VWidget>(), same(held));
    expect(r.ref<VWidget>(), isNull);
    expect(r.ref<VWidget>(), isNull);
    expect(r.refs<VWidget>(), [held]);
    expect(r.unresolved, [-71, 72], reason: 'what the client lacks is asked for by the caller');
  });

  test("the forms only Java writes: RGB, float and double lists, maps, typed lists", () {
    final bytes = BytesBuilder()
      ..add([1, 10, 20, 30]) // rgb: present, r, g, b
      ..add([0]) // rgb absent
      ..add([3]) // floats: 2 + 1
      ..add((ByteData(8)
            ..setFloat32(0, 0.5, Endian.little)
            ..setFloat32(4, -2, Endian.little))
          .buffer
          .asUint8List())
      ..add([2]) // doubles: 1 + 1
      ..add((ByteData(8)..setFloat64(0, 1e10, Endian.little)).buffer.asUint8List())
      ..add([0]) // floats null
      ..add([2, 3, 0x6B, 3, 0x76]) // stringMap: one entry, "k" -> "v"
      ..add([3, 3, 0x61, 2, 3, 0x62, 1]) // boolMap: "a" -> true, "b" -> false
      ..add([0, 0]) // both maps null
      ..add([3, 2, 0]); // a list of two boolBoxed: true, null
    final r = WidgetWireReader(bytes.toBytes());

    final rgb = r.rgb()!;
    expect([rgb.red, rgb.green, rgb.blue], [10, 20, 30]);
    expect(r.rgb(), isNull);
    expect(r.floats(), [0.5, -2]);
    expect(r.doubles(), [1e10]);
    expect(r.floats(), isNull);
    expect(r.stringMap(), {'k': 'v'});
    expect(r.boolMap(), {'a': true, 'b': false});
    expect(r.stringMap(), isNull);
    expect(r.boolMap(), isNull);
    expect(r.list((r) => r.boolBoxed()), [true, null]);
  });

  test('an image named by id is the same stand-in wherever the frame names it', () {
    // An image id is written as a widget's is.
    final r = _readBack((w) => w
      ..ref(501)
      ..ref(501)
      ..ref(null));

    final first = r.image()!;
    expect(first.id, 501);
    expect(first.isReference, isTrue);
    expect(r.image(), same(first), reason: 'its record further down fills the one stand-in');
    expect(r.image(), isNull);
  });

  test('sendTo and sendOn frame their message on its channel, from its numbers', () {
    EquoCommBase.recordSentFrames = true;
    addTearDown(() {
      EquoCommBase.recordSentFrames = false;
      EquoCommService.commForTesting.clearSentFrames();
    });
    EquoCommService.commForTesting.clearSentFrames();

    EquoCommService.sendTo(WireType.shell, 42, WireChannel.selectionSelection, _Probe()..count = 5);
    EquoCommService.sendOn(WireChannel.swtEvolveUrlOpen, 'añ');

    final sent = EquoCommService.commForTesting.sentFrames;
    expect(sent, hasLength(2));
    final (wireChannel, wireStart) = ChannelHeader.read(sent[0])!;
    expect(wireChannel, 'Shell/42/Selection/Selection');
    expect(WidgetWireReader(Uint8List.sublistView(sent[0], wireStart)).fields(_Probe()).count, 5);
    final (textChannel, textStart) = ChannelHeader.read(sent[1])!;
    expect(textChannel, 'swt.evolve.url.open');
    expect(sent[1].sublist(textStart), [6, 0x61, 0, 0xF1, 0],
        reason: 'a string as the widget wire writes one: past ASCII, two UTF-16 code units, copied, not encoded');
  });

  test('a type or channel without a wire number is refused where it is named, never sent spelled out', () {
    expect(() => WidgetKey.typeOf('NoSuchType'), throwsArgumentError);
    expect(() => channels.frame('NoSuchType/7/Selection/Selection', Uint8List(0)), throwsArgumentError);
    expect(() => channels.frame('Shell/7/no.such.channel', Uint8List(0)), throwsArgumentError);
    expect(() => channels.frame('no.such.channel', Uint8List(0)), throwsArgumentError);
    expect(() => EquoCommService.commForTesting.onNamed('no.such.channel', (_) {}), throwsArgumentError);
    expect(ChannelHeader.read(Uint8List.fromList([0, 1, 0x61])), isNull,
        reason: 'a header with no channel number is not one');
  });
}
