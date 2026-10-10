// On the web the keydown that commits a dead-key composition (`´` + `a` = `á`) also carries the
// composed letter, so the editor is offered it twice and must insert it once.

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/composed_text_input.dart';

void main() {
  _sharedGateTests();
  group('macOS', _macosTests);
  late List<String> committed;
  late List<String> composing;
  late ComposedTextInput input;

  setUp(() {
    committed = [];
    composing = [];
    input = ComposedTextInput(
      onComposedText: committed.add,
      onComposing: (text, caret) => composing.add(text),
    );
  });

  void compose(String text) => input.updateEditingValue(TextEditingValue(
        text: text,
        selection: TextSelection.collapsed(offset: text.length),
        composing: TextRange(start: 0, end: text.length),
      ));

  void commit(String text) => input.updateEditingValue(TextEditingValue(
        text: text,
        selection: TextSelection.collapsed(offset: text.length),
      ));

  test('the key that commits a composition is not typed again', () {
    compose('´');
    expect(input.swallowsKey('´'), isTrue, reason: 'the dead key belongs to the composition');

    commit('á');
    expect(committed, ['á']);

    expect(input.swallowsKey('á'), isTrue,
        reason: 'the committing keydown carries the composed letter and would double it');
  });

  test('the next real keystroke still goes through', () {
    compose('´');
    commit('á');
    input.swallowsKey('á');

    expect(input.swallowsKey('b'), isFalse);
  });

  test('a key that does not match what was committed is not swallowed', () {
    compose('´');
    commit('á');

    expect(input.swallowsKey('z'), isFalse,
        reason: 'only the key carrying the composed text is the duplicate');
    expect(input.swallowsKey('á'), isFalse,
        reason: 'and the commit is answered once, so a later á is the user typing');
  });

  test('ordinary typing is never swallowed', () {
    expect(input.swallowsKey('a'), isFalse);
    expect(input.swallowsKey(''), isFalse);
  });

  test('every key is swallowed while a composition is still running', () {
    compose('n');
    expect(input.swallowsKey('n'), isTrue);
    expect(input.swallowsKey('~'), isTrue);
    expect(composing, ['n']);
    expect(committed, isEmpty);
  });

  test('an abandoned composition commits nothing and swallows nothing after', () {
    compose('´');
    commit('');

    expect(committed, isEmpty);
    expect(input.swallowsKey('a'), isFalse);
  });
}

void _macosTests() {
  // On macOS the text input only receives the keys the editor leaves to it, so it gets the key that
  // ends a composition itself and nothing follows it to a forwarder.
  late List<String> committed;
  late ComposedTextInput input;

  setUp(() {
    debugDefaultTargetPlatformOverride = TargetPlatform.macOS;
    committed = [];
    input = ComposedTextInput(onComposedText: committed.add);
  });
  tearDown(() => debugDefaultTargetPlatformOverride = null);

  void update(String text, {bool composing = false}) => input.updateEditingValue(TextEditingValue(
        text: text,
        selection: TextSelection.collapsed(offset: text.length),
        composing: composing ? TextRange(start: 0, end: text.length) : TextRange.empty,
      ));

  test('on macOS a letter the dead key does not combine with is typed after the accent', () {
    update('´', composing: true);
    update('´');
    update('´x');

    expect(committed, ['´', 'x']);
  });

  test('on macOS a composition that follows a commit commits only its own text', () {
    update('´', composing: true);
    update('é');
    input.updateEditingValue(const TextEditingValue(
        text: 'é¨', selection: TextSelection.collapsed(offset: 2), composing: TextRange(start: 1, end: 2)));
    update('éü');

    expect(committed, ['é', 'ü']);
  });

  test('on macOS an abandoned composition commits nothing and clears what it showed', () {
    final shown = <String>[];
    input = ComposedTextInput(onComposedText: committed.add, onComposing: (text, _) => shown.add(text));
    update('´', composing: true);
    update('');

    expect(committed, isEmpty);
    expect(shown, ['´', '']);
  });

  test('on macOS the next keystroke is never taken for the committing key', () {
    update('´', composing: true);
    update('é');

    expect(committed, ['é']);
    expect(input.swallowsKey('é'), isFalse, reason: 'the committing key went to the composition');
  });
}

void _sharedGateTests() {
  test('a key with no character does not answer the commit', () {
    final committed = <String>[];
    final input = ComposedTextInput(onComposedText: committed.add);
    input.updateEditingValue(const TextEditingValue(
        text: '´', selection: TextSelection.collapsed(offset: 1), composing: TextRange(start: 0, end: 1)));
    input.updateEditingValue(const TextEditingValue(
        text: 'é', selection: TextSelection.collapsed(offset: 1)));

    expect(input.swallowsKey(''), isFalse, reason: 'a modifier is not the composed letter');
    expect(input.swallowsKey('é'), isTrue,
        reason: 'and the real key still finds the commit waiting for it');
  });
}
