// In an editor of fixed line height every line is one row tall, plus the vertical indent a line
// was given: a line with properties of its own but no indent, such as the current line's
// background, is a row like any other.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

const _style = TextStyle(fontSize: 12, fontFamily: 'Liberation Mono');

TextShape _shape(String text, Map<int, LineProperties> props) => TextShape(text, Offset.zero, _style, null, null,
    false, const Size(400, 300), true, 1, null, TextEditingState(lineProperties: props), null, 14.0, 4, null,
    const LineProperties(), 0, null, TextDirection.ltr, false, const [], const [], 0, true);

void main() {
  final text = List.generate(50, (i) => 'line $i').join('\n');

  test('a line with a background of its own is one row, like the others', () {
    final plain = _shape(text, const {});
    final highlighted = _shape(text, const {21: LineProperties(background: Color(0xFF373737))});
    expect(highlighted.textHeight, plain.textHeight);
  });

  test('a vertical indent adds its height above its line', () {
    final plain = _shape(text, const {});
    final indented = _shape(text, const {
      21: LineProperties(background: Color(0xFF373737)),
      30: LineProperties(verticalIndent: 7),
    });
    expect(indented.textHeight, plain.textHeight + 7);
  });
}
