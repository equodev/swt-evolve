// A table row has no state of its own: a change to it is drawn by the table, which is told on the
// row's behalf. A frame that changes many rows tells the table once, after the whole frame is in,
// not once per row.

import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'support/deliver.dart';
import 'package:swtflutter/src/comm/widget_key.dart';

const _rows = [10, 11, 12, 13, 14];

VTableItem _item(int id) => VTableItem()
  ..swt = 'TableItem'
  ..id = id
  ..seq = 1
  ..text = 'row $id';

/// A widget frame of one text change per row, each written after the last.
Uint8List _textChanges(Map<int, String> texts) {
  final out = BytesBuilder();
  out.add((ByteData(4)..setUint32(0, texts.length, Endian.little)).buffer.asUint8List());
  var stamp = 1;
  for (final MapEntry(key: id, value: text) in texts.entries) {
    stamp++;
    // Type number, id, stamp as the step from the previous record's (the frame starts at 0), and
    // the base it builds on (the rows' first write) as the distance back from it.
    final record = Uint8List.fromList(WidgetWireWriter.encodeWith((w) {
      w.varint(WidgetKey.typeOf('TableItem') * 2 + 1);
      w.varint(id * 2);
      w.varint(stamp == 2 ? 2 : 1);
      w.varint(stamp - 1);
      w.key(WireKey.k_Item_text);
      w.string(text);
    }));
    out.addByte(wireChange);
    var length = record.length;
    while (length >= 0x80) {
      out.addByte((length & 0x7F) | 0x80);
      length >>= 7;
    }
    out.addByte(length);
    out.add(record);
  }
  return out.toBytes();
}

void main() {
  setUp(freshClient);

  test('a frame changing many rows tells their table once', () async {
    final registry = VRegistry.instance;
    final table = VTable()
      ..swt = 'Table'
      ..id = 1
      ..seq = 1
      ..style = SWT.NONE
      ..items = [for (final id in _rows) _item(id)];
    registry.register(table);
    registry.holds('Table/1', table.items!);
    // The rows' first write, as delivered.
    for (final id in _rows) {
      deliveryGate.applied('TableItem/$id', 1);
    }
    var told = 0;
    registry.watch('Table/1', (_) => told++);

    registry.applyWire(_textChanges({for (final id in _rows) id: 'changed $id'}));

    expect(told, 1);
    for (final id in _rows) {
      expect((registry.valueOn('TableItem/$id') as VTableItem).text, 'changed $id');
    }
  });
}
