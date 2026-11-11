// A shell Java opens after the display's record has listed it hidden. Its setVisible(true) reaches
// the client as a change on the shell's own record; whether Java sends the display again after it
// depends on what else that flush carries. The display must draw the shell either way, or the page
// stays blank until a reload.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/comm/widget_wire.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

import 'delivery/support/deliver.dart' show encodeFrame, encodeRecord;

const _viewport = Size(1280, 800);

/// The boxed boolean as Java writes it: 0 null, 1 false, 2 true.
void _visible(WidgetWireWriter w, bool visible) => w
  ..key(WireKey.k_Control_visible)
  ..u8(visible ? 2 : 1);

/// A display and the one shell it lists. Each test has its own ids: the registry keeps the last
/// record of every holder across [VRegistry.clear].
class _Session {
  _Session(this.displayId) : shellId = displayId * 1000 + 1;

  final int displayId;
  final int shellId;

  /// The shell described whole, hidden, as Java sends it before its first setVisible(true).
  Uint8List shellWhole(int stamp) => encodeRecord(wireWhole, (w) {
        w
          ..varint(WireType.shell * 2 + 1)
          ..int32(shellId)
          ..varint(stamp)
          ..key(WireKey.k_Control_bounds)
          ..rect(0, 0, _viewport.width.toInt(), _viewport.height.toInt());
        _visible(w, false);
        w
          ..key(WireKey.k_Decorations_text)
          ..string('Workbench')
          ..key(WireKey.k_Widget_style)
          ..int32(SWT.SHELL_TRIM | SWT.LEFT_TO_RIGHT);
      });

  /// A change to the shell's own record, on top of the one stamped [base]: what [write] writes.
  Uint8List shellChange(int stamp, int base, void Function(WidgetWireWriter w) write) =>
      encodeRecord(wireChange, (w) {
        w
          ..varint(WireType.shell * 2 + 1)
          ..int32(shellId)
          ..varint(stamp)
          ..varint(stamp - base);
        write(w);
      });

  /// The shell made visible by a change on its own record.
  Uint8List shellShown(int stamp, int base) => shellChange(stamp, base, (w) => _visible(w, true));

  /// The shell retitled by a change on its own record, still hidden.
  Uint8List shellRetitled(int stamp, int base) => shellChange(stamp, base, (w) => w
    ..key(WireKey.k_Decorations_text)
    ..string('Workbench - retitled'));

  /// The display's record, listing the shell.
  Uint8List display(int stamp) => encodeRecord(wireHolder, (w) => w
    ..varint(WireType.display * 2 + 1)
    ..int32(displayId)
    ..varint(stamp)
    ..key(WireKey.k_Display_shells)
    ..varint(2) // one ref, counted as length + 1
    ..ref(shellId)
    ..key(WireKey.k_Display_mainShellId)
    ..int64(shellId));

  VShell? get held => VRegistry.instance.valueOn('Shell/$shellId') as VShell?;

  Finder get drawn => find.byWidgetPredicate((w) => w is ShellSwt && w.value.id == shellId);
}

void main() {
  setUp(resetConfigFlags);
  tearDown(() {
    resetConfigFlags();
    VRegistry.instance.clear();
  });

  /// Mounts the display as on first load: the shell is listed, not yet shown.
  Future<void> mountDisplay(WidgetTester tester, _Session s) async {
    tester.view.physicalSize = _viewport;
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    VRegistry.instance.applyWire(encodeFrame([s.shellWhole(10), s.display(11)]));
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: DisplaySwt(value: VDisplay()..id = s.displayId),
    ));
    await tester.pumpAndSettle();
    expect(s.held?.visible, isFalse);
    expect(s.drawn, findsNothing, reason: 'a hidden shell is held, not drawn');
  }

  testWidgets('a shell shown by a change on its own record is drawn', (tester) async {
    final s = _Session(1);
    await mountDisplay(tester, s);

    VRegistry.instance.applyWire(encodeFrame([s.shellShown(12, 10)]));
    await tester.pumpAndSettle();

    expect(s.held?.visible, isTrue, reason: 'the change applied to the shell the display lists');
    expect(s.drawn, findsOneWidget,
        reason: 'the display lists the shell and the shell is visible: with no further display '
            'record, nothing else draws it and the page stays blank');
  });

  testWidgets('a shell shown and then followed by a display record is drawn', (tester) async {
    final s = _Session(2);
    await mountDisplay(tester, s);

    VRegistry.instance.applyWire(encodeFrame([s.shellShown(12, 10), s.display(13)]));
    await tester.pumpAndSettle();

    expect(s.drawn, findsOneWidget);
  });

  testWidgets('a change inside a shell that stays hidden does not rebuild the display', (tester) async {
    final s = _Session(3);
    await mountDisplay(tester, s);
    var displayBuilds = 0;
    debugOnRebuildDirtyWidget = (element, _) {
      if (element.widget is DisplaySwt) displayBuilds++;
    };
    addTearDown(() => debugOnRebuildDirtyWidget = null);

    VRegistry.instance.applyWire(encodeFrame([s.shellRetitled(12, 10)]));
    await tester.pumpAndSettle();

    expect(s.held?.text, 'Workbench - retitled', reason: 'the change applied to the shell the display lists');
    expect(s.drawn, findsNothing);
    expect(displayBuilds, 0, reason: 'the shells the display draws did not change');
  });
}
