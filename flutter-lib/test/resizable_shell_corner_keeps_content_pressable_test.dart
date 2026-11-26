// A press on a control at the top-right corner of a resizable trim-less shell reaches the control.
// JFace's PopupDialog (Eclipse's Quick Access, the hover popups) puts its dialog-menu ToolItem
// there, a few pixels from the shell's corner: on every native platform a press on its icon opens
// the menu, and the shell's resize border sits on the shell's edge, not over its content.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/utils/image_utils.dart';

import 'support/sent_events.dart';

// DisplaySwt caches the last state it saw per display id, so each test uses its own id.
int _base(int n) => 8600 + n * 10;

const _shellW = 420;
const _shellH = 240;
const _imageRef = 61;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// PopupDialog's shell: `SWT.ON_TOP | SWT.RESIZE` with no title, its dialog area filling it and
/// the menu ToolBar at the right end of the title row.
VDisplay _popupDialog(int base) => VDisplay()
  ..swt = 'Display'
  ..id = base
  ..activeShellId = base + 1
  ..shells = [
    VShell()
      ..swt = 'Shell'
      ..id = base + 1
      ..style = SWT.ON_TOP | SWT.RESIZE
      ..visible = true
      ..bounds = _rect(100, 100, _shellW, _shellH)
      ..children = [
        VComposite()
          ..swt = 'Composite'
          ..id = base + 2
          ..style = SWT.NONE
          ..enabled = true
          ..visible = true
          ..bounds = _rect(0, 0, _shellW, _shellH)
          ..children = [
            VToolBar()
              ..swt = 'ToolBar'
              ..id = base + 3
              ..style = SWT.FLAT
              ..enabled = true
              ..visible = true
              ..bounds = _rect(_shellW - 26, 1, 24, 22)
              ..items = [
                VToolItem()
                  ..swt = 'ToolItem'
                  ..id = base + 4
                  ..style = SWT.PUSH
                  ..enabled = true
                  ..toolTipText = 'Menu'
                  ..image = (VImage()
                    ..remoteRef = _imageRef
                    ..width = 16
                    ..height = 16),
              ],
          ],
      ],
  ];

Future<Rect> _pumpPopup(WidgetTester tester, int base) async {
  final render = (await tester.runAsync(() => createTestImage(width: 16, height: 16)))!;
  ImageUtils.registerRemoteImage(_imageRef, render);
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: DisplaySwt(value: _popupDialog(base)),
  ));
  await tester.pumpAndSettle();
  EquoCommService.commForTesting.clearSentFrames();
  return tester.getRect(find.byType(RawImage));
}

Future<void> _click(WidgetTester tester, Offset at) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await gesture.addPointer(location: at);
  addTearDown(gesture.removePointer);
  await tester.pump();
  await gesture.down(at);
  await tester.pump();
  await gesture.up();
  await tester.pumpAndSettle();
}

List<String> _selections(int itemId) => sentEvents()
    .map((e) => e.$1)
    .where((c) => c.startsWith('ToolItem/$itemId/') && c.contains('Selection'))
    .toList();

/// The render objects a press at [at] hits, innermost first, for the failure message.
String _hitPath(WidgetTester tester, Offset at) =>
    tester.hitTestOnBinding(at).path.take(12).map((e) => e.target.runtimeType).join(' < ');

void main() {
  setUp(() {
    ImageUtils.clearCache();
    EquoCommBase.recordSentFrames = true;
  });

  testWidgets('a press on the middle of the menu icon selects the item', (tester) async {
    final base = _base(1);
    final icon = await _pumpPopup(tester, base);

    await _click(tester, icon.center);

    expect(_selections(base + 4), isNotEmpty,
        reason: 'hit path at ${icon.center}: ${_hitPath(tester, icon.center)}');
  });

  testWidgets('a press on the left edge of the menu icon selects the item', (tester) async {
    final base = _base(2);
    final icon = await _pumpPopup(tester, base);
    final at = icon.centerLeft + const Offset(2, 0);

    await _click(tester, at);

    expect(_selections(base + 4), isNotEmpty, reason: 'hit path at $at: ${_hitPath(tester, at)}');
  });
}
