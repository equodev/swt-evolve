import '../gen/image.dart';

/// Keeps every image described in [json], a frame received but not decoded into widgets.
///
/// An image is described in full the first time it is written for a client and named after that,
/// and the sender counts it delivered once the frame carrying it is sent. Decoding a frame keeps
/// its images; a frame that is held, dropped or declined instead has to keep them here, or every
/// later name for one resolves to nothing.
void keepImagesIn(Object? json) {
  if (json is Map) {
    if (json['swt'] == 'Image' && !json.containsKey('_r')) {
      VImage.fromJson(Map<String, dynamic>.from(json));
      return;
    }
    json.values.forEach(keepImagesIn);
  } else if (json is List) {
    json.forEach(keepImagesIn);
  }
}
