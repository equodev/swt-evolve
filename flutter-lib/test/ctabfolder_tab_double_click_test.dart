// A double-click on a tab maximizes its part stack in an Eclipse workbench: the workbench listens
// for SWT.MouseDoubleClick on the CTabFolder and acts when the press is inside the tab strip
// (`e.y <= getTabHeight()`). CTabFolder itself answers the same event with DefaultSelection when
// `getItem(new Point(e.x, e.y))` finds a tab. Both need the event, in the folder's coordinates.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/utils/double_tap_detector.dart';

class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, String ev, VEvent? payload) => onEvent(ev, payload);
}

const _labels = ['Cube 1', 'Map 2', 'Section 3'];

VCTabFolder _folder({bool controls = false}) => VCTabFolder()
  ..id = 1
  ..minimizeVisible = controls
  ..maximizeVisible = controls
  ..style = SWT.NONE
  ..enabled = true
  ..selection = 0
  ..items = List.generate(
    _labels.length,
    (i) => VCTabItem()
      ..id = 100 + i
      ..text = _labels[i]
      ..showing = true,
  );

void main() {
  late DateTime fake;

  setUp(() {
    fake = DateTime(2020);
    DoubleTapDetector.clock = () => fake;
  });
  tearDown(() => DoubleTapDetector.clock = DateTime.now);

  Future<List<VEvent>> pumpFolder(WidgetTester tester, {bool controls = false}) async {
    final doubleClicks = <VEvent>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 500,
        height: 240,
        child: _CapturingCTabFolderSwt(
          value: _folder(controls: controls),
          onEvent: (ev, payload) {
            if (ev == 'Mouse/MouseDoubleClick') doubleClicks.add(payload ?? VEvent());
          },
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}
    return doubleClicks;
  }

  Future<void> click(WidgetTester tester, Offset at, {int buttons = kPrimaryButton}) async {
    final gesture = await tester.startGesture(at, buttons: buttons);
    await tester.pump();
    await gesture.up();
    await tester.pump();
    fake = fake.add(const Duration(milliseconds: 60));
  }

  Rect folderRect(WidgetTester tester) =>
      tester.getRect(find.byWidgetPredicate((w) => w is CTabFolderSwt<VCTabFolder>));

  testWidgets('double-clicking a tab reports MouseDoubleClick on that tab',
      (WidgetTester tester) async {
    final doubleClicks = await pumpFolder(tester);

    final tab = tester.getRect(
        find.ancestor(of: find.text(_labels[1]), matching: find.byType(AnimatedContainer)).first);
    final folder = folderRect(tester);

    await click(tester, tab.center);
    expect(doubleClicks, isEmpty, reason: 'a single click is not a double-click');
    await click(tester, tab.center);

    expect(doubleClicks, hasLength(1));
    final e = doubleClicks.single;
    expect(e.button, 1);
    expect(e.count, 2);
    expect(e.x, closeTo(tab.center.dx - folder.left, 1),
        reason: 'CTabFolder.getItem(Point) has to find the tab at this point');
    expect(e.y, closeTo(tab.center.dy - folder.top, 1));
    expect(e.y, lessThanOrEqualTo(tab.bottom - folder.top),
        reason: 'the workbench ignores a double-click below the tab strip');

    await tester.pump(const Duration(milliseconds: 400));
  });

  testWidgets('double-clicking the empty part of the strip reports MouseDoubleClick',
      (WidgetTester tester) async {
    final doubleClicks = await pumpFolder(tester);

    final lastTab = tester.getRect(
        find.ancestor(of: find.text(_labels.last), matching: find.byType(AnimatedContainer)).first);
    final folder = folderRect(tester);
    final empty = Offset((lastTab.right + folder.right) / 2, lastTab.center.dy);

    await click(tester, empty);
    await click(tester, empty);

    expect(doubleClicks, hasLength(1),
        reason: 'the strip belongs to the folder, so a native CTabFolder reports it there too');

    await tester.pump(const Duration(milliseconds: 400));
  });

  // GTK and Win32 report a double click of any button; the workbench and CTabFolder act on button 1 only.
  testWidgets('a secondary-button double-click is reported with its button',
      (WidgetTester tester) async {
    final doubleClicks = await pumpFolder(tester);

    final tab = tester.getCenter(find.text(_labels[0]));
    await click(tester, tab, buttons: kSecondaryButton);
    await click(tester, tab, buttons: kSecondaryButton);

    expect(doubleClicks, hasLength(1));
    expect(doubleClicks.single.button, 3);

    await tester.pump(const Duration(milliseconds: 400));
  });

  // A native CTabFolder draws its minimize and maximize buttons as a ToolBar of its own, which takes
  // the press; a workbench that hears the folder's MouseDoubleClick there maximizes the part.
  testWidgets('double-clicking the maximize button reports no MouseDoubleClick on the folder',
      (WidgetTester tester) async {
    final doubleClicks = await pumpFolder(tester, controls: true);

    final button = tester.getCenter(find.byIcon(Icons.fullscreen));
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: button);
    await tester.pumpAndSettle();
    for (var i = 0; i < 2; i++) {
      await mouse.down(button);
      await tester.pump();
      await mouse.up();
      await tester.pump();
      fake = fake.add(const Duration(milliseconds: 60));
    }
    await mouse.removePointer();

    expect(doubleClicks, isEmpty);

    await tester.pump(const Duration(milliseconds: 400));
  });

  testWidgets('a click on a tab focuses no CTabFolder', (WidgetTester tester) async {
    final focus = <String>[];
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 500,
        height: 240,
        child: _CapturingCTabFolderSwt(
          value: _folder(),
          onEvent: (ev, payload) {
            if (ev.startsWith('Focus/')) focus.add(ev);
          },
        ),
      ),
    ));
    await tester.pumpAndSettle();
    while (tester.takeException() != null) {}

    await click(tester, tester.getCenter(find.text(_labels[1])));
    await tester.pump(const Duration(milliseconds: 400));

    expect(focus, isEmpty);
  });
}
