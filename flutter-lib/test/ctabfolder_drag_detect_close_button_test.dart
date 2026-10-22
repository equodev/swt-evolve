// A drag that starts on a tab's close button is not a drag, as in native SWT, and the Java side
// decides it the upstream way: against the close buttons as this side draws them. So the strip
// report carries each drawn close button, and DragDetect carries where the press went down.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

class _CapturingCTabFolderSwt extends CTabFolderSwt<VCTabFolder> {
  const _CapturingCTabFolderSwt({required super.value, required this.onEvent});

  final void Function(String ev, VEvent? payload) onEvent;

  @override
  void sendEvent(VCTabFolder val, String ev, VEvent? payload) => onEvent(ev, payload);
}

const _labels = ['First View', 'Second View'];

VCTabFolder _folder(int id) => VCTabFolder()
  ..id = id
  ..style = SWT.CLOSE
  ..enabled = true
  ..selection = 0
  ..items = List.generate(
    _labels.length,
    (i) => VCTabItem()
      ..id = id * 10 + i
      ..text = _labels[i]
      ..showClose = true
      ..showing = true,
  );

class _Run {
  final List<VEvent> dragDetects = [];
  String? strip;

  /// The first tab's close button as last reported, in the folder's coordinates.
  Rect? get firstClose {
    final close = strip?.split(';');
    if (close == null || close.length < 2) return null;
    final v = close[1].split(',').map(int.parse).toList();
    return Rect.fromLTWH(v[0].toDouble(), v[1].toDouble(), v[2].toDouble(), v[3].toDouble());
  }
}

Future<_Run> _pump(WidgetTester tester, int folderId) async {
  final run = _Run();
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: SizedBox(
      width: 600,
      height: 240,
      child: _CapturingCTabFolderSwt(
        value: _folder(folderId),
        onEvent: (ev, payload) {
          if (ev.endsWith('DragDetect') && payload != null) run.dragDetects.add(payload);
          if (ev.endsWith('stripLaidOut') && payload != null) run.strip = payload.text;
        },
      ),
    ),
  ));
  await tester.pumpAndSettle();
  return run;
}

Future<void> _dragFrom(WidgetTester tester, Offset from) async {
  final gesture = await tester.startGesture(from);
  await tester.pump(const Duration(milliseconds: 50));
  for (var i = 1; i <= 8; i++) {
    await gesture.moveTo(Offset.lerp(from, from + const Offset(0, 140), i / 8)!);
    await tester.pump(const Duration(milliseconds: 20));
  }
  await gesture.up();
  await tester.pumpAndSettle();
  await tester.pump(const Duration(milliseconds: 400));
}

/// [global] in the folder's own coordinates, which is what Java is told.
Offset _inFolder(WidgetTester tester, Offset global, double scale) {
  final folder = tester.getTopLeft(find.byWidgetPredicate((w) => w is CTabFolderSwt<VCTabFolder>));
  return (global - folder) / scale;
}

void main() {
  for (final (scale, id) in [(1.0, 1), (1.5, 2)]) {
    testWidgets('at scale $scale, the strip report says where the close button is drawn',
        (tester) async {
      setAppScaleOverride(scale);
      addTearDown(() => setAppScaleOverride(null));
      final run = await _pump(tester, id);

      final close = run.firstClose;
      expect(close, isNotNull, reason: 'the report carries the close buttons after the strip');
      final icon = _inFolder(tester, tester.getCenter(find.byIcon(Icons.close).first), scale);
      expect(close!.contains(icon), isTrue,
          reason: 'the drawn close button, in the folder\'s coordinates: $close vs $icon');
    });

    testWidgets('at scale $scale, a drag from the close button reports a point on it',
        (tester) async {
      setAppScaleOverride(scale);
      addTearDown(() => setAppScaleOverride(null));
      final run = await _pump(tester, id + 10);
      final press = tester.getCenter(find.byIcon(Icons.close).first);

      await _dragFrom(tester, press);

      expect(run.dragDetects, hasLength(1));
      final point = Offset(run.dragDetects.single.x!.toDouble(), run.dragDetects.single.y!.toDouble());
      expect((point - _inFolder(tester, press, scale)).distance, lessThan(1.5),
          reason: 'where the press went down, so Java can tell it was on the close button');
      expect(run.firstClose!.contains(point), isTrue);
    });

    testWidgets('at scale $scale, a drag from the label reports where it was pressed',
        (tester) async {
      setAppScaleOverride(scale);
      addTearDown(() => setAppScaleOverride(null));
      final run = await _pump(tester, id + 20);
      final press = tester.getCenter(find.text(_labels[0]));

      await _dragFrom(tester, press);

      expect(run.dragDetects, hasLength(1));
      final point = Offset(run.dragDetects.single.x!.toDouble(), run.dragDetects.single.y!.toDouble());
      expect((point - _inFolder(tester, press, scale)).distance, lessThan(1.5));
      expect(run.firstClose!.contains(point), isFalse);
    });
  }
}
