// A floating shell hidden by a change on its own record, the display's record unchanged: the
// workbench's drag overlay does this on every frame of a tab drag. The hidden shell draws nothing,
// and the main shell still fills the page.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/impl/display_evolve.dart';

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

const _mainId = 9101;
const _overlayId = 9102;

VShell _main(Size page) => VShell()
  ..id = _mainId
  ..seq = 10
  ..style = SWT.SHELL_TRIM
  ..text = 'Workbench'
  ..visible = true
  ..bounds = _rect(0, 0, page.width.toInt(), page.height.toInt());

VShell _overlay({required bool visible, int seq = 10}) => VShell()
  ..id = _overlayId
  ..seq = seq
  ..style = SWT.NO_TRIM | SWT.ON_TOP
  ..visible = visible
  ..bounds = _rect(114, 89, 2, 24);

Rect _mainRect(WidgetTester tester) =>
    tester.getRect(find.byWidgetPredicate((w) => w is ShellSwt && w.value.id == _mainId));

void main() {
  testWidgets('a floating shell its own record hides leaves the main shell filling the page', (tester) async {
    freshClient();
    final page = tester.view.physicalSize / tester.view.devicePixelRatio;
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(
        value: VDisplay()
          ..id = 9100
          ..mainShellId = _mainId
          ..shells = [_main(page), _overlay(visible: true)],
      ),
    ));
    await tester.pumpAndSettle();
    final before = _mainRect(tester);
    expect(before.left, 0, reason: 'sanity: the main shell fills the page');

    await deliverChange(_overlay(visible: false, seq: 11), keys: [WireKey.k_Control_visible], base: 10);
    await tester.pumpAndSettle();

    final after = _mainRect(tester);
    expect(after.left, 0, reason: 'the main shell stays where it was');
    expect(after.width, before.width, reason: 'and keeps the page width');
  });
}
