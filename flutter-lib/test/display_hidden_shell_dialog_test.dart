// A MessageBox whose parent Shell was never opened, with no visible Shell to host it, is parked on
// that hidden parent. SWT shows such a box with no visible owner, so the client must show it too:
// otherwise the page stays blank and Java waits on the box forever.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/messagebox.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart' show encodeFrame, encodeRecord;

const _viewport = Size(1280, 800);
const _question = 'Open another workspace?';

/// A display and the one shell it lists. Each test has its own ids: the registry keeps the last
/// record of every holder across [VRegistry.clear].
class _Session {
  _Session(this.displayId)
      : shellId = displayId * 1000 + 1,
        boxId = displayId * 1000 + 2;

  final int displayId;
  final int shellId;
  final int boxId;

  VMessageBox get box => VMessageBox()
    ..id = boxId
    ..swt = 'MessageBox'
    ..style = SWT.ICON_WARNING | SWT.YES | SWT.NO
    ..text = 'Workspace in use'
    ..message = _question;

  void _dialogs(WidgetWireWriter w, bool withBox) => w
    ..key(WireKey.k_Shell_dialogs)
    ..polys<VMessageBox>(withBox ? [box] : [], (e) => e.swt);

  /// The shell described whole. `new Shell(display, SWT.ON_TOP)` that was never opened is hidden
  /// and unsized; an opened one is visible and sized.
  Uint8List shellWhole(int stamp, {required bool visible, required bool withBox}) =>
      encodeRecord(wireWhole, (w) {
        w
          ..varint(WireType.shell * 2 + 1)
          ..int32(shellId)
          ..varint(stamp)
          ..key(WireKey.k_Control_bounds)
          ..rect(0, 0, visible ? 400 : 0, visible ? 300 : 0)
          ..key(WireKey.k_Control_visible)
          ..u8(visible ? 2 : 1)
          ..key(WireKey.k_Decorations_text)
          ..string('Startup');
        _dialogs(w, withBox);
        w
          ..key(WireKey.k_Widget_style)
          ..int32((visible ? SWT.SHELL_TRIM : SWT.ON_TOP) | SWT.LEFT_TO_RIGHT);
      });

  /// The box added to the shell by a change on the shell's own record.
  Uint8List boxAdded(int stamp, int base) => encodeRecord(wireChange, (w) {
        w
          ..varint(WireType.shell * 2 + 1)
          ..int32(shellId)
          ..varint(stamp)
          ..varint(stamp - base);
        _dialogs(w, true);
      });

  /// The box removed from the shell, as Java does once it is answered.
  Uint8List boxRemoved(int stamp, int base) => encodeRecord(wireChange, (w) {
        w
          ..varint(WireType.shell * 2 + 1)
          ..int32(shellId)
          ..varint(stamp)
          ..varint(stamp - base);
        _dialogs(w, false);
      });

  /// The shell hidden by a change on its own record.
  Uint8List shellHidden(int stamp, int base) => encodeRecord(wireChange, (w) => w
    ..varint(WireType.shell * 2 + 1)
    ..int32(shellId)
    ..varint(stamp)
    ..varint(stamp - base)
    ..key(WireKey.k_Control_visible)
    ..u8(1));

  /// The display's record, listing the shell.
  Uint8List display(int stamp) => encodeRecord(wireHolder, (w) => w
    ..varint(WireType.display * 2 + 1)
    ..int32(displayId)
    ..varint(stamp)
    ..key(WireKey.k_Display_shells)
    ..varint(2) // one ref, counted as length + 1
    ..ref(shellId));
}

void main() {
  setUp(resetConfigFlags);
  tearDown(() {
    resetConfigFlags();
    VRegistry.instance.clear();
  });

  /// Mounts the display, then delivers [first] as the client's first frame.
  Future<void> connect(WidgetTester tester, _Session s, List<Uint8List> first) async {
    tester.view.physicalSize = _viewport;
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: VDisplay()..id = s.displayId),
    ));
    VRegistry.instance.applyWire(encodeFrame(first));
    await tester.pumpAndSettle();
  }

  testWidgets('a box opened on a shown shell is shown', (tester) async {
    final s = _Session(21);
    await connect(tester, s, [s.shellWhole(10, visible: true, withBox: false), s.display(11)]);
    expect(find.text(_question), findsNothing);

    VRegistry.instance.applyWire(encodeFrame([s.boxAdded(12, 10)]));
    await tester.pumpAndSettle();

    expect(find.text(_question), findsOneWidget);
  });

  testWidgets('a box opened on a never-opened shell is shown', (tester) async {
    final s = _Session(22);
    await connect(tester, s, [s.shellWhole(10, visible: false, withBox: false), s.display(11)]);
    expect(find.text(_question), findsNothing);

    VRegistry.instance.applyWire(encodeFrame([s.boxAdded(12, 10)]));
    await tester.pumpAndSettle();

    expect(find.text(_question), findsOneWidget,
        reason: 'its parent is hidden and no other shell is shown: the box has no host but the '
            'display, and Java waits on it');
  });

  testWidgets('a shown shell that reaches the client with its box shows the box', (tester) async {
    final s = _Session(27);
    await connect(tester, s, [s.shellWhole(10, visible: true, withBox: true), s.display(11)]);

    expect(find.text(_question), findsOneWidget);
  });

  testWidgets('a shown shell listed before it arrives with its box shows the box', (tester) async {
    final s = _Session(28);
    await connect(tester, s, [s.display(10), s.shellWhole(11, visible: true, withBox: true)]);

    expect(find.text(_question), findsOneWidget);
  });

  testWidgets('a box on a never-opened shell that is then shown is shown once', (tester) async {
    final s = _Session(29);
    await connect(tester, s, [s.shellWhole(10, visible: false, withBox: true), s.display(11)]);
    expect(find.text(_question), findsOneWidget);

    VRegistry.instance.applyWire(encodeFrame([s.shellWhole(12, visible: true, withBox: true), s.display(13)]));
    await tester.pumpAndSettle();
    VRegistry.instance.applyWire(encodeFrame([s.shellWhole(14, visible: true, withBox: true)]));
    await tester.pumpAndSettle();

    expect(find.text(_question), findsOneWidget, reason: 'the display already showed it');
  });

  testWidgets('a box opened on a shown shell that is then hidden is shown once', (tester) async {
    final s = _Session(26);
    await connect(tester, s, [s.shellWhole(10, visible: true, withBox: false), s.display(11)]);
    VRegistry.instance.applyWire(encodeFrame([s.boxAdded(12, 10)]));
    await tester.pumpAndSettle();

    VRegistry.instance.applyWire(encodeFrame([s.shellHidden(13, 12), s.display(14)]));
    await tester.pumpAndSettle();

    expect(find.text(_question), findsOneWidget, reason: 'the shell already showed it');
  });

  testWidgets('a never-opened shell that reaches the client with its box shows the box', (tester) async {
    final s = _Session(23);
    await connect(tester, s, [s.shellWhole(10, visible: false, withBox: true), s.display(11)]);

    expect(find.text(_question), findsOneWidget);
  });

  testWidgets('a box on a never-opened shell is shown once across later changes', (tester) async {
    final s = _Session(24);
    await connect(tester, s, [s.shellWhole(10, visible: false, withBox: true), s.display(11)]);

    VRegistry.instance.applyWire(encodeFrame([s.boxAdded(12, 10), s.display(13)]));
    await tester.pumpAndSettle();

    expect(find.text(_question), findsOneWidget);
  });

  testWidgets('a box removed from a never-opened shell and added again is shown again', (tester) async {
    final s = _Session(25);
    await connect(tester, s, [s.shellWhole(10, visible: false, withBox: true), s.display(11)]);
    await tester.tap(find.text('Yes'));
    await tester.pumpAndSettle();
    expect(find.text(_question), findsNothing);

    VRegistry.instance.applyWire(encodeFrame([s.boxRemoved(12, 10)]));
    await tester.pumpAndSettle();
    VRegistry.instance.applyWire(encodeFrame([s.boxAdded(13, 12)]));
    await tester.pumpAndSettle();

    expect(find.text(_question), findsOneWidget);
  });
}
