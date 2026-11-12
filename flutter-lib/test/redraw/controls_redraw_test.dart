// What each operation on each kind of control rebuilds and redraws, in a window holding one of
// every common control. An operation on one control should redraw that control and nothing else.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/ccombo.dart';
import 'package:swtflutter/src/gen/clabel.dart';
import 'package:swtflutter/src/gen/combo.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/datetime.dart';
import 'package:swtflutter/src/gen/group.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/link.dart';
import 'package:swtflutter/src/gen/list.dart';
import 'package:swtflutter/src/gen/progressbar.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scale.dart';
import 'package:swtflutter/src/gen/slider.dart';
import 'package:swtflutter/src/gen/spinner.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import '../delivery/support/deliver.dart';
import 'support/budget.dart';
import 'support/redraw_probe.dart';

const double _width = 1200;
const double _height = 800;
const int _columns = 4;
const int _cellWidth = 280;
const int _cellHeight = 90;

/// One kind of control: how Java describes it, and one change Java makes to it.
class _Kind {
  const _Kind(this.name, this.create, this.change, this.keys, {this.again});

  final String name;
  final VControl Function() create;

  /// The change, as a value holding only what changed.
  final VControl Function() change;
  final List<int> keys;

  /// A second change after [change], where the first one costs what later ones do not.
  final VControl Function()? again;
}

final _kinds = <_Kind>[
  _Kind(
    'label',
    () => VLabel()
      ..style = SWT.NONE
      ..text = 'A label',
    () => VLabel()..text = 'Changed label',
    [WireKey.k_Label_text],
  ),
  _Kind(
    'button',
    () => VButton()
      ..style = SWT.PUSH
      ..text = 'Push',
    () => VButton()..text = 'Pushed',
    [WireKey.k_Button_text],
  ),
  _Kind(
    'check',
    () => VButton()
      ..style = SWT.CHECK
      ..text = 'Check',
    () => VButton()..selection = true,
    [WireKey.k_Button_selection],
  ),
  _Kind(
    'radio',
    () => VButton()
      ..style = SWT.RADIO
      ..text = 'Radio',
    () => VButton()..selection = true,
    [WireKey.k_Button_selection],
  ),
  _Kind(
    'text',
    () => VText()
      ..style = SWT.SINGLE | SWT.BORDER
      ..editable = true
      ..text = 'Some text',
    () => VText()..text = 'Some other text',
    [WireKey.k_Text_text],
    // The first new text gives the field a layer of its own, which repaints the window once; a
    // field whose text never changes keeps none.
    again: () => VText()..text = 'Yet another text',
  ),
  _Kind(
    'text.multi',
    () => VText()
      ..style = SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
      ..editable = true
      ..text = 'one\ntwo\nthree',
    () => VText()..text = 'one\ntwo\nthree\nfour',
    [WireKey.k_Text_text],
  ),
  _Kind(
    'combo',
    () => VCombo()
      ..style = SWT.DROP_DOWN
      ..items = ['alpha', 'beta', 'gamma']
      ..text = 'alpha',
    () => VCombo()..text = 'beta',
    [WireKey.k_Combo_text],
  ),
  _Kind(
    'ccombo',
    () => VCCombo()
      ..style = SWT.BORDER
      ..items = ['alpha', 'beta', 'gamma']
      ..text = 'alpha',
    () => VCCombo()..text = 'beta',
    [WireKey.k_CCombo_text],
  ),
  _Kind(
    'list',
    () => VList()
      ..style = SWT.SINGLE | SWT.BORDER
      ..items = ['one', 'two', 'three', 'four'],
    () => VList()..selection = [2],
    [WireKey.k_List_selection],
  ),
  _Kind(
    'progress',
    () => VProgressBar()
      ..style = SWT.HORIZONTAL
      ..maximum = 100
      ..selection = 30,
    () => VProgressBar()..selection = 60,
    [WireKey.k_ProgressBar_selection],
  ),
  _Kind(
    'scale',
    () => VScale()
      ..style = SWT.HORIZONTAL
      ..maximum = 100
      ..selection = 30,
    () => VScale()..selection = 60,
    [WireKey.k_Scale_selection],
  ),
  _Kind(
    'slider',
    () => VSlider()
      ..style = SWT.HORIZONTAL
      ..maximum = 100
      ..thumb = 10
      ..selection = 30,
    () => VSlider()..selection = 60,
    [WireKey.k_Slider_selection],
  ),
  _Kind(
    'spinner',
    () => VSpinner()
      ..style = SWT.BORDER
      ..maximum = 100
      ..selection = 3,
    () => VSpinner()..selection = 4,
    [WireKey.k_Spinner_selection],
  ),
  _Kind('link', () => VLink()..text = 'Visit <a>the site</a> now', () => VLink()..text = 'Visit <a>another</a>', [
    WireKey.k_Link_text,
  ]),
  _Kind(
    'clabel',
    () => VCLabel()
      ..style = SWT.NONE
      ..text = 'A custom label',
    () => VCLabel()..text = 'Changed custom label',
    [WireKey.k_CLabel_text],
  ),
  _Kind(
    'group',
    () => VGroup()
      ..style = SWT.NONE
      ..text = 'Group',
    () => VGroup()..text = 'Renamed group',
    [WireKey.k_Group_text],
  ),
  _Kind(
    'datetime',
    () => VDateTime()
      ..style = SWT.DATE
      ..year = 2026
      ..month = 9
      ..day = 12,
    () => VDateTime()..day = 13,
    [WireKey.k_DateTime_day],
  ),
  _Kind(
    'tabfolder',
    () => VTabFolder()
      ..style = SWT.TOP
      ..items = [
        VTabItem()
          ..id = 90001
          ..text = 'First',
        VTabItem()
          ..id = 90002
          ..text = 'Second',
      ]
      ..selection = [VTabItem()..id = 90001],
    () => VTabFolder()..selection = [VTabItem()..id = 90002],
    [WireKey.k_TabFolder_selection],
  ),
];

int _idOf(int index) => 100 + index;

VRectangle _cell(int index) => VRectangle()
  ..x = 10 + (index % _columns) * (_cellWidth + 10)
  ..y = 10 + (index ~/ _columns) * (_cellHeight + 10)
  ..width = _cellWidth
  ..height = _tall.contains(_kinds[index].name) ? _cellHeight : 28;

const _tall = {'text.multi', 'list', 'group', 'tabfolder'};

VComposite _window() => VComposite()
  ..id = 1
  ..seq = 1
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = (VRectangle()
    ..width = _width.toInt()
    ..height = _height.toInt())
  ..children = [
    for (var i = 0; i < _kinds.length; i++)
      _kinds[i].create()
        ..id = _idOf(i)
        ..seq = 1
        ..enabled = true
        ..visible = true
        ..bounds = _cell(i),
  ];

const budgets = <String, Budget>{
  'label.java': Budget(2, 338, 1.00),
  'label.hover': Budget(0, 0, 0.01),
  'label.click': Budget(0, 0, 0.01),
  'label.disable': Budget(12, 338, 1.01),
  'button.java': Budget(11, 327, 1.00),
  'button.hover': Budget(22, 654, 2.00),
  'button.click': Budget(32, 1475, 1.35),
  'button.disable': Budget(18, 28, 0.01),
  'check.java': Budget(3, 180, 0.01),
  'check.hover': Budget(0, 0, 0.01),
  'check.click': Budget(22, 80, 0.01),
  'check.disable': Budget(30, 378, 1.02),
  'radio.java': Budget(23, 140, 0.01),
  'radio.hover': Budget(0, 0, 0.01),
  'radio.click': Budget(0, 0, 0.01),
  'radio.disable': Budget(30, 417, 1.02),
  'text.java': Budget(1, 257, 1.01),
  'text.java.again': Budget(1, 2, 0.01),
  'text.hover': Budget(60, 530, 2.01),
  'text.click': Budget(157, 1058, 2.19),
  'text.disable': Budget(102, 550, 0.18),
  'text.multi.java': Budget(19, 245, 1.00),
  'text.multi.hover': Budget(78, 747, 3.06),
  'text.multi.click': Budget(181, 590, 1.61),
  'text.multi.disable': Budget(116, 547, 1.58),
  'combo.java': Budget(14, 210, 1.00),
  'combo.hover': Budget(0, 0, 0.01),
  'combo.click': Budget(70, 210, 1.00),
  'combo.disable': Budget(104, 420, 2.00),
  'ccombo.java': Budget(2, 210, 1.00),
  'ccombo.hover': Budget(113, 1470, 3.21),
  'ccombo.click': Budget(325, 237, 0.03),
  'ccombo.disable': Budget(58, 858, 0.22),
  'list.java': Budget(68, 15, 0.02),
  'list.hover': Budget(14, 0, 0.01),
  'list.click': Budget(75, 30, 0.03),
  'list.disable': Budget(60, 216, 1.03),
  'progress.java': Budget(5, 176, 1.00),
  'progress.hover': Budget(0, 0, 0.01),
  'progress.click': Budget(0, 0, 0.01),
  'progress.disable': Budget(10, 176, 1.01),
  'scale.java': Budget(26, 159, 1.00),
  'scale.hover': Budget(48, 318, 2.00),
  'scale.click': Budget(102, 223, 1.03),
  'scale.disable': Budget(67, 320, 1.09),
  'slider.java': Budget(16, 126, 1.00),
  'slider.hover': Budget(0, 0, 0.01),
  'slider.click': Budget(28, 252, 2.00),
  'slider.disable': Budget(18, 155, 1.09),
  'spinner.java': Budget(43, 64, 0.01),
  'spinner.hover': Budget(46, 234, 0.04),
  'spinner.click': Budget(163, 1174, 1.18),
  'spinner.disable': Budget(147, 1044, 1.17),
  'link.java': Budget(6, 16, 0.01),
  'link.hover': Budget(6, 32, 0.01),
  'link.click': Budget(6, 32, 0.01),
  'link.disable': Budget(11, 16, 0.01),
  'clabel.java': Budget(4, 5, 0.01),
  'clabel.hover': Budget(0, 0, 0.01),
  'clabel.click': Budget(0, 0, 0.01),
  'clabel.disable': Budget(9, 113, 1.01),
  'group.java': Budget(16, 20, 0.03),
  'group.hover': Budget(0, 0, 0.01),
  'group.click': Budget(0, 0, 0.01),
  'group.disable': Budget(16, 0, 0.01),
  'datetime.java': Budget(37, 62, 0.01),
  'datetime.hover': Budget(0, 0, 0.01),
  'datetime.click': Budget(81, 26, 0.01),
  'datetime.disable': Budget(84, 62, 0.01),
  'tabfolder.java': Budget(69, 51, 0.03),
  'tabfolder.hover': Budget(0, 0, 0.01),
  'tabfolder.click': Budget(0, 0, 0.01),
  'tabfolder.disable': Budget(130, 51, 0.03),
};

void main() {
  testWidgets('every operation on every control', (tester) async {
    tester.view.physicalSize = const Size(_width, _height);
    tester.view.devicePixelRatio = 1;
    addTearDown(tester.view.reset);
    freshClient();
    await tester.pumpWidget(
      EvolveApp(
        theme: ThemeMode.light,
        contentWidget: Align(
          alignment: Alignment.topLeft,
          // A Shell's own layer, as the Display gives every shell it shows.
          child: RepaintBoundary(
            child: SizedBox(
              width: _width,
              height: _height,
              child: CompositeSwt<VComposite>(value: _window()),
            ),
          ),
        ),
      ),
    );
    await tester.pumpAndSettle();

    final probe = RedrawProbe(tester, const Size(_width, _height));
    final costs = <RedrawCost>[];
    final stamps = <int, int>{};
    void java(VControl value, int id, List<int> keys) {
      final base = stamps[id] ?? 1;
      value
        ..id = id
        ..seq = base + 1;
      stamps[id] = base + 1;
      applyChange(value, keys: keys, base: base);
    }

    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: const Offset(_width - 5, _height - 5));
    addTearDown(mouse.removePointer);
    await tester.pumpAndSettle();

    for (var i = 0; i < _kinds.length; i++) {
      final kind = _kinds[i];
      final id = _idOf(i);
      final cell = _cell(i);
      final center = Offset(cell.x! + cell.width! / 2, cell.y! + cell.height! / 2);
      Future<void> measure(String op, Future<void> Function() run) async {
        costs.add(await probe.measure('${kind.name}.$op', run));
        while (tester.takeException() != null) {}
      }

      await measure('java', () async => java(kind.change(), id, kind.keys));
      final again = kind.again;
      if (again != null) await measure('java.again', () async => java(again(), id, kind.keys));
      await measure('hover', () async {
        await mouse.moveTo(center);
        await tester.pump(const Duration(milliseconds: 50));
        await mouse.moveTo(const Offset(_width - 5, _height - 5));
      });
      await measure('click', () async {
        await mouse.moveTo(center);
        await mouse.down(center);
        await tester.pump(const Duration(milliseconds: 30));
        await mouse.up();
        await tester.pump(const Duration(milliseconds: 300));
        await mouse.moveTo(const Offset(_width - 5, _height - 5));
      });
      // Closes whatever the click opened (a combo's list, a date picker) before the next control.
      await tester.tapAt(const Offset(_width - 5, _height - 5));
      await tester.pumpAndSettle();
      await measure('disable', () async => java(kind.create()..enabled = false, id, [WireKey.k_Control_enabled]));
    }
    probe.uninstall();
    // ignore: avoid_print
    print('\n[redraw] summary\n${costs.join('\n')}');
    expectWithinBudgets(costs, budgets);
  });
}
