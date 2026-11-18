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
      onComposedText: (text, replacing) => committed.add(text),
      onComposing: (text, caret, replacing) => composing.add(text),
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

  test('text inserted with no key behind it reaches the document', () {
    // Inserted in one step, with no composition and no keydown carrying the text.
    commit('xyz');

    expect(committed, ['xyz']);
  });

  test('a key typed after text inserted with no key behind it is not swallowed', () {
    commit('x');

    expect(input.swallowsKey('x'), isFalse, reason: 'no keydown carried the inserted x');
  });

  test('a composition reported without its range is committed once, by the browser\'s commit', () {
    // The web engine reports no composing range when the caret starts inside the composed text,
    // here inside the surrogate pair. Clearing the buffer then cancels the browser's composition,
    // and its commit arrives as a keyless insert of the same text.
    input.updateEditingValue(const TextEditingValue(
      text: '\u{1F389}',
      selection: TextSelection.collapsed(offset: 1),
    ));
    commit('\u{1F389}');

    expect(committed, ['\u{1F389}']);
  });

  testWidgets('the text a held key types is left to the key stream', (tester) async {
    await simulateKeyDownEvent(LogicalKeyboardKey.keyA, platform: 'web');
    commit('a');
    await simulateKeyUpEvent(LogicalKeyboardKey.keyA, platform: 'web');

    expect(committed, isEmpty, reason: 'the keydown already typed it');
  });

  testWidgets('a held modifier does not make inserted text a keystroke', (tester) async {
    await simulateKeyDownEvent(LogicalKeyboardKey.controlLeft, platform: 'web');
    await simulateKeyDownEvent(LogicalKeyboardKey.metaLeft, platform: 'web');
    commit('😀');
    await simulateKeyUpEvent(LogicalKeyboardKey.metaLeft, platform: 'web');
    await simulateKeyUpEvent(LogicalKeyboardKey.controlLeft, platform: 'web');

    expect(committed, ['😀']);
  });

  testWidgets('a pick that replaces the typed key replaces it in the document', (tester) async {
    // macOS press-and-hold: holding e types it, then picking an accented e in the accent menu with
    // the mouse makes the browser replace the e it typed before the caret.
    final replaced = <(String, int)>[];
    final picker = ComposedTextInput(onComposedText: (text, replacing) => replaced.add((text, replacing)));
    await simulateKeyDownEvent(LogicalKeyboardKey.keyE, platform: 'web');
    picker.updateEditingValue(const TextEditingValue(
        text: 'e', selection: TextSelection.collapsed(offset: 1)));
    await simulateKeyUpEvent(LogicalKeyboardKey.keyE, platform: 'web');

    expect(picker.currentTextEditingValue?.text, 'e',
        reason: 'the browser can only replace the e while its buffer still holds it');
    picker.updateEditingValue(const TextEditingValue(
        text: 'é', selection: TextSelection.collapsed(offset: 1)));

    expect(replaced, [('é', 1)], reason: 'é takes the place of the e, it is not appended to it');
  });

  testWidgets('a composition after a typed key reports only its own text', (tester) async {
    final composed = <(String, int, int)>[];
    final input = ComposedTextInput(
      onComposedText: (text, replacing) => composed.add((text, text.length, replacing)),
      onComposing: (text, caret, replacing) => composed.add((text, caret, replacing)),
    );
    await simulateKeyDownEvent(LogicalKeyboardKey.keyB, platform: 'web');
    input.updateEditingValue(const TextEditingValue(
        text: 'b', selection: TextSelection.collapsed(offset: 1)));
    await simulateKeyUpEvent(LogicalKeyboardKey.keyB, platform: 'web');

    input.updateEditingValue(const TextEditingValue(
        text: 'b´', selection: TextSelection.collapsed(offset: 2), composing: TextRange(start: 1, end: 2)));
    input.updateEditingValue(const TextEditingValue(
        text: 'bá', selection: TextSelection.collapsed(offset: 2)));

    expect(composed, [('´', 1, 0), ('á', 1, 0)]);
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
    final input = ComposedTextInput(onComposedText: (text, replacing) => committed.add(text));
    input.updateEditingValue(const TextEditingValue(
        text: '´', selection: TextSelection.collapsed(offset: 1), composing: TextRange(start: 0, end: 1)));
    input.updateEditingValue(const TextEditingValue(
        text: 'é', selection: TextSelection.collapsed(offset: 1)));

    expect(input.swallowsKey(''), isFalse, reason: 'a modifier is not the composed letter');
    expect(input.swallowsKey('é'), isTrue,
        reason: 'and the real key still finds the commit waiting for it');
  });
}
