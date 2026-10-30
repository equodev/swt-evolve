// An image described beside a record the client does not apply.
//
// An image is described in full the first time it is written for a client and named after that.
// The sender counts it delivered once the frame carrying it is sent, whether or not the client
// goes on to apply the widget record next to it. A record can be declined - a change computed from
// a state the widget does not hold, or one written before the state already held - and the image
// described with it has to be kept all the same, or every later name for it resolves to nothing:
// an icon that never appears.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/gen/toolitem.dart';
import 'package:swtflutter/src/gen/widgets.dart' show WireKey;

import 'support/deliver.dart';

VImage _imageWhole(int id) => VImage()
  ..id = id
  ..width = 16
  ..height = 16
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..depth = 24);

VImage _imageName(int id) => VImage()
  ..id = id
  ..isReference = true;

VToolItem _itemValue(int id, int seq, {VImage? image}) => VToolItem()
  ..id = id
  ..seq = seq
  ..style = SWT.PUSH
  ..image = image;

VToolItem _item(VRegistry registry, int id) => registry.valueOn('ToolItem/$id') as VToolItem;

void main() {
  late VRegistry registry;

  setUp(() {
    deliveryGate.reset();
    registry = VRegistry();
  });

  test('an image described beside a change that does not fit is kept', () {
    applyWhole(_itemValue(21, 10), registry: registry);

    // Computed from a state this item does not hold, so it is asked for again rather than applied.
    applyChange(_itemValue(21, 20, image: _imageWhole(9101)),
        keys: [WireKey.k_Item_image], base: 5, registry: registry);
    // The answer describes the item whole, and names the image the sender counts as delivered.
    applyWhole(_itemValue(21, 30, image: _imageName(9101)), registry: registry);

    expect(_item(registry, 21).image!.imageData, isNotNull,
        reason: 'the name must resolve to the image described beside the record not applied');
  });

  test('an image described beside a change older than the state held is kept', () {
    applyWhole(_itemValue(22, 10), registry: registry);

    applyChange(_itemValue(22, 9, image: _imageWhole(9102)),
        keys: [WireKey.k_Item_image], base: 8, registry: registry);
    applyWhole(_itemValue(22, 30, image: _imageName(9102)), registry: registry);

    expect(_item(registry, 22).image!.imageData, isNotNull);
  });

  test('an image described beside a whole record older than the state held is kept', () {
    applyWhole(_itemValue(23, 10), registry: registry);

    applyWhole(_itemValue(23, 9, image: _imageWhole(9103)), registry: registry);
    applyWhole(_itemValue(23, 30, image: _imageName(9103)), registry: registry);

    expect(_item(registry, 23).image!.imageData, isNotNull);
  });
}
