package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code Browser.evaluate}'s argument is a <em>function body</em>, not an expression: it may run
 * statements and hands its value back with {@code return}. Every native SWT backend implements
 * that by wrapping the script in a function before running it ({@code WebBrowser.evaluate},
 * {@code Webkit2AsyncToSync.evaluate}), and callers rely on it — an embedded JS source editor
 * reads itself back with {@code evaluate("return editor.getValue();")}.
 *
 * <p>The Dart-backed Browser used to hand the script straight to {@code eval()} instead, which
 * makes exactly that idiom an <em>Illegal return statement</em> and fails the call. These tests
 * pin {@link BrowserScripting#asFunctionBody} — the single place the wrapping now happens, for
 * both {@code EvolveBrowser} mirrors and both the web-iframe and desktop-webview backends.
 *
 * <p>They are deliberately structural: no JDK ships a JS engine any more (Nashorn was removed in
 * Java 15), so the wrapper's <em>behaviour</em> can only be asserted against a real browser, and
 * that is what {@code BrowserFlutterTest.Scripting}'s {@code *_swtContract} tests do. What is
 * worth pinning cheaply here is the shape the behaviour rests on, since it is exactly what an
 * innocent-looking simplification would drop.
 */
class BrowserScriptingTest {

    @Test
    @DisplayName("the script becomes the body of an immediately-invoked function")
    void wrapsScriptAsAnImmediatelyInvokedFunction() {
        String wrapped = BrowserScripting.asFunctionBody("return editor.getValue();");
        // A function body is what makes `return` legal; invoking it inline is what makes the
        // value come straight back out of the eval, with no callback hop.
        assertThat(wrapped).startsWith("(function() {");
        assertThat(wrapped).endsWith("})();");
        assertThat(wrapped).contains("return editor.getValue();");
    }

    @Test
    @DisplayName("the closing braces sit on their own line, so a trailing // comment can't eat them")
    void closingBracesAreNotCommentedOut() {
        // Regression guard for the newlines: with the closer on the same line as the script,
        // "return 7; // done" comments out `})();` and the whole thing is a syntax error.
        String wrapped = BrowserScripting.asFunctionBody("return 7; // done");
        assertThat(wrapped).contains("\n})();");
        assertThat(wrapped).doesNotContain("// done})");
    }

    @Test
    @DisplayName("the opening brace is followed by a newline, so the script can start with a comment")
    void scriptStartsOnItsOwnLine() {
        assertThat(BrowserScripting.asFunctionBody("// leading\nreturn 1;"))
                .contains("{\n// leading");
    }

    @Test
    @DisplayName("a null script yields an empty body, never the literal JS `null`")
    void nullScriptBecomesAnEmptyBody() {
        // Browser.evaluate(null) is rejected upstream with ERROR_NULL_ARGUMENT long before this,
        // but the wrapper must still never splice the string "null" into the page's JS.
        assertThat(BrowserScripting.asFunctionBody(null)).isEqualTo("(function() {\n\n})();");
    }
}
