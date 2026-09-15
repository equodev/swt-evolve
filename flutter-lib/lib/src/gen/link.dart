import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/cursor.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
import '../gen/region.dart';
import '../gen/widget.dart';
import '../impl/link_evolve.dart';
import 'event.dart';
import 'widgets.dart';

part 'link.g.dart';

class LinkSwt<V extends VLink> extends ControlSwt<V> {
  const LinkSwt({super.key, required super.value});

  @override
  State createState() => LinkImpl<LinkSwt<VLink>, VLink>();

  void sendSelectionDefaultSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/DefaultSelection", payload);
  }

  void sendSelectionSelection(V val, VEvent? payload) {
    sendEvent(val, "Selection/Selection", payload);
  }
}

@JsonSerializable()
class VLink extends VControl {
  VLink() : this.empty();
  VLink.empty() {
    swt = "Link";
  }

  VColor? linkForeground;
  String? text;

  @override
  void copyFrom(VWidget other) {
    super.copyFrom(other);
    if (other is VLink) {
      linkForeground = other.linkForeground;
      text = other.text;
    }
  }

  @override
  void readProperty(String key, Map<String, dynamic> json) {
    switch (key) {
      case 'linkForeground':
        linkForeground = json['linkForeground'] == null
            ? null
            : VColor.fromJson(json['linkForeground'] as Map<String, dynamic>);
      case 'text':
        text = json['text'] as String?;
      default:
        super.readProperty(key, json);
    }
  }

  factory VLink.fromJson(Map<String, dynamic> json) =>
      _$VLinkFromJson(json)..isReference = json.containsKey('_r');
  Map<String, dynamic> toJson() => _$VLinkToJson(this);
}
