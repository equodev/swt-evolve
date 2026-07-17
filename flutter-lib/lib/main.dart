import 'package:flutter/material.dart';
import 'package:flutter/semantics.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/custom/main_composite.dart';
import 'package:swtflutter/src/custom/ewt_evolve.dart';
import 'package:swtflutter/src/custom/csd/csd_scaffold.dart';
import 'package:swtflutter/src/custom/csd/csd_state.dart';
import 'package:swtflutter/src/custom/csd/equo_window.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'src/styles.dart';
import 'src/theme/theme.dart'
    show
        createLightDefaultTheme,
        createLightNonDefaultTheme,
        createDarkDefaultTheme,
        createDarkNonDefaultTheme,
        createColorSchemeExtension,
        parseGlobalSeedColor;
import 'src/theme/named_themes.dart';
import 'dart:async';
import 'dart:convert';
import 'dart:ui';

import 'native_platform.dart' if (dart.library.html) 'web_platform.dart';

import 'src/comm/comm.dart';
import 'src/gen/display.dart';
import 'src/gen/widgets.dart' as gen;
import 'src/impl/display_evolve.dart';
import 'fontSize.dart' as font_size;
import 'imageSize.dart' as image_size;
import 'widgetSize.dart' as widget_size;
import 'bench.dart' as bench;
import 'test_harness.dart' as test_harness;
import 'src/gen/gc.dart';
import 'src/impl/gcdrawer_evolve.dart';

bool _themeConfigLogged = false;
Completer<void>? _swtEvolvePropertiesCompleter;
bool _swtEvolvePropertiesListenerRegistered = false;
_DisplayMetricsReporter? _displayMetricsReporter;

void main(List<String> args) async {
  int? port = getPort(args);
  if (port != null) {
    print("Using port $port");
    EquoCommService.setPort(port);
  }

  // Dormant test-support channel (no-op in production; see test_harness.dart).
  test_harness.registerTestQueryChannel();

  int? widgetId = getWidgetId(args);
  String? widgetName = getWidgetName(args);
  if (widgetId == null || widgetName == null || widgetName.isEmpty) {
    return;
  }

  // Self-embed guard (web): refuse to boot when this document is running inside
  // an SWT Browser widget's own <iframe>. That happens when the iframe's src
  // resolved back to the app origin (empty src -> parent URL, then the server's
  // SPA fallback serves index.html), which would otherwise start a second full
  // copy of the app *inside* the Browser widget. A legitimate host embed is not
  // named `equo-browser-*`, so it is unaffected. See isSelfEmbeddedBrowserWidget.
  if (isSelfEmbeddedBrowserWidget()) {
    print("Refusing to boot: running inside an SWT Browser widget iframe");
    return;
  }

  WidgetsFlutterBinding.ensureInitialized();
  // Forces the semantics tree on so Flutter Web emits real <flt-semantics> DOM nodes (with our
  // Semantics identifiers, see ControlImpl.wrap) for E2E tools like Playwright to find and click.
  // Off by default: building the semantics tree has a real runtime cost, so production must not pay
  // for it. This is a pure runtime toggle (no separate build): the embedding app sets
  // -Ddev.equo.swt.web.enableTestSemantics=true on its own JVM and WebFlutterServer forwards it into
  // the served index.html as window.evolve.enableTestSemantics (read by getEnableTestSemantics),
  // so an already-shipped jar can be flipped into E2E-testable mode without recompiling.
  if (getEnableTestSemantics(args)) {
    SemanticsBinding.instance.ensureSemantics();
  }

  if (widgetName == "FontMeasureBridge") {
    font_size.measureRequest(widgetName, widgetId);
    sendClientReady(widgetName, widgetId);
    return;
  }
  else if (widgetName == "ImageSizeBridge") {
    image_size.measureRequest(widgetName, widgetId);
    sendClientReady(widgetName, widgetId);
    return;
  }
  else if (widgetName == "WidgetSizeBridge") {
    widget_size.measureRequest(widgetName, widgetId);
    sendClientReady(widgetName, widgetId, sendWindowSize: true);
    return;
  }
  else if (widgetName == "GCImageDrawer") {
    // Java channels use "GC/{id}/..." prefix, not "GCImageDrawer/{id}/..."
    final state = VGC()..swt = "GC"..id = widgetId!;
    GCDrawer.standalone(state);
    sendClientReady(widgetName, widgetId);
    return;
  }
  else if (widgetName == "BenchBridge") {
    bench.measureRequest(widgetName, widgetId);
    sendClientReady(widgetName, widgetId);
    return;
  }

  var theme = getTheme(args) == "dark" ? ThemeMode.dark : ThemeMode.light;
  int? backgroundColor = getBackgroundColor(args);
  int? parentBackgroundColor = getParentBackgroundColor(args);
  setCurrentTheme(theme == ThemeMode.dark);
  setParentBackgroundColor(parentBackgroundColor);
  unawaited(initSwtEvolveProperties());

  if (widgetName == "Display") {
    csdMainWindowId = widgetId;
    // Track window focus so the macOS controls grey out when the window is inactive.
    EquoWindow.installWindowStateListeners(
      (active) => csdWindowActive.value = active,
    );
  }

  Widget contentWidget = createContentWidget(widgetName!, widgetId!);

  runApp(
    EvolveApp(
      contentWidget: contentWidget,
      theme: theme,
      backgroundColor: backgroundColor,
      isMainWindow: widgetName == "Display",
    ),
  );

  if (widgetName == "Display") {
    _displayMetricsReporter ??= _DisplayMetricsReporter(widgetId);
  } else {
    sendClientReady(widgetName, widgetId);
  }
}

void sendClientReady(String widgetName, int widgetId, {bool sendWindowSize = false}) {
  if (sendWindowSize) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _sendWindowSizedClientReady(widgetName, widgetId);
    });
  } else {
    EquoCommService.send("${widgetName}/${widgetId}/ClientReady");
  }
}

class _DisplayMetricsReporter {
  _DisplayMetricsReporter(this.widgetId) {
    observeViewportChanges(_sendCurrentSize);
    observeWindowClose(_sendWindowClose);
  }

  final int widgetId;
  bool _windowCloseSent = false;

  // The window/tab is tearing down (pagehide/beforeunload): ask Java to close the SWT shells (no-op on
  // desktop-native, where the close is detected natively). This fires for a *refresh* exactly as it does
  // for a real close — the browser can't tell them apart here — so it is sent on the dedicated WinUnload
  // channel, which Java defers by a grace period and cancels if the refreshed page reconnects (vs the
  // CSD close button, which uses WinClose for an immediate, unambiguous close). Sent at most once —
  // pagehide and beforeunload both fire during one teardown, and a duplicate would race the Java side.
  void _sendWindowClose() {
    if (_windowCloseSent) return;
    _windowCloseSent = true;
    print('[WinUnload] Display/$widgetId');
    EquoCommService.send("Display/$widgetId/WinUnload");
  }

  void _sendCurrentSize() {
    final rounded = _currentLogicalViewSize();
    if (rounded == null) return;
    _sendWindowSizedClientReady("Display", widgetId, sizeOverride: rounded);
  }
}

/// True only for real, usable dimensions. Guards the `.toInt()` in
/// [_sendWindowSizedClientReady]: on Windows early init the view's
/// devicePixelRatio can be 0, so `physicalSize / dpr` yields Infinity/NaN — and
/// `Infinity <= 0` / `NaN <= 0` are both false, so a bare `<= 0` check lets them
/// through and `.toInt()` then throws "Unsupported operation: Infinity or NaN toInt".
bool _isFinitePositiveSize(double w, double h) =>
    w.isFinite && h.isFinite && w > 0 && h > 0;

Size? _currentLogicalViewSize() {
  final viewportSize = getViewportSize();
  if (viewportSize != null &&
      _isFinitePositiveSize(viewportSize.width, viewportSize.height)) {
    return Size(
      viewportSize.width.roundToDouble(),
      viewportSize.height.roundToDouble(),
    );
  }
  final dispatcher = WidgetsBinding.instance.platformDispatcher;
  if (dispatcher.views.isEmpty) {
    return null;
  }
  final view = dispatcher.views.first;
  if (view.devicePixelRatio <= 0) {
    return null;
  }
  final size = view.physicalSize / view.devicePixelRatio;
  if (!_isFinitePositiveSize(size.width, size.height)) {
    return null;
  }
  return Size(size.width.roundToDouble(), size.height.roundToDouble());
}

/// The logical size of the monitor the window is currently on. Drives the SWT
/// `Display`/`Monitor` bounds on desktop (where the Display is the screen, not the
/// window). Uses the window's own [FlutterView.display] — not `displays.first` —
/// so it stays correct on multi-monitor setups. On web this is the (synthetic)
/// browser display, which the Java web bridge ignores (there the viewport IS the
/// Display). Returns null when no plausible display size is available.
Size? _currentLogicalMonitorSize() {
  final dispatcher = WidgetsBinding.instance.platformDispatcher;
  if (dispatcher.views.isEmpty) {
    return null;
  }
  final display = dispatcher.views.first.display;
  if (display.devicePixelRatio <= 0) {
    return null;
  }
  final size = display.size / display.devicePixelRatio;
  if (!_isFinitePositiveSize(size.width, size.height)) {
    return null;
  }
  return Size(size.width.roundToDouble(), size.height.roundToDouble());
}

bool _displayClientReadySent = false;

void _sendWindowSizedClientReady(String widgetName, int widgetId, {Size? sizeOverride}) {
  final size = sizeOverride ?? _currentLogicalViewSize();
  final bool isFirst = !_displayClientReadySent;
  _displayClientReadySent = true;
  final int w = size?.width.toInt() ?? 1280;
  final int h = size?.height.toInt() ?? 720;
  // The monitor the window is on (0 when unknown → Java keeps its default/viewport).
  final monitor = _currentLogicalMonitorSize();
  final int mw = monitor?.width.toInt() ?? 0;
  final int mh = monitor?.height.toInt() ?? 0;
  print('[ClientReady] Display/$widgetId isFirst=$isFirst size=${w}x${h} monitor=${mw}x${mh}');
  EquoCommService.sendPayload("$widgetName/$widgetId/ClientReady", {
    'width': w,
    'height': h,
    'isFirst': isFirst,
    'displayWidth': mw,
    'displayHeight': mh,
  });
}

Future<void> initSwtEvolveProperties() async {
  _swtEvolvePropertiesCompleter ??= Completer<void>();

  if (!_swtEvolvePropertiesListenerRegistered) {
    _swtEvolvePropertiesListenerRegistered = true;
    EquoCommService.onRaw("swt.evolve.properties", (dynamic data) {
      try {
        applyConfigFlags(ConfigFlags.fromJson(data as Map<String, dynamic>));
      } catch (e) {
        print('Error parsing properties: $e');
      } finally {
        if (!(_swtEvolvePropertiesCompleter?.isCompleted ?? true)) {
          _swtEvolvePropertiesCompleter!.complete();
        }
      }
    });
  }

  await _swtEvolvePropertiesCompleter!.future.timeout(
    const Duration(seconds: 2),
    onTimeout: () {
      print('swt.evolve.properties timeout; continuing startup');
    },
  );
}

Widget createContentWidget(String widgetName, int widgetId) {
  Map<String, dynamic> child = {
    'swt': widgetName,
    'id': widgetId,
    'style': 0,
  };
  return customWidget(child) ?? gen.mapWidget(child);
}

Widget? customWidget(Map<String, dynamic> child) {
  var type = child['swt'];
  var id = child['id'];
  return switch (type) {
    "MainToolbar" => ToolbarComposite(
        key: ValueKey(id),
        value: VComposite.fromJson(child),
        useBoundsLayout: true,
    ),
    "SideBar" => SideBarComposite(key: ValueKey(id), value: VComposite.fromJson(child)),
    "StatusBar" => StatusBarComposite(key: ValueKey(id), value: VComposite.fromJson(child)),
    "MainComposite" => MainComposite(key: ValueKey(id), value: VComposite.fromJson(child)),
    "EwtWidget" => EwtWidgetSwt(key: ValueKey(id), value: VComposite.fromJson(child)),
    "Display" => DisplaySwt(key: ValueKey(id), value: VDisplay()..swt = "Display"..id = id),
    _ => null
  };
}

Widget? customWidgetFromValue(VWidget child) {
  var type = child.swt;
  var id = child.id;
  return switch (type) {
    "MainToolbar" => ToolbarComposite(
        key: ValueKey(id),
        value: child as VComposite,
        useBoundsLayout: true,
    ),
    "SideBar" => SideBarComposite(key: ValueKey(id), value: child as VComposite),
    "StatusBar" => StatusBarComposite(key: ValueKey(id), value: child as VComposite),
    "MainComposite" => MainComposite(key: ValueKey(id), value: child as VComposite),
    "EwtWidget" => EwtWidgetSwt(key: ValueKey(id), value: child as VComposite),
    _ => null
  };
}

class EvolveApp extends StatelessWidget {
  final Widget contentWidget;
  final ThemeMode theme;
  final int? backgroundColor;

  /// Only the top-level window (widgetName == "Display") gets CSD window chrome; other
  /// Flutter clients (size bridges, child browser views) must not grow resize edges.
  final bool isMainWindow;

  const EvolveApp({
    Key? key,
    required this.contentWidget,
    required this.theme,
    this.backgroundColor,
    this.isMainWindow = false,
  }) : super(key: key);

  /// Wraps the main window's body with CSD chrome (resize edges, optional title strip /
  /// floating controls). Sub-view clients pass through unchanged.
  Widget _maybeWrapCsd(Widget body) =>
      isMainWindow ? CsdShell(child: body) : body;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: configFlagsVersion,
      builder: (context, _) {
        final flags = getConfigFlags();
        final forcedThemeMode = parseForcedThemeMode(flags.force_theme);
        final effectiveThemeMode = forcedThemeMode ?? theme;
        setCurrentTheme(effectiveThemeMode == ThemeMode.dark);
        final themeName = flags.theme_name?.trim();
        final namedTheme = (themeName != null && themeName.isNotEmpty)
            ? kNamedThemes[themeName]
            : null;
        final seedColor = parseGlobalSeedColor(flags);
        if (!_themeConfigLogged) {
          _themeConfigLogged = true;
          print(
            'Theme config -> theme_name=$themeName, '
            'force_theme=${flags.force_theme}, '
            'effective_theme_mode=${effectiveThemeMode.name}, '
            'theme_color=${flags.theme_color}, theme_colors_by_widget=${flags.theme_colors_by_widget}, parsed_seed=${seedColor?.value.toRadixString(16)}',
          );
        }
        final ThemeData lightTheme;
        final ThemeData darkTheme;
        if (namedTheme != null) {
          final darkScheme = namedTheme.darkColorScheme ?? namedTheme.lightColorScheme;
          lightTheme = createLightNonDefaultTheme(
            backgroundColor,
            overrideColorScheme: namedTheme.lightColorScheme,
            overrideColorSchemeExtension: namedTheme.lightColorSchemeExtension
                ?? createColorSchemeExtension(namedTheme.lightColorScheme),
          );
          darkTheme = createDarkNonDefaultTheme(
            backgroundColor,
            overrideColorScheme: darkScheme,
            overrideColorSchemeExtension: namedTheme.darkColorSchemeExtension
                ?? createColorSchemeExtension(darkScheme),
          );
        } else {
          lightTheme = createLightDefaultTheme(backgroundColor, seedColor: seedColor);
          darkTheme = createDarkDefaultTheme(backgroundColor, seedColor: seedColor);
        }

        return MaterialApp(
          title: 'Evolve',
          theme: lightTheme,
          darkTheme: darkTheme,
          themeMode: effectiveThemeMode,
          debugShowCheckedModeBanner: false,
          home: Scaffold(
            backgroundColor: effectiveThemeMode == ThemeMode.dark
                ? darkTheme.scaffoldBackgroundColor
                : lightTheme.scaffoldBackgroundColor,
            body: _maybeWrapCsd(
              ValueListenableBuilder<double>(
                valueListenable: appScaleNotifier,
                builder: (ctx, scale, _) {
                  return Transform.scale(
                    scale: scale,
                    alignment: Alignment.topLeft,
                    child: contentWidget,
                  );
                },
              ),
            ),
          ),
        );
      },
    );
  }
}

ThemeMode? parseForcedThemeMode(String? forceTheme) {
  if (forceTheme == null) {
    return null;
  }
  final normalized = forceTheme.trim().toLowerCase();
  if (normalized == 'dark') {
    return ThemeMode.dark;
  }
  if (normalized == 'light') {
    return ThemeMode.light;
  }
  return null;
}
//  vim: set ts=2 sw=2 tw=0 et :
