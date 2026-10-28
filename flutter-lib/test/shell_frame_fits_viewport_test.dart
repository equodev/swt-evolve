// A floating Shell is drawn with its title bar above the body Java laid out, so a Shell as tall as
// the viewport -- what JFace makes of a dialog it constrained to the monitor -- does not fit as
// drawn. The body is cut to what fits under the title bar and that size goes back to Java, which
// then lays the dialog out in the room there is: a button bar laid out at the bottom of a body
// taller than the frame is behind the frame's edge, and nothing scrolls it into view.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/display.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/shell.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/display_evolve.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme_extensions/display_theme_extension.dart';

const _viewport = Size(1200, 900);
const _dialogId = 2002;

VRectangle _rect(int x, int y, int w, int h) => VRectangle()
  ..x = x
  ..y = y
  ..width = w
  ..height = h;

VShell _shell({
  required int id,
  required String text,
  required VRectangle bounds,
  required int style,
}) => VShell()
  ..id = id
  ..style = style
  ..text = text
  ..bounds = bounds
  ..visible = true
  ..children = [
    VComposite()
      ..id = id + 1
      ..style = 0
      ..bounds = _rect(0, 0, bounds.width, bounds.height),
  ];

VShell _main() => _shell(
  id: 1001,
  text: 'Main',
  bounds: _rect(0, 0, _viewport.width.toInt(), _viewport.height.toInt()),
  style: SWT.SHELL_TRIM,
);

VShell _dialog(int height) => _shell(
  id: _dialogId,
  text: 'Preferences',
  bounds: _rect(300, 0, 600, height),
  style: SWT.DIALOG_TRIM,
);

/// Delivers an inbound frame exactly as the transport would (2-byte name length, name, JSON
/// body), so the Shell receives the update the way Java pushes one.
void _receiveJson(String actionId, Object payload) {
  final actionBytes = utf8.encode(actionId);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + actionBytes.length + body.length);
  frame[0] = (actionBytes.length >> 8) & 0xFF;
  frame[1] = actionBytes.length & 0xFF;
  frame.setRange(2, 2 + actionBytes.length, actionBytes);
  frame.setRange(2 + actionBytes.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
}

List<Map<String, dynamic>> _reports() => EquoCommService.commForTesting.sentFrames
    .map(EquoCommBase.decodeFrame)
    .where((f) => f.$1 == 'Shell/$_dialogId/Shell/SetBounds')
    .map((f) => jsonDecode(f.$2) as Map<String, dynamic>)
    .toList();

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  Future<void> pumpDialog(WidgetTester tester, VShell dialog) async {
    tester.view.physicalSize = _viewport;
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);
    EquoCommBase.recordSentFrames = true;
    addTearDown(() => EquoCommBase.recordSentFrames = false);
    EquoCommService.commForTesting.clearSentFrames();

    await tester.pumpWidget(
      EvolveApp(
        theme: ThemeMode.light,
        contentWidget: DisplaySwt(
          value: VDisplay()
            ..id = 7
            ..shells = [_main(), dialog]
            ..mainShellId = 1001,
        ),
      ),
    );
    await tester.pumpAndSettle();
    await tester.pump();
  }

  /// What the frame draws around the body: the title bar above it and the border on each side.
  double chromeHeight(WidgetTester tester) {
    final theme = Theme.of(
      tester.element(find.byKey(const ValueKey(_dialogId)).first),
    ).extension<DisplayThemeExtension>()!;
    return theme.titleBarHeight + 2 * theme.dialogBorderWidth;
  }

  // The body: the clipped box the Shell lays its client area out in, under the title bar.
  Rect drawnBody(WidgetTester tester) => tester.getRect(
    find
        .descendant(
          of: find.byKey(const ValueKey(_dialogId)),
          matching: find.byWidgetPredicate((w) => w is ClipRect && w.child is SizedBox),
        )
        .first,
  );

  testWidgets('a dialog as tall as the viewport is drawn to fit under its title bar', (
    tester,
  ) async {
    await pumpDialog(tester, _dialog(_viewport.height.toInt()));

    final fits = _viewport.height - chromeHeight(tester);
    final body = drawnBody(tester);
    expect(body.height, fits, reason: 'the body is all the room the frame leaves');
    expect(
      body.bottom,
      lessThanOrEqualTo(_viewport.height),
      reason: 'the bottom of the body, where a dialog lays out its buttons, is on screen',
    );
    expect(
      _reports().last['height'],
      fits.round(),
      reason: 'Java lays the dialog out in the room there is, not in the height it asked for',
    );
  });

  testWidgets('a dialog Java grows past the viewport after it was drawn is told its drawn size', (
    tester,
  ) async {
    await pumpDialog(tester, _dialog(500));
    expect(_reports().last['height'], 500, reason: 'sanity: a dialog that fits keeps its size');
    final before = _reports().length;

    // JFace growing the dialog to a page's preferred size, up to the monitor's client area, which
    // on this surface is the whole viewport.
    _receiveJson('Shell/$_dialogId', _dialog(_viewport.height.toInt()).toJson());
    await tester.pumpAndSettle();
    await tester.pump();

    final fits = _viewport.height - chromeHeight(tester);
    expect(drawnBody(tester).height, fits);
    final reports = _reports();
    expect(reports.length, greaterThan(before), reason: 'the drawn size goes back to Java');
    expect(reports.last['height'], fits.round());
  });

  testWidgets('a dialog drawn at the size Java set reports it once', (tester) async {
    await pumpDialog(tester, _dialog(500));
    final before = _reports().length;

    _receiveJson('Shell/$_dialogId', _dialog(500).toJson());
    await tester.pumpAndSettle();
    await tester.pump();

    expect(_reports().length, before, reason: 'nothing to tell Java it does not already hold');
  });
}
