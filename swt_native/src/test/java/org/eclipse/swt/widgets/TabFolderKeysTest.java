package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** How a TabFolder takes the focus and switches tabs from the pointer and the keys, as GTK and Win32 do. */
class TabFolderKeysTest extends KeyRoutingTestBase {

    private final List<String> log = new ArrayList<>();

    private TabFolder folder() {
        Shell shell = shell();
        TabFolder folder = new TabFolder(shell, SWT.TOP);
        when(shell.getChildren()).thenReturn(new Control[] {folder});
        for (String text : new String[] {"&One", "&Two", "T&hree"}) new TabItem(folder, SWT.NONE).setText(text);
        folder.setSelection(1);
        for (int type : new int[] {SWT.Traverse, SWT.KeyDown, SWT.Selection}) {
            folder.addListener(type, e -> log.add(name(e)));
        }
        return folder;
    }

    private static String name(Event e) {
        if (e.type == SWT.Traverse) return "Traverse " + e.detail + (e.doit ? "" : " doit=false");
        if (e.type == SWT.KeyDown) return "KeyDown " + e.keyCode;
        return "Selection " + ((TabItem) e.item).getText() + " stateMask=" + e.stateMask;
    }

    @Test
    @DisplayName("an arrow is offered as a traversal the folder refuses, then selects the tab beside the selected one")
    void arrowsSelectTheNeighbouringTab() {
        TabFolder folder = folder();

        press(folder, SWT.ARROW_RIGHT);
        press(folder, SWT.ARROW_RIGHT);
        press(folder, SWT.ARROW_LEFT);

        assertThat(log).containsExactly(
                "Traverse " + SWT.TRAVERSE_ARROW_NEXT + " doit=false", "KeyDown " + SWT.ARROW_RIGHT, "Selection T&hree stateMask=0",
                "Traverse " + SWT.TRAVERSE_ARROW_NEXT + " doit=false", "KeyDown " + SWT.ARROW_RIGHT,
                "Traverse " + SWT.TRAVERSE_ARROW_PREVIOUS + " doit=false", "KeyDown " + SWT.ARROW_LEFT, "Selection &Two stateMask=0");
        assertThat(folder.getSelectionIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ctrl+PgDn and Ctrl+PgUp turn the page as a traversal, with no KeyDown, and the Selection carries Ctrl")
    void ctrlPageTurnsThePage() {
        TabFolder folder = folder();

        press(folder, SWT.PAGE_DOWN, SWT.CTRL);
        press(folder, SWT.PAGE_UP, SWT.CTRL);

        assertThat(log).containsExactly(
                "Traverse " + SWT.TRAVERSE_PAGE_NEXT, "Selection T&hree stateMask=" + SWT.CTRL,
                "Traverse " + SWT.TRAVERSE_PAGE_PREVIOUS, "Selection &Two stateMask=" + SWT.CTRL);
    }

    @Test
    @DisplayName("a page traversal from a control on a page turns its folder's page")
    void ctrlPageFromAPageTurnsItsFolder() {
        TabFolder folder = folder();
        Text text = new Text(folder, SWT.BORDER);
        folder.getItem(1).setControl(text);

        press(text, SWT.PAGE_DOWN, SWT.CTRL);

        assertThat(folder.getSelectionIndex()).isEqualTo(2);
        assertThat(log).containsExactly("Selection T&hree stateMask=" + SWT.CTRL);
    }

    @Test
    @DisplayName("a tab clicked on the render side is selected and announced with the button held")
    void aTabClickCarriesTheButton() {
        TabFolder folder = folder();

        TabFolderHelper.select((DartTabFolder) folder.getImpl(), 0, SWT.BUTTON1);
        TabFolderHelper.select((DartTabFolder) folder.getImpl(), 0, SWT.BUTTON1);

        assertThat(log).containsExactly("Selection &One stateMask=" + SWT.BUTTON1);
    }

    @Test
    @DisplayName("Alt and a tab's mnemonic select it with no KeyDown, and the Selection carries Alt")
    void mnemonicSelectsTheTabWithAlt() {
        TabFolder folder = folder();

        MnemonicActivationTest.pressAlt(folder, 'h');

        assertThat(folder.getSelectionIndex()).isEqualTo(2);
        assertThat(log).containsExactly("Traverse " + SWT.TRAVERSE_MNEMONIC, "Selection T&hree stateMask=" + SWT.ALT);
    }

    @Test
    @DisplayName("a primary press on a tab focuses the folder ahead of its MouseDown; one on the strip past the tabs does not")
    void aPressOnATabFocusesTheFolder() {
        TabFolder folder = folder();
        folder.setSize(400, 30);
        org.eclipse.swt.graphics.Rectangle second = folder.getItem(1).getBounds();
        DartControl impl = (DartControl) folder.getImpl();

        assertThat(ControlHelper.takesFocusOnPress(impl, press(1, second.x + second.width / 2, second.y + second.height / 2))).isTrue();
        assertThat(ControlHelper.takesFocusOnPress(impl, press(1, 390, second.y + second.height / 2))).isFalse();
        assertThat(ControlHelper.takesFocusOnPress(impl, press(3, second.x + second.width / 2, second.y + second.height / 2))).isFalse();
    }

    private static Event press(int button, int x, int y) {
        Event e = new Event();
        e.button = button;
        e.x = x;
        e.y = y;
        return e;
    }

    @Test
    @DisplayName("only a plain left or right arrow moves the selection, and not past the first or the last tab")
    void otherKeysAndTheEndsMoveNothing() {
        TabFolder folder = folder();
        folder.setSelection(0);

        press(folder, SWT.ARROW_LEFT);
        press(folder, SWT.ARROW_RIGHT, SWT.SHIFT);
        press(folder, 'a');

        assertThat(folder.getSelectionIndex()).isZero();
        assertThat(log).noneMatch(entry -> entry.startsWith("Selection"));
    }

    @Test
    @DisplayName("a page traversal wraps around, and one with no folder to turn, or an empty one, is delivered as a KeyDown")
    void ctrlPageWrapsAndFallsThrough() {
        TabFolder folder = folder();
        folder.setSelection(0);
        press(folder, SWT.PAGE_UP, SWT.CTRL);
        assertThat(folder.getSelectionIndex()).isEqualTo(2);

        List<String> keys = new ArrayList<>();
        Text text = new Text(folder.getShell(), SWT.BORDER);
        text.addListener(SWT.KeyDown, e -> keys.add("text"));
        press(text, SWT.PAGE_DOWN, SWT.CTRL);
        TabFolder empty = new TabFolder(folder.getShell(), SWT.TOP);
        empty.addListener(SWT.KeyDown, e -> keys.add("empty"));
        press(empty, SWT.PAGE_DOWN, SWT.CTRL);

        assertThat(keys).containsExactly("text", "empty");
    }

    @Test
    @DisplayName("a press focuses only a TabFolder that takes the focus, and no other control through this rule")
    void aPressFocusesOnlyAFocusableFolder() {
        Shell shell = shell();
        TabFolder noFocus = new TabFolder(shell, SWT.TOP | SWT.NO_FOCUS);
        new TabItem(noFocus, SWT.NONE).setText("One");
        noFocus.setSize(400, 30);
        org.eclipse.swt.graphics.Rectangle tab = noFocus.getItem(0).getBounds();

        assertThat(TabFolderHelper.pressFocuses((DartControl) noFocus.getImpl(),
                press(1, tab.x + tab.width / 2, tab.y + tab.height / 2))).isFalse();
        assertThat(TabFolderHelper.pressFocuses((DartControl) new Label(shell, SWT.NONE).getImpl(), press(1, 1, 1))).isFalse();
    }
}
