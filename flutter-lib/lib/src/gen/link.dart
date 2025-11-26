import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';
import '../comm/comm.dart';
import '../gen/color.dart';
import '../gen/control.dart';
import '../gen/font.dart';
import '../gen/image.dart';
import '../gen/menu.dart';
import '../gen/rectangle.dart';
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


@JsonSerializable() class VLink extends VControl {
  VLink() : this.empty();
  VLink.empty()  { swt = "Link"; }
  
  VColor? linkForeground;
  String? text;
  
  factory VLink.fromJson(Map<String, dynamic> json) => _$VLinkFromJson(json);
  Map<String, dynamic> toJson() => _$VLinkToJson(this);
  
}