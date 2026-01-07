import 'dart:convert';

import 'package:flutter/painting.dart';
import 'package:swtflutter/src/gen/point.dart';

import 'src/comm/comm.dart';

Size measureText(String text, TextStyle style) {
  final painter = TextPainter(
    text: TextSpan(text: text, style: style),
    textDirection: TextDirection.ltr,
  )..layout();

  var size = painter.size;
  painter.dispose();
  return size;
}

void measureRequest(String bridge, int id) {
  print("Listen on $bridge/$id/fontSizeRequest");
  EquoCommService.onRaw("$bridge/$id/fontSizeRequest", (payload) {
    print("on $bridge/$id/fontSizeRequest $payload");
    var textRequest = jsonDecode(payload as String) as Map<String, dynamic>;
    var text = textRequest["text"] as String;
    var font = textRequest["font"] as String;
    var height = textRequest["size"] as int;
    var italic = textRequest["style"] as bool;
    var bold = textRequest["weight"] as bool;

    final size = measureText(text, TextStyle(fontFamily: font, fontSize: height.toDouble(),
        fontStyle: italic ? FontStyle.italic : FontStyle.normal,
        fontWeight: bold ? FontWeight.bold : FontWeight.normal));
    print("dart size for $font @ $height: ${size.width}x${size.height}");
    // final point = VPoint()
    //   ..x = size.width.round()
    //   ..y = size.height.round();
    final point = [size.width, size.height];
    print("send $bridge/$id/fontSizeResponse $point");
    EquoCommService.sendPayload("$bridge/$id/fontSizeResponse", point);
  });
}

