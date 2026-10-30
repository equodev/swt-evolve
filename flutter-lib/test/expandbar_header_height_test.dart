// Java decides how tall a header is (ExpandItem.getHeaderHeight: 24, the font's line or the item's image) and
// places everything from it: computeSize, the bounds of the items, the body under a header, the control in the body.
// The client draws a header exactly that tall, and the body, the next item and the margin where Java put them.
// Java sends the height as `headerHeight`; a platform that measures none sends 0 and the client keeps its own.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/expandbar.dart';
import 'package:swtflutter/src/gen/expanditem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widgets.dart' show wireChannelNames;

const _band = 24;
const _spacing = 4;
const _barWidth = 200;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VExpandItem _item(
  int id,
  String text, {
  bool expanded = false,
  int height = 0,
  int headerHeight = _band,
}) {
  final item = VExpandItem()
    ..id = id
    ..text = text
    ..expanded = expanded
    ..height = height
    ..headerHeight = headerHeight;
  if (height > 0) {
    item.control = VComposite()
      ..id = id + 100
      ..style = SWT.NONE
      ..enabled = true
      ..bounds = _rect(
        1,
        headerHeight,
        _barWidth - 2 * _spacing - 2,
        height - 1,
      )
      ..children = [
        VButton()
          ..id = id + 200
          ..style = SWT.PUSH
          ..enabled = true
          ..text = '$text button'
          ..bounds = _rect(5, 5, 100, 30),
      ];
  }
  return item;
}

VExpandBar _bar(List<VExpandItem> items, {VRectangle? bounds}) => VExpandBar()
  ..id = 1
  ..style = SWT.NONE
  ..enabled = true
  ..spacing = _spacing
  ..bounds = bounds
  ..items = items;

class _RecordingBar extends ExpandBarSwt<VExpandBar> {
  final List<String> calls;
  const _RecordingBar({required VExpandBar value, required this.calls})
    : super(value: value);

  @override
  void sendEvent(VExpandBar val, int channel, VEvent? payload) => calls.add(wireChannelNames[channel]!);
}

Future<void> _pump(
  WidgetTester tester,
  VExpandBar value, {
  List<String>? calls,
}) async {
  await tester.binding.setSurfaceSize(const Size(400, 1000));
  addTearDown(() => tester.binding.setSurfaceSize(null));
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: _barWidth.toDouble(),
          child: calls == null
              ? ExpandBarSwt(value: value)
              : _RecordingBar(value: value, calls: calls),
        ),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

/// The rectangle of the header of the item with this title: what a tap on it hits.
Rect _header(WidgetTester tester, String title) => tester.getRect(
  find
      .ancestor(of: find.text(title), matching: find.byType(GestureDetector))
      .first,
);

/// The rectangle of the whole item.
Rect _itemRect(WidgetTester tester, int id) => tester.getRect(
  find
      .byWidgetPredicate(
        (w) => w is Semantics && w.properties.identifier == 'ExpandItem/$id',
      )
      .last,
);

void main() {
  testWidgets('a header is as tall as the band Java measured', (tester) async {
    await _pump(
      tester,
      _bar([
        _item(10, 'Plain'),
        _item(11, 'Big font', headerHeight: 40),
        _item(12, 'Tall image', headerHeight: 31),
      ]),
    );

    expect(_header(tester, 'Plain').height, _band);
    expect(_header(tester, 'Big font').height, 40);
    expect(_header(tester, 'Tall image').height, 31);
    for (final title in ['Plain', 'Big font', 'Tall image']) {
      expect(
        _header(tester, title).width,
        _barWidth - 2 * _spacing,
        reason: '$title spans the bar inside the spacing',
      );
    }
  });

  testWidgets('the items sit at the coordinates Java gave them', (
    tester,
  ) async {
    await _pump(
      tester,
      _bar([
        _item(10, 'One'),
        _item(11, 'Two', expanded: true, height: 90),
        _item(12, 'Three'),
      ]),
    );

    final bar = tester.getRect(find.byType(ExpandBarSwt));
    // Cocoa's layoutItems: y = spacing, then the header, the body when open, and spacing again.
    var y = _spacing;
    for (final entry in {10: 0, 11: 90, 12: 0}.entries) {
      final item = _itemRect(tester, entry.key);
      expect(item.top - bar.top, y, reason: 'item ${entry.key}');
      expect(item.left - bar.left, _spacing);
      expect(item.height, _band + entry.value, reason: 'item ${entry.key}');
      y += _band + entry.value + _spacing;
    }
    expect(bar.height, greaterThanOrEqualTo(y));
  });

  testWidgets('the body starts under its header and is as tall as Java says', (
    tester,
  ) async {
    await _pump(tester, _bar([_item(11, 'Two', expanded: true, height: 90)]));

    final header = _header(tester, 'Two');
    final body = tester.getRect(find.byType(SizeTransition));
    expect(body.top, header.bottom);
    expect(body.height, 90);
    // The control is inside the body, which is inside the item's border.
    final item = _itemRect(tester, 11);
    final button = tester.getRect(find.text('Two button'));
    expect(button.top, greaterThanOrEqualTo(body.top));
    expect(button.bottom, lessThanOrEqualTo(body.bottom));
    expect(body.left, item.left);
    expect(body.right, item.right);
  });

  testWidgets('a bar Java sized to N headers shows N', (tester) async {
    const n = 4;
    // computeSize of N closed items: spacing, then a header and spacing for each.
    const javaHeight = _spacing + n * (_band + _spacing);
    await _pump(
      tester,
      _bar([
        for (var i = 0; i < n; i++) _item(10 + i, 'Header $i'),
      ], bounds: _rect(0, 0, _barWidth, javaHeight)),
    );

    expect(tester.getSize(find.byType(ExpandBarSwt)).height, javaHeight);
    for (var i = 0; i < n; i++) {
      expect(
        find.text('Header $i').hitTestable(),
        findsOneWidget,
        reason: 'Header $i',
      );
    }
    final scrollable = tester.state<ScrollableState>(find.byType(Scrollable));
    expect(
      scrollable.position.maxScrollExtent,
      0,
      reason: 'the bar is exactly as tall as its content: nothing to scroll',
    );
  });

  testWidgets(
    'a platform that measures no band keeps the header its content gives',
    (tester) async {
      await _pump(tester, _bar([_item(10, 'None', headerHeight: 0)]));

      expect(_header(tester, 'None').height, greaterThan(_band));
    },
  );

  testWidgets(
    'a tap on a header reports the press and the release, then the Expand',
    (tester) async {
      final calls = <String>[];
      await _pump(
        tester,
        _bar([_item(10, 'Tap me', height: 50)]),
        calls: calls,
      );

      await tester.tap(find.text('Tap me'));
      await tester.pumpAndSettle();

      // The bar reports the pointer like any control, so Java's own toggle on the MouseUp runs first and the
      // Expand that follows finds the state it asks for: ExpandBarHelper.sendExpand then fires nothing.
      expect(calls, ['Mouse/MouseDown', 'Mouse/MouseUp', 'Expand/Expand']);
      await tester.tap(find.text('Tap me'));
      await tester.pumpAndSettle();
      // One toggle per tap, whatever the press chrome around it says.
      expect(calls.where((c) => c.startsWith('Expand/')), [
        'Expand/Expand',
        'Expand/Collapse',
      ]);
    },
  );
}
