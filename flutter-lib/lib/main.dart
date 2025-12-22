import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/custom/toolbar_composite.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'src/swt/composite.dart';
import 'src/styles.dart';
import 'src/theme/theme.dart' show createLightTheme, createDarkTheme;
import 'dart:convert';

import 'native_platform.dart' if (dart.library.html) 'web_platform.dart';

import 'src/comm/comm.dart';
import 'src/widgets.dart';
import 'src/gen/widgets.dart' as gen;

void main(List<String> args) async {
  if (args.isNotEmpty) {
    print("Using port ${args.first}");
    EquoCommService.setPort(int.parse(args.first));
  }

  int? widgetId = getWidgetId(args);
  String? widgetName = getWidgetName(args);
  if (widgetId == null || widgetName == null) {
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

  WidgetsFlutterBinding.ensureInitialized();

  Widget contentWidget = createContentWidget(widgetName!, widgetId!);

  runApp(MyApp(
    contentWidget: contentWidget,
    theme: theme,
    backgroundColor: backgroundColor,
  ));

  EquoCommService.send("${widgetName}/${widgetId}/ClientReady");
  print("Sent ${widgetName}/${widgetId}/ClientReady");
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
  return customWidget(child) ?? gen.mapWidget(child) ?? mapWidget(child);
}

Widget? customWidget(Map<String, dynamic> child) {
  var type = child['swt'];
  var id = child['id'];
  return switch (type) {
    "MainToolbar" => ToolbarComposite(key: ValueKey(id), value: VComposite.fromJson(child)),
    "SideBar" => SideBarComposite(key: ValueKey(id), value: VComposite.fromJson(child)),
    _ => null
  };
}

class MyApp extends StatelessWidget {
  final Widget contentWidget;
  final ThemeMode theme;
  final int? backgroundColor;

  const MyApp({
    Key? key,
    required this.contentWidget,
    required this.theme,
    this.backgroundColor,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final lightTheme = createLightTheme(backgroundColor);
    final darkTheme = createDarkTheme(backgroundColor);
    
    return MaterialApp(
      title: 'Flutter Demo',
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
