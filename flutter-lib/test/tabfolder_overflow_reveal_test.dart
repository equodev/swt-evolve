// A TabFolder narrower than its tabs keeps every tab reachable and the selected tab in view. Java
// selects the tab an arrow key or setSelection picks (TabFolderHelper.moveSelectionByKey), so both
// reach the strip as a new selection.

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/tabfolder.dart';
import 'package:swtflutter/src/gen/tabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;
// ignore: unused_import
import 'package:swtflutter/src/impl/tabfolder_evolve.dart';
import 'delivery/support/deliver.dart';

const _folderId = 1000;
const double _width = 300;
const double _height = 160;
final _labels = [for (var i = 1; i <= 12; i++) 'Page ${i.toString().padLeft(2, '0')}'];

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

final _items = [
  for (final (i, label) in _labels.indexed)
    VTabItem()
      ..id = _folderId + 10 + 2 * i
      ..text = label
      ..control = (VComposite()
        ..id = _folderId + 11 + 2 * i
        ..style = SWT.NONE
        ..bounds = _rect(0, 0, _width.toInt(), _height.toInt() - 30)),
];

VTabFolder _folder({int selected = 0, int seq = 0}) => VTabFolder()
  ..id = _folderId
  ..seq = seq
  ..style = SWT.TOP
  ..enabled = true
  ..bounds = _rect(0, 0, _width.toInt(), _height.toInt())
  ..items = _items
  ..selection = [_items[selected]];

class _TabFolderUnderTest extends TabFolderSwt<VTabFolder> {
  const _TabFolderUnderTest({required super.value, this.onEvent});

  final void Function(String channel, VEvent? payload)? onEvent;

  @override
  void sendEvent(VTabFolder val, int channel, VEvent? payload) => onEvent?.call(wireChannelNames[channel]!, payload);
}

Future<void> _pump(WidgetTester tester, VTabFolder value,
    {Map<ShortcutActivator, Intent> shortcuts = const {},
    void Function(String channel, VEvent? payload)? onEvent}) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Shortcuts(
      shortcuts: shortcuts,
      child: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: _width, height: _height, child: _TabFolderUnderTest(value: value, onEvent: onEvent)),
      ),
    ),
  ));
  await _settle(tester);
}

Future<void> _settle(WidgetTester tester) async {
  await tester.pumpAndSettle();
  // Tabs running past the folder are this test's subject, not a layout error.
  while (tester.takeException() != null) {}
}

Finder get _folderWidget => find.byWidgetPredicate((w) => w is TabFolderSwt<VTabFolder>);

/// The band the tabs sit in: the folder's width, above the page.
Rect _strip(WidgetTester tester) {
  final folder = tester.getRect(_folderWidget);
  final pageTop = tester.getRect(find.descendant(of: _folderWidget, matching: find.byType(IndexedStack))).top;
  return Rect.fromLTRB(folder.left, folder.top, folder.right, pageTop);
}

Finder _label(String label) => find.descendant(of: _folderWidget, matching: find.text(label));

Rect _tab(WidgetTester tester, String label) =>
    tester.getRect(find.ancestor(of: _label(label), matching: find.byType(InkWell)).first);

/// The whole tab lies inside the strip and a press on its label lands on it, not on whatever
/// covers that part of the strip.
bool _shown(WidgetTester tester, String label) {
  final strip = _strip(tester);
  final tab = _tab(tester, label);
  if (tab.left < strip.left - 0.5 || tab.right > strip.right + 0.5) return false;
  final paragraph = tester.renderObject(_label(label));
  final hit = tester.hitTestOnBinding(tester.getCenter(_label(label)));
  return hit.path.any((entry) => identical(entry.target, paragraph) || _isBelow(entry.target, paragraph));
}

bool _isBelow(HitTestTarget target, RenderObject ancestor) {
  if (target is! RenderObject) return false;
  for (RenderObject? node = target; node != null; node = node.parent) {
    if (identical(node, ancestor)) return true;
  }
  return false;
}

void main() {
  testWidgets('a folder that opens on a tab past the strip shows that tab', (WidgetTester tester) async {
    await _pump(tester, _folder(selected: 11));

    expect(_shown(tester, 'Page 01') && _shown(tester, 'Page 12'), isFalse,
        reason: 'sanity: twelve tabs do not fit a $_width px folder');
    expect(_shown(tester, 'Page 12'), isTrue, reason: 'the selected tab is in view');
  });

  testWidgets('a tab Java selects past the strip, by setSelection or an arrow key, is brought into it',
      (WidgetTester tester) async {
    await _pump(tester, _folder());
    expect(_shown(tester, 'Page 01'), isTrue, reason: 'sanity: the first tab starts in view');
    expect(_shown(tester, 'Page 12'), isFalse, reason: 'sanity: the last tab starts out of view');

    await deliverWhole(_folder(selected: 11, seq: 2));
    await _settle(tester);
    expect(_shown(tester, 'Page 12'), isTrue);

    await deliverWhole(_folder(selected: 0, seq: 3));
    await _settle(tester);
    expect(_shown(tester, 'Page 01'), isTrue, reason: 'selecting the first tab again brings it back');
  });

  // Whatever the strip offers for it (arrows at its end, a wheel over it, a tab that scrolls into
  // view when pressed), the pointer alone reaches the last tab.
  testWidgets('the pointer reaches every tab of a strip that overflows', (WidgetTester tester) async {
    await _pump(tester, _folder());
    expect(_shown(tester, 'Page 01'), isTrue, reason: 'sanity: the first tab starts in view');
    expect(_shown(tester, 'Page 12'), isFalse, reason: 'sanity: the last tab starts out of view');

    for (var i = 0; i < 40 && !_shown(tester, 'Page 12'); i++) {
      final strip = _strip(tester);
      final pointer = TestPointer(1, PointerDeviceKind.mouse);
      final over = strip.center;
      await tester.sendEventToBinding(pointer.hover(over));
      await tester.sendEventToBinding(pointer.scroll(const Offset(0, 120)));
      await tester.sendEventToBinding(pointer.scroll(const Offset(120, 0)));
      await _settle(tester);
      if (_shown(tester, 'Page 12')) break;
      await tester.tapAt(Offset(strip.right - 4, strip.center.dy));
      await _settle(tester);
    }
    expect(_shown(tester, 'Page 12'), isTrue);
  });

  testWidgets('the arrows at the strip\'s end step through the tabs without selecting one',
      (WidgetTester tester) async {
    await _pump(tester, _folder());
    final strip = _strip(tester);
    final back = tester.getRect(_arrow(Icons.chevron_left));
    final forward = tester.getRect(_arrow(Icons.chevron_right));
    expect(forward.right, moreOrLessEquals(strip.right, epsilon: 1), reason: 'the arrows end the strip');
    expect(back.right, moreOrLessEquals(forward.left, epsilon: 1));
    expect(_tab(tester, 'Page 01').left, moreOrLessEquals(strip.left, epsilon: 1));

    await tester.tapAt(back.center);
    await _settle(tester);
    expect(_tab(tester, 'Page 01').left, moreOrLessEquals(strip.left, epsilon: 1),
        reason: 'the strip is at its start already');

    await tester.tapAt(forward.center);
    await _settle(tester);
    expect(_tab(tester, 'Page 02').left, moreOrLessEquals(strip.left, epsilon: 1), reason: 'one tab on');

    for (var i = 0; i < 12; i++) {
      await tester.tapAt(forward.center);
      await _settle(tester);
    }
    expect(_shown(tester, 'Page 12'), isTrue);
    expect(_tab(tester, 'Page 12').right, lessThanOrEqualTo(back.left + 0.5), reason: 'no tab under the arrows');
    expect(_selectedPage(tester), 'Page 01', reason: 'scrolling the strip selects nothing');

    await tester.tapAt(back.center);
    await _settle(tester);
    expect(_shown(tester, 'Page 12'), isFalse, reason: 'one tab back');
  });

  testWidgets('an arrow key on a tab moves the selection only, not the strip, under the web\'s shortcuts',
      (WidgetTester tester) async {
    await _pump(tester, _folder(), shortcuts: const {
      SingleActivator(LogicalKeyboardKey.arrowRight): ScrollIntent(direction: AxisDirection.right),
    });
    final strip = _strip(tester);
    await tester.tap(_label('Page 01'));
    await _settle(tester);
    await tester.sendKeyEvent(LogicalKeyboardKey.arrowRight);
    await _settle(tester);
    expect(_tab(tester, 'Page 01').left, moreOrLessEquals(strip.left, epsilon: 1));
  });

  // Java lays the tabs out from the folder's left edge: TabItem.getBounds, TabFolder.getItem(Point)
  // and the press focus read the scroll and the viewport from this report.
  testWidgets('the strip reports its scroll and viewport to Java, and that it no longer scrolls once the tabs fit',
      (WidgetTester tester) async {
    final reports = <String>[];
    await _pump(tester, _folder(), onEvent: (channel, payload) {
      if (channel == 'TabFolder/stripLaidOut') reports.add(payload?.text ?? '');
    });
    expect(reports, isNotEmpty);
    final viewport = int.parse(reports.last.split(',')[1]);
    expect(reports.last, '0,$viewport');
    expect(viewport, lessThan(_width), reason: 'the arrows sit past the viewport');

    final pointer = TestPointer(1, PointerDeviceKind.mouse);
    await tester.sendEventToBinding(pointer.hover(_strip(tester).center));
    await tester.sendEventToBinding(pointer.scroll(const Offset(0, 120)));
    await _settle(tester);
    final scrolled = (_strip(tester).left - _tab(tester, 'Page 01').left).round();
    expect(scrolled, greaterThan(0), reason: 'sanity: the wheel scrolled the strip');
    expect(reports.last, '$scrolled,$viewport');

    await deliverWhole(_folder(seq: 2)..items = _items.take(2).toList());
    await _settle(tester);
    expect(reports.last, '');
  });

  testWidgets('a strip whose tabs fit has no arrows', (WidgetTester tester) async {
    final value = _folder()..items = _items.take(2).toList();
    await _pump(tester, value);
    expect(_shown(tester, 'Page 02'), isTrue, reason: 'sanity: two tabs fit');
    expect(_arrow(Icons.chevron_right), findsNothing);
    expect(_arrow(Icons.chevron_left), findsNothing);
  });
}

Finder _arrow(IconData icon) => find.descendant(of: _folderWidget, matching: find.byIcon(icon));

String? _selectedPage(WidgetTester tester) {
  final stack = tester.widget<IndexedStack>(find.descendant(of: _folderWidget, matching: find.byType(IndexedStack)));
  final index = stack.index;
  return index == null ? null : _labels[index];
}
