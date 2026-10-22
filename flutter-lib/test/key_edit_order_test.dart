import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/key_edit_order.dart';

void main() {
  test('a KeyUp that overtakes its edit waits for it', () {
    final order = KeyEditOrder();
    final sent = <String>[];

    order.forward(typed: true, send: () => sent.add('KeyDown x'));
    order.forward(typed: false, send: () => sent.add('KeyUp x'));
    order.forward(typed: true, send: () => sent.add('KeyDown y'));
    expect(sent, ['KeyDown x']);

    sent.add('Modify x');
    order.editApplied();
    expect(sent, ['KeyDown x', 'Modify x', 'KeyUp x', 'KeyDown y']);
  });

  test('a KeyUp after its edit goes at once', () {
    final order = KeyEditOrder();
    final sent = <String>[];

    order.forward(typed: true, send: () => sent.add('KeyDown x'));
    sent.add('Modify x');
    order.editApplied();
    order.forward(typed: false, send: () => sent.add('KeyUp x'));

    expect(sent, ['KeyDown x', 'Modify x', 'KeyUp x']);
  });

  testWidgets('a key the field never applies holds nothing for good', (tester) async {
    final order = KeyEditOrder(timeout: const Duration(milliseconds: 250));
    final sent = <String>[];

    order.forward(typed: true, send: () => sent.add('KeyDown x'));
    order.forward(typed: false, send: () => sent.add('KeyUp x'));
    await tester.pump(const Duration(milliseconds: 300));
    order.forward(typed: false, send: () => sent.add('KeyUp Shift'));

    expect(sent, ['KeyDown x', 'KeyUp x', 'KeyUp Shift']);
  });

  test('keys typed nowhere are never held', () {
    final order = KeyEditOrder();
    final sent = <String>[];

    order.forward(typed: false, send: () => sent.add('KeyDown F1'));
    order.forward(typed: false, send: () => sent.add('KeyUp F1'));

    expect(sent, ['KeyDown F1', 'KeyUp F1']);
  });
}
