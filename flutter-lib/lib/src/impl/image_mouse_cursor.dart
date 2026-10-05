import 'dart:async';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import '../gen/cursor.dart';
import 'utils/image_utils.dart';

/// The cursor for an SWT cursor made from an image, or null when this platform cannot show one.
///
/// Only the Windows desktop engine takes a cursor image (`createCustomCursor/windows` on the
/// `flutter/mousecursor` channel); the image arrives PNG-encoded, as every image does.
ImageMouseCursor? imageMouseCursorOf(VCursor cursor) {
  final data = cursor.image?.imageData?.data;
  if (data == null || kIsWeb || defaultTargetPlatform != TargetPlatform.windows) return null;
  final png = ImageUtils.asBytes(data);
  return ImageMouseCursor(
    name: 'swt-cursor-${cursor.image?.id ?? Object.hashAll(png)}',
    png: png,
    hotX: cursor.hotspotX ?? 0,
    hotY: cursor.hotspotY ?? 0,
  );
}

/// A mouse cursor drawn from an image, registered with the engine once per [name].
class ImageMouseCursor extends MouseCursor {
  const ImageMouseCursor({
    required this.name,
    required this.png,
    required this.hotX,
    required this.hotY,
  });

  final String name;
  final Uint8List png;
  final int hotX;
  final int hotY;

  static final Map<String, Future<bool>> _registered = {};

  /// Registers the cursor with the engine; completes with whether it can be shown.
  Future<bool> _register() => _registered[name] ??= () async {
    try {
      final codec = await ui.instantiateImageCodec(png);
      final image = (await codec.getNextFrame()).image;
      final rgba = await image.toByteData(format: ui.ImageByteFormat.rawStraightRgba);
      final width = image.width, height = image.height;
      image.dispose();
      if (rgba == null) return false;
      await SystemChannels.mouseCursor.invokeMethod<void>('createCustomCursor/windows', {
        'name': name,
        'buffer': rgbaToBgra(rgba.buffer.asUint8List()),
        'width': width,
        'height': height,
        'hotX': hotX.toDouble(),
        'hotY': hotY.toDouble(),
      });
      return true;
    } catch (_) {
      return false;
    }
  }();

  @override
  MouseCursorSession createSession(int device) => _ImageMouseCursorSession(this, device);

  @override
  String get debugDescription => 'ImageMouseCursor($name)';

  @override
  bool operator ==(Object other) => other is ImageMouseCursor && other.name == name;

  @override
  int get hashCode => name.hashCode;
}

class _ImageMouseCursorSession extends MouseCursorSession {
  _ImageMouseCursorSession(ImageMouseCursor cursor, int device) : super(cursor, device);

  bool _disposed = false;

  @override
  ImageMouseCursor get cursor => super.cursor as ImageMouseCursor;

  @override
  Future<void> activate() async {
    final ok = await cursor._register();
    // A session replaced while the image was being registered must not take the cursor back.
    if (_disposed) return;
    if (ok) {
      await SystemChannels.mouseCursor.invokeMethod<void>('setCustomCursor/windows', {
        'name': cursor.name,
      });
    } else {
      await SystemChannels.mouseCursor.invokeMethod<void>('activateSystemCursor', {
        'device': device,
        'kind': SystemMouseCursors.basic.kind,
      });
    }
  }

  @override
  void dispose() {
    _disposed = true;
  }
}

/// The engine takes the pixels in the byte order of a Windows bitmap.
@visibleForTesting
Uint8List rgbaToBgra(Uint8List rgba) {
  final bgra = Uint8List(rgba.length);
  for (var i = 0; i + 3 < rgba.length; i += 4) {
    bgra[i] = rgba[i + 2];
    bgra[i + 1] = rgba[i + 1];
    bgra[i + 2] = rgba[i];
    bgra[i + 3] = rgba[i + 3];
  }
  return bgra;
}
