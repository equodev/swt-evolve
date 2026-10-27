/// The value-class shape the generated `dev.equo.swt.size` themes are written in.
///
/// A record would say the same thing in one line, but `swt_native/src/main` is compiled at the
/// oldest JDK each supported SWT release runs on — Java 8 for 3.114 — and records need 16.
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
  buffer.writeln('    @Override');
  buffer.writeln('    public boolean equals(Object o) {');
  buffer.writeln('        if (this == o) return true;');
  buffer.writeln('        if (!(o instanceof $className)) return false;');
  buffer.writeln('        $className other = ($className) o;');
  final comparisons = components
      .map(
        (c) => _isPrimitive(c.$1)
            ? '${c.$2} == other.${c.$2}'
            : 'java.util.Objects.equals(${c.$2}, other.${c.$2})',
      )
      .toList();
  buffer.writeln('        return ${comparisons.join('\n            && ')};');
  buffer.writeln('    }');
  buffer.writeln();
  buffer.writeln('    @Override');
  buffer.writeln(
    '    public int hashCode() { return java.util.Objects.hash(${components.map((c) => c.$2).join(', ')}); }',
  );
  buffer.writeln();
  buffer.writeln('    @Override');
  final fields = components.map((c) => '${c.$2}=" + ${c.$2} + "').join(', ');
  buffer.writeln(
    '    public String toString() { return "$className[$fields]"; }',
  );
  buffer.writeln();
  return buffer.toString();
}

bool _isPrimitive(String type) => const {
  'int',
  'long',
  'double',
  'float',
  'boolean',
  'char',
  'byte',
  'short',
}.contains(type);
