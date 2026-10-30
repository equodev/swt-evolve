// What Java's GcWire and GCImageDrawer send, built from the value classes the client reads them
// back into, for tests that feed a drawer the way Java does.

import 'dart:typed_data';

import 'package:swtflutter/src/comm/channel_header.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/gc_wire.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/color.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/gcdrawer.dart' show gcOpSignatures;
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
import 'fields_only.dart';
import 'channels.dart' as channels;
import 'channels.dart' show NamedChannels;

/// A frame as it arrives: [channel]'s header, then [body].
Uint8List frame(String channel, List<int> body) => channels.frame(channel, Uint8List.fromList(body));

/// Delivers [body] on [channel], as Java sends it.
void receive(String channel, List<int> body) =>
    EquoCommService.commForTesting.receiveBinary(frame(channel, body));

/// A payload of maps, lists and scalars, as tagged values.
Uint8List tagged(Object? value) => TaggedValues.encode(value);

/// A gcDispose body as Java writes it (`GcWire.Dispose`), from the fields a test names:
/// `fullRepaint` (default true), `damage` (`x`, `y`, `width`, `height`), `ref`, `pixels`, `retain`.
Uint8List gcDisposeOf(Map<String, dynamic> fields) {
  final damage = fields['damage'] as Map?;
  return Uint8List.fromList(WidgetWireWriter.encodeWith((out) {
    out.u8((fields['fullRepaint'] == false ? 0 : 1) |
        (damage != null ? 2 : 0) |
        (fields['pixels'] == true ? 4 : 0) |
        (fields['retain'] == true ? 8 : 0));
    out.varint((fields['ref'] as int?) ?? 0);
    if (damage != null) {
      for (final key in const ['x', 'y', 'width', 'height']) {
        out.int32((damage[key] as num).toInt());
      }
    }
  }));
}

/// [image], described the way Java describes an image ahead of the GC frame that names it: an image
/// record of a widget frame. [image] must carry an id.
void describeImage(VImage image) {
  final rest = Uint8List.fromList(WidgetWireWriter.encodeWith((w) {
    w.int64(image.id!);
    image.writeWire(w);
  }));
  final head = Uint8List.fromList(WidgetWireWriter.encodeWith((w) => w
    ..u8(wireImage)
    ..varint(rest.length)));
  final count = ByteData(4)..setUint32(0, 1, Endian.little);
  VRegistry.instance.applyWire(Uint8List.fromList([...count.buffer.asUint8List(), ...head, ...rest]));
}

/// Image ids for test images that have none: Java names every image it sends by one.
int _nextImageId = 1 << 29;

/// [image], given an id when it has none and described ahead as [describeImage] does: what an op
/// that names it needs held.
VImage heldImage(VImage image) {
  image.id ??= _nextImageId++;
  describeImage(image);
  return image;
}

/// A GC frame of the one op [name], its arguments [args]; see [GcFrame.opNamed].
Uint8List gcOp(String name, List<Object?> args) => GcFrame().opNamed(name, args).bytes;

/// An image drawer's `imageInit`: the Image's properties as the widget wire writes an object's fields.
Uint8List imageInit(VImage image) => Uint8List.fromList(WidgetWireWriter.encodeWith((w) {
      image.writeWire(w);
      w.u8(0);
    }));

/// A colour as Java sends one.
VColor colourOf(int r, int g, int b, [int a = 255]) => VColor()
  ..red = r
  ..green = g
  ..blue = b
  ..alpha = a;

/// A rectangle as Java sends one.
VRectangle rectOf(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

/// A GC's binary frame (`GC/<id>/b`), records added in the order Java writes them.
class GcFrame {
  final BytesBuilder _out = BytesBuilder();

  /// The whole state: what [state] leaves unset is at its default.
  GcFrame whole(VGC state) => _state(GcWireReader.whole, state, null);

  /// A change to the state held: only the properties of [state] numbered [keys].
  GcFrame change(VGC state, Set<int> keys) => _state(GcWireReader.state, state, keys);

  /// An op both sides number, as Java writes it: by that number the first time the frame uses it,
  /// by its place in the frame after; then its arguments as [args] writes them.
  GcFrame numbered(String name, void Function(GcFrame) args) {
    final local = _local[name];
    if (local != null) {
      _varint(GcWireReader.opFirst + local);
    } else {
      _local[name] = _local.length;
      _varint(GcWireReader.opNumbered);
      _varint(wireChannelNames.entries.firstWhere((e) => e.value == name).key);
    }
    args(this);
    return this;
  }

  final Map<String, int> _local = {};

  /// The op both sides number [name] by, its arguments [args] in the order its signature lists them:
  /// an int (`i`), a list of ints (`I`), a bool (`b`), a string (`s`), a float (`f`), a double
  /// (`d`), or a value as the widget wire writes it (`w`): an image by id, an object by its fields.
  GcFrame opNamed(String name, List<Object?> args) {
    final number = wireChannelNames.entries.firstWhere((e) => e.value == name).key;
    final signature = gcOpSignatures[number]!;
    if (signature.length != args.length) {
      throw ArgumentError('$name takes ${signature.length} arguments, not ${args.length}');
    }
    _varint(GcWireReader.opNumbered);
    _varint(number);
    for (var k = 0; k < signature.length; k++) {
      final v = args[k];
      switch (signature[k]) {
        case 'i':
          i(v as int);
        case 'I':
          final list = v as List<int>?;
          if (list == null) {
            _varint(0);
          } else {
            _varint(list.length + 1);
            list.forEach(i);
          }
        case 'b':
          b(v as bool);
        case 's':
          s(v as String);
        case 'f':
          f((v as num).toDouble());
        case 'd':
          d((v as num).toDouble());
        default:
          w((w) {
            if (v is VImage?) {
              w.ref(v?.id);
            } else {
              w.object((v as WireWritable?)?.writeWire);
            }
          });
      }
    }
    return this;
  }

  /// A channel and its payload, as tagged values; a gcDispose's as Java writes one ([gcDisposeOf]).
  GcFrame escape(String channel, Object? payload) {
    _varint(GcWireReader.escape);
    _out.add(channels.frame(channel, Uint8List(0)));
    _lengthPrefixed(channel.endsWith('/gcDispose') ? gcDisposeOf(payload as Map<String, dynamic>) : tagged(payload));
    return this;
  }

  void i(int v) => _varint(v >= 0 ? v * 2 : -v * 2 - 1);

  void b(bool v) => _out.addByte(v ? 1 : 0);

  /// A string as the widget wire writes one: Latin-1 bytes or UTF-16 code units.
  void s(String v) => _out.add(WidgetWireWriter.encodeWith((w) => w.string(v)));

  void f(double v) => _out.add((ByteData(4)..setFloat32(0, v, Endian.little)).buffer.asUint8List());

  void d(double v) => _out.add((ByteData(8)..setFloat64(0, v, Endian.little)).buffer.asUint8List());

  /// A `w` argument: the value [write] writes as the widget wire encodes its type.
  void w(void Function(WidgetWireWriter) write) =>
      _lengthPrefixed(Uint8List.fromList(WidgetWireWriter.encodeWith(write)));

  Uint8List get bytes => _out.toBytes();

  GcFrame _state(int tag, VGC state, Set<int>? keys) {
    _varint(tag);
    _lengthPrefixed(Uint8List.fromList([...fieldsOnly(state, keys), 0]));
    return this;
  }

  void _lengthPrefixed(Uint8List bytes) {
    _varint(bytes.length + 1);
    _out.add(bytes);
  }

  void _varint(int v) {
    while (v >= 0x80) {
      _out.addByte((v & 0x7F) | 0x80);
      v >>= 7;
    }
    _out.addByte(v);
  }
}

/// A GC frame of one state record: a change naming [changed] when given, else the whole state.
Uint8List gcState(VGC state, {Set<int>? changed}) =>
    (changed != null ? GcFrame().change(state, changed) : GcFrame().whole(state)).bytes;
