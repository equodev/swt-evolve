import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart' as mat;
import 'package:swtflutter/src/gen/composite.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'src/custom/maintoolbar_impl.dart';
import 'src/swt/composite.dart';
import 'src/styles.dart';

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

  WidgetsFlutterBinding.ensureInitialized();

  Widget contentWidget = createContentWidget(widgetName!, widgetId!);

  runApp(MyApp(
    contentWidget: contentWidget,
    theme: theme,
  ));

  EquoCommService.send("${widgetName}/${widgetId}/ClientReady");
  print("Sent ${widgetName}/${widgetId}/ClientReady");
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
    "MainToolbar" =>
        MainToolbarSwt(key: ValueKey(id), value: VComposite.fromJson(child)),
    _ => null
  };
}

class MyApp extends StatelessWidget {
  final Widget contentWidget;
  final ThemeMode theme;

  const MyApp({
    Key? key,
    required this.contentWidget,
    this.theme = ThemeMode.dark,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final bool useDarkTheme = getCurrentTheme();
    final Color backgroundColor = useDarkTheme ? Color(0xFF2C2C2C) : Color(0xFFF2F4F7);
    final ThemeMode currentTheme = useDarkTheme ? ThemeMode.dark : ThemeMode.light;

    return FluentApp(
      title: 'Flutter Demo',
      darkTheme: FluentThemeData.light().copyWith(
        accentColor: Colors.blue,
        scaffoldBackgroundColor: backgroundColor,
      ),
      theme: FluentThemeData.light().copyWith(
        accentColor: Colors.blue,
        scaffoldBackgroundColor: backgroundColor,
      ),
      themeMode: currentTheme,
      debugShowCheckedModeBanner: false,
      home: Container(
        color: backgroundColor,
        child: contentWidget,
      ),
    );
  }
}
//  vim: set ts=2 sw=2 tw=0 et :