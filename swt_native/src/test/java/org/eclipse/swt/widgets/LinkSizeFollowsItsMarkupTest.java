package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Link shows its text without the tags of its anchors and without the {@code '&'} of a mnemonic, and
 * is sized for what it shows; any other markup is text. The reading of a malformed anchor is the one of
 * {@code Link.parse}.
 */
@Tag("native-unit")
class LinkSizeFollowsItsMarkupTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Point natural(Control control) {
        return control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    }

    private static Point sizeOf(String text) {
        Link link = new Link(DartMocks.dartShell(), SWT.NONE);
        link.setText(text);
        return natural(link);
    }

    @Test
    @DisplayName("displayText drops the tags of an anchor and keeps the text around it")
    void anchorsAreDropped() {
        assertThat(LinkHelper.displayText("Hello <a href=\"x\">big</a> world")).isEqualTo("Hello big world");
        assertThat(LinkHelper.displayText("<A HREF=\"x\">Up</A>")).isEqualTo("Up");
        assertThat(LinkHelper.displayText("<a target=\"t\" href=\"x\">Go</a>")).isEqualTo("Go");
        assertThat(LinkHelper.displayText("<a href=\"a>b\">Go</a>")).isEqualTo("Go");
        assertThat(LinkHelper.displayText("<a>one</a><a>two</a>")).isEqualTo("onetwo");
        assertThat(LinkHelper.displayText("x<a></a>y")).isEqualTo("xy");
    }

    @Test
    @DisplayName("displayText reads an anchor that is not closed as its text")
    void unclosedAnchors() {
        assertThat(LinkHelper.displayText("<a>foo")).isEqualTo("foo");
        assertThat(LinkHelper.displayText("before <a href=\"x\">after")).isEqualTo("before after");
        assertThat(LinkHelper.displayText("<a>foo</a> tail <a>bar")).isEqualTo("foo tail bar");
    }

    @Test
    @DisplayName("displayText keeps what is not an anchor as it was written")
    void otherMarkupIsText() {
        assertThat(LinkHelper.displayText("<b>x</b>")).isEqualTo("<b>x</b>");
        assertThat(LinkHelper.displayText("1 < 2 > 0")).isEqualTo("1 < 2 > 0");
        assertThat(LinkHelper.displayText("<")).isEqualTo("<");
        assertThat(LinkHelper.displayText("<a")).isEqualTo("<a");
        assertThat(LinkHelper.displayText("")).isEmpty();
        assertThat(LinkHelper.displayText(null)).isEmpty();
    }

    @Test
    @DisplayName("displayText reads an end tag with no anchor open as the start of an unclosed one")
    void strayEndTag() {
        assertThat(LinkHelper.displayText("stray </a> end")).isEqualTo("stray  end");
    }

    @Test
    @DisplayName("displayText drops a mnemonic marker, in and out of an anchor, and shows && as one &")
    void mnemonics() {
        assertThat(LinkHelper.displayText("&Save")).isEqualTo("Save");
        assertThat(LinkHelper.displayText("Save&")).isEqualTo("Save");
        assertThat(LinkHelper.displayText("a&&b")).isEqualTo("a&b");
        assertThat(LinkHelper.displayText("a&&&b")).isEqualTo("a&b");
        assertThat(LinkHelper.displayText("&Go <a>&now</a>")).isEqualTo("Go now");
        assertThat(LinkHelper.displayText("a&<a>b</a>")).isEqualTo("ab");
        assertThat(LinkHelper.displayText("<a href=\"x?a=1&b=2\">Go</a>")).isEqualTo("Go");
    }

    @Test
    @DisplayName("a Link is as big as the text it shows, whatever markup it was given")
    void sizeIsThatOfTheShownText() {
        assertThat(sizeOf("<a href=\"http://example.com/a/very/long/address\">Click</a>")).isEqualTo(sizeOf("Click"));
        assertThat(sizeOf("<a href=\"a>b\">Click</a>")).isEqualTo(sizeOf("Click"));
        assertThat(sizeOf("&Save")).isEqualTo(sizeOf("Save"));
        assertThat(sizeOf("<a>&Go</a>")).isEqualTo(sizeOf("Go"));
        assertThat(sizeOf("<b>x</b>").x).isGreaterThan(sizeOf("x").x);
        assertThat(sizeOf("a&&b").x).isGreaterThan(sizeOf("ab").x);
    }

    @Test
    @DisplayName("wrapped text wraps at the same places with or without markup")
    void wrappedSizeIsThatOfTheShownText() {
        Link plain = new Link(DartMocks.dartShell(), SWT.NONE);
        plain.setText("one two three four five six seven eight nine ten");
        Link markup = new Link(DartMocks.dartShell(), SWT.NONE);
        markup.setText("one <a href=\"http://example.com/a/long/address/that/is/not/shown\">two three</a> four five six seven eight nine ten");
        assertThat(markup.computeSize(90, SWT.DEFAULT)).isEqualTo(plain.computeSize(90, SWT.DEFAULT));
    }

    @Test
    @DisplayName("a negative hint other than DEFAULT counts as zero")
    void negativeHints() {
        Link link = new Link(DartMocks.dartShell(), SWT.NONE);
        link.setText("OK");
        assertThat(link.computeSize(-5, -7)).isEqualTo(new Point(0, 0));
        assertThat(link.computeSize(-2, SWT.DEFAULT).x).isZero();
        assertThat(link.computeSize(SWT.DEFAULT, -2).y).isZero();
    }

    @Test
    @DisplayName("a Link with no text to show has no default width: it is narrower than one that shows a character")
    void emptyLinkHasNoDefaultWidth() {
        Link link = new Link(DartMocks.dartShell(), SWT.NONE);
        int empty = link.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        link.setText("<a></a>");
        assertThat(link.computeSize(SWT.DEFAULT, SWT.DEFAULT).x).isEqualTo(empty);
        link.setText("x");
        assertThat(link.computeSize(SWT.DEFAULT, SWT.DEFAULT).x).isGreaterThan(empty);
        assertThat(empty).isLessThan(64);
    }
}
