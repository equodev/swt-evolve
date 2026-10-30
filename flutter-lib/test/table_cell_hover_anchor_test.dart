// A Table cell grows a copy of itself in the Overlay while hovered, which needs the cell to lead
// that copy and to hide behind it. A table shows hundreds of cells and few are ever hovered: at rest
// a cell adds no layer of its own, only the hovered one leads a copy and stops painting, and it
// draws as before once the pointer leaves.

import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/gestures.dart' show PointerDeviceKind;
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/table.dart';
import 'package:swtflutter/src/gen/tablecolumn.dart';
import 'package:swtflutter/src/gen/tableitem.dart';
import 'package:swtflutter/src/theme/theme_extensions/table_theme_extension.dart';

const double _width = 300;
const double _height = 200;
const String _probe = 'row 3 / 0';

VTable _table() => VTable()
  ..id = 1
  ..seq = 1
  ..style = SWT.BORDER | SWT.FULL_SELECTION
  ..headerVisible = true
  ..linesVisible = true
  ..itemCount = 100
  ..columns = [
    for (var c = 0; c < 2; c++)
      VTableColumn()
        ..id = 2 + c
        ..seq = 1
        ..width = 140
        ..text = 'Column $c'
  ]
  ..items = [
    for (var i = 0; i < 100; i++)
      VTableItem()
        ..id = 100 + i
        ..seq = 1
        ..texts = ['row $i / 0', 'row $i / 1']
  ]
  ..bounds = (VRectangle()
    ..width = _width.toInt()
    ..height = _height.toInt());

final _shot = GlobalKey();

Widget _wrap({required bool zoom}) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Builder(builder: (context) {
        final theme = Theme.of(context);
        final table = theme.extension<TableThemeExtension>()!;
        return Theme(
          data: theme.copyWith(
              extensions: [...theme.extensions.values, table.copyWith(cellHoverZoomEnabled: zoom)]),
          child: RepaintBoundary(
            key: _shot,
            child: SizedBox(width: _width, height: _height, child: TableSwt<VTable>(value: _table())),
          ),
        );
      }),
    );

Future<Uint8List> _pixels(WidgetTester tester) async {
  final boundary = _shot.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  return (await tester.runAsync(() async {
    final image = await boundary.toImage();
    return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
  }))!;
}

Map<Type, int> _layers(WidgetTester tester) {
  final counts = <Type, int>{};
  void visit(Layer layer) {
    counts[layer.runtimeType] = (counts[layer.runtimeType] ?? 0) + 1;
    if (layer is ContainerLayer) {
      for (var child = layer.firstChild; child != null; child = child.nextSibling) {
        visit(child);
      }
    }
  }

  visit(tester.binding.renderView.debugLayer!);
  return counts;
}

/// The in-place cell, the one inside the table rather than its copy in the Overlay.
Finder _inPlace() => find.descendant(of: find.byType(Table), matching: find.text(_probe));

/// What draws the cell or hides it: the nearest ancestor of its text that the hover owns.
RenderObject _anchor(WidgetTester tester) => tester.renderObject(find
    .ancestor(of: _inPlace(), matching: find.byWidgetPredicate((w) => w.runtimeType.toString() == '_HoverAnchor'))
    .first);

Future<TestGesture> _hover(WidgetTester tester) async {
  final gesture = await tester.createGesture(kind: PointerDeviceKind.mouse);
  await gesture.addPointer(location: Offset.zero);
  addTearDown(gesture.removePointer);
  await gesture.moveTo(tester.getCenter(_inPlace()));
  await tester.pumpAndSettle();
  return gesture;
}

void main() {
  testWidgets('a cell draws again, as it did before, once the pointer leaves it', (tester) async {
    await tester.pumpWidget(_wrap(zoom: true));
    await tester.pumpAndSettle();
    final before = await _pixels(tester);
    final gesture = await _hover(tester);
    await gesture.moveTo(Offset.zero);
    await tester.pumpAndSettle();
    expect(find.text(_probe), findsOneWidget, reason: 'the copy must be gone');
    expect(await _pixels(tester), orderedEquals(before));
  });

  testWidgets('at rest no cell adds a layer of its own', (tester) async {
    await tester.pumpWidget(_wrap(zoom: false));
    await tester.pumpAndSettle();
    final without = _layers(tester);
    await tester.pumpWidget(const SizedBox());
    await tester.pumpWidget(_wrap(zoom: true));
    await tester.pumpAndSettle();
    expect(_layers(tester), without);
  });

  testWidgets('the hovered cell alone leads its copy, and is hidden behind it', (tester) async {
    await tester.pumpWidget(_wrap(zoom: true));
    await tester.pumpAndSettle();
    expect(_anchor(tester), paints..paragraph());

    await _hover(tester);
    expect(find.text(_probe), findsNWidgets(2), reason: 'the copy must be showing');
    expect(_layers(tester)[LeaderLayer], 1);
    expect(_anchor(tester), paintsNothing);
  });
}
