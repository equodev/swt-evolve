// Reports the generated value-class properties that nothing on the Dart side reads.
//
// Every property a `V*` class declares is serialized by Java and decoded here, so one nothing reads
// is paid for on every send: bytes on the wire, a field in the decode, a name in the change record.
// The generator cannot answer this for itself - it writes the value classes, so from where it sits
// every property is used - which is why the question is asked from the consuming side instead.
//
// Usage is resolved, not matched by name: `.text` on a Flutter widget is not a use of `VLabel.text`,
// and a name-based scan would call almost everything used. Anything under `lib/src/gen` is ignored
// as a source of uses, because the generated code references every property it declares (copyFrom,
// readProperty, the codecs) and would make the whole exercise circular.
//
//   dart run tool/unused_value_properties.dart [--out <file>] [--json] [--include-tests]
//
// Without `--include-tests` a property read only by a test still counts as unused, which is the
// question the generator wants answered; the summary reports those separately either way.

import 'dart:convert';
import 'dart:io';

import 'package:analyzer/dart/analysis/analysis_context_collection.dart';
import 'package:analyzer/dart/analysis/results.dart';
import 'package:analyzer/dart/ast/ast.dart';
import 'package:analyzer/dart/ast/visitor.dart';
import 'package:analyzer/dart/element/element2.dart';

Future<void> main(List<String> args) async {
  final outPath = _option(args, '--out');
  final asJson = args.contains('--json');
  final includeTests = args.contains('--include-tests');

  final root = Directory.current.absolute.path;
  final genPrefix = _join(root, 'lib/src/gen');

  final roots = [_join(root, 'lib'), _join(root, 'test'), _join(root, 'tool')]
      .where((p) => Directory(p).existsSync())
      .toList();

  final collection = AnalysisContextCollection(includedPaths: roots);

  final declared = <String, Set<String>>{};
  final usedInLib = <String, Set<String>>{};
  final usedInTest = <String, Set<String>>{};

  var scanned = 0;
  for (final context in collection.contexts) {
    for (final path in context.contextRoot.analyzedFiles()) {
      if (!path.endsWith('.dart')) continue;
      if (!roots.any(path.startsWith)) continue;

      final unit = await context.currentSession.getResolvedUnit(path);
      if (unit is! ResolvedUnitResult) continue;
      scanned++;

      if (path.startsWith(genPrefix)) {
        unit.unit.accept(_DeclarationCollector(declared));
        continue;
      }
      final into = path.startsWith(_join(root, 'test')) ? usedInTest : usedInLib;
      unit.unit.accept(_UsageCollector(into));
    }
  }

  final report = <String, Map<String, List<String>>>{};
  var totalDeclared = 0;
  var totalUnused = 0;
  var totalTestOnly = 0;

  for (final entry in declared.entries.toList()
    ..sort((a, b) => a.key.compareTo(b.key))) {
    final owner = entry.key;
    final lib = usedInLib[owner] ?? const <String>{};
    final tests = usedInTest[owner] ?? const <String>{};

    final unused = <String>[];
    final testOnly = <String>[];
    for (final property in entry.value.toList()..sort()) {
      totalDeclared++;
      if (lib.contains(property)) continue;
      if (tests.contains(property)) {
        testOnly.add(property);
        if (includeTests) continue;
      }
      unused.add(property);
    }
    totalTestOnly += testOnly.length;
    totalUnused += unused.length;
    if (unused.isNotEmpty || testOnly.isNotEmpty) {
      report[owner] = {
        if (unused.isNotEmpty) 'unused': unused,
        if (testOnly.isNotEmpty) 'testOnly': testOnly,
      };
    }
  }

  if (asJson) {
    _emit(outPath, const JsonEncoder.withIndent('  ').convert(report));
  } else {
    final lines = <String>[
      '# Generated value-class properties nothing reads on the Dart side.',
      '# Produced by tool/unused_value_properties.dart - re-run it rather than editing this.',
      '# One "Class.property" per line.',
    ];
    for (final owner in report.keys) {
      for (final property in report[owner]!['unused'] ?? const <String>[]) {
        lines.add('$owner.$property');
      }
    }
    _emit(outPath, lines.join('\n'));
  }

  stderr.writeln('scanned $scanned files, '
      '${declared.length} value classes, $totalDeclared properties');
  stderr.writeln('unused: $totalUnused'
      '${includeTests ? '' : ' (of which read only by tests: $totalTestOnly)'}');
}

void _emit(String? outPath, String content) {
  if (outPath == null) {
    stdout.writeln(content);
  } else {
    File(outPath).writeAsStringSync('$content\n');
    stderr.writeln('wrote $outPath');
  }
}

String? _option(List<String> args, String name) {
  final i = args.indexOf(name);
  return i == -1 || i + 1 >= args.length ? null : args[i + 1];
}

String _join(String root, String rest) =>
    '$root${Platform.pathSeparator}${rest.replaceAll('/', Platform.pathSeparator)}';

/// The instance properties each `V*` class declares itself, inherited ones excluded: a property is
/// dropped where it is declared, so that is where it has to be counted.
class _DeclarationCollector extends RecursiveAstVisitor<void> {
  _DeclarationCollector(this.into);

  final Map<String, Set<String>> into;

  @override
  void visitClassDeclaration(ClassDeclaration node) {
    final owner = node.name.lexeme;
    if (owner.startsWith('V')) {
      for (final member in node.members.whereType<FieldDeclaration>()) {
        if (member.isStatic) continue;
        for (final variable in member.fields.variables) {
          into.putIfAbsent(owner, () => <String>{}).add(variable.name.lexeme);
        }
      }
    }
    super.visitClassDeclaration(node);
  }
}

/// Every resolved reference to a member of a `V*` class, recorded against the class that declares
/// it - so reading `enabled` off a `VLabel` counts as a use of `VControl.enabled`, which is where
/// it is declared and where dropping it would bite.
class _UsageCollector extends RecursiveAstVisitor<void> {
  _UsageCollector(this.into);

  final Map<String, Set<String>> into;

  void _record(Element2? element) {
    if (element == null) return;
    if (element is! PropertyAccessorElement2 && element is! FieldElement2) return;

    var name = element.name3;
    if (name == null) return;
    if (name.endsWith('=')) name = name.substring(0, name.length - 1);

    final owner = element.enclosingElement2?.name3;
    if (owner == null || !owner.startsWith('V')) return;
    into.putIfAbsent(owner, () => <String>{}).add(name);
  }

  @override
  void visitSimpleIdentifier(SimpleIdentifier node) {
    _record(node.element);
    super.visitSimpleIdentifier(node);
  }

  // Writing a property is using it, and the identifier on the left of an assignment does not carry
  // the element it resolves to - the assignment does. Missing these reported every property that is
  // only ever written as unread, which is most of what an event value is for.
  @override
  void visitAssignmentExpression(AssignmentExpression node) {
    _record(node.writeElement2);
    _record(node.readElement2);
    super.visitAssignmentExpression(node);
  }

  @override
  void visitPostfixExpression(PostfixExpression node) {
    _record(node.writeElement2);
    _record(node.readElement2);
    super.visitPostfixExpression(node);
  }

  @override
  void visitPrefixExpression(PrefixExpression node) {
    _record(node.writeElement2);
    _record(node.readElement2);
    super.visitPrefixExpression(node);
  }
}
