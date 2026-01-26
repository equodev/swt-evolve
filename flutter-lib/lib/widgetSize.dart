import 'dart:convert';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:flutter/rendering.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:swtflutter/screenshot.dart';
import 'package:swtflutter/src/gen/point.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/widget.dart';
import 'package:swtflutter/src/gen/widgets.dart';
import 'package:swtflutter/src/impl/config_flags.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import 'package:swtflutter/src/theme/theme.dart';

import 'main.dart';
import 'src/comm/comm.dart';

const SCREENSHOT = false;

class MeasurementResult {
  final Size widget;
  final Size? text;
  final TextStyle? textStyle;
  final Size? image;

  MeasurementResult(this.widget, this.text, this.textStyle, this.image);
}

(MeasurementResult, RenderBox?) _measureCurrentCase(GlobalKey key) {
  final context = key.currentContext;
  if (context == null) {
    print("ERROR: context is null - widget not in tree");
    return (MeasurementResult(Size.zero, null, null, null), null);
  }

  final renderBox = context.findRenderObject() as RenderBox?;
  if (renderBox == null) {
    print("ERROR: renderBox is null");
    return (MeasurementResult(Size.zero, null, null, null), null);
  }

  if (!renderBox.hasSize) {
    print("ERROR: renderBox has no size (not laid out yet)");
    return (MeasurementResult(Size.zero, null, null, null), null);
  }

  printSizes(renderBox);
  final widgetSize = renderBox.size;
  final (textSize, textStyle) = _findTextSize(renderBox);
  final imageSize = _findImageSize(renderBox);

  return (MeasurementResult(widgetSize, textSize, textStyle, imageSize), renderBox);
}

(Size?, TextStyle?) _findTextSize(RenderBox renderBox) {
  if (renderBox is RenderParagraph) {
    final inlineSpan = renderBox.text;
    final textContent = inlineSpan.toPlainText();
    if (textContent.length != 1) { // discard single char texts like arrow
      if (inlineSpan is TextSpan) {
        return (renderBox.size, inlineSpan.style);
      }
      return (renderBox.size, null);
    }
  } else if (renderBox is RenderEditable) {
    final inlineSpan = renderBox.text;
    if (inlineSpan != null) {
      final textContent = inlineSpan.toPlainText();
      if (inlineSpan is TextSpan) {
        return (renderBox.size, inlineSpan.style);
      }
    }
    return (renderBox.size, null);
  }

  (Size?, TextStyle?) result = (null, null);
  renderBox.visitChildren((child) {
    if (result.$1 == null && child is RenderBox) {
      result = _findTextSize(child);
    }
  });
  return result;
}

Size? _findImageSize(RenderBox renderBox) {
  if (renderBox.runtimeType.toString().contains('RenderImage')) {
    return renderBox.size;
  }
  Size? result;
  renderBox.visitChildren((child) {
    if (result == null && child is RenderBox) {
      result = _findImageSize(child);
    }
  });
  return result;
}

void printSizes(RenderBox renderBox) {
  var size = renderBox.size;
  // print("flutter size for ${renderBox}: ${size.width}x${size.height}");
  renderBox.visitChildren((child) {
    if (child is RenderBox) {
      printSizes(child);
    }
  });
}

void measureRequest(String bridge, int id) {
  print("Listen on $bridge/$id/widgetSizeRequest");
  EquoCommService.onRaw("$bridge/$id/widgetSizeRequest", (payload) {
    print("on $bridge/$id/sizeRequest $payload");
    final Map<String, dynamic> widgetConfig = jsonDecode(payload as String);
    // final widgetValue = mapWidgetValue(widgetConfig["widget"]);
    // var widgetValue = widgetWithConfig[0];

    if (widgetConfig.containsKey("config")) {
      final config = ConfigFlags.fromJson(widgetConfig["config"]);
      setConfigFlags(config);
    }

    final key = GlobalKey();
    var widgetValue = widgetConfig["widget"];
    final widget = mapWidget(widgetValue);
    final widgetName = widgetValue["swt"];
    final caseName = widgetConfig["name"] as String;
    final testApp = _TestApp(KeyedSubtree(key: key, child: widget), bridge, id);
    runApp(testApp);

    // Schedule callback after widget is run, and wait for it to be fully laid out
    WidgetsBinding.instance.addPostFrameCallback((_) async {
        final (result, renderBox) = _measureCurrentCase(key);
        if (SCREENSHOT) {
          await captureScreenshot(renderBox, widgetName, caseName);
        }

        final response = {
          'widget': {'x': result.widget.width.round(), 'y': result.widget.height.round()},
          'text': result.text != null ? {'x': result.text!.width, 'y': result.text!.height} : null,
          'textStyle': result.textStyle != null ? {'name': result.textStyle!.fontFamily, 'size': result.textStyle!.fontSize, 'bold': result.textStyle!.fontWeight == FontWeight.bold, 'italic': result.textStyle!.fontStyle == FontStyle.italic} : null,
          'image': result.image != null ? {'x': result.image!.width, 'y': result.image!.height} : null,
        };

        // print("send $bridge/$id/widgetSizeResponse $response");
        EquoCommService.sendPayload("$bridge/$id/widgetSizeResponse", jsonEncode(response));
    });
  });
  runApp(SizedBox());
}

class _TestApp extends StatefulWidget {
  Widget swtwidget;
  String bridge;
  int id;

  _TestApp(this.swtwidget, this.bridge, this.id);

  @override
  State<_TestApp> createState() => _TestAppState();
}

class _TestAppState extends State<_TestApp> {
  late ThemeData lightTheme;
  late ThemeData darkTheme;

  @override
  void initState() {
    super.initState();
    lightTheme = createLightNonDefaultTheme(null);
    darkTheme = createDarkNonDefaultTheme(null);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: "Widget Measurement",
      theme: lightTheme,
      darkTheme: darkTheme,
      themeMode: ThemeMode.light,
      home: Scaffold(
        // appBar: AppBar(
        //   title: Text('Widget Measurement'),
        // ),
        body: Center(
            child: RepaintBoundary(child: widget.swtwidget),
        ),
      ),
    );
  }
}
