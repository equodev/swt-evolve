// A shell built while hidden is described whole on its own channel before the client listens
// there, and when shown, the display names its children by reference. The held frame's replay
// must fill in the stand-in the shell's child list holds, or the shell lays out a child with no
// bounds (drawn 0x0).

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/deliver.dart';

const int _shellId = 5;
const int _childId = 6;

Map<String, dynamic> _shell(Map<String, dynamic> child, int seq) => {
  'swt': 'Shell',
  'id': _shellId,
  '_s': seq,
  'style': SWT.NO_TRIM,
  'bounds': {'x': 0, 'y': 0, 'width': 140, 'height': 63},
  'children': [child],
};

Map<String, dynamic> _childWhole(int seq) => {
  'swt': 'Composite',
  'id': _childId,
  '_s': seq,
  'style': SWT.NONE,
  'bounds': {'x': 0, 'y': 0, 'width': 140, 'height': 63},
};

Map<String, dynamic> _reference(int id, String swt) => {
  'swt': swt,
  'id': id,
  'style': SWT.NONE,
  '_r': 1,
};

void main() {
  late VRegistry registry;

  setUp(() => registry = VRegistry());

  test(
    'a child named before its held description is replayed is filled in where it is drawn',
    () async {
      // Built hidden: described whole on the shell's own channel, which nothing listens to yet.
      await deliverFrame('Shell/$_shellId', _shell(_childWhole(45), 44));

      // Shown: the display introduces the shell and names the child it already sent.
      final shell =
          registry.register(VShell.fromJson(_shell(_reference(_childId, 'Composite'), 47)))
              as VShell;
      // One microtask for the replay of the held frame, one for the ask a reference defers.
      await Future<void>.microtask(() {});
      await Future<void>.microtask(() {});

      final drawnByTheShell = shell.children!.single as VComposite;
      expect(
        drawnByTheShell.bounds?.width,
        140,
        reason: 'the shell lays its child out from this object; without bounds it is drawn 0x0',
      );
      expect(
        identical(drawnByTheShell, registry.valueOn('Composite/$_childId')),
        isTrue,
        reason: 'one widget, one object - whoever named it and whoever answered for it agree',
      );
    },
  );
}
