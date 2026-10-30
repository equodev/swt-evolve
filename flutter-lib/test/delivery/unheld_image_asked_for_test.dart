// An image a record names is held here or described in the same frame. One that is neither was
// counted as delivered by Java without reaching this client: the widget that named it is asked for,
// and Java describes it again with its images.

import 'package:flutter_test/flutter_test.dart';

import 'package:swtflutter/src/comm/comm_frame.dart';
import 'package:swtflutter/src/comm/delivery_gate.dart';
import 'package:swtflutter/src/comm/v_registry.dart';
import 'package:swtflutter/src/gen/button.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/swt.dart';

import 'support/deliver.dart';

void main() {
  late VRegistry registry;
  late List<(String, Object?)> sent;

  setUp(() {
    deliveryGate.reset();
    registry = VRegistry();
    sent = [];
    EquoCommBase.sendObserverForTesting = (actionId, payload) => sent.add((actionId, payload));
  });
  tearDown(() {
    EquoCommBase.sendObserverForTesting = null;
    registry.clear();
  });

  VButton button(int seq, {required VImage image}) => VButton()
    ..id = 31
    ..seq = seq
    ..style = SWT.PUSH
    ..text = 'b'
    ..image = image;

  test('a widget naming an image that never arrived is asked for', () {
    // Named by id only: the frame carries no record for it.
    applyWhole(button(10, image: VImage()..id = 777), registry: registry, describeImages: false);

    expect(sent.where((s) => s.$1 == EquoCommBase.widgetRefreshChannel).map((s) => s.$2), ['31']);
  });

  test('a widget naming an image the frame describes asks for nothing', () {
    final image = VImage()
      ..id = 778
      ..svgContent = '<svg xmlns="http://www.w3.org/2000/svg" width="1" height="1"/>';
    applyWhole(button(10, image: image), registry: registry);

    expect(sent.where((s) => s.$1 == EquoCommBase.widgetRefreshChannel), isEmpty);
  });
}
