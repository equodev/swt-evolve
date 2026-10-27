// The measure tool writes into swt_native/src/main, which is compiled at the oldest JDK a supported
// SWT release runs on — Java 8 for 3.114. A record would say the same thing in one line and would
// not compile there, so the emitters go through javaValueClass instead.

import 'package:flutter_test/flutter_test.dart';

import '../tool/java_value_class.dart';

void main() {
  test('a single-component theme emits a final class, not a record', () {
    final java = javaValueClass('ButtonTheme', const [
      ('TextStyle', 'textStyle'),
    ]);

    expect(java, isNot(contains('record')));
    expect(java, contains('public final class ButtonTheme {'));
    expect(java, contains('    private final TextStyle textStyle;'));
    expect(
      java,
      contains('    public TextStyle textStyle() { return textStyle; }'),
    );
    expect(
      java,
      contains(
        '        return java.util.Objects.equals(textStyle, other.textStyle);',
      ),
    );
  });

  test('primitive components are compared by value and joined across lines', () {
    final java = javaValueClass('CanvasTheme', const [
      ('int', 'red'),
      ('int', 'green'),
      ('int', 'blue'),
    ]);

    expect(
      java,
      contains('    public CanvasTheme(int red, int green, int blue) {'),
    );
    expect(
      java,
      contains(
        '        return red == other.red\n'
        '            && green == other.green\n'
        '            && blue == other.blue;',
      ),
    );
    expect(
      java,
      contains(
        '    public String toString() { return "CanvasTheme[red=" + red + ", green=" + green + ", blue=" + blue + "]"; }',
      ),
    );
  });
}
