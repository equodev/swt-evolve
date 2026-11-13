package org.eclipse.swt.widgets;

import dev.equo.swt.harness.UserInput;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tab from the last control of a shell wraps to the first, and Shift+Tab from the first wraps to the
 * last, through {@link Control#traverse(int)} and a real key alike. Natively a shell with no key
 * listener declines the tab-group focus, so the walk goes round it.
 */
@Tag("flutter-it")
class TabTraversalWrapsFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Text first;
    private Text last;

    @BeforeEach
    void open() {
        display = flutter.boot();
        Shell shell = new Shell(display);
        shell.setLayout(new RowLayout(SWT.VERTICAL));
        first = new Text(shell, SWT.BORDER);
        new Text(shell, SWT.BORDER);
        last = new Text(shell, SWT.BORDER);
        shell.setSize(300, 200);
        shell.open();
        flutter.settle();
    }

    @AfterEach
    void shutdown() {
        flutter.teardown();
    }

    @Test
    void traverseTabPreviousFromTheFirstControlWrapsToTheLast() {
        focus(first);

        assertThat(first.traverse(SWT.TRAVERSE_TAB_PREVIOUS)).isTrue();

        assertThat(display.getFocusControl()).isSameAs(last);
    }

    @Test
    void traverseTabNextFromTheLastControlWrapsToTheFirst() {
        focus(last);

        assertThat(last.traverse(SWT.TRAVERSE_TAB_NEXT)).isTrue();

        assertThat(display.getFocusControl()).isSameAs(first);
    }

    @Test
    void traverseTabPreviousFromTheFirstControlStopsAtAShellThatListensForKeys() {
        Shell shell = first.getShell();
        shell.addListener(SWT.KeyDown, e -> { });
        focus(first);

        assertThat(first.traverse(SWT.TRAVERSE_TAB_PREVIOUS)).isTrue();

        assertThat(display.getFocusControl()).isSameAs(shell);
    }

    @Test
    void shiftTabFromTheFirstControlWrapsToTheLast() {
        focus(first);

        flutter.input().press(UserInput.Key.TAB, SWT.SHIFT);
        flutter.settle();

        assertThat(display.getFocusControl()).isSameAs(last);
    }

    @Test
    void tabFromTheLastControlWrapsToTheFirst() {
        focus(last);

        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();

        assertThat(display.getFocusControl()).isSameAs(first);
    }

    private void focus(Text text) {
        double[] rect = flutter.renderedRect(text);
        flutter.input().click(rect[0] + rect[2] / 2, rect[1] + rect[3] / 2);
        flutter.settle();
        assertThat(display.getFocusControl()).isSameAs(text);
    }
}
