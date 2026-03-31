import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
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

import 'native_platform.dart' if (dart.library.html) 'web_platform.dart';

import 'src/comm/comm.dart';
import 'src/gen/widgets.dart' as gen;
import 'fontSize.dart' as font_size;
import 'imageSize.dart' as image_size;
import 'widgetSize.dart' as widget_size;
import 'src/gen/gc.dart';
import 'src/impl/gcdrawer_evolve.dart';

bool _themeConfigLogged = false;
Completer<void>? _swtEvolvePropertiesCompleter;
bool _swtEvolvePropertiesListenerRegistered = false;
final ValueNotifier<int> _configFlagsVersion = ValueNotifier<int>(0);

void main(List<String> args) async {
  if (args.isNotEmpty) {
    print("Using port ${args.first}");
    EquoCommService.setPort(int.parse(args.first));
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

  var theme = getTheme(args) == "dark" ? ThemeMode.dark : ThemeMode.light;
  int? backgroundColor = getBackgroundColor(args);
  int? parentBackgroundColor = getParentBackgroundColor(args);
  // Set the global theme for getCurrentTheme() usage
  setCurrentTheme(theme == ThemeMode.dark);
  // Set the parent background color for buttons
  setParentBackgroundColor(parentBackgroundColor);

  unawaited(initSwtEvolveProperties());

  Widget contentWidget = createContentWidget(widgetName!, widgetId!);

  runApp(EvolveApp(
    contentWidget: contentWidget,
    theme: theme,
    backgroundColor: backgroundColor,
  ));

  sendClientReady(widgetName, widgetId);
}

void sendClientReady(String widgetName, int widgetId, {bool sendWindowSize = false}) {
  if (sendWindowSize) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final view = WidgetsBinding.instance.platformDispatcher.views.first;
      final size = view.physicalSize / view.devicePixelRatio;
      final point = VPoint()
        ..x = 1280
        ..y = 720;
      if (size.width > 0 && size.height > 0) {
        point.x = size.width.round();
        point.y = size.height.round();
      }
      // print("Sent ${widgetName}/${widgetId}/ClientReady with windowSize ${point.x}x${point.y}");
      EquoCommService.sendPayload("${widgetName}/${widgetId}/ClientReady", point);
    });
  } else {
    // print("Sent ${widgetName}/${widgetId}/ClientReady");
    EquoCommService.send("${widgetName}/${widgetId}/ClientReady");
  }
}

Future<void> initSwtEvolveProperties() async {
  _swtEvolvePropertiesCompleter ??= Completer<void>();

  if (!_swtEvolvePropertiesListenerRegistered) {
    _swtEvolvePropertiesListenerRegistered = true;
    EquoCommService.onRaw("swt.evolve.properties", (dynamic data) {
      try {
        final configFlags = ConfigFlags.fromJson(jsonDecode(data));
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
            'theme_color_widget=${flags.theme_color_widget}, parsed_seed=${seedColor?.value.toRadixString(16)}',
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
            body: contentWidget,
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
