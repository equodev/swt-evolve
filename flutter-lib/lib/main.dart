import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart' as mat;

import 'native_platform.dart' if (dart.library.html) 'web_platform.dart';

import 'src/swt/button.dart';
import 'src/comm/comm.dart';
import 'src/widgets.dart';

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
  return mapWidget(child);
}

class MyApp extends StatelessWidget {
  final Widget contentWidget;
  final ThemeMode theme;

  const MyApp({
    Key? key,
    required this.contentWidget,
    this.theme = ThemeMode.light,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return FluentApp(
      title: 'Flutter Demo',
      darkTheme: FluentThemeData.dark().copyWith(
        accentColor: Colors.blue,
      ),
      theme: FluentThemeData.light().copyWith(
        accentColor: Colors.blue,
      ),
      themeMode: theme,
      debugShowCheckedModeBanner: false,
      home: Acrylic(child: contentWidget),
    );
  }
}
//  vim: set ts=2 sw=2 tw=0 et :
