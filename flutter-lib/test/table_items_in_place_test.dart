// Rows a list of items describes in place.
//
// Java describes a table's or a tree's new item inside the list that names it rather than by a
// record of its own after the list. Both forms are written here by hand, byte by byte as Java's
// WidgetWire writes them, and the client has to come out of either holding the same thing: the same
// values under the same ids, stamped the same, held by the same widgets.

import 'dart:async';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/treeitem.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart';

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

  void strings(List<String> list) {
    varint(list.length + 1);
    list.forEach(string);
  }

  /// A list of items: [described] entries in place, with their stamp steps and fields; the rest named.
  void items(List<int> ids, {Map<int, ({int step, void Function(_Out o) fields})> described = const {}}) {
    varint(ids.length + 1);
    for (final id in ids) {
      final z = id >= 0 ? id * 2 : -id * 2 - 1;
      final entry = described[id];
      if (entry == null) {
        varint(z * 2 + 1);
        continue;
      }
      varint(z * 2 + 2);
      varint(entry.step);
      final outer = _lastKey;
      _lastKey = 0;
      entry.fields(this);
      varint(0);
      _lastKey = outer;
    }
  }

  Uint8List bytes() => _b.toBytes();
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

void _row(_Out o, List<String> texts) => o
  ..key(WireKey.k_Widget_style)
  ..zigzag(0)
  ..key(WireKey.k_TableItem_texts)
  ..strings(texts);

void _child(
  _Out o,
  String text, {
  List<int> children = const [],
  Map<int, ({int step, void Function(_Out o) fields})> described = const {},
}) {
  o
    ..key(WireKey.k_Widget_style)
    ..zigzag(0)
    ..key(WireKey.k_Item_text)
    ..string(text);
  if (children.isNotEmpty) {
    o
      ..key(WireKey.k_TreeItem_items)
      ..items(children, described: described);
  }
}

/// What [value] holds, a widget it names by its id.
Map<int, Object?> _state(VWidget value) {
  Object? plain(Object? v) => switch (v) {
    VWidget() => 'ref ${v.id}',
    List() => v.map(plain).toList(),
    _ => v,
  };
  final state = <int, Object?>{};
  value.visitWire((key, v) => state[key] = plain(v));
  return state;
}

/// Every frame received so far applied: when the registry settles what was let go of.
Future<void> _applied() {
  final applied = Completer<void>();
  EquoCommBase.whenApplied(applied.complete);
  return applied.future;
}

/// What the client holds for [channels]: each one's state and stamp.
Map<String, Object?> _held(VRegistry registry, List<String> channels) => {
  for (final c in channels)
    c: registry.valueOn(c) == null
        ? null
        : {'state': _state(registry.valueOn(c)!), 'seq': registry.valueOn(c)!.seq, 'gate': deliveryGate.heldSeq(c)},
};

void main() {
  setUp(deliveryGate.reset);

  const table = 100;
  const rows = [101, 102, 103];
  final rowChannels = [for (final id in rows) 'TableItem/$id'];

  Uint8List tableWithRows({required bool inPlace}) {
    final described = {
      for (final (i, id) in rows.indexed) id: (step: 1, fields: (_Out o) => _row(o, ['row $i', 'value $i'])),
    };
    return _frame([
      _record(
        wireWhole,
        WireType.table,
        table,
        10,
        (o) => o
          ..key(WireKey.k_Table_items)
          ..items(rows, described: inPlace ? described : const {}),
      ),
      if (!inPlace)
        for (final (i, id) in rows.indexed)
          _record(wireWhole, WireType.tableItem, id, 1, (o) => _row(o, ['row $i', 'value $i'])),
    ]);
  }

  /// Changes a row, then drops another from the table: what holds what decides who hears of the first
  /// and what is forgotten after the second.
  Future<Map<String, Object?>> exercise(VRegistry registry) async {
    var tableTold = 0;
    registry.watch('Table/$table', (change) => tableTold++);
    registry.applyWire(
      _frame([
        _record(
          wireChange,
          WireType.tableItem,
          102,
          20,
          baseStep: 20 - 12,
          (o) => o
            ..key(WireKey.k_TableItem_texts)
            ..strings(['changed', 'value 1']),
        ),
      ]),
    );
    final afterChange = _held(registry, rowChannels);
    final told = tableTold;
    registry.applyWire(
      _frame([
        _record(
          wireChange,
          WireType.table,
          table,
          30,
          baseStep: 30 - 10,
          (o) => o
            ..key(WireKey.k_Table_items)
            ..items([101, 102]),
        ),
      ]),
    );
    await _applied();
    return {'afterChange': afterChange, 'tableTold': told, 'afterDrop': _held(registry, rowChannels)};
  }

  test('rows described in the table\'s list are held as rows described by records of their own', () async {
    final before = VRegistry()..applyWire(tableWithRows(inPlace: false));
    final heldBefore = _held(before, ['Table/$table', ...rowChannels]);
    final exercisedBefore = await exercise(before);

    deliveryGate.reset();
    final after = VRegistry()..applyWire(tableWithRows(inPlace: true));
    final heldAfter = _held(after, ['Table/$table', ...rowChannels]);
    expect((after.valueOn('TableItem/102')! as VTableItem).texts, ['row 1', 'value 1']);
    expect(after.valueOn('TableItem/103')!.seq, 13, reason: 'each step counts from the item before it');
    final exercisedAfter = await exercise(after);

    expect(heldAfter, heldBefore);
    expect(exercisedAfter, exercisedBefore);
    expect(
      ((exercisedAfter['afterChange']! as Map)['TableItem/102'] as Map)['seq'],
      20,
      reason: 'a change computed from the row as described in place merges into it',
    );
    expect(exercisedAfter['tableTold'], 1, reason: 'the table holds its rows, so it hears of a change to one');
    expect(
      (exercisedAfter['afterDrop']! as Map)['TableItem/103'],
      isNull,
      reason: 'a row the table stops naming is forgotten',
    );
    expect(deliveryGate.recoveries, 0);
  });

  test('a tree item\'s children are described inside its own entry, in the order Java stamps them', () {
    const tree = 200;
    final channels = [
      'Tree/$tree',
      for (final id in [201, 202, 203, 204]) 'TreeItem/$id',
    ];
    final nested = {
      202: (step: 1, fields: (_Out o) => _child(o, 'b')),
      203: (step: 1, fields: (_Out o) => _child(o, 'c')),
    };
    final inPlace = _frame([
      _record(
        wireWhole,
        WireType.tree,
        tree,
        10,
        (o) => o
          ..key(WireKey.k_Tree_items)
          ..items(
            [201, 204],
            described: {
              201: (step: 1, fields: (_Out o) => _child(o, 'a', children: [202, 203], described: nested)),
              204: (step: 1, fields: (_Out o) => _child(o, 'd')),
            },
          ),
      ),
    ]);
    final records = _frame([
      _record(
        wireWhole,
        WireType.tree,
        tree,
        10,
        (o) => o
          ..key(WireKey.k_Tree_items)
          ..items([201, 204]),
      ),
      _record(wireWhole, WireType.treeItem, 201, 1, (o) => _child(o, 'a', children: [202, 203])),
      _record(wireWhole, WireType.treeItem, 202, 1, (o) => _child(o, 'b')),
      _record(wireWhole, WireType.treeItem, 203, 1, (o) => _child(o, 'c')),
      _record(wireWhole, WireType.treeItem, 204, 1, (o) => _child(o, 'd')),
    ]);

    final before = _held(VRegistry()..applyWire(records), channels);
    deliveryGate.reset();
    final registry = VRegistry()..applyWire(inPlace);
    final after = _held(registry, channels);

    expect(after, before);
    expect((registry.valueOn('TreeItem/201')! as VTreeItem).items!.map((e) => e.id), [202, 203]);
    expect(registry.valueOn('TreeItem/204')!.seq, 14);
  });

  test('a row described in place that is older than the one held is read past, and the list names the held one', () {
    final registry = VRegistry()
      ..applyWire(
        _frame([
          _record(wireWhole, WireType.tableItem, 101, 50, (o) => _row(o, ['newer', ''])),
        ]),
      );
    registry.applyWire(
      _frame([
        _record(
          wireWhole,
          WireType.table,
          table,
          40,
          (o) => o
            ..key(WireKey.k_Table_items)
            ..items(
              [101, 102],
              described: {
                // Stamped 40, before the 50 held.
                101: (step: 0, fields: (_Out o) => _row(o, ['older', ''])),
                102: (step: 1, fields: (_Out o) => _row(o, ['fresh', ''])),
              },
            ),
        ),
      ]),
    );

    expect((registry.valueOn('TableItem/101')! as VTableItem).texts, ['newer', '']);
    expect(registry.valueOn('TableItem/101')!.seq, 50);
    expect((registry.valueOn('TableItem/102')! as VTableItem).texts, ['fresh', '']);
    expect(registry.valueOn('TableItem/102')!.seq, 41);
    final items = (registry.valueOn('Table/$table')! as VTable).items!;
    expect(identical(items.first, registry.valueOn('TableItem/101')), isTrue);
  });
}
