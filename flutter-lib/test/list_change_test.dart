// A list of widgets changed by a change to it.
//
// Java writes a composite's children, a table's rows and the other lists of widgets a value holds as
// the change to the list the client holds when that is shorter than the list: where the change
// starts, how many it removes there, what it inserts, and the checksum of the list it makes. The
// frames are written here by hand, byte by byte as Java's WidgetWire writes them.

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/widgets.dart';

/// Java's WidgetWire.LIST_CHANGE.
const _listChange = (1 << 31) + 1;

/// Bytes as Java's WidgetWire writes them.
class _Out {
  final BytesBuilder _b = BytesBuilder();
  int _lastKey = 0;

  void varint(int v) {
    while (v >= 0x80) {
      _b.addByte((v & 0x7F) | 0x80);
      v >>= 7;
    }
    _b.addByte(v);
  }

  void zigzag(int v) => varint(v >= 0 ? v * 2 : -v * 2 - 1);

  void key(int key) {
    zigzag(key - _lastKey);
    _lastKey = key;
  }

  void string(String s) {
    varint(s.length * 2 + 1);
    _b.add(s.codeUnits);
  }

  void ref(int id) => varint((id >= 0 ? id * 2 : -id * 2 - 1) + 1);

  void refs(List<int> ids) {
    varint(ids.length + 1);
    ids.forEach(ref);
  }

  /// The change turning a list into [result]: [removed] at [from] replaced by [inserted], each written
  /// by [entry].
  void change(List<int> result, int from, int removed, List<int> inserted, {void Function(int id)? entry, int? checksum}) {
    varint(_listChange);
    varint(checksum ?? _checksum(result));
    varint(from);
    varint(removed);
    varint(inserted.length);
    inserted.forEach(entry ?? ref);
  }

  Uint8List bytes() => _b.toBytes();
}

/// The checksum Java writes with a list change, over the ids of the list it makes.
int _checksum(List<int> ids) {
  var hash = ids.length;
  for (final id in ids) {
    hash = (hash * 31 + id) % 0x40000000;
  }
  return hash;
}

/// A record: kind, length, type, id, stamp step, base step for a change, then [fields].
Uint8List _record(int kind, int type, int id, int step, void Function(_Out o) fields, {int? baseStep}) {
  final body = _Out()
    ..varint(type * 2 + 1)
    ..zigzag(id)
    ..varint(step);
  if (baseStep != null) body.varint(baseStep);
  fields(body);
  final bytes = body.bytes();
  return (_Out()
        ..varint(kind)
        ..varint(bytes.length)
        .._b.add(bytes))
      .bytes();
}

Uint8List _frame(List<Uint8List> records) {
  final out = BytesBuilder();
  out.add((ByteData(4)..setUint32(0, records.length, Endian.little)).buffer.asUint8List());
  records.forEach(out.add);
  return out.toBytes();
}

Uint8List _label(int id, int step) => _record(wireWhole, WireType.label, id, step, (o) => o
  ..key(WireKey.k_Label_text)
  ..string('label $id'));

void main() {
  const composite = 200;
  final asked = <String>[];

  setUp(() {
    deliveryGate.reset();
    asked.clear();
    EquoCommBase.sendObserverForTesting = (name, payload) {
      if (name == 'swt.evolve.widget.refresh') asked.add('$payload');
    };
  });

  tearDown(() => EquoCommBase.sendObserverForTesting = null);

  List<int> children(VRegistry registry) =>
      [for (final c in (registry.valueOn('Composite/$composite')! as VComposite).children ?? const []) c.id];

  VRegistry withChildren(List<int> ids) => VRegistry()
    ..applyWire(_frame([
      _record(wireWhole, WireType.composite, composite, 10, (o) => o
        ..key(WireKey.k_Composite_children)
        ..refs(ids)),
      for (final id in ids) _label(id, 1),
    ]));

  test('a child added and a child removed change the list held', () {
    final registry = withChildren([201, 202, 203]);

    registry.applyWire(_frame([
      _record(wireChange, WireType.composite, composite, 20, baseStep: 20 - 10, (o) => o
        ..key(WireKey.k_Composite_children)
        ..change([201, 202, 203, 204], 3, 0, [204])),
      _label(204, 1),
    ]));
    expect(children(registry), [201, 202, 203, 204]);

    registry.applyWire(_frame([
      _record(wireChange, WireType.composite, composite, 30, baseStep: 30 - 20, (o) => o
        ..key(WireKey.k_Composite_children)
        ..change([201, 203, 204], 1, 1, [])),
    ]));
    expect(children(registry), [201, 203, 204]);
    expect(asked, isEmpty);
    expect(deliveryGate.recoveries, 0);
  });

  test('a change made against another list asks for the widget whole and keeps the list held', () {
    final registry = withChildren([201, 202, 203]);

    // Made against [201, 202]: it fits the list held, but does not make the list Java has.
    registry.applyWire(_frame([
      _record(wireChange, WireType.composite, composite, 20, baseStep: 20 - 10, (o) => o
        ..key(WireKey.k_Composite_children)
        ..change([201, 202, 204], 2, 0, [204])),
      _label(204, 1),
    ]));
    expect(asked, ['$composite']);
    expect(children(registry), [201, 202, 203]);

    asked.clear();
    // One that runs past the end of the list held is not applied either.
    registry.applyWire(_frame([
      _record(wireChange, WireType.composite, composite, 30, baseStep: 30 - 10, (o) => o
        ..key(WireKey.k_Composite_children)
        ..change([201], 1, 5, [])),
    ]));
    expect(asked, ['$composite']);
    expect(children(registry), [201, 202, 203]);
  });

  test('a row added by a change to the rows is described in place', () {
    const table = 300;
    final registry = VRegistry()
      ..applyWire(_frame([
        _record(wireWhole, WireType.table, table, 10, (o) => o
          ..key(WireKey.k_Table_items)
          ..refs([])),
      ]));
    void row(_Out o, int id) {
      o.varint((id * 2) * 2 + 2);
      o.varint(1);
      final outer = o._lastKey;
      o._lastKey = 0;
      o
        ..key(WireKey.k_TableItem_texts)
        ..varint(2)
        ..string('row $id');
      o.varint(0);
      o._lastKey = outer;
    }

    registry.applyWire(_frame([
      _record(wireChange, WireType.table, table, 20, baseStep: 10, (o) {
        o.key(WireKey.k_Table_items);
        o.change([301, 302], 0, 0, [301, 302], entry: (id) => row(o, id));
      }),
    ]));

    final rows = (registry.valueOn('Table/$table')! as VTable).items!;
    expect([for (final r in rows) r.id], [301, 302]);
    expect((registry.valueOn('TableItem/302')! as VTableItem).texts, ['row 302']);
    expect(asked, isEmpty);
  });

  test('the checksum is the one Java computes', () {
    expect(wireListChecksum([for (final id in [5, -7, 2147483647, -2147483648]) VComposite()..id = id]),
        _checksum([5, -7, 2147483647, -2147483648]));
  });
}
