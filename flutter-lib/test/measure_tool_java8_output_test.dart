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
  });

  test(
    'a theme has fields, a constructor and accessors, no equals, hashCode or toString',
    () {
      final java = javaValueClass('CanvasTheme', const [
        ('int', 'red'),
        ('int', 'green'),
        ('int', 'blue'),
      ]);

      expect(
        java,
        contains('    public CanvasTheme(int red, int green, int blue) {'),
      );
      for (final name in const ['red', 'green', 'blue']) {
        expect(java, contains('    private final int $name;'));
        expect(java, contains('        this.$name = $name;'));
        expect(java, contains('    public int $name() { return $name; }'));
      }
      expect(java, isNot(contains('equals(')));
      expect(java, isNot(contains('hashCode(')));
      expect(java, isNot(contains('toString(')));
    },
  );
}
