import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'cached_text.dart';
import 'text_utils.dart';

/// Whether mnemonic characters are drawn underlined right now: while Alt is held, except on
/// macOS, where native SWT strips the `&` and never underlines anything.
///
/// Only [MnemonicText]s that actually carry a mnemonic listen, so pressing Alt rebuilds those
/// texts and nothing else; with no listener, no key handler is installed at all.
class MnemonicCue extends ChangeNotifier implements ValueListenable<bool> {
  MnemonicCue._();

  static final MnemonicCue shown = MnemonicCue._();

  bool _value = false;
  AppLifecycleListener? _lifecycle;

  // With no listener nothing tracks Alt, so the first one to subscribe reads it live.
  @override
  bool get value => hasListeners ? _value : _altHeld;

  set _shown(bool shown) {
    if (shown == _value) return;
    _value = shown;
    notifyListeners();
  }

  @override
  void addListener(VoidCallback listener) {
    final first = !hasListeners;
    super.addListener(listener);
    if (!first) return;
    HardwareKeyboard.instance.addHandler(_onKey);
    // Alt released while the window is in the background (Alt+Tab) never reaches us.
    _lifecycle = AppLifecycleListener(onStateChange: (state) {
      if (state != AppLifecycleState.resumed) _shown = false;
    });
    _value = _altHeld;
  }

  @override
  void removeListener(VoidCallback listener) {
    super.removeListener(listener);
    if (hasListeners || _lifecycle == null) return;
    HardwareKeyboard.instance.removeHandler(_onKey);
    _lifecycle?.dispose();
    _lifecycle = null;
    _value = false;
  }

  bool _onKey(KeyEvent _) {
    _shown = _altHeld;
    return false;
  }

  static bool get _altHeld =>
      defaultTargetPlatform != TargetPlatform.macOS &&
      defaultTargetPlatform != TargetPlatform.iOS &&
      HardwareKeyboard.instance.isAltPressed;
}

/// A [Text] for SWT text that may carry a `&` mnemonic marker: the marker is stripped, and the
/// mnemonic character is underlined while [MnemonicCue.shown] is set.
class MnemonicText extends StatelessWidget {
  const MnemonicText(
    this.data, {
    super.key,
    this.style,
    this.textAlign,
    this.softWrap,
    this.overflow,
    this.maxLines,
  });

  /// The raw SWT text, `&` markers included.
  final String? data;
  final TextStyle? style;
  final TextAlign? textAlign;
  final bool? softWrap;
  final TextOverflow? overflow;
  final int? maxLines;

  @override
  Widget build(BuildContext context) {
    final mnemonic = parseMnemonic(data);
    if (mnemonic.index < 0) return _plain(mnemonic.text);
    return ValueListenableBuilder<bool>(
      valueListenable: MnemonicCue.shown,
      builder: (context, shown, _) =>
          shown ? _underlined(mnemonic.text, mnemonic.index) : _plain(mnemonic.text),
    );
  }

  Widget _plain(String text) => ColorCachedText(
        text,
        style: style,
        textAlign: textAlign,
        softWrap: softWrap,
        overflow: overflow,
        maxLines: maxLines,
      );

  Widget _underlined(String text, int index) => Text.rich(
        TextSpan(children: mnemonicSpans(text, index, underline: true)),
        style: style,
        textAlign: textAlign,
        softWrap: softWrap,
        overflow: overflow,
        maxLines: maxLines,
      );
}

/// [text] as spans, with the character at [index] underlined when [underline] is set; [mouseCursor]
/// goes on every span, since a span's cursor does not reach its children.
List<TextSpan> mnemonicSpans(String text, int index,
    {required bool underline, TextStyle? style, MouseCursor? mouseCursor}) {
  if (!underline || index < 0) return [TextSpan(text: text, style: style, mouseCursor: mouseCursor)];
  final end = index + 1 < text.length && _isHighSurrogate(text.codeUnitAt(index)) ? index + 2 : index + 1;
  return [
    TextSpan(text: text.substring(0, index), style: style, mouseCursor: mouseCursor),
    TextSpan(
      text: text.substring(index, end),
      style: (style ?? const TextStyle()).copyWith(decoration: TextDecoration.underline),
      mouseCursor: mouseCursor,
    ),
    TextSpan(text: text.substring(end), style: style, mouseCursor: mouseCursor),
  ];
}

bool _isHighSurrogate(int unit) => unit >= 0xD800 && unit <= 0xDBFF;
