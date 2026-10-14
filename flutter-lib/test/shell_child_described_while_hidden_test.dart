// A shell built while hidden is described whole on its own channel before the client listens there.
// When it is shown, the display names it and the children it already sent by reference. Those
// children are drawn from the description held for the shell's channel; drawn from anything else
// they have no bounds and lay out at 0x0, so the shell is on screen with nothing in it.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart';

const int _shellId = 2002;
const int _childId = 2003;

Map<String, dynamic> _rect(int x, int y, int w, int h) => {'x': x, 'y': y, 'width': w, 'height': h};

Map<String, dynamic> _shell(
  Map<String, dynamic> child, {
  required int seq,
  required bool visible,
}) => {
  'swt': 'Shell',
  'id': _shellId,
  '_s': seq,
  'style': SWT.NO_TRIM,
  'bounds': _rect(450, 360, 140, 63),
  'visible': visible,
  'children': [child],
};

VShell _main() => VShell()
  ..id = 1001
  ..style = SWT.SHELL_TRIM
  ..text = 'Main'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 1200
    ..height = 900)
  ..visible = true
  ..children = [];

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  testWidgets('a child described while its shell was hidden is drawn at its size', (tester) async {
    tester.view.physicalSize = const Size(1200, 900);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    await deliverFrame(
      'Shell/$_shellId',
      _shell(
        {
          'swt': 'Composite',
          'id': _childId,
          '_s': 45,
          'style': SWT.NONE,
          'bounds': _rect(0, 0, 140, 63),
        },
        seq: 44,
        visible: false,
      ),
    );

    final shown = VShell.fromJson(
      _shell(
        {'swt': 'Composite', 'id': _childId, 'style': SWT.NONE, '_r': 1},
        seq: 47,
        visible: true,
      ),
    );
    await tester.pumpWidget(
      EvolveApp(
        theme: ThemeMode.light,
        contentWidget: DisplaySwt(
          value: VDisplay()
            ..id = 7
            ..shells = [_main(), shown]
            ..mainShellId = 1001,
        ),
      ),
    );
    await tester.pumpAndSettle();

    expect(tester.getSize(find.byKey(const ValueKey(_childId)).first), const Size(140, 63));
  });
}
