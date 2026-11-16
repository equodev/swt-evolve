import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/browser.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:webview_platform_interface/webview_platform_interface.dart';

import 'delivery/support/deliver.dart';

/// A Browser hidden with setVisible(false) shows nothing, as natively, and shown again it shows the
/// same page: its web view stays mounted, so the page is neither recreated nor reloaded.
void main() {
  late _FakeWebViewPlatform platform;

  setUp(() {
    platform = _FakeWebViewPlatform();
    _PlatformView.mounts = 0;
    WebViewPlatform.instance = platform;
  });

  testWidgets('a hidden Browser paints nothing and keeps its page for when it is shown',
      (tester) async {
    await tester.pumpWidget(EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(width: 200, height: 100, child: BrowserSwt(value: _browser(visible: true))),
      ),
    ));
    await tester.pump();
    expect(await _paintsAfterRepaint(tester), greaterThan(0));
    final loads = List.of(platform.loads);
    expect(loads, ['https://example.com/']);

    await deliverWhole(_browser(visible: false, seq: 10));
    await tester.pump();
    expect(await _paintsAfterRepaint(tester), 0, reason: 'a hidden Browser shows its page');

    await deliverWhole(_browser(visible: true, seq: 20));
    await tester.pump();
    expect(await _paintsAfterRepaint(tester), greaterThan(0));
    expect(platform.controllers, 1, reason: 'showing the Browser again recreated its web view');
    expect(_PlatformView.mounts, 1, reason: 'hiding the Browser unmounted its web view');
    expect(platform.loads, loads, reason: 'showing the Browser again reloaded its page');
  });
}

VBrowser _browser({required bool visible, int seq = 0}) => VBrowser()
  ..id = 78
  ..seq = seq
  ..style = SWT.NONE
  ..visible = visible
  ..url = 'https://example.com/'
  ..bounds = (VRectangle()
    ..x = 0
    ..y = 0
    ..width = 200
    ..height = 100);

/// How many times the web view painted after asking it to repaint.
Future<int> _paintsAfterRepaint(WidgetTester tester) async {
  _WebViewPainter.paints = 0;
  _WebViewPainter.repaint.value++;
  await tester.pump();
  return _WebViewPainter.paints;
}

class _FakeWebViewPlatform extends WebViewPlatform {
  int controllers = 0;
  final List<String> loads = [];

  @override
  PlatformWebViewController createPlatformWebViewController(
      PlatformWebViewControllerCreationParams params) {
    controllers++;
    return _FakeController(params, loads);
  }

  @override
  PlatformNavigationDelegate createPlatformNavigationDelegate(
          PlatformNavigationDelegateCreationParams params) =>
      _FakeNavigationDelegate(params);

  @override
  PlatformWebViewWidget createPlatformWebViewWidget(
          PlatformWebViewWidgetCreationParams params) =>
      _FakeWebViewWidget(params);
}

class _FakeController extends PlatformWebViewController {
  _FakeController(super.params, this.loads) : super.implementation();

  final List<String> loads;

  @override
  Future<void> setJavaScriptMode(JavaScriptMode javaScriptMode) async {}
  @override
  Future<void> setPlatformNavigationDelegate(PlatformNavigationDelegate handler) async {}
  @override
  Future<void> addJavaScriptChannel(JavaScriptChannelParams javaScriptChannelParams) async {}
  @override
  Future<void> loadRequest(LoadRequestParams params) async => loads.add('${params.uri}');
  @override
  Future<void> loadHtmlString(String html, {String? baseUrl}) async => loads.add('html');
  @override
  Future<String?> currentUrl() async => null;
  @override
  Future<String?> getTitle() async => null;
  @override
  Future<void> runJavaScript(String javaScript) async {}
  @override
  Future<Object> runJavaScriptReturningResult(String javaScript) async => '';
}

class _FakeNavigationDelegate extends PlatformNavigationDelegate {
  _FakeNavigationDelegate(super.params) : super.implementation();

  @override
  Future<void> setOnNavigationRequest(NavigationRequestCallback onNavigationRequest) async {}
  @override
  Future<void> setOnPageStarted(PageEventCallback onPageStarted) async {}
  @override
  Future<void> setOnPageFinished(PageEventCallback onPageFinished) async {}
  @override
  Future<void> setOnProgress(ProgressCallback onProgress) async {}
  @override
  Future<void> setOnWebResourceError(WebResourceErrorCallback onWebResourceError) async {}
  @override
  Future<void> setOnUrlChange(UrlChangeCallback onUrlChange) async {}
  @override
  Future<void> setOnHttpAuthRequest(HttpAuthRequestCallback onHttpAuthRequest) async {}
  @override
  Future<void> setOnHttpError(HttpResponseErrorCallback onHttpError) async {}
}

class _FakeWebViewWidget extends PlatformWebViewWidget {
  _FakeWebViewWidget(super.params) : super.implementation();

  @override
  Widget build(BuildContext context) => const _PlatformView();
}

/// Stands for the platform view (the iframe on web): a remount builds a new one, which loads its
/// page again.
class _PlatformView extends StatefulWidget {
  const _PlatformView();

  static int mounts = 0;

  @override
  State<_PlatformView> createState() => _PlatformViewState();
}

class _PlatformViewState extends State<_PlatformView> {
  @override
  void initState() {
    super.initState();
    _PlatformView.mounts++;
  }

  @override
  Widget build(BuildContext context) => CustomPaint(painter: _WebViewPainter(), size: Size.infinite);
}

/// Stands for the page: counts the frames it is drawn in.
class _WebViewPainter extends CustomPainter {
  _WebViewPainter() : super(repaint: repaint);

  static final ValueNotifier<int> repaint = ValueNotifier(0);
  static int paints = 0;

  @override
  void paint(Canvas canvas, Size size) => paints++;

  @override
  bool shouldRepaint(_WebViewPainter oldDelegate) => true;
}
