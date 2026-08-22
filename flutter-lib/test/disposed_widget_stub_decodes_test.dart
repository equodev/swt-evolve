import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/widgets.dart';
import 'package:swtflutter/src/gen/canvas.dart';
import 'package:swtflutter/src/gen/composite.dart';

/// A widget disposed while an ancestor tree is being serialized travels as the identity stub
/// `{id, swt, seq, style}` — Serializer.writeWithId can't read its checkWidget()-guarded getters.
///
/// The stub has to decode in every position a widget reference can appear. `null` does not: the
/// generated decoders cast each entry of a widget array to a map, so one null element throws and
/// takes the whole ancestor payload with it. Neither does a stub missing `style`, which every
/// decoder reads as a non-nullable num. Both shapes crashed the client on a disposed Menu still
/// attached to its Canvas.
void main() {
  test('a stub in a widget reference field decodes', () {
    final canvas = VCanvas.fromJson({
      'id': 1,
      'swt': 'Canvas',
      'seq': 2,
      'style': 33554432,
      'menu': {'id': 3, 'swt': 'Menu', 'seq': 4, 'style': 0},
      'children': <dynamic>[],
    });

    expect(canvas.menu!.id, 3);
    expect(canvas.menu!.items, isNull, reason: 'the stub carries no state');
  });

  test('a stub inside a widget array decodes', () {
    final composite = VComposite.fromJson({
      'id': 1,
      'swt': 'Composite',
      'seq': 2,
      'style': 0,
      'children': [
        {'id': 3, 'swt': 'Button', 'seq': 4, 'style': 8},
        {'id': 5, 'swt': 'Button', 'seq': 6, 'style': 8, 'text': 'live'},
      ],
    });

    expect(composite.children!.map((c) => c.id), [3, 5]);
  });
}
