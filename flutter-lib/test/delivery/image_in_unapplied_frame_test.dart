// An image described inside a frame the client does not apply.
//
// An image is described in full the first time it is written for a client and named after that.
// The sender counts it delivered once the frame carrying it is sent, whether or not the client
// goes on to apply that frame. A frame can be declined - a change computed from a state the widget
// does not hold, or one written before the state already held - and the image described inside it
// has to be kept all the same, or every later name for it resolves to nothing: an icon that never
// appears.

import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/comm_ws.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolitem.dart';

Map<String, dynamic> _imageWhole(int id) => {
      'id': id,
      'swt': 'Image',
      'width': 16,
      'height': 16,
      'imageData': {'width': 16, 'height': 16, 'depth': 24},
    };

Map<String, dynamic> _imageName(int id) => {'id': id, 'swt': 'Image', '_r': 1};

Map<String, dynamic> _itemWhole(int id, int seq, {Map<String, dynamic>? image}) => {
      'swt': 'ToolItem',
      'id': id,
      '_s': seq,
      'style': SWT.PUSH,
      if (image != null) 'image': image,
    };

Map<String, dynamic> _itemChange(int id, int seq, int base, Map<String, dynamic> image) => {
      'swt': 'ToolItem',
      'id': id,
      '_s': seq,
      '_b': base,
      '_d': ['image'],
      'image': image,
    };

Future<void> _deliver(String channel, Object payload) async {
  final action = utf8.encode(channel);
  final body = utf8.encode(json.encode(payload));
  final frame = Uint8List(2 + action.length + body.length);
  frame[0] = (action.length >> 8) & 0xFF;
  frame[1] = action.length & 0xFF;
  frame.setRange(2, 2 + action.length, action);
  frame.setRange(2 + action.length, frame.length, body);
  EquoCommService.commForTesting.receiveBinary(frame);
  await Future<void>.microtask(() {});
}

VToolItem _item(VRegistry registry, int id) => registry.valueOn('ToolItem/$id') as VToolItem;

void main() {
  late VRegistry registry;

  setUp(() {
    deliveryGate.reset();
    registry = VRegistry();
  });

  test('an image described in a change that does not fit is kept', () async {
    registry.register(VToolItem.fromJson(_itemWhole(21, 10)));
    deliveryGate.applied('ToolItem/21', 10);

    // Computed from a state this item does not hold, so it is asked for again rather than applied.
    await _deliver('ToolItem/21', _itemChange(21, 20, 5, _imageWhole(9101)));
    // The answer describes the item whole, and names the image the sender counts as delivered.
    await _deliver('ToolItem/21', _itemWhole(21, 30, image: _imageName(9101)));

    expect(_item(registry, 21).image!.imageData, isNotNull,
        reason: 'the name must resolve to the image described in the frame that was not applied');
  });

  test('an image described in a change older than the state held is kept', () async {
    registry.register(VToolItem.fromJson(_itemWhole(22, 10)));
    deliveryGate.applied('ToolItem/22', 10);

    await _deliver('ToolItem/22', _itemChange(22, 9, 8, _imageWhole(9102)));
    await _deliver('ToolItem/22', _itemWhole(22, 30, image: _imageName(9102)));

    expect(_item(registry, 22).image!.imageData, isNotNull);
  });

  test('an image described in a frame nothing is listening for yet is kept', () async {
    // Held for a listener that may never come, or dropped when too many pile up.
    await _deliver('ToolItem/24', _itemWhole(24, 10, image: _imageWhole(9104)));

    expect(VImage.fromJson(_imageName(9104)).imageData, isNotNull);
  });

  test('an image described in a whole frame older than the state held is kept', () async {
    registry.register(VToolItem.fromJson(_itemWhole(23, 10)));
    deliveryGate.applied('ToolItem/23', 10);

    await _deliver('ToolItem/23', _itemWhole(23, 9, image: _imageWhole(9103)));
    await _deliver('ToolItem/23', _itemWhole(23, 30, image: _imageName(9103)));

    expect(_item(registry, 23).image!.imageData, isNotNull);
  });
}
