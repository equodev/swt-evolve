// An on-screen icon-only ToolItem must keep its semantics identifier node.
//
// On Flutter Web an identifier-only semantics node with no accessible name of its own is absorbed
// into its one unnamed child; the surviving leaf keeps the button role but loses the identifier, so
// a control named only by its tooltip has no addressable node while on screen. tagSemantics marks
// such a node explicitChildNodes, keeping it a container that retains its node and id the way a
// ToolBar always did. This guards that structural invariant at the framework layer: the node is a
// container, not a merged leaf (childrenCount was 0 before the fix). The DOM-attribute symptom
// itself only reproduces in the real web engine, which flutter test does not model.

import 'package:flutter/gestures.dart' show PointerDeviceKind;
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

// A 1x1 PNG, so the item is genuinely icon-only (no text to name it).
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

SemanticsNode? _nodeWith(WidgetTester tester, String identifier) {
  SemanticsNode? found;
  void visit(SemanticsNode n) {
    if (n.getSemanticsData().identifier == identifier) found = n;
    n.visitChildren((c) {
      visit(c);
      return true;
    });
  }

  visit(tester.binding.pipelineOwner.semanticsOwner!.rootSemanticsNode!);
  return found;
}

VToolBar _iconBar() => VToolBar()
  ..id = 200
  ..style = SWT.HORIZONTAL | SWT.FLAT
  ..enabled = true
  ..visible = true
  ..items = [
    VToolItem()
      ..id = 201
      ..style = SWT.PUSH
      ..enabled = true
      ..toolTipText = 'Skip All Breakpoints (Ctrl+Alt+B)'
      ..image = (VImage()
        ..imageData = (VImageData()
          ..width = 16
          ..height = 16
          ..data = _pngBytes)),
  ]
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 400
    ..height = 40);

Widget _host() => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 400, height: 40, child: ToolBarSwt(value: _iconBar())),
      ),
    );

Future<void> _settle(WidgetTester tester) async {
  // The png may never finish decoding under test, so pump fixed frames rather than pumpAndSettle.
  for (var i = 0; i < 20; i++) {
    await tester.pump(const Duration(milliseconds: 100));
  }
  while (tester.takeException() != null) {}
}

void main() {
  testWidgets('an icon-only ToolItem identifier node is a container, not a merged leaf',
      (tester) async {
    final handle = tester.ensureSemantics();
    await tester.pumpWidget(_host());
    await _settle(tester);

    final node = _nodeWith(tester, 'ToolItem/201');
    expect(node, isNotNull, reason: 'the icon-only item must carry a semantics node');
    expect(node!.childrenCount, greaterThan(0),
        reason: 'the identifier node must keep its child as a separate node (a container); a merged '
            'leaf loses its flt-semantics-identifier on Flutter Web');
    handle.dispose();
  });

  testWidgets('the identifier node stays a container while the item is hovered', (tester) async {
    final handle = tester.ensureSemantics();
    await tester.pumpWidget(_host());
    await _settle(tester);

    final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await gesture.addPointer(location: Offset.zero);
    addTearDown(gesture.removePointer);
    await gesture.moveTo(tester.getCenter(find.byType(ToolBarSwt)));
    await _settle(tester);

    final node = _nodeWith(tester, 'ToolItem/201');
    expect(node, isNotNull, reason: 'still on screen while its tooltip is up');
    expect(node!.childrenCount, greaterThan(0),
        reason: 'hovering must not collapse the identifier node into a leaf');
    handle.dispose();
  });
}
