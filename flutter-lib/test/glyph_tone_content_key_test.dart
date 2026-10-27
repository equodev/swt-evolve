import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/glyph_tone.dart';

Uint8List _content(int length) =>
    Uint8List.fromList([for (var i = 0; i < length; i++) (i * 7919 + 13) & 0xFF]);

/// [content] at byte offset [offset] of a larger buffer, as a view.
Uint8List _viewAt(Uint8List content, int offset) {
  final backing = Uint8List(offset + content.length)..setRange(offset, offset + content.length, content);
  return Uint8List.sublistView(backing, offset);
}

void main() {
  test('the same content has the same key wherever its buffer starts', () {
    final content = _content(1027);
    final key = GlyphTone.contentKey(content);
    for (final offset in [1, 2, 3, 4, 5]) {
      expect(GlyphTone.contentKey(_viewAt(content, offset)), key, reason: 'offset $offset');
    }
  });

  test('a difference in any byte changes the key, including the trailing ones', () {
    final content = _content(1027);
    final key = GlyphTone.contentKey(content);
    for (final index in [0, 3, 4, 512, 1023, 1024, 1026]) {
      final changed = Uint8List.fromList(content)..[index] ^= 0x01;
      expect(GlyphTone.contentKey(changed), isNot(key), reason: 'byte $index');
    }
  });

  test('buffers shorter than one word still get a key from their content', () {
    expect(GlyphTone.contentKey(Uint8List.fromList([1, 2, 3])),
        isNot(GlyphTone.contentKey(Uint8List.fromList([1, 2, 4]))));
    expect(GlyphTone.contentKey(Uint8List(0)), 'bin-0-0');
  });
}
