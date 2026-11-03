// A vertical SWT `Scale` has its minimum at the bottom, as on Cocoa: `selection == minimum` draws the
// knob at the bottom, dragging it up raises the selection, and the Up arrow raises it.
// `swt.evolve.scale_vertical_minimum_at_top` puts the minimum at the top instead, as on GTK
// (`gtk_scale_new` with no inversion) and Win32 (a bare `TBS_VERT` trackbar), where Up lowers it.
// Left and Right step down and up either way, as they do on a horizontal Scale.

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/scale.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

const double _width = 40;
const double _height = 200;

class _RecordingScaleSwt extends ScaleSwt<VScale> {
  final List<int?> selections;
  const _RecordingScaleSwt({required VScale value, required this.selections})
      : super(value: value);

  @override
  void sendSelectionSelection(VScale val, VEvent? payload) =>
      selections.add(payload?.index);
}

VScale _verticalScale(int selection) => VScale()
  ..id = 1
  ..style = SWT.VERTICAL
  ..enabled = true
  ..minimum = 0
  ..maximum = 100
  ..selection = selection
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = _width.toInt()
    ..height = _height.toInt());

Future<List<int?>> _pumpScale(WidgetTester tester, int selection) async {
  final selections = <int?>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: SizedBox(
        width: _width,
        height: _height,
        child: _RecordingScaleSwt(value: _verticalScale(selection), selections: selections),
      ),
    ),
  ));
  await tester.pump();
  return selections;
}

Future<List<int?>> _pressKey(WidgetTester tester, LogicalKeyboardKey key) async {
  final selections = await _pumpScale(tester, 50);
  tester.widget<Slider>(find.byType(Slider)).focusNode!.requestFocus();
  await tester.pump();
  await tester.sendKeyEvent(key);
  await tester.pump();
  return selections;
}

/// Where the Slider's minimum and maximum ends land on screen: the Material Slider keeps its minimum
/// at its local left edge, wherever the rotation puts it.
(Offset, Offset) _ends(WidgetTester tester) {
  final box = tester.renderObject<RenderBox>(find.byType(Slider));
  final mid = box.size.height / 2;
  return (box.localToGlobal(Offset(0, mid)), box.localToGlobal(Offset(box.size.width, mid)));
}

Future<List<int?>> _dragUp(WidgetTester tester) async {
  final selections = await _pumpScale(tester, 50);
  final gesture = await tester.startGesture(tester.getCenter(find.byType(Slider)));
  await tester.pump();
  for (var i = 0; i < 8; i++) {
    await gesture.moveBy(const Offset(0, -10));
    await tester.pump();
  }
  await gesture.up();
  await tester.pump();
  return selections;
}

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  group('by default, as on Cocoa', () {
    testWidgets('a vertical Scale draws its minimum at the bottom', (tester) async {
      await _pumpScale(tester, 0);
      final (minimum, maximum) = _ends(tester);
      expect(minimum.dy, greaterThan(maximum.dy));
    });

    testWidgets('dragging a vertical Scale knob up raises the selection', (tester) async {
      final selections = await _dragUp(tester);
      expect(selections, isNotEmpty);
      expect(selections.last, greaterThan(60));
    });

    testWidgets('the Up arrow raises a vertical Scale selection', (tester) async {
      final selections = await _pressKey(tester, LogicalKeyboardKey.arrowUp);
      expect(selections, hasLength(1));
      expect(selections.single, greaterThan(50));
    });

    testWidgets('the Down arrow lowers a vertical Scale selection', (tester) async {
      final selections = await _pressKey(tester, LogicalKeyboardKey.arrowDown);
      expect(selections, hasLength(1));
      expect(selections.single, lessThan(50));
    });
  });

  group('with scale_vertical_minimum_at_top, as on GTK and Win32', () {
    setUp(() => setConfigFlags(ConfigFlags()..scale_vertical_minimum_at_top = true));

    testWidgets('a vertical Scale draws its minimum at the top', (tester) async {
      await _pumpScale(tester, 0);
      final (minimum, maximum) = _ends(tester);
      expect(minimum.dy, lessThan(maximum.dy));
    });

    testWidgets('dragging a vertical Scale knob up lowers the selection', (tester) async {
      final selections = await _dragUp(tester);
      expect(selections, isNotEmpty);
      expect(selections.last, lessThan(40));
    });

    testWidgets('the Up arrow lowers a vertical Scale selection', (tester) async {
      final selections = await _pressKey(tester, LogicalKeyboardKey.arrowUp);
      expect(selections, hasLength(1));
      expect(selections.single, lessThan(50));
    });

    testWidgets('the Down arrow raises a vertical Scale selection', (tester) async {
      final selections = await _pressKey(tester, LogicalKeyboardKey.arrowDown);
      expect(selections, hasLength(1));
      expect(selections.single, greaterThan(50));
    });
  });

  for (final minimumAtTop in [false, true]) {
    final how = minimumAtTop ? ' (minimum at the top)' : '';
    testWidgets('the Left arrow lowers a vertical Scale selection$how', (tester) async {
      if (minimumAtTop) setConfigFlags(ConfigFlags()..scale_vertical_minimum_at_top = true);
      final selections = await _pressKey(tester, LogicalKeyboardKey.arrowLeft);
      expect(selections.single, lessThan(50));
    });

    testWidgets('the Right arrow raises a vertical Scale selection$how', (tester) async {
      if (minimumAtTop) setConfigFlags(ConfigFlags()..scale_vertical_minimum_at_top = true);
      final selections = await _pressKey(tester, LogicalKeyboardKey.arrowRight);
      expect(selections.single, greaterThan(50));
    });
  }
}
