import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/impl/styledtext_evolve.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('containsPoint recognizes a click below the first line of unwrapped multi-line text', () {
    const style = TextStyle(fontSize: 16, height: 1.2);
    final shape = TextShape(
      'line one\nline two\nline three\nline four',
      Offset.zero,
      style,
      null, // clipRect
      null, // textSpan
      null, // caretInfo
      false, // wordWrap
      const Size(400, 400), // canvasSize
      true, // editable
    );

    // Roughly the third line's vertical band — well inside the rendered 4-line block, but past
    // where the old maxLines: 1 cap stopped measuring.
    final pointOnLineThree = const Offset(10, 45);

    expect(
      shape.containsPoint(pointOnLineThree, const Size(400, 400)),
      isTrue,
      reason: 'a click on line 3 of 4 must land inside the text, not just line 1',
    );
  });
}
