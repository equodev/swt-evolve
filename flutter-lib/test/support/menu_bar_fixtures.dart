// A Shell and the main toolbar that hosts its menu bar, as Java serializes them.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/control.dart';
import 'package:swtflutter/src/gen/decorations.dart';
import 'package:swtflutter/src/gen/menu.dart';
import 'package:swtflutter/src/gen/menuitem.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolbar.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

const double viewportWidth = 800;
const double viewportHeight = 300;

VRectangle rect(int x, int y, int width, int height) => VRectangle()
  ..x = x
  ..y = y
  ..width = width
  ..height = height;

/// A CASCADE entry opening a drop-down with a single item; [id] + 1 and + 2 go to those two.
VMenuItem cascade(int id, String text, String childText) => VMenuItem()
  ..id = id
  ..style = SWT.CASCADE
  ..enabled = true
  ..text = text
  ..menu = (VMenu()
    ..id = id + 1
    ..style = SWT.DROP_DOWN
    ..enabled = true
    ..items = [
      VMenuItem()
        ..id = id + 2
        ..style = SWT.PUSH
        ..enabled = true
        ..text = childText,
    ]);

/// A BAR menu holding a `File` cascade, or nothing.
VMenu fileMenuBar({bool withItems = true}) => VMenu()
  ..id = 200
  ..style = SWT.BAR
  ..enabled = true
  ..items = withItems ? [cascade(201, 'File', 'Exit')] : <VMenuItem>[];

VToolBar toolBar({int y = 5}) => VToolBar()
  ..id = 11
  ..style = SWT.FLAT
  ..enabled = true
  ..visible = true
  ..bounds = rect(0, y, 200, 40)
  ..items = [
    VToolItem()
      ..id = 12
      ..style = SWT.PUSH
      ..enabled = true
      ..text = 'Save',
  ];

/// Java's geometry for the bar: with a menu strip it is taller and its row starts below the strip.
VComposite mainToolbar({
  bool withMenuStrip = false,
  bool visible = true,
  List<VControl>? children,
}) =>
    VComposite()
      ..id = 10
      ..swt = 'MainToolbar'
      ..style = SWT.NONE
      ..enabled = true
      ..visible = visible
      ..bounds = rect(0, 0, viewportWidth.toInt(), withMenuStrip ? 68 : 40)
      ..children = children ?? [toolBar(y: withMenuStrip ? 33 : 5)];

/// [ownMenuStrip] and [menuStripHeight] are Java's answers (`Sizes.ownMenuStripHeight`,
/// `Sizes.menuStripHeight`); left null, the Shell draws no strip of its own.
VDecorations decorations({
  required VMenu? menuBar,
  required List<VControl> children,
  bool? ownMenuStrip,
  int? menuStripHeight,
}) =>
    VDecorations()
      ..id = 1
      ..style = SWT.NONE
      ..enabled = true
      ..visible = true
      ..bounds = rect(0, 0, viewportWidth.toInt(), viewportHeight.toInt())
      ..menuBar = menuBar
      ..children = children
      ..ownMenuStrip = ownMenuStrip
      ..menuStripHeight = menuStripHeight;

Future<void> pumpDecorations(WidgetTester tester, VDecorations value) async {
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: viewportWidth,
      height: viewportHeight,
      child: DecorationsSwt<VDecorations>(value: value),
    ),
  ));
  // A second pump lets the toolbar's post-layout measurement of the vertical menu button settle.
  await tester.pump();
}
