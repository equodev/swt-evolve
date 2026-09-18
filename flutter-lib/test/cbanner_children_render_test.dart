// cbanner_evolve.dart shipped the generator's placeholder build() — `return const Text("CBanner")`
// — which bypasses CompositeImpl.buildComposite(), the only thing that lays `children` out. An
// Eclipse 3.x WorkbenchWindow puts its whole top trim (the CoolBar) inside a CBanner, so every such
// app rendered the literal word "CBanner" where its toolbar belongs, and nothing else.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/cbanner.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/label.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

/// The Eclipse 3.x shape: the banner's `left` control is the trim composite that hosts the
/// workbench CoolBar. CBannerLayout runs in Java, so the child arrives with its final bounds.
///
/// `left`/`right`/`bottom` are deliberately off the wire — `VCBanner`'s Java getters carry
/// `@JsonAttribute(ignore = true)` — so the control reaches Dart only as a child, which is also the
/// only thing the render path reads.
VCBanner _banner() {
  final left = VComposite()
    ..id = 2
    ..style = SWT.NONE
    ..visible = true
    ..bounds = _rect(0, 0, 600, 28)
    ..children = [
      VLabel()
        ..id = 3
        ..style = SWT.NONE
        ..visible = true
        ..text = 'toolbar'
        ..bounds = _rect(0, 0, 120, 28)
    ];
  return VCBanner()
    ..id = 1
    ..style = SWT.NONE
    ..enabled = true
    ..visible = true
    ..bounds = _rect(0, 0, 600, 28)
    ..children = [left];
}

void main() {
  testWidgets('a CBanner renders its children, not a placeholder label', (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: SizedBox(
        width: 600,
        height: 28,
        child: CBannerSwt<VCBanner>(value: _banner()),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('toolbar'), findsOneWidget,
        reason: "the banner's left control -- an Eclipse workbench's whole CoolBar -- must render");
    expect(find.text('CBanner'), findsNothing,
        reason: 'the generator placeholder must not reach a running app');
  });
}
