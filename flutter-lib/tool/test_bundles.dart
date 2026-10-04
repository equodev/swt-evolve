// Writes test/bundle_<i>_of_<n>_test.dart: each imports a slice of the test files and runs their
// main() inside a group, so `flutter test` compiles and boots n test isolates instead of one per
// file. test/flutter_test_config.dart resets the per-client state between tests, which is what lets
// files share an isolate. Files with @Tags or @TestOn are left out of the bundles, since a library
// annotation would apply to the whole bundle; `flutter test` runs them on their own.
//
//   dart run tool/test_bundles.dart 4
//   flutter test test/bundle_*_test.dart $(cat build/standalone_tests.txt)
import 'dart:io';

void main(List<String> args) {
  final n = args.isEmpty ? 4 : int.parse(args.first);
  final testDir = Directory('test');
  for (final old in testDir.listSync().whereType<File>()) {
    if (RegExp(r'bundle_\d+_of_\d+_test\.dart$').hasMatch(old.path)) old.deleteSync();
  }
  final bundled = <String>[];
  final alone = <String>[];
  for (final f in testDir.listSync(recursive: true).whereType<File>()) {
    final rel = f.path.substring(testDir.path.length + 1).replaceAll('\\', '/');
    if (!rel.endsWith('_test.dart')) continue;
    final src = f.readAsStringSync();
    if (src.contains('@Tags(') || src.contains('@TestOn(')) {
      alone.add(rel);
    } else {
      bundled.add(rel);
    }
  }
  bundled.sort();
  for (var i = 0; i < n; i++) {
    final slice = [for (var k = i; k < bundled.length; k += n) bundled[k]];
    final out = StringBuffer("import 'package:flutter_test/flutter_test.dart';\n");
    for (var k = 0; k < slice.length; k++) {
      out.writeln("import '${slice[k]}' as t$k;");
    }
    out.writeln('void main() {');
    for (var k = 0; k < slice.length; k++) {
      out.writeln("  group('${slice[k]}', t$k.main);");
    }
    out.writeln('}');
    File('test/bundle_${i}_of_${n}_test.dart').writeAsStringSync(out.toString());
  }
  Directory('build').createSync(recursive: true);
  File('build/standalone_tests.txt').writeAsStringSync(alone.map((p) => 'test/$p').join('\n'));
  stdout.writeln('${bundled.length} files in $n bundles; standalone: ${alone.length}');
}
