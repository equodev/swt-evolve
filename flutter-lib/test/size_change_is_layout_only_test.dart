// A control whose build reads its size only as the constraints its parent's layout imposes anyway is
// laid out again, not rebuilt, when only its size changes: a window or sash drag resizes every such
// control on every step. It ends at the size and position a fresh mount at the new bounds has.
// A change the build does read (a single-line Text's height) still rebuilds.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'delivery/support/deliver.dart';

VRectangle _rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

VText _text(VRectangle bounds, {int seq = 10, int style = SWT.BORDER}) => VText()
  ..id = 601
  ..style = style
  ..seq = seq
  ..enabled = true
  ..visible = true
  ..text = 'Field'
  ..bounds = bounds;

VComposite _form(VRectangle bounds, List<VControl> children, {int seq = 10}) => VComposite()
  ..id = 600
  ..style = SWT.NONE
  ..seq = seq
  ..enabled = true
  ..visible = true
  ..bounds = bounds
  ..children = children;

/// Mounts [form] as the one child of a window-sized Composite, which lays it out by its bounds.
Future<void> _mount(WidgetTester tester, VComposite form) async {
  await tester.pumpWidget(const SizedBox());
  freshClient();
  final window = VComposite()
    ..id = 599
    ..style = SWT.NONE
    ..seq = 10
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, 600, 300)
    ..children = [form];
  await tester.pumpWidget(
    EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 600, height: 300, child: CompositeSwt<VComposite>(value: window)),
      ),
    ),
  );
  await tester.pumpAndSettle();
}

/// Counts the rebuilds of the Text and of the form from here on.
({int Function() text, int Function() form}) _countBuilds() {
  var text = 0, form = 0;
  debugOnRebuildDirtyWidget = (element, _) {
    if (element.widget is TextSwt) text++;
    if (element.widget is CompositeSwt && (element.widget as CompositeSwt).value.id == 600) form++;
  };
  addTearDown(() => debugOnRebuildDirtyWidget = null);
  return (text: () => text, form: () => form);
}

Rect _field(WidgetTester tester) => tester.getRect(find.byType(TextSwt<VText>));

void main() {
  testWidgets('a wider single-line Text is laid out again without a rebuild', (tester) async {
    await _mount(tester, _form(_rect(0, 0, 400, 100), [_text(_rect(10, 10, 200, 24))]));
    final builds = _countBuilds();

    await deliverChange(
      _text(_rect(10, 10, 320, 24), seq: 11),
      keys: [WireKey.k_Control_bounds],
      base: 10,
    );
    await tester.pumpAndSettle();

    expect(builds.text(), 0);
    final resized = _field(tester);

    await _mount(tester, _form(_rect(0, 0, 400, 100), [_text(_rect(10, 10, 320, 24))]));
    expect(resized, _field(tester), reason: 'where a field mounted at those bounds stands');
  });

  testWidgets('a taller single-line Text rebuilds', (tester) async {
    await _mount(tester, _form(_rect(0, 0, 400, 100), [_text(_rect(10, 10, 200, 24))]));
    final builds = _countBuilds();

    await deliverChange(
      _text(_rect(10, 10, 200, 30), seq: 11),
      keys: [WireKey.k_Control_bounds],
      base: 10,
    );
    await tester.pumpAndSettle();

    expect(builds.text(), 1);
    expect(_field(tester).size, const Size(200, 30));
  });

  testWidgets('a multi-line Text rebuilds for a new width', (tester) async {
    await _mount(
      tester,
      _form(_rect(0, 0, 400, 100), [_text(_rect(10, 10, 200, 60), style: SWT.BORDER | SWT.MULTI)]),
    );
    final builds = _countBuilds();

    await deliverChange(
      _text(_rect(10, 10, 320, 60), seq: 11, style: SWT.BORDER | SWT.MULTI),
      keys: [WireKey.k_Control_bounds],
      base: 10,
    );
    await tester.pumpAndSettle();

    expect(builds.text(), 1);
  });

  testWidgets('a resized plain Composite is laid out again without a rebuild', (tester) async {
    final text = _text(_rect(10, 10, 200, 24));
    await _mount(tester, _form(_rect(0, 0, 400, 100), [text]));
    final builds = _countBuilds();

    await deliverChange(
      _form(_rect(0, 0, 500, 120), [text], seq: 11),
      keys: [WireKey.k_Control_bounds],
      base: 10,
    );
    await tester.pumpAndSettle();

    expect(builds.form(), 0);
    expect(
      tester.getSize(find.byWidgetPredicate((w) => w is CompositeSwt && w.value.id == 600)),
      const Size(500, 120),
    );
  });
}
