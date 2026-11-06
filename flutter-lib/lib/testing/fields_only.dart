import 'dart:typed_data';

import 'package:swtflutter/src/comm/widget_wire.dart';

/// [value]'s fields as its writer writes them, keeping only those numbered in [keys] (all of them
/// when null): what a change carrying just those properties holds. Not closed by a 0.
Uint8List fieldsOnly(WireWritable value, Set<int>? keys) {
  final w = _MarkingWriter();
  value.writeWire(w);
  final bytes = w.written;
  if (keys == null) return Uint8List.fromList(bytes);
  final out = BytesBuilder(copy: false);
  var last = 0;
  for (var i = 0; i < w.marks.length; i++) {
    final (key, start) = w.marks[i];
    final end = i + 1 < w.marks.length ? w.marks[i + 1].$2 : bytes.length;
    if (!keys.contains(key)) continue;
    // The key is a delta from the previous field's: from the previous field kept, here.
    var at = start;
    while (bytes[at] >= 0x80) {
      at++;
    }
    out.add(Uint8List.fromList(WidgetWireWriter.encodeWith((w) => w.int32(key - last))));
    out.add(Uint8List.sublistView(bytes, at + 1, end));
    last = key;
  }
  return out.toBytes();
}

/// Writes as [WidgetWireWriter] does, noting where each of the value's own fields starts.
class _MarkingWriter extends WidgetWireWriter {
  final List<(int, int)> marks = [];
  int _depth = 0;

  void _mark(int key) {
    if (_depth == 0) marks.add((key, size));
  }

  @override
  void key(int key) {
    _mark(key);
    super.key(key);
  }

  @override
  void keyInt32(int key, int v) {
    _mark(key);
    super.keyInt32(key, v);
  }

  @override
  void keyInt64(int key, int v) {
    _mark(key);
    super.keyInt64(key, v);
  }

  @override
  void keyChar16(int key, int v) {
    _mark(key);
    super.keyChar16(key, v);
  }

  @override
  void keyBoolean(int key, bool v) {
    _mark(key);
    super.keyBoolean(key, v);
  }

  @override
  void keyRef(int key, int? id) {
    _mark(key);
    super.keyRef(key, id);
  }

  @override
  void object(void Function(WidgetWireWriter w)? fields) {
    _depth++;
    try {
      super.object(fields);
    } finally {
      _depth--;
    }
  }
}
