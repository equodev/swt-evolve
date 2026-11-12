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
//
// A property the client (`lib`) uses only through some of the classes that inherit it is reported for
// each other one as `Subclass.property` (a class it is not declared on): `--subclasses-of <V...>` limits
// that part to the classes given and their subclasses. A use through the declaring class, a class in
// between, a class beneath, or a receiver of no known value class counts for every subclass it could be.

import 'dart:convert';
import 'dart:io';

import 'package:analyzer/dart/analysis/analysis_context_collection.dart';
import 'package:analyzer/dart/analysis/results.dart';
import 'package:analyzer/dart/ast/ast.dart';
import 'package:analyzer/dart/ast/visitor.dart';
import 'package:analyzer/dart/element/element.dart';
import 'package:analyzer/dart/element/type.dart';

Future<void> main(List<String> args) async {
  final outPath = _option(args, '--out');
  final asJson = args.contains('--json');
  final includeTests = args.contains('--include-tests');
  final subclassesOf = _option(args, '--subclasses-of')?.split(',').toSet();

  final root = Directory.current.absolute.path;
  final genPrefix = _join(root, 'lib/src/gen');

  final roots = [_join(root, 'lib'), _join(root, 'test'), _join(root, 'tool')]
      .where((p) => Directory(p).existsSync())
      .toList();

  final collection = AnalysisContextCollection(includedPaths: roots);

  final declared = <String, Set<String>>{};
  final supers = <String, String>{};
  final usedInLib = <String, Set<String>>{};
  final usedInTest = <String, Set<String>>{};
  final libReceivers = <String, Set<String>>{};

  var scanned = 0;
  for (final context in collection.contexts) {
    for (final path in context.contextRoot.analyzedFiles()) {
      if (!path.endsWith('.dart')) continue;
      if (!roots.any(path.startsWith)) continue;

      final unit = await context.currentSession.getResolvedUnit(path);
      if (unit is! ResolvedUnitResult) continue;
      scanned++;

      if (path.startsWith(genPrefix)) {
        unit.unit.accept(_DeclarationCollector(declared, supers));
        continue;
      }
      final test = path.startsWith(_join(root, 'test'));
      final client = path.startsWith(_join(root, 'lib'));
      unit.unit.accept(_UsageCollector(test ? usedInTest : usedInLib, client ? libReceivers : null));
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

  final bySubclass = _unreadBySubclass(declared, supers, usedInLib, libReceivers, subclassesOf);

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
    lines.addAll(bySubclass);
    _emit(outPath, lines.join('\n'));
  }

  stderr.writeln('scanned $scanned files, '
      '${declared.length} value classes, $totalDeclared properties');
  stderr.writeln('unused: $totalUnused'
      '${includeTests ? '' : ' (of which read only by tests: $totalTestOnly)'}');
  stderr.writeln('unread by a subclass: ${bySubclass.length}');
}

/// `Subclass.property` for each inherited property no use in the client can be making of that subclass.
List<String> _unreadBySubclass(Map<String, Set<String>> declared, Map<String, String> supers,
    Map<String, Set<String>> used, Map<String, Set<String>> receivers, Set<String>? only) {
  bool inherits(String sub, String ancestor) {
    for (String? c = supers[sub]; c != null; c = supers[c]) {
      if (c == ancestor) return true;
    }
    return false;
  }

  bool wanted(String c) => only == null || only.any((o) => c == o || inherits(c, o));
  final lines = <String>[];
  for (final owner in declared.keys.toList()..sort()) {
    for (final property in declared[owner]!.toList()..sort()) {
      if (!(used[owner]?.contains(property) ?? false)) continue;
      final via = receivers['$owner.$property'] ?? const <String>{};
      if (via.contains('*')) continue;
      for (final sub in declared.keys.toList()..sort()) {
        if (!inherits(sub, owner) || !wanted(sub)) continue;
        final read = via.any((r) => r == sub || inherits(sub, r) || inherits(r, sub));
        if (!read) lines.add('$sub.$property');
      }
    }
  }
  return lines;
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
  _DeclarationCollector(this.into, this.supers);

  final Map<String, Set<String>> into;

  /// Each value class's superclass, by name.
  final Map<String, String> supers;

  @override
  void visitClassDeclaration(ClassDeclaration node) {
    final owner = node.namePart.typeName.lexeme;
    if (owner.startsWith('V')) {
      into.putIfAbsent(owner, () => <String>{});
      final superclass = node.extendsClause?.superclass.name.lexeme;
      if (superclass != null) supers[owner] = superclass;
      for (final member in node.body.members.whereType<FieldDeclaration>()) {
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
  _UsageCollector(this.into, this.receivers);

  final Map<String, Set<String>> into;

  /// The value class each `Owner.property` is used through, `*` for a receiver of none; null when
  /// not asked for.
  final Map<String, Set<String>>? receivers;

  void _record(Element? element, [AstNode? at]) {
    if (element == null) return;
    if (element is! PropertyAccessorElement && element is! FieldElement) return;

    var name = element.name;
    if (name == null) return;
    if (name.endsWith('=')) name = name.substring(0, name.length - 1);

    final owner = element.enclosingElement?.name;
    if (owner == null || !owner.startsWith('V')) return;
    into.putIfAbsent(owner, () => <String>{}).add(name);
    receivers?.putIfAbsent('$owner.$name', () => <String>{}).add(at == null ? '*' : _receiverOf(at));
  }

  /// The value class the property named at [node] is read off, or `*`.
  static String _receiverOf(AstNode node) {
    final parent = node.parent;
    DartType? type;
    if (node is PrefixedIdentifier) {
      type = node.prefix.staticType;
    } else if (node is PropertyAccess) {
      type = node.realTarget.staticType;
    } else if (parent is PrefixedIdentifier && identical(parent.identifier, node)) {
      type = parent.prefix.staticType;
    } else if (parent is PropertyAccess && identical(parent.propertyName, node)) {
      type = parent.realTarget.staticType;
    } else {
      final extension = node.thisOrAncestorOfType<ExtensionDeclaration>();
      type = extension != null
          ? extension.declaredFragment?.element.extendedType
          : node.thisOrAncestorOfType<ClassDeclaration>()?.declaredFragment?.element.thisType;
    }
    if (type is TypeParameterType) type = type.bound;
    final name = type is InterfaceType ? type.element.name : null;
    return name != null && name.startsWith('V') ? name : '*';
  }

  @override
  void visitSimpleIdentifier(SimpleIdentifier node) {
    _record(node.element, node);
    super.visitSimpleIdentifier(node);
  }

  // Writing a property is using it, and the identifier on the left of an assignment does not carry
  // the element it resolves to - the assignment does. Missing these reported every property that is
  // only ever written as unread, which is most of what an event value is for.
  @override
  void visitAssignmentExpression(AssignmentExpression node) {
    _record(node.writeElement, node.leftHandSide);
    _record(node.readElement, node.leftHandSide);
    super.visitAssignmentExpression(node);
  }

  @override
  void visitPostfixExpression(PostfixExpression node) {
    _record(node.writeElement, node.operand);
    _record(node.readElement, node.operand);
    super.visitPostfixExpression(node);
  }

  @override
  void visitPrefixExpression(PrefixExpression node) {
    _record(node.writeElement, node.operand);
    _record(node.readElement, node.operand);
    super.visitPrefixExpression(node);
  }
}
