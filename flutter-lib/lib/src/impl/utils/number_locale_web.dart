import 'dart:js_interop';
import 'dart:ui' show PlatformDispatcher;

@JS('Intl.NumberFormat')
extension type _NumberFormat._(JSObject _) implements JSObject {
  external factory _NumberFormat(JSString locale);

  external JSString format(JSNumber value);
}

String? _tag;
String _separator = '.';

/// On the web the locale is the browser's, the one the client reports to Java with its viewport, so both
/// sides write numbers the same way.
String decimalSeparator() {
  final tag = PlatformDispatcher.instance.locale.toLanguageTag();
  if (tag != _tag) {
    _tag = tag;
    try {
      final text = _NumberFormat(tag.toJS).format(1.5.toJS).toDart;
      _separator = text.substring(1, text.length - 1);
    } catch (_) {
      _separator = '.';
    }
  }
  return _separator;
}
