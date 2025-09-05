import 'dart:ui_web' as ui_web;

import 'package:web/web.dart' as web;
import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/material.dart';

import '../swt/browser.dart';

import '../impl/composite_impl.dart';

class BrowserImpl<T extends BrowserSwt, V extends BrowserValue>
    extends CompositeImpl<T, V> {
  late web.HTMLIFrameElement iframeElement;

  @override
  void initState() {
    super.initState();
    iframeElement =
        web.document.createElement('iframe') as web.HTMLIFrameElement;
    ui_web.PlatformViewRegistry().registerViewFactory(
      'iframeElement',
      (int viewId) => iframeElement,
    );
  }

  @override
  Widget build(BuildContext context) {
    const String viewType = 'iframeElement';

    if (state.url != null) {
      iframeElement.src = state.url ?? "";
    } else if (state.text != null) {
      iframeElement.src = "data:text/html;charset=utf-8,${state.text}" ?? "";
    }

    return const SizedBox(
        width: 800,
        height: 600,
        child: HtmlElementView(
          viewType: viewType,
        ));
  }
}
