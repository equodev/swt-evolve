// SWT lays out in the view divided by the factor the app is drawn at. That factor has inputs on
// both sides -- the UI zoom Java sends, the monitor zoom the view reports, the scale control -- and
// any of them can move alone. Each move has to land in the one scale everything reads, and the area
// Java is told has to be divided by that same scale.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/utils/swt_zoom_scale.dart';
import 'package:swtflutter/src/impl/widget_config.dart';

void main() {
  setUp(resetConfigFlags);
  tearDown(resetConfigFlags);

  test('the conversions divide by the scale the app is drawn at', () {
    setAppScaleOverride(1.6);

    expect(logicalToSwt(const Offset(160, 80)), const Offset(100, 50));
    expect(logicalToSwtSize(const Size(1600, 800)), const Size(1000, 500));
  });

  test('a config re-broadcast leaves the scale control\'s factor alone', () {
    setAppScaleOverride(1.6);

    applyConfigFlags(ConfigFlags()..ui_zoom = 100);

    expect(appScaleNotifier.value, 1.6);
  });

  testWidgets('Java is told the view over the scale control\'s factor', (tester) async {
    tester.view.devicePixelRatio = 1.0;
    tester.view.physicalSize = const Size(2400, 1200);
    addTearDown(tester.view.reset);

    setAppScaleOverride(1.6);

    expect(currentSwtViewSize(), const Size(1500, 750));
  });

  testWidgets('a scale change reaches Java as one report of the view over the scale', (tester) async {
    tester.view.devicePixelRatio = 1.0;
    tester.view.physicalSize = const Size(2400, 1200);
    addTearDown(tester.view.reset);
    final sent = <Map<String, Object>>[];
    final realSender = clientReadySender;
    clientReadySender = (channel, payload) async => sent.add(payload as Map<String, Object>);
    addTearDown(() {
      clientReadySender = realSender;
      resetClientReadyState();
    });
    resetClientReadyState();
    sendWindowSizedClientReady('Display', 1);
    sent.clear();

    setAppScaleOverride(1.6);
    // The scale listener and the config listener both re-report the same area.
    sendWindowSizedClientReady('Display', 1);
    sendWindowSizedClientReady('Display', 1);

    expect(sent, hasLength(1));
    expect(sent.single['width'], 1500);
    expect(sent.single['height'], 750);
  });

  test('a refresh reports whether the scale moved, so a caller can tell who already re-reported',
      () {
    expect(refreshAppScale(), isFalse);

    setAppScaleOverride(1.6);
    expect(appScaleNotifier.value, 1.6);
    expect(refreshAppScale(), isFalse);
  });
}
