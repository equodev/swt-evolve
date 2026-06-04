import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/custom/main_composite.dart';
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
import 'src/gen/gc.dart';
import 'src/impl/gcdrawer_evolve.dart';

bool _themeConfigLogged = false;
Completer<void>? _swtEvolvePropertiesCompleter;
bool _swtEvolvePropertiesListenerRegistered = false;
final ValueNotifier<int> _configFlagsVersion = ValueNotifier<int>(0);
_DisplayMetricsReporter? _displayMetricsReporter;

void main(List<String> args) async {
  int? port = getPort(args);
  if (port != null) {
    print("Using port $port");
    EquoCommService.setPort(port);
  }

  int? widgetId = getWidgetId(args);
  String? widgetName = getWidgetName(args);
  if (widgetId == null || widgetName == null || widgetName.isEmpty) {
    return;
  }

  WidgetsFlutterBinding.ensureInitialized();

  if (widgetName == "FontSizeBridge") {
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

  Widget contentWidget = createContentWidget(widgetName!, widgetId!);

  runApp(EvolveApp(
    contentWidget: contentWidget,
    theme: theme,
    backgroundColor: backgroundColor,
  ));

  sendClientReady(widgetName, widgetId, sendWindowSize: (widgetName == "Display"));
  if (widgetName == "Display") {
    _displayMetricsReporter ??= _DisplayMetricsReporter(widgetId);
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

class _DisplayMetricsReporter with WidgetsBindingObserver {
  _DisplayMetricsReporter(this.widgetId) {
    WidgetsBinding.instance.addObserver(this);
    observeViewportChanges(_sendCurrentSize);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _sendCurrentSize();
    });
  }

  final int widgetId;
  Size? _lastSentSize;

  @override
  void didChangeMetrics() {
    _sendCurrentSize();
  }

  void _sendCurrentSize() {
    final rounded = _currentLogicalViewSize();
    if (rounded == null) {
      return;
    }
    if (_lastSentSize == rounded) {
      return;
    }
    _lastSentSize = rounded;
    _sendWindowSizedClientReady("Display", widgetId, sizeOverride: rounded);
  }
}

Size? _currentLogicalViewSize() {
  final viewportSize = getViewportSize();
  if (viewportSize != null) {
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
  final size = view.physicalSize / view.devicePixelRatio;
  if (size.width <= 0 || size.height <= 0) {
    return null;
  }
  return Size(size.width.roundToDouble(), size.height.roundToDouble());
}

void _sendWindowSizedClientReady(String widgetName, int widgetId, {Size? sizeOverride}) {
  final size = sizeOverride ?? _currentLogicalViewSize();
  final point = VPoint()
    ..x = size?.width.toInt() ?? 1280
    ..y = size?.height.toInt() ?? 720;
  EquoCommService.sendPayload("$widgetName/$widgetId/ClientReady", point);
}

Future<void> initSwtEvolveProperties() async {
  _swtEvolvePropertiesCompleter ??= Completer<void>();

  if (!_swtEvolvePropertiesListenerRegistered) {
    _swtEvolvePropertiesListenerRegistered = true;
    EquoCommService.onRaw("swt.evolve.properties", (dynamic data) {
      try {
        final configFlags = ConfigFlags.fromJson(data as Map<String, dynamic>);
        setConfigFlags(configFlags);
        _configFlagsVersion.value++;
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
    _ => null
  };
}

class EvolveApp extends StatelessWidget {
  final Widget contentWidget;
  final ThemeMode theme;
  final int? backgroundColor;

  const EvolveApp({
    Key? key,
    required this.contentWidget,
    required this.theme,
    this.backgroundColor,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _configFlagsVersion,
      builder: (context, _) {
        final flags = getConfigFlags();
        final forcedThemeMode = parseForcedThemeMode(flags.force_theme);
        final effectiveThemeMode = forcedThemeMode ?? theme;
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
            body: ValueListenableBuilder<double>(
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
