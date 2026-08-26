import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

const List<int> _pngBytes = <int>[
  0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, //
  0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
  0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
  0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, 0xC4,
  0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
  0x54, 0x78, 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
  0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4, 0x00,
  0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE,
  0x42, 0x60, 0x82,
];

VImage _icon() => VImage()
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..data = _pngBytes);

/// One trim-stack item: image only, no text — a restore PUSH or a part CHECK.
VToolItem _item(int id, int style) => VToolItem()
  ..id = id
  ..style = style
  ..enabled = true
  ..toolTipText = 'item$id'
  ..image = _icon();

/// The ToolBar an Eclipse trim stack creates: `orientation | SWT.FLAT | SWT.WRAP`
/// on a right/left trim bar, plus one PUSH restore item and one CHECK per part.
/// Its height is what `Sizes.computeSize(DartToolBar)` reports — 22 per item.
VToolBar _trimStack(int itemCount, {int idBase = 100}) {
  final items = <VToolItem>[
    _item(idBase, SWT.PUSH),
    for (var i = 1; i < itemCount; i++) _item(idBase + i, SWT.CHECK),
  ];
  return VToolBar()
    ..id = idBase - 1
    ..style = SWT.VERTICAL | SWT.FLAT | SWT.WRAP
    ..enabled = true
    ..visible = true
    ..items = items
    ..bounds = (VRectangle()
      ..x = 0
      ..y = 0
      ..width = 24
      ..height = 22 * itemCount);
}

/// The trim bar hands the stack an UNBOUNDED height (a Column), so nothing but
/// the ToolBar's own bounds stops the strip from growing past its allocation.
Widget _inTrimColumn(VToolBar bar) => Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: 30,
        height: bar.bounds!.height.toDouble(),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [ToolBarSwt(value: bar)],
        ),
      ),
    );

bool _pointReachesItem(WidgetTester tester, Finder item, Offset global) {
  final box = tester.renderObject<RenderBox>(item);
  final result = HitTestResult();
  WidgetsBinding.instance.hitTestInView(result, global, tester.view.viewId);
  return result.path.any((entry) => identical(entry.target, box));
}

void main() {
  // One pumped tree per test file: a second EvolveApp mount in the same file never
  // settles, which is a harness artifact unrelated to this widget.
  testWidgets('a vertical trim toolbar fits its allocation and every item stays clickable',
      (tester) async {
    const itemCount = 8;
    final bar = _trimStack(itemCount);
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: _inTrimColumn(bar),
    ));
    await tester.pumpAndSettle();

    expect(
      tester.getSize(find.byType(ToolBarSwt)).height,
      lessThanOrEqualTo(bar.bounds!.height.toDouble()),
      reason: 'a strip taller than its allocation is painted outside the parent box, '
          'where Flutter no longer hit-tests it',
    );

    final items = find.byType(ToolItemSwt);
    expect(items, findsNWidgets(itemCount));

    for (var i = 0; i < itemCount; i++) {
      final rect = tester.getRect(items.at(i));
      // Not just the centre: a user aiming anywhere on the painted icon must land on
      // the item. The bug leaves the tail of the strip painted but pointer-dead.
      for (final dy in <double>[1, rect.height / 2, rect.height - 1]) {
        expect(
          _pointReachesItem(tester, items.at(i), Offset(rect.center.dx, rect.top + dy)),
          isTrue,
          reason: 'item $i is painted at $rect but a pointer at +$dy does not reach it',
        );
      }
    }
  });
}
