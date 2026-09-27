// On the web the keydown that commits a dead-key composition (`´` + `a` = `á`) also carries the
// composed letter, so the editor is offered it twice and must insert it once.

import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/src/impl/utils/composed_text_input.dart';

void main() {
  _sharedGateTests();
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
