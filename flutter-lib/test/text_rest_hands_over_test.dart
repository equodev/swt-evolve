// A Text at rest has no TextField, and still has to behave as a field: a pointer reaching it, focus
// arriving by traversal or from Java, or a tap mount the TextField and hand it the focus, which Java
// hears about once.

import 'dart:typed_data';
import 'dart:ui' as ui;
import 'dart:ui' show Tristate;

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/text.dart';
import 'package:swtflutter/src/impl/focus_requests.dart';
import 'package:swtflutter/src/impl/text_evolve.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;
import 'package:swtflutter/src/impl/utils/force_live.dart';

import 'delivery/support/deliver.dart';

class _RecordingTextSwt extends TextSwt<VText> {
  const _RecordingTextSwt({required super.value, required this.events});

  final List<String> events;

  @override
  void sendFocusFocusIn(VText val, VEvent? payload) => events.add('in');

  @override
  void sendFocusFocusOut(VText val, VEvent? payload) => events.add('out');
}

VText _value({String text = 'Value', int seq = 10}) => VText()
  ..id = 7
  ..seq = seq
  ..style = SWT.SINGLE | SWT.BORDER
  ..enabled = true
  ..editable = true
  ..visible = true
  ..text = text
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 160
    ..height = 24);

Future<List<String>> _mount(WidgetTester tester, {GlobalKey? key}) async {
  freshClient();
  final events = <String>[];
  await tester.pumpWidget(EvolveApp(
    theme: ThemeMode.light,
    contentWidget: Align(
      alignment: Alignment.topLeft,
      child: RepaintBoundary(key: key, child: _RecordingTextSwt(value: _value(), events: events)),
    ),
  ));
  await tester.pumpAndSettle();
  expect(find.byType(EditableText), findsNothing, reason: 'the field is not at rest');
  return events;
}

bool _textFieldFocused(WidgetTester tester) =>
    tester.widget<EditableText>(find.byType(EditableText)).focusNode.hasFocus;

/// The text field node under the field's identifier, at rest or live.
SemanticsNode _textField(WidgetTester tester) {
  final identified = tester.semantics.find(find.bySemanticsIdentifier('Text/7'));
  SemanticsNode? field;
  identified.visitChildren((child) {
    if (child.getSemanticsData().flagsCollection.isTextField) field = child;
    return field == null;
  });
  return field!;
}

void main() {
  setUp(() => FocusRequests.instance.reset());
  tearDown(() => FocusRequests.instance.reset());

  testWidgets('semantics turned on after the field is at rest describe it as a text field', (tester) async {
    await _mount(tester);

    final semantics = tester.ensureSemantics();
    try {
      await tester.pump();
      await tester.pump();

      expect(find.byType(EditableText), findsNothing);
      expect(_textField(tester).getSemanticsData().value, 'Value');
    } finally {
      semantics.dispose();
    }
  }, semanticsEnabled: false);

  for (final action in [SemanticsAction.focus, SemanticsAction.tap]) {
    testWidgets('a field at rest is a text field to assistive technology, and its ${action.name} action '
        'focuses it', (tester) async {
      final events = await _mount(tester);
      final atRest = _textField(tester).getSemanticsData();
      expect(atRest.value, 'Value');
      expect(atRest.hasAction(action), isTrue);

      tester.binding.pipelineOwner.semanticsOwner!.performAction(_textField(tester).id, action);
      await tester.pumpAndSettle();
      expect(_textFieldFocused(tester), isTrue);
      expect(events, ['in']);
      expect(_textField(tester).getSemanticsData().flagsCollection.isFocused, Tristate.isTrue);
    });
  }

  testWidgets('a change of the system fonts lays the text at rest out again', (tester) async {
    await _mount(tester);
    final atRest = tester.renderObject(find.byWidgetPredicate((w) => '${w.runtimeType}' == '_TextAtRest'));
    expect(atRest.debugNeedsLayout, isFalse);

    await tester.binding.defaultBinaryMessenger.handlePlatformMessage(
      SystemChannels.system.name,
      SystemChannels.system.codec.encodeMessage({'type': 'fontsChange'}),
      (_) {},
    );

    expect(atRest.debugNeedsLayout, isTrue);
  });

  testWidgets('a pointer reaching a field at rest mounts its TextField', (tester) async {
    await _mount(tester);
    final mouse = await tester.createGesture(kind: PointerDeviceKind.mouse);
    await mouse.addPointer(location: Offset.zero);
    addTearDown(mouse.removePointer);
    await mouse.moveTo(tester.getCenter(find.byType(_RecordingTextSwt)));
    await tester.pump();
    expect(find.byType(EditableText), findsOneWidget);
  });

  testWidgets('Tab reaches a field at rest and hands the focus to its TextField', (tester) async {
    final events = await _mount(tester);
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pumpAndSettle();
    expect(_textFieldFocused(tester), isTrue);
    expect(events, ['in']);
  });

  testWidgets('a focus request from Java focuses a field at rest', (tester) async {
    final events = await _mount(tester);
    FocusRequests.instance.request(7);
    await tester.pumpAndSettle();
    expect(_textFieldFocused(tester), isTrue);
    expect(events, ['in']);
  });

  testWidgets('a tap on a field at rest focuses it', (tester) async {
    final events = await _mount(tester);
    await tester.tap(find.byType(_RecordingTextSwt));
    await tester.pumpAndSettle();
    expect(_textFieldFocused(tester), isTrue);
    expect(events, ['in']);
  });

  testWidgets('a field at rest shows the text Java sets', (tester) async {
    final key = GlobalKey();
    await _mount(tester, key: key);
    await deliverChange(_value(text: 'Changed', seq: 11), keys: [WireKey.k_Text_text], base: 10);
    await tester.pumpAndSettle();
    expect(find.byType(EditableText), findsNothing);
    final rest = await _pixels(tester, key);

    freshClient();
    await tester.pumpWidget(ForceLive.when(true, EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: RepaintBoundary(key: key, child: TextSwt<VText>(value: _value(text: 'Changed'))),
      ),
    )));
    await tester.pumpAndSettle();
    expect(rest, orderedEquals(await _pixels(tester, key)));
  });
}

Future<Uint8List> _pixels(WidgetTester tester, GlobalKey key) async {
  final boundary = key.currentContext!.findRenderObject()! as RenderRepaintBoundary;
  return (await tester.runAsync(() async {
    final image = await boundary.toImage();
    return (await image.toByteData(format: ui.ImageByteFormat.rawRgba))!.buffer.asUint8List();
  }))!;
}
