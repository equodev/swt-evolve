// import 'package:bitsdojo_window/bitsdojo_window.dart';
import 'dart:async';
import 'dart:js_interop';
import 'dart:ui' show Size;

//late final ShellListener? listener;
@JS('window.equoCommPort')
external JSNumber? get _equoCommPort;

@JS('window.evolve.widgetId')
external JSNumber? get _widgetId;

@JS('window.evolve.widgetName')
external JSString? get _widgetName;

@JS('window.evolve.theme')
external JSString? get _theme;

@JS('window.innerWidth')
external JSNumber? get _innerWidth;

@JS('window.innerHeight')
external JSNumber? get _innerHeight;

@JS('window.addEventListener')
external void _addWindowEventListener(JSString type, JSFunction listener);

@JS('window.requestAnimationFrame')
external JSNumber _requestAnimationFrame(JSFunction callback);

int? getPort(List<String> _) {
  print("Using web port: $_equoCommPort");
  return _equoCommPort?.toDartInt;
}

int? getWidgetId(List<String> _) {
  final params = Uri.base.queryParameters;
  final value = params['widgetId'];
  return value != null ? int.tryParse(value) : _widgetId?.toDartInt;
}

String? getWidgetName(List<String> _) {
  final params = Uri.base.queryParameters;
  return params['widgetName'] ?? _widgetName?.toDart;
}

String? getTheme(List<String> args) {
  final params = Uri.base.queryParameters;
  return params['theme'] ?? _theme?.toDart ?? "light";
}

int? getBackgroundColor(List<String> args) {
  return null;
}

int? getParentBackgroundColor(List<String> args) {
  return null;
}

Size? getViewportSize() {
  final width = _innerWidth?.toDartDouble;
  final height = _innerHeight?.toDartDouble;
  if (width == null || height == null || width <= 0 || height <= 0) {
    return null;
  }
  return Size(width, height);
}

void observeViewportChanges(void Function() onChange) {
  Timer? debounce;

  _addWindowEventListener('resize'.toJS, (() {
    debounce?.cancel();
    debounce = Timer(const Duration(milliseconds: 200), onChange);
  }).toJS);

  _requestAnimationFrame((JSNumber _) { onChange(); }.toJS);
}

void close() {
  //if (listener != null) {
  //  windowManager.removeListener(listener!);
  //}
  //windowManager.close();
}

//class ShellListener extends WindowListener {
//  @override
//  void onWindowClose() {
//    print("onWindowClose");
//    EquoCommService.send("close");
//  }
//}
