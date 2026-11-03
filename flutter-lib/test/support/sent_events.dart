// The client's sent frames read back as (channel, fields), and Java's answers delivered as tagged
// maps, for tests that assert on a few fields of an event rather than on the whole VEvent.

import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/tagged_values.dart';
import 'package:swtflutter/src/gen/event.dart';

import 'channels.dart' as channels;

/// Java sending [body] on [channel], as it sends a map.
void deliverTagged(String channel, Map<String, Object?> body) =>
    EquoCommService.commForTesting.receiveBinary(channels.frame(channel, TaggedValues.encode(body)));

/// The fields of [e] the drag tests read; an unset field is left out.
Map<String, dynamic> eventFields(VEvent e) => {
      if (e.x != null) 'x': e.x,
      if (e.y != null) 'y': e.y,
      if (e.stateMask != null) 'stateMask': e.stateMask,
      if (e.detail != null) 'detail': e.detail,
      if (e.index != null) 'index': e.index,
      if (e.itemId != null) 'itemId': e.itemId,
      if (e.segments != null) 'segments': e.segments,
      if (e.width != null) 'width': e.width,
      if (e.height != null) 'height': e.height,
    };

/// Every frame sent so far, as (channel, the fields of the event it carries).
List<(String, Map<String, dynamic>)> sentEvents() => EquoCommService.commForTesting.sentFrames.map((f) {
      final channel = EquoCommBase.decodeFrame(f).$1;
      Map<String, dynamic> fields;
      try {
        fields = eventFields(EquoCommBase.decodeEvent(f));
      } catch (_) {
        fields = <String, dynamic>{};
      }
      return (channel, fields);
    }).toList();
