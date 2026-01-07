import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'src/comm/comm.dart';

Future<ui.Image> decodeImage(Uint8List bytes) async {
  final codec = await ui.instantiateImageCodec(bytes);
  final frame = await codec.getNextFrame();
  return frame.image;
}

void measureRequest(String bridge, int id) {
  print("Listen on $bridge/$id/imageSizeRequest");
  EquoCommService.onRaw("$bridge/$id/imageSizeRequest", (payload) async {
    print("on $bridge/$id/imageSizeRequest");
    var imageRequest = jsonDecode(payload as String) as Map<String, dynamic>;
    var base64Data = imageRequest["imageData"] as String;

    Uint8List imageBytes = base64Decode(base64Data);

    // Decode image to get dimensions
    final image = await decodeImage(imageBytes);
    final width = image.width.toDouble();
    final height = image.height.toDouble();

    print("dart image size: ${width}x${height}");

    final result = [width, height];
    print("send $bridge/$id/imageSizeResponse $result");
    EquoCommService.sendPayload("$bridge/$id/imageSizeResponse", result);

    image.dispose();
  });
}