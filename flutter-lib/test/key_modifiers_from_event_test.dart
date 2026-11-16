// A key's stateMask comes from the modifiers its own event reports. The browser reports Ctrl on
// the Space of Ctrl+Space even when Control's own keydown never reached the page (pressed before
// the page had the focus); Flutter's web engine does not synthesize that missing press, so
// HardwareKeyboard still has no Control down.
import 'dart:ui' as ui;

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/event.dart';
import 'package:swtflutter/src/impl/key_mapping.dart';
import 'package:swtflutter/src/impl/menu_evolve.dart';

/// RawKeyEventDataWeb's bits in a web key message's `metaState`.
const _webAlt = 0x02;
const _webCtrl = 0x04;

typedef _WebKey = ({
  PhysicalKeyboardKey physical,
  LogicalKeyboardKey logical,
  String code,
  String key,
  int keyCode,
  String? character,
});

const _WebKey _space = (
  physical: PhysicalKeyboardKey.space,
  logical: LogicalKeyboardKey.space,
  code: 'Space',
  key: ' ',
  keyCode: 32,
  character: ' ',
);
const _WebKey _controlLeft = (
  physical: PhysicalKeyboardKey.controlLeft,
  logical: LogicalKeyboardKey.controlLeft,
  code: 'ControlLeft',
  key: 'Control',
  keyCode: 17,
  character: null,
);
const _WebKey _altGraph = (
  physical: PhysicalKeyboardKey.altRight,
  logical: LogicalKeyboardKey.altGraph,
  code: 'AltRight',
  key: 'AltGraph',
  keyCode: 225,
  character: null,
);

/// AltGr+Q on a German layout.
const _WebKey _at = (
  physical: PhysicalKeyboardKey.keyQ,
  logical: LogicalKeyboardKey.at,
  code: 'KeyQ',
  key: '@',
  keyCode: 81,
  character: '@',
);

/// One browser key event, as the web engine reports it: the converted key data, then the raw
/// message with the event's modifier flags.
Future<void> _web(WidgetTester tester, _WebKey key, String type, {required int metaState}) async {
  final down = type == 'keydown';
  tester.binding.keyEventManager.handleKeyData(ui.KeyData(
    timeStamp: Duration.zero,
    type: down ? ui.KeyEventType.down : ui.KeyEventType.up,
    physical: key.physical.usbHidUsage,
    logical: key.logical.keyId,
    character: down ? key.character : null,
    synthesized: false,
  ));
  await tester.binding.keyEventManager.handleRawKeyMessage(<String, Object?>{
    'type': type,
    'keymap': 'web',
    'code': key.code,
    'key': key.key,
    'location': 0,
    'metaState': metaState,
    'keyCode': key.keyCode,
  });
}

void main() {
  final mapped = <LogicalKeyboardKey, VEvent>{};
  bool capture(KeyEvent event) {
    if (event is KeyDownEvent) mapped[event.logicalKey] = mapNewKeyEventToSwt(event);
    return false;
  }

  setUp(() {
    mapped.clear();
    HardwareKeyboard.instance.addHandler(capture);
  });
  tearDown(() => HardwareKeyboard.instance.removeHandler(capture));

  testWidgets('a Space whose event reports Ctrl is Ctrl+Space without a Control keydown before it',
      (tester) async {
    await _web(tester, _space, 'keydown', metaState: _webCtrl);
    await _web(tester, _space, 'keyup', metaState: _webCtrl);

    final space = mapped[LogicalKeyboardKey.space];
    expect(space, isNotNull);
    expect(space!.stateMask! & SWT.CTRL, SWT.CTRL);
  });

  testWidgets('a Space after a Control keydown is Ctrl+Space', (tester) async {
    await simulateKeyDownEvent(LogicalKeyboardKey.controlLeft, platform: 'web');
    await simulateKeyDownEvent(LogicalKeyboardKey.space, platform: 'web');
    await simulateKeyUpEvent(LogicalKeyboardKey.space, platform: 'web');
    await simulateKeyUpEvent(LogicalKeyboardKey.controlLeft, platform: 'web');

    expect(mapped[LogicalKeyboardKey.space]!.stateMask! & SWT.CTRL, SWT.CTRL);
  });

  testWidgets('a key held while a pop-up opens is played with the modifiers its event reported',
      (tester) async {
    VEvent? played;
    final menu = Object();
    bool hold(KeyEvent event) {
      if (event is KeyDownEvent && event.logicalKey == LogicalKeyboardKey.space) {
        OpenMenus.hold(event);
      }
      return false;
    }

    HardwareKeyboard.instance.addHandler(hold);
    OpenMenus.forwardHeld = (event) {
      played = mapNewKeyEventToSwt(event);
      return true;
    };
    addTearDown(() {
      HardwareKeyboard.instance.removeHandler(hold);
      OpenMenus.forwardHeld = null;
    });

    OpenMenus.startOpening(menu);
    await _web(tester, _space, 'keydown', metaState: _webCtrl);
    await _web(tester, _space, 'keyup', metaState: 0);
    OpenMenus.endOpening(menu);
    await tester.pump();

    expect(played, isNotNull);
    expect(played!.stateMask! & SWT.CTRL, SWT.CTRL);
  });

  // Windows reports AltGr as a Control keydown and an AltGraph keydown, and its characters with
  // Ctrl and Alt set. StyledText types a character with no modifiers; Ctrl or Alt alone would make
  // it a shortcut.
  testWidgets('an AltGr character on Windows carries neither Ctrl nor Alt', (tester) async {
    await _web(tester, _controlLeft, 'keydown', metaState: _webCtrl);
    await _web(tester, _altGraph, 'keydown', metaState: _webCtrl | _webAlt);
    await _web(tester, _at, 'keydown', metaState: _webCtrl | _webAlt);
    await _web(tester, _at, 'keyup', metaState: _webCtrl | _webAlt);
    await _web(tester, _altGraph, 'keyup', metaState: _webCtrl);
    await _web(tester, _controlLeft, 'keyup', metaState: 0);

    final at = mapped[LogicalKeyboardKey.at];
    expect(at, isNotNull);
    expect(at!.character, '@'.codeUnitAt(0));
    expect(at.stateMask! & (SWT.CTRL | SWT.ALT), 0);
  });

  testWidgets('an AltGr character on Linux does not carry Alt', (tester) async {
    await _web(tester, _altGraph, 'keydown', metaState: _webAlt);
    await _web(tester, _at, 'keydown', metaState: _webAlt);
    await _web(tester, _at, 'keyup', metaState: _webAlt);
    await _web(tester, _altGraph, 'keyup', metaState: 0);

    final at = mapped[LogicalKeyboardKey.at];
    expect(at, isNotNull);
    expect(at!.stateMask! & (SWT.CTRL | SWT.ALT), 0);
  });
}
