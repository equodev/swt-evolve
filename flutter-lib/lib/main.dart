import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'src/styles.dart';
import 'src/theme/theme.dart' show createLightDefaultTheme, createLightNonDefaultTheme, createDarkDefaultTheme, createDarkNonDefaultTheme;
import 'dart:convert';

import 'native_platform.dart' if (dart.library.html) 'web_platform.dart';

import 'src/comm/comm.dart';
import 'src/gen/widgets.dart' as gen;
import 'fontSize.dart' as font_size;
import 'imageSize.dart' as image_size;
import 'widgetSize.dart' as widget_size;

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

  var theme = getTheme(args) == "dark" ? ThemeMode.dark : ThemeMode.light;
  int? backgroundColor = getBackgroundColor(args);
  int? parentBackgroundColor = getParentBackgroundColor(args);
  // Set the global theme for getCurrentTheme() usage
  setCurrentTheme(theme == ThemeMode.dark);
  // Set the parent background color for buttons
  setParentBackgroundColor(parentBackgroundColor);

  initSwtEvolveProperties();

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

void initSwtEvolveProperties() {
  EquoCommService.onRaw("swt.evolve.properties", (dynamic data) {
    try {
      // print('Flutter received raw data: $data');
      final configFlags = ConfigFlags.fromJson(jsonDecode(data));
      // print('Flutter parsed ConfigFlags: assets_path=${configFlags.assets_path}');
      setConfigFlags(configFlags);
    } catch (e) {
      print('Error parsing properties: $e');
    }
  });
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
    "MainToolbar" => ToolbarComposite(key: ValueKey(id), value: VComposite.fromJson(child), useBoundsLayout: true),
    "SideBar" => SideBarComposite(key: ValueKey(id), value: VComposite.fromJson(child)),
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

  static const bool useDefaultTheme = false;

  @override
  Widget build(BuildContext context) {
    final lightTheme = useDefaultTheme
        ? createLightDefaultTheme(backgroundColor)
        : createLightNonDefaultTheme(backgroundColor);
    final darkTheme = useDefaultTheme
        ? createDarkDefaultTheme(backgroundColor)
        : createDarkNonDefaultTheme(backgroundColor);
    
    return MaterialApp(
      title: 'Evolve',
      theme: lightTheme,
      darkTheme: darkTheme,
      themeMode: theme,
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        backgroundColor: theme == ThemeMode.dark 
            ? darkTheme.scaffoldBackgroundColor
            : lightTheme.scaffoldBackgroundColor,
        body: contentWidget,
      ),
    );
  }
}
//  vim: set ts=2 sw=2 tw=0 et :
