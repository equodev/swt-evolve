package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Tab is a traversal only when the shell holds a control that takes the focus from it; with nowhere
 * to go, every native delivers the key as a KeyDown. These are the controls that count.
 */
class TabWithNowhereToGoTest extends KeyRoutingTestBase {

    @Test
    @DisplayName("labels, groups, sashes, a cool bar and an empty Link are no tab stop")
    void controlsTabPassesOver() {
        Composite parent = new Composite(shell(), SWT.NONE);
        new Label(parent, SWT.NONE).setText("name");
        new Group(new Composite(parent, SWT.NONE), SWT.NONE);
        new Sash(parent, SWT.VERTICAL);
        new CoolBar(parent, SWT.NONE);
        new Link(parent, SWT.NONE).setText("no link here");

        assertThat(ControlHelper.hasTabStop(parent)).isFalse();
    }

    @Test
    @DisplayName("a button, a field or a Link with a link anywhere inside is a tab stop")
    void controlsTabFocuses() {
        Composite parent = new Composite(shell(), SWT.NONE);
        Composite nested = new Composite(parent, SWT.NONE);
        new Label(parent, SWT.NONE);
        new Button(nested, SWT.PUSH).setText("OK");
        assertThat(ControlHelper.hasTabStop(parent)).isTrue();

        Composite withLink = new Composite(shell(), SWT.NONE);
        new Link(withLink, SWT.NONE).setText("see <a>the docs</a>");
        assertThat(ControlHelper.hasTabStop(withLink)).isTrue();
    }

    @Test
    @DisplayName("a Link holds a link only for <a followed by > or a space, as Link.parse reads one")
    void linkTextIsReadAsLinkParseDoes() {
        assertThat(ControlHelper.holdsLink("see <a>the docs</a>")).isTrue();
        assertThat(ControlHelper.holdsLink("see <A HREF=\"x\">the docs</A>")).isTrue();
        assertThat(ControlHelper.holdsLink("an <abbr>abbreviation</abbr>")).isFalse();
        assertThat(ControlHelper.holdsLink("a<all")).isFalse();
        assertThat(ControlHelper.holdsLink("ends in <a")).isFalse();
    }

    @Test
    @DisplayName("a hidden, disabled or NO_FOCUS control is no tab stop")
    void hiddenDisabledOrNoFocusIsNot() {
        Composite parent = new Composite(shell(), SWT.NONE);
        new Button(parent, SWT.PUSH).setVisible(false);
        new Text(parent, SWT.BORDER).setEnabled(false);
        new Button(parent, SWT.PUSH | SWT.NO_FOCUS);

        assertThat(ControlHelper.hasTabStop(parent)).isFalse();
    }

    @Test
    @DisplayName("a Canvas is a tab stop only while it hooks key events")
    void canvasCountsWhileItHooksKeys() {
        Composite parent = new Composite(shell(), SWT.NONE);
        Canvas canvas = new Canvas(parent, SWT.NONE);
        assertThat(ControlHelper.hasTabStop(parent)).isFalse();

        canvas.addListener(SWT.KeyDown, e -> { });
        assertThat(ControlHelper.hasTabStop(parent)).isTrue();
    }

    @Test
    @DisplayName("a Tab in a shell holding only a Label is delivered as a KeyDown")
    void tabWithNowhereToGoArrivesAsKeyDown() {
        Shell shell = shell();
        Label label = new Label(shell, SWT.NONE);
        java.util.List<String> events = new ArrayList<>();
        label.addListener(SWT.Traverse, e -> events.add("Traverse"));
        label.addListener(SWT.KeyDown, e -> events.add("KeyDown"));

        Event tab = new Event();
        tab.keyCode = SWT.TAB;
        tab.character = SWT.TAB;
        ControlHelper.routeKeyDown((DartControl) label.getImpl(), tab);

        assertThat(events).containsExactly("Traverse", "KeyDown");
    }
}
