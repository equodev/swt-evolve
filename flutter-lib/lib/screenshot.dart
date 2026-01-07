import 'dart:io';
import 'dart:ui' as ui;

import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';

Future<void> captureScreenshot(RenderBox renderBox, String fqn, String caseName) async {
  try {
    RenderRepaintBoundary? boundary;
    RenderObject? current = renderBox;
    while (current != null) {
      if (current is RenderRepaintBoundary) {
        boundary = current;
        break;
      }
      current = current.parent;
    }

    if (boundary == null) {
      print('Warning: Could not find RenderRepaintBoundary for screenshot');
      return;
    }

    final image = await boundary.toImage(pixelRatio: 1.0);
    final byteData = await image.toByteData(format: ui.ImageByteFormat.png);
    if (byteData == null) {
      print('Warning: Failed to convert image to bytes');
      return;
    }

    final screenshotsDir = Directory('./build/screenshots/${fqn.split('.').last}');
    if (!screenshotsDir.existsSync()) {
      screenshotsDir.createSync(recursive: true);
    }

    final file = File('${screenshotsDir.path}/$caseName.png');
    await file.writeAsBytes(byteData.buffer.asUint8List());
    print('Screenshot saved: ${file.path}');
  } catch (e) {
    print('Error capturing screenshot: $e');
  }
}

