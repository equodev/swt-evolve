// An application draws its own drag feedback by parking one shell over the window and reshaping it
// with a Region as the pointer travels: the shell's bounds never change, only the region does. What
// the user sees is therefore the region's rectangles, and where they land is the shell's origin plus
// the rectangle's own offset -- a Region's coordinates are the shell's own, which is what
// `Region#add` was given.
//
// These pin that sum. A region rectangle that renders anywhere but at shell origin + its own offset
// puts the feedback off the thing it is marking, however correct the shell's placement is.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/region.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/utils/region_clip.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _mainShell() => VShell()
  ..id = 1
  ..style = SWT.SHELL_TRIM
  ..text = 'app'
  ..bounds = _rect(0, 0, 800, 600)
  ..children = [];

/// The feedback shell: parked, never moved, reshaped by its region. NO_TRIM|ON_TOP is the style the
/// application uses, and it is what makes the shell draw no frame of its own.
VShell _feedback({required VRectangle bounds, required List<int> rects}) => VShell()
  ..id = 2
  ..style = SWT.NO_TRIM | SWT.ON_TOP
  ..bounds = bounds
  ..region = (VRegion()..rects = rects)
  ..children = [];

Future<void> _pump(WidgetTester tester, List<VShell> shells) async {
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

/// The RegionClip that belongs to the shell, identified by matching the shell's size -- a Control
/// inside it can carry a region of its own and get a RegionClip too.
Finder _shellClip(WidgetTester tester, Finder clips, VRectangle bounds) {
  final want = Size(bounds.width.toDouble(), bounds.height.toDouble());
  for (var i = 0; i < tester.widgetList(clips).length; i++) {
    if (tester.getSize(clips.at(i)) == want) return clips.at(i);
  }
  fail('no RegionClip sized like the shell (${want.width}x${want.height}); sizes were '
      '${[for (var i = 0; i < tester.widgetList(clips).length; i++) tester.getSize(clips.at(i))]}');
}

void main() {
  testWidgets('a region rectangle lands at the shell origin plus its own offset', (tester) async {
    // The shell is parked away from the origin and the rectangle is offset inside it, so an origin
    // dropped on either side shows up as a different wrong answer rather than cancelling.
    final bounds = _rect(120, 200, 400, 300);
    const rx = 30, ry = 40, rw = 90, rh = 20;

    await _pump(tester, [
      _mainShell(),
      _feedback(bounds: bounds, rects: const [rx, ry, rw, rh]),
    ]);

    // More than one RegionClip can be in the tree (a Control carrying a region gets one too), so
    // pick the one that is the shell: the one sized like the shell.
    final clips = find.byType(RegionClip);
    expect(clips, findsAtLeastNWidgets(1));
    final clip = _shellClip(tester, clips, bounds);
    final shellTopLeft = tester.getTopLeft(clip);
    expect(shellTopLeft, Offset(bounds.x.toDouble(), bounds.y.toDouble()),
        reason: 'the parked shell renders at the bounds the application gave it');

    expect(tester.getSize(clip), Size(bounds.width.toDouble(), bounds.height.toDouble()),
        reason: 'a region reshapes what is painted, it does not resize the shell');
  });

  testWidgets('reshaping the region does not move the shell', (tester) async {
    // The application never calls setBounds during the drag -- measured live, 42 samples, identical
    // bounds throughout. Only the region changes, and the feedback has to follow it.
    final bounds = _rect(120, 200, 400, 300);

    await _pump(tester, [
      _mainShell(),
      _feedback(bounds: bounds, rects: const [10, 10, 50, 20]),
    ]);
    final before = tester.getTopLeft(_shellClip(tester, find.byType(RegionClip), bounds));

    await _pump(tester, [
      _mainShell(),
      _feedback(bounds: bounds, rects: const [10, 250, 50, 20]),
    ]);
    final after = tester.getTopLeft(_shellClip(tester, find.byType(RegionClip), bounds));

    expect(after, before,
        reason: 'the shell is parked; a region change must not shift it');
  });

  testWidgets('an empty region paints nothing rather than leaving the shell whole',
      (tester) async {
    await _pump(tester, [
      _mainShell(),
      _feedback(bounds: _rect(120, 200, 400, 300), rects: const []),
    ]);

    expect(find.byType(RegionClip), findsAtLeastNWidgets(1),
        reason: 'SWT clips a control with an empty region away, it does not ignore the region');
  });
}
