// flutter test --platform chrome --wasm test/web/
@TestOn('browser')
library;

import 'dart:js_interop';

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/raw_pointer.dart';
import 'package:web/web.dart' as web;

void main() {
  // Pointer events report fractional positions; dart2wasm refuses a non-integer read as an int.
  test('a raw move at a fractional position reaches the listener with that position', () {
    final moves = <(double, double, int)>[];
    listenRawPointerMoves((x, y, buttons) => moves.add((x, y, buttons)));

    final init = {'clientX': 10.5, 'clientY': 20.25, 'buttons': 1}.jsify() as web.PointerEventInit;
    web.window.dispatchEvent(web.PointerEvent('pointerrawupdate', init));

    expect(moves, [(10.5, 20.25, 1)]);
  });
}
