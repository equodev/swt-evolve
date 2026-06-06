// Web implementation of BrowserImpl — embeds a plain <iframe> via HtmlElementView.
// No native desktop dependencies. Imported only on web via browser_evolve.dart.

import 'dart:ui_web' as ui_web;

import 'package:flutter/widgets.dart';
import 'package:web/web.dart' as web;

import '../gen/browser.dart';
import '../gen/event.dart';
import '../gen/widget.dart';
import '../impl/composite_evolve.dart';

class BrowserImpl<T extends BrowserSwt, V extends VBrowser>
    extends CompositeImpl<T, V> {

  web.HTMLIFrameElement? _iframe;
  late final String _viewType;

  @override
  void initState() {
    super.initState();
    _viewType = 'browser-${state.swt}-${state.id}';
    ui_web.platformViewRegistry.registerViewFactory(_viewType, (int viewId) {
      final iframe = web.HTMLIFrameElement()
        ..style.width = '100%'
        ..style.height = '100%'
        ..style.border = 'none'
        ..src = state.url ?? 'about:blank';
      _iframe = iframe;
      return iframe;
    });
  }

  @override
  void extraSetState() {
    super.extraSetState();
    final f = _iframe;
    if (f == null) return;
    if (state.url != null && state.url != f.src) {
      f.src = state.url!;
      widget.sendLocationchanged(state, VEvent()..text = state.url);
      widget.sendProgresscompleted(state, null);
    } else if (state.text != null) {
      f.src = 'data:text/html;charset=utf-8,${Uri.encodeFull(state.text!)}';
      widget.sendProgresscompleted(state, null);
    }
  }

  @override
  Widget build(BuildContext context) =>
      HtmlElementView(viewType: _viewType);
}
