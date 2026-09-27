// Guards that a horizontal wheel flick carries on from the client's own last request, since the
// position Java reports lags a round trip behind.

import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/styledtext_evolve.dart';

void main() {
  const geometry = (increment: 10, minimum: 0, maximum: 1030, thumb: 661);

  int? tick(HorizontalWheelScroll scroll, int reported, int lines) => scroll.next(
        reported: reported,
        lines: lines,
        increment: geometry.increment,
        minimum: geometry.minimum,
        maximum: geometry.maximum,
        thumb: geometry.thumb,
      );

  test('the events of one gesture compose while Java is still catching up', () {
    final scroll = HorizontalWheelScroll();

    // Java keeps reporting 0 for the whole flick, because our own requests are what will move it.
    expect(tick(scroll, 0, 1), 10);
    expect(tick(scroll, 0, 1), 20);
    expect(tick(scroll, 0, 1), 30);
    expect(tick(scroll, 0, 1), 40);
  });

  test('once Java catches up it is the position that counts', () {
    final scroll = HorizontalWheelScroll();
    expect(tick(scroll, 0, 1), 10);
    expect(tick(scroll, 0, 1), 20);

    // Java arrives where it was asked; the next event carries on from there, without a jump.
    expect(tick(scroll, 20, 1), 30);
  });

  test('a move Java made for its own reasons wins', () {
    final scroll = HorizontalWheelScroll();
    expect(tick(scroll, 0, 1), 10);

    // Caret navigation put the view somewhere else entirely.
    expect(tick(scroll, 200, 1), 210);
  });

  test('scrolling back the other way composes too', () {
    final scroll = HorizontalWheelScroll();
    expect(tick(scroll, 0, 1), 10);
    expect(tick(scroll, 0, 1), 20);
    expect(tick(scroll, 0, -1), 10);
    expect(tick(scroll, 0, -1), 0);
  });

  test('nothing is asked for at the end of the track', () {
    final scroll = HorizontalWheelScroll();
    // maximum - thumb (1030 - 661) is as far as it goes.
    expect(tick(scroll, 369, 1), isNull);
    expect(tick(scroll, 0, -1), isNull);
  });
}
