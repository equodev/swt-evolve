// A Composite that owner-draws through SWT.Paint had its GC stacked OVER its children, so an
// application filling the composite's client area from a paint listener erased every control
// inside it. Native SWT cannot do that: a child is its own clipped region and the parent's paint
// never reaches it.
//
// The shape that motivated this: a text-viewer panel whose parent composite fills its whole client
// area with the theme's separator gray and whose only child is the StyledText holding the text.
// The fill landed on top and the pane read as one flat gray rectangle.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/gc.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/composite_evolve.dart';

VRectangle _bounds(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VComposite _panel() => VComposite()
  ..id = 560897187
  ..style = SWT.NONE
  ..enabled = true
  ..visible = true
  ..bounds = _bounds(0, 0, 1472, 408)
  ..children = [
    VComposite()
      ..id = 1217883757
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = _bounds(9, 0, 1454, 408),
  ];

/// The live GC layer: [CompositeImpl] keeps the GC mounted offstage until Java has drawn on it,
/// and only the visible form can occlude anything.
bool _isGCLayer(Widget w) => w is Positioned && w.child is IgnorePointer;

Future<GlobalKey<CompositeImpl>> _pumpWithGC(WidgetTester tester) async {
  final key = GlobalKey<CompositeImpl>();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 1472,
      height: 408,
      child: CompositeSwt<VComposite>(key: key, value: _panel()),
    ),
  ));
  key.currentState!.notifyGCReady(VGC()..id = 560897187);
  await tester.pump();
  return key;
}

void main() {
  testWidgets('the GC of a Composite with children paints under them', (tester) async {
    final key = await _pumpWithGC(tester);

    final stack = tester
        .widgetList<Stack>(find.byType(Stack))
        .firstWhere((s) => s.children.any(_isGCLayer));
    final gcIndex = stack.children.indexWhere(_isGCLayer);

    expect(gcIndex, lessThan(stack.children.length - 1),
        reason: "the children layer must paint above the composite's own GC drawing");
    expect(key.currentState!.gcOverlay, isNotNull);
  });

  testWidgets('and over its own background, so its drawing stays visible', (tester) async {
    final key = await _pumpWithGC(tester);

    const background = SizedBox.shrink();
    const children = SizedBox.expand();
    final stack = key.currentState!
        .stackGCUnderChildren((_) => background, children) as Stack;

    final backgroundIndex =
        stack.children.indexWhere((w) => w is Positioned && w.child == background);
    final gcIndex = stack.children.indexWhere(_isGCLayer);
    final childrenIndex = stack.children.indexOf(children);

    expect(backgroundIndex, isNonNegative);
    expect(backgroundIndex, lessThan(gcIndex));
    expect(gcIndex, lessThan(childrenIndex));
  });
}
