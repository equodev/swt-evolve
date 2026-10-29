import 'package:json_annotation/json_annotation.dart';
import '../gen/imagedata.dart';

part 'image.g.dart';

@JsonSerializable()
class VImage {
  VImage() : this.empty();
  VImage.empty();

  /// The id Java knows this image by. An image used to travel as pixels under whatever
  /// widget owned it, so the same icon arrived once per owner and again with every full
  /// description of one; with an id it can be named instead.
  @JsonKey(includeToJson: false)
  int? id;

  /// True when this image arrived as a name rather than as a description.
  @JsonKey(includeToJson: false, includeFromJson: false)
  bool isReference = false;
  String? filename;
  int? height;
  VImageData? imageData;
  int? remoteRef;
  String? svgContent;
  int? width;

  factory VImage.fromJson(Map<String, dynamic> json) => rememberOrResolveImage(
    _$VImageFromJson(json)..isReference = json.containsKey('_r'),
  );
  Map<String, dynamic> toJson() => _$VImageToJson(this);
}

/// Every image this client holds, by its Java id, until Java disposes it ([forgetImage]).
final Map<int, VImage> _imagesHeld = {};

/// Answers a named image with the one already held, and remembers a described one.
VImage rememberOrResolveImage(VImage value) {
  final id = value.id;
  if (id == null) return value;
  if (value.isReference) return _imagesHeld[id] ?? value;
  // Only an image that arrived with something to draw is worth holding: one that is just a
  // size and a remote ref is resolved through the render cache instead.
  if (value.imageData != null || value.svgContent != null) {
    _imagesHeld[id] = value;
  }
  return value;
}

/// Drops the image Java disposed under [id]. A frame applied in the same turn may still name
/// it, so it goes a turn later, and not if a newer image has taken the id by then.
void forgetImage(int id) {
  final held = _imagesHeld[id];
  if (held == null) return;
  Future(() {
    if (identical(_imagesHeld[id], held)) _imagesHeld.remove(id);
  });
}

int get heldImageCount => _imagesHeld.length;
