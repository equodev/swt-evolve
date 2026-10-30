// Where a tab sits in its folder does not depend on how a panel above the folder scales it: a slot
// smaller than twice panelChildGap leaves the panel's FittedBox empty, with an all-zero paint
// transform, and the strip measured through it must still be the one the folder draws.

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/custom/main_composite.dart';
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

/// Paints its child translated by NaN, as aligning it in an infinite size does.
class _Translate extends SingleChildRenderObjectWidget {
  const _Translate({required this.nan, super.child});

  final bool nan;

  @override
  RenderObject createRenderObject(BuildContext context) => _RenderTranslate(nan);

  @override
  void updateRenderObject(BuildContext context, _RenderTranslate renderObject) =>
      renderObject.nan = nan;
}

class _RenderTranslate extends RenderProxyBox {
  _RenderTranslate(this.nan);

  bool nan;

  @override
  void applyPaintTransform(RenderBox child, Matrix4 transform) {
    if (nan) transform.translate(double.nan, double.nan);
  }
}

class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, int channel, VEvent? payload) => onEvent(wireChannelNames[channel]!, payload);
}

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

const _labels = ['Project Explorer', 'Outline', 'Problems'];

// Shorter than twice the theme's panelChildGap, so the panel's FittedBox gets no height at all.
const _collapsedSlotHeight = 2;

VCTabFolder _folder({VRectangle? bounds}) => VCTabFolder()
  ..swt = 'CTabFolder'
  ..id = 10
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..selection = 0
  ..bounds = bounds
  ..items = List.generate(
    _labels.length,
    (i) => VCTabItem()
      ..id = 100 + i
      ..text = _labels[i]
      ..showing = true,
  );

Future<List<String>> _stripReports(WidgetTester tester, Widget Function(Widget folder) place) async {
  final reports = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: place(_CapturingCTabFolderSwt(
        value: _folder(),
        onEvent: (ev, payload) {
          if (ev == 'CTabFolder/stripLaidOut' && payload?.text != null) {
            reports.add(payload!.text!);
          }
        },
      )),
    ),
  ));
  await tester.pumpAndSettle();
  return reports;
}

void main() {
  testWidgets('a part stack squeezed into a collapsed sash slot lays out without throwing',
      (WidgetTester tester) async {
    final sash = VComposite()
      ..swt = 'MainComposite'
      ..id = 1
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _rect(0, 0, 199, _collapsedSlotHeight)
      ..children = [_folder(bounds: _rect(0, 0, 199, _collapsedSlotHeight))];

    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
            width: 199, height: _collapsedSlotHeight.toDouble(), child: MainComposite(value: sash)),
      ),
    ));
    await tester.pumpAndSettle();

    expect(tester.takeException(), isNull);
  });

  testWidgets('a tab placed at NaN in its folder gives no strip instead of throwing',
      (WidgetTester tester) async {
    final folderKey = GlobalKey();
    final tabKey = GlobalKey();
    Future<String?> reportWith({required bool nanOffset}) async {
      await tester.pumpWidget(Directionality(
        textDirection: TextDirection.ltr,
        child: Align(
          alignment: Alignment.topLeft,
          child: SizedBox(
            key: folderKey,
            width: 300,
            height: 32,
            child: _Translate(
              nan: nanOffset,
              child: Align(
                alignment: Alignment.topLeft,
                child: SizedBox(key: tabKey, width: 108, height: 32),
              ),
            ),
          ),
        ),
      ));
      final folder = folderKey.currentContext!.findRenderObject()! as RenderBox;
      final tab = tabKey.currentContext!.findRenderObject()! as RenderBox;
      return stripReport(folder, [DrawnTab(tab, null)]);
    }

    expect(await reportWith(nanOffset: false), '0,108;0,0,0,0');
    expect(await reportWith(nanOffset: true), isNull,
        reason: 'a folder laid out at an infinite size aligns its tabs at NaN for that frame');
  });

  testWidgets('a strip that could not be placed is asked for again on the next frame',
      (WidgetTester tester) async {
    var calls = 0;
    final folder = _folder();
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: TabDragRow(
          children: const [SizedBox(width: 50, height: 20)],
          tabs: const [],
          folderState: folder,
          folderWidget: CTabFolderSwt(value: folder),
          rowWrapper: (row) => row,
          onStripLayout: (_) => ++calls > 1,
        ),
      ),
    ));
    expect(calls, 1, reason: 'the first painted frame asks for the strip');

    await tester.pump();
    expect(calls, 2, reason: 'a frame that laid the folder out again, with no rebuild of the row');

    await tester.pump();
    expect(calls, 2, reason: 'once placed, nothing asks again until the row changes');
  });

  testWidgets('a folder drawn through an empty FittedBox reports the strip it reports when drawn',
      (WidgetTester tester) async {
    Widget sized(Widget folder) => SizedBox(width: 600, height: 200, child: folder);

    final drawn = await _stripReports(tester, sized);
    final collapsed = await _stripReports(
        tester,
        (folder) => SizedBox(
              width: 600,
              height: 0,
              child: FittedBox(fit: BoxFit.fill, child: sized(folder)),
            ));

    expect(tester.takeException(), isNull);
    expect(drawn, isNotEmpty, reason: 'a laid-out strip says where its tabs are');
    expect(collapsed, isNotEmpty, reason: 'a collapsed panel still lays its strip out');
    expect(collapsed.last, drawn.last,
        reason: 'where a tab sits in its folder does not depend on how the panel scales it');
  });
}
