
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireChannel, WireType;
import 'package:swtflutter/src/custom/csd/csd_state.dart';
import 'package:swtflutter/src/custom/csd/equo_window.dart';
import 'package:swtflutter/src/custom/csd/window_controls.dart';

import '../support/channels.dart' as channels;

/// The macOS green traffic light puts the window in full screen (its own Space, Dock hidden), as
/// it does for a native window; only the other platforms' maximize button resizes it to the work
/// area. Asserted on what reaches the host: the comm frames sent to Java and the calls made on the
/// native window channel. Full screen the window enters or leaves on its own is reported to Java,
/// and the state Java answers with is what the controls show.
void _deliver(String actionId, Object payload) {
  EquoCommService.commForTesting.receiveBinary(channels.frame(actionId, TaggedValues.encode(payload)));
}

void main() {
  const windowId = 4242;
  const windowChannel = MethodChannel('dev.equo.swt/window');
  late List<String> windowCalls;

  List<String> sentChannels() => EquoCommService.commForTesting.sentFrames
      .map((f) => EquoCommBase.decodeFrame(f).$1)
      .toList();

  setUp(() {
    windowCalls = [];
    csdMaximized.value = false;
    csdMainWindowId = windowId;
    EquoCommBase.recordSentFrames = true;
    EquoCommService.commForTesting.clearSentFrames();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(windowChannel, (call) async {
          windowCalls.add(call.method);
          return null;
        });
  });

  tearDown(() {
    // Leaves the window state as it found it, so a later test does not start in full screen.
    csdSetFullScreen(false);
    csdMainWindowId = null;
    EquoCommBase.recordSentFrames = false;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(windowChannel, null);
  });

  Future<void> pump(WidgetTester tester, String os) => tester.pumpWidget(
    MaterialApp(
      home: Scaffold(
        body: Center(
          child: SizedBox(height: 32, child: WindowControls(osOverride: os)),
        ),
      ),
    ),
  );

  testWidgets('mac green button enters and exits full screen, never zooms', (
    tester,
  ) async {
    final semantics = tester.ensureSemantics();
    await pump(tester, 'mac');

    await tester.tap(find.bySemanticsLabel('Enter Full Screen'));
    await tester.pump(const Duration(milliseconds: 1));

    expect(sentChannels(), contains('Display/$windowId/WinFullScreen'));
    expect(windowCalls, isNot(contains('maximize')));
    expect(csdMaximized.value, isTrue);

    EquoCommService.commForTesting.clearSentFrames();
    await tester.tap(find.bySemanticsLabel('Exit Full Screen'));
    await tester.pump(const Duration(milliseconds: 1));

    expect(sentChannels(), contains('Display/$windowId/WinExitFullScreen'));
    expect(windowCalls, isNot(contains('restore')));
    expect(csdMaximized.value, isFalse);
    semantics.dispose();
  });

  testWidgets('title-bar toggle while in full screen exits full screen', (
    tester,
  ) async {
    csdSetFullScreen(true);
    EquoCommService.commForTesting.clearSentFrames();

    csdToggleMaximize();
    await tester.pump(const Duration(milliseconds: 1));

    expect(sentChannels(), contains('Display/$windowId/WinExitFullScreen'));
    expect(windowCalls, isNot(contains('restore')));
    expect(csdMaximized.value, isFalse);
  });

  testWidgets('windows maximize button still maximizes the window', (
    tester,
  ) async {
    await pump(tester, 'windows');

    await tester.tap(find.byTooltip('Maximize'));
    await tester.pump(const Duration(milliseconds: 1));

    expect(windowCalls, contains('maximize'));
    expect(sentChannels(), isNot(contains('Display/$windowId/WinFullScreen')));
  });

  testWidgets(
    'a full screen the window enters on its own is reported to Java',
    (tester) async {
      EquoWindow.installWindowStateListeners(
        (_) {},
        onFullScreen: csdReportWindowFullScreen,
      );

      await TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .handlePlatformMessage(
            windowChannel.name,
            windowChannel.codec.encodeMethodCall(
              const MethodCall('fullScreen', true),
            ),
            (_) {},
          );
      await TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .handlePlatformMessage(
            windowChannel.name,
            windowChannel.codec.encodeMethodCall(
              const MethodCall('fullScreen', false),
            ),
            (_) {},
          );

      expect(sentChannels(), [
        'Display/$windowId/WinDidEnterFullScreen',
        'Display/$windowId/WinDidExitFullScreen',
      ]);
    },
  );

  testWidgets('the green button shows the full-screen state Java reports', (
    tester,
  ) async {
    final semantics = tester.ensureSemantics();
    csdListenFullScreenState(windowId);
    await pump(tester, 'mac');
    expect(find.bySemanticsLabel('Enter Full Screen'), findsOneWidget);

    _deliver('Display/$windowId/WinFullScreenState', {'fullScreen': true});
    await tester.pumpAndSettle();
    expect(csdMaximized.value, isTrue);
    expect(find.bySemanticsLabel('Exit Full Screen'), findsOneWidget);

    // Leaving full screen from the green button now asks Java to leave it.
    EquoCommService.commForTesting.clearSentFrames();
    await tester.tap(find.bySemanticsLabel('Exit Full Screen'));
    await tester.pump(const Duration(milliseconds: 1));
    expect(sentChannels(), contains('Display/$windowId/WinExitFullScreen'));

    _deliver('Display/$windowId/WinFullScreenState', {'fullScreen': false});
    await tester.pumpAndSettle();
    expect(csdMaximized.value, isFalse);
    expect(find.bySemanticsLabel('Enter Full Screen'), findsOneWidget);
    EquoCommService.remove(WireType.display, windowId, WireChannel.winFullScreenState);
    semantics.dispose();
  });
}
