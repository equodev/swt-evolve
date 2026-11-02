/// The value-class shape the generated `dev.equo.swt.size` themes are written in.
///
/// A record would say the same thing in one line, but `swt_native/src/main` is compiled at the
/// oldest JDK each supported SWT release runs on — Java 8 for 3.114 — and records need 16.
/// No equals, hashCode or toString: nothing compares, hashes or prints a theme.
String javaValueClass(
  String className,
  List<(String type, String name)> components,
) {
  final buffer = StringBuffer();
  buffer.writeln('public final class $className {');
  for (final (type, name) in components) {
    buffer.writeln('    private final $type $name;');
  }
  buffer.writeln();
  buffer.writeln(
    '    public $className(${components.map((c) => '${c.$1} ${c.$2}').join(', ')}) {',
  );
  for (final (_, name) in components) {
    buffer.writeln('        this.$name = $name;');
  }
  buffer.writeln('    }');
  buffer.writeln();
  for (final (type, name) in components) {
    buffer.writeln('    public $type $name() { return $name; }');
  }
  buffer.writeln();
  return buffer.toString();
}
