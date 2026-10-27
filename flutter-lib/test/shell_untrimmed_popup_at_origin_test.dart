// Centring belongs to a shell nobody placed -- the dialog opened at the platform's default. An
// untrimmed shell is never one: the application anchors it to a control, and anchored at the
// window's top-left its location is the origin, which no (0,0) test can tell from unplaced.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';

const int _viewportW = 1728;
const int _viewportH = 958;
const int _popupW = 1692;
const int _popupH = 180;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _mainShell() => VShell()
  ..id = 1
  ..style = SWT.SHELL_TRIM
  ..text = 'app'
  ..bounds = _rect(0, 0, _viewportW, _viewportH)
  ..children = [];

VShell _shell({required int style, required VRectangle bounds}) => VShell()
  ..id = 2
  ..style = style
  ..bounds = bounds
  ..children = [];

Future<void> _pump(WidgetTester tester, List<VShell> shells) async {
  tester.view.physicalSize = const Size(_viewportW * 1.0, _viewportH * 1.0);
  tester.view.devicePixelRatio = 1.0;
  addTearDown(tester.view.resetPhysicalSize);
  addTearDown(tester.view.resetDevicePixelRatio);

  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(
      value: VDisplay()
        ..shells = shells
        ..mainShellId = 1,
    ),
  ));
  await tester.pumpAndSettle();
}

/// The non-main shell in the tree, picked by its size -- the main shell fills the viewport.
Finder _floatingShell(WidgetTester tester) {
  final shells = find.byType(ShellSwt);
  for (var i = 0; i < tester.widgetList(shells).length; i++) {
    final size = tester.getSize(shells.at(i));
    if (size.width < _viewportW) return shells.at(i);
  }
  fail('no floating shell in the tree; sizes were '
      '${[for (var i = 0; i < tester.widgetList(shells).length; i++) tester.getSize(shells.at(i))]}');
}

void main() {
  testWidgets('an untrimmed popup placed at the window origin renders there', (tester) async {
    await _pump(tester, [
      _mainShell(),
      _shell(
        style: SWT.NO_TRIM | SWT.ON_TOP,
        bounds: _rect(0, 0, _popupW, _popupH),
      ),
    ]);

    expect(tester.getTopLeft(_floatingShell(tester)), Offset.zero,
        reason: 'the application anchored this popup to a control at the top-left of the window; '
            'centring it puts it over the middle of the window instead');
  });

  testWidgets('an untrimmed popup placed away from the origin still renders there', (tester) async {
    // Narrow enough to fit: a popup wider than the viewport is pulled back inside it, which would
    // measure that clamping rather than the placement this pins.
    await _pump(tester, [
      _mainShell(),
      _shell(
        style: SWT.NO_TRIM | SWT.ON_TOP,
        bounds: _rect(51, 21, 400, _popupH),
      ),
    ]);

    expect(tester.getTopLeft(_floatingShell(tester)), const Offset(51, 21));
  });

  testWidgets('a trimmed dialog at the origin is still centred', (tester) async {
    // The case the centring exists for: a dialog the application never positioned.
    await _pump(tester, [
      _mainShell(),
      _shell(
        style: SWT.DIALOG_TRIM,
        bounds: _rect(0, 0, 400, 300),
      ),
    ]);

    expect(tester.getTopLeft(_floatingShell(tester)).dx, greaterThan(0),
        reason: 'a dialog nobody placed is still centred in the viewport');
  });
}
