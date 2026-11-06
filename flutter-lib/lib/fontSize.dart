import 'package:flutter/painting.dart';
import 'package:swtflutter/src/gen/point.dart';

import 'src/comm/comm.dart';
import 'src/gen/widgets.dart' show WireChannel;
import 'src/comm/widget_key.dart' show WidgetKey;
import 'src/impl/utils/bundled_assets.dart';

Size measureText(String text, TextStyle style) {
  final painter = TextPainter(
    text: TextSpan(text: text, style: style),
    textDirection: TextDirection.ltr,
  )..layout();

  var size = painter.size;
  painter.dispose();
  return size;
}

/// CSS-style numeric weight (300, 400, 500, ...) to Flutter's FontWeight.
FontWeight _fontWeightFromCss(int weight) {
  final index = ((weight.clamp(100, 900) - 100) / 100).round();
  return FontWeight.values[index];
}

void measureRequest(String bridge, int id) {
  print("Listen on $bridge/$id/fontSizeRequest");
  EquoCommService.on(WidgetKey.typeOf(bridge), id, WireChannel.fontSizeRequest, (payload) {
    print("on $bridge/$id/fontSizeRequest $payload");
    var textRequest = payload as Map<String, dynamic>;
    var text = textRequest["text"] as String;
    var font = textRequest["font"] as String;
    var height = textRequest["size"] as int;
    var italic = textRequest["style"] as bool;
    var weightRaw = textRequest["weight"];

    final fontWeight = weightRaw is bool
        ? (weightRaw ? FontWeight.bold : FontWeight.normal)
        : _fontWeightFromCss(weightRaw as int);

    final size = measureText(
      text,
      TextStyle(
        fontFamily: BundledAssets.family(font),
        fontSize: height.toDouble(),
        fontStyle: italic ? FontStyle.italic : FontStyle.normal,
        fontWeight: fontWeight,
      ),
    );
    print("dart size for $font @ $height: ${size.width}x${size.height}");
    final point = [size.width, size.height];
    print("send $bridge/$id/fontSizeResponse $point");
    EquoCommService.sendTo(WidgetKey.typeOf(bridge), id, WireChannel.fontSizeResponse, point);
  });
}
