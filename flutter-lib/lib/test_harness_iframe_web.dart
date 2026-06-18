import 'dart:js_interop';

import 'package:web/web.dart' as web;

import 'src/comm/comm.dart';

/// Test-only (web): relays an embedded page's on-load `postMessage` ping up to
/// the Java side over the dormant `evolve.test.iframeLoaded` channel.
///
/// A Browser renders the page in a cross-origin `<iframe>`, and on web neither
/// the navigation delegate's `onPageFinished` (it fires even for a blocked
/// load) nor reading `contentDocument` (cross-origin throws) can tell whether
/// the page actually rendered. The reliable, cross-origin-safe signal is the
/// page posting to its parent once it runs: a blocked iframe never executes the
/// page script, so no ping arrives. The fixture pages post
/// `'equo-iframe-loaded:' + document.title`; this listener forwards the title
/// so an integration test can assert the embedded page really loaded.
///
/// Inert in production: nothing posts that message and nothing listens on the
/// channel there.
void registerIframeLoadProbe() {
  const prefix = 'equo-iframe-loaded:';
  web.window.addEventListener(
    'message',
    ((web.MessageEvent e) {
      final raw = e.data;
      final data = raw?.dartify();
      if (data is String && data.startsWith(prefix)) {
        EquoCommService.sendPayload('evolve.test.iframeLoaded', {
          'page': data.substring(prefix.length),
        });
      }
    }).toJS,
  );
}
