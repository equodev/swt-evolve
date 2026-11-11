package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A focused Link gives the keyboard to one anchor at a time: Enter or Space selects it, Tab and
 * Shift+Tab move between its anchors and leave the Link only past the last or the first, and a click
 * on an anchor makes it the focused one.
 */
class LinkAnchorKeysTest extends KeyRoutingTestBase {

    private static final String HREF = "https://www.eclipse.org";

    private java.util.List<String> events;

    private Link link() {
        Link link = new Link(shell(), SWT.NONE);
        link.setText("<a href=\"" + HREF + "\">First</a> between <a>Second</a>");
        events = new ArrayList<>();
        link.addListener(SWT.Selection, e -> events.add("Selection " + e.text));
        link.addListener(SWT.Traverse, e -> events.add("Traverse doit=" + e.doit));
        link.addListener(SWT.KeyDown, e -> events.add("KeyDown " + e.keyCode));
        return link;
    }

    @Test
    @DisplayName("Enter and Space select the first anchor, with its href")
    void enterAndSpaceSelectTheFocusedAnchor() {
        Link link = link();

        press(link, SWT.CR);
        press(link, ' ');

        assertThat(events).filteredOn(e -> e.startsWith("Selection")).containsExactly("Selection " + HREF, "Selection " + HREF);
    }

    @Test
    @DisplayName("a KeyDown listener that vetoes Enter keeps the anchor unselected")
    void vetoedEnterSelectsNothing() {
        Link link = link();
        link.addListener(SWT.KeyDown, e -> e.doit = false);

        press(link, SWT.CR);

        assertThat(events).noneMatch(e -> e.startsWith("Selection"));
    }

    @Test
    @DisplayName("Tab keeps the focus inside the Link and moves it to the next anchor; past the last one it leaves")
    void tabWalksTheAnchors() {
        Link link = link();

        press(link, SWT.TAB);
        press(link, SWT.CR);
        press(link, SWT.TAB);

        assertThat(events).startsWith("Traverse doit=false", "KeyDown 9", "Traverse doit=true", "KeyDown 13",
                "Selection Second", "Traverse doit=true");
    }

    @Test
    @DisplayName("Shift+Tab from the second anchor goes back to the first; from the first it leaves")
    void shiftTabWalksBack() {
        Link link = link();
        press(link, SWT.TAB);
        events.clear();

        press(link, SWT.TAB, SWT.SHIFT);
        press(link, SWT.CR);
        press(link, SWT.TAB, SWT.SHIFT);

        assertThat(events).startsWith("Traverse doit=false", "KeyDown 9", "Traverse doit=true", "KeyDown 13",
                "Selection " + HREF, "Traverse doit=true");
    }

    @Test
    @DisplayName("a click on the second anchor selects it and makes it the one Enter selects")
    void clickFocusesTheAnchor() {
        Link link = link();
        Event click = new Event();
        click.text = "Second";
        click.index = 1;

        LinkHelper.clicked((DartLink) link.getImpl(), click);
        press(link, SWT.CR);

        assertThat(events).filteredOn(e -> e.startsWith("Selection")).containsExactly("Selection Second", "Selection Second");
    }

    @Test
    @DisplayName("a press of button 1 focuses a Link that holds an anchor, not one without")
    void pressFocusesALinkWithAnAnchor() {
        Link withAnchor = link();
        Link plain = new Link(shell(), SWT.NONE);
        plain.setText("no anchor here");

        assertThat(ControlHelper.takesFocusOnPress((DartControl) withAnchor.getImpl(), 1)).isTrue();
        assertThat(ControlHelper.takesFocusOnPress((DartControl) withAnchor.getImpl(), 3)).isFalse();
        assertThat(ControlHelper.takesFocusOnPress((DartControl) plain.getImpl(), 1)).isFalse();
    }
}
