import 'package:fluent_ui/fluent_ui.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart' as mat;

import 'native_platform.dart' if (dart.library.html) 'web_platform.dart';

import 'src/swt/button.dart';
import 'src/comm/comm.dart';

void main(List<String> args) async {
  if (args.isNotEmpty) {
    print("Using port ${args.first}");
    EquoCommService.setPort(int.parse(args.first));
  }



  int? widgetId = getWidgetId(args);
  var theme = getTheme(args) == "dark" ? ThemeMode.dark : ThemeMode.light;
  WidgetsFlutterBinding.ensureInitialized();

  preRunApp(widgetId);
  runApp(MyApp(widgetId: widgetId ?? 1, theme: theme));
  EquoCommService.send("ClientReady");
  print("Sent ClientReady");
}

class MyApp extends StatelessWidget {
  final int widgetId;
  final ThemeMode theme;

  // const MyApp({ super.key, required this.id});
  const MyApp({Key? key, required this.widgetId, this.theme = ThemeMode.light}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return FluentApp (
      title: 'Flutter Demo',
      // color: Colors.red,
      darkTheme: FluentThemeData.dark().copyWith(
        accentColor: Colors.blue),
        // accentColor: SystemTheme.accentColor.accent.toAccentColor()),
      theme: FluentThemeData.light().copyWith(
        // accentColor: SystemTheme.accentColor.accent.toAccentColor(),
        accentColor: Colors.blue,
          /*scaffoldBackgroundColor: Colors.teal, acrylicBackgroundColor: Colors.black, inactiveBackgroundColor: Colors.green, micaBackgroundColor: Colors.purple*/),
      themeMode: theme,
      debugShowCheckedModeBanner: false,
      // darkTheme: FluentThemeData(
      //   brightness: Brightness.dark,
      //   // accentColor: appTheme.color,
      //   // scaffoldBackgroundColor: Colors.black,
      //   visualDensity: VisualDensity.compact,
      //   focusTheme: FocusThemeData(
      //     glowFactor: is10footScreen(context) ? 2.0 : 1.0,
      //   ),
      // ),
      // theme: FluentThemeData(
      //   accentColor: Colors.orange,
      //   visualDensity: VisualDensity.compact,

      //   focusTheme: FocusThemeData(
      //     glowFactor: is10footScreen(context) ? 2.0 : 0.0,
      //   ),
      // ),

      home: MyHomePage(widgetId: widgetId),
    );
    // return mat.MaterialApp(
    //   title: 'Flutter Demo',
    //   theme: mat.ThemeData(
    //     // This is the theme of your application.
    //     //
    //     // TRY THIS: Try running your application with "flutter run". You'll see
    //     // the application has a purple toolbar. Then, without quitting the app,
    //     // try changing the seedColor in the colorScheme below to Colors.green
    //     // and then invoke "hot reload" (save your changes or press the "hot
    //     // reload" button in a Flutter-supported IDE, or press "r" if you used
    //     // the command line to start the app).
    //     //
    //     // Notice that the counter didn't reset back to zero; the application
    //     // state is not lost during the reload. To reset the state, use hot
    //     // restart instead.
    //     //
    //     // This works for code too, not just values: Most code changes can be
    //     // tested with just a hot reload.
    //     colorScheme: mat.ColorScheme.fromSeed(seedColor: mat.Colors.deepPurple),
    //     useMaterial3: true,
    //   ),
    //   home: const MyHomePage(),
    // );
  }
}

class MyHomePage extends StatelessWidget {
  final int widgetId;

  // const MyHomePage({super.key, required this.id});
  const MyHomePage({Key? key, required this.widgetId}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final widget = ButtonValue.empty();
    widget.id = widgetId;

    // return Mica(child: ShellSwt(value: shell));
    return Acrylic(child: ButtonSwt(value: widget));
    // return ScaffoldPage(
    //   content: Mica(child: ShellSwt(value: shell)),
    // );
    // return mat.Scaffold(
    //   // body: Center(child: ShellSwt(value: shell)),
    //   body: ShellSwt(value: shell),
    // );
  }
}
