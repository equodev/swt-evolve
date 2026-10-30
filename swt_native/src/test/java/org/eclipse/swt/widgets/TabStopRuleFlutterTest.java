package org.eclipse.swt.widgets;

import dev.equo.swt.harness.UserInput;
import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tab between two Texts across a Canvas or Composite. Natively a Canvas or Composite takes the focus
 * from Tab only while it hooks KeyDown or KeyUp, children or not, and otherwise passes it on to its
 * first child or to the next control (Composite.setTabGroupFocus, the same on Win32, GTK and Cocoa).
 * A CTabFolder hooks KeyDown itself, so it is one stop, and focusing it selects its first tab. A
 * ToolBar is one stop however many items it has (GTK's bar is one tab group, Cocoa focuses its first
 * item; Win32 alone makes one that is not FLAT NO_FOCUS).
 */
@Tag("flutter-it")
class TabStopRuleFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;
    private Text before;

    @BeforeEach
    void open() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setLayout(new RowLayout(SWT.VERTICAL));
        before = new Text(shell, SWT.BORDER);
    }

    @AfterEach
    void shutdown() {
        flutter.teardown();
    }

    @Test
    void tabPassesOverACanvasThatHooksNoKeys() {
        new Canvas(shell, SWT.NONE).setLayoutData(new RowData(80, 40));
        Text after = new Text(shell, SWT.BORDER);

        tabFromBefore();

        assertThat(display.getFocusControl()).isSameAs(after);
    }

    @Test
    void tabPassesOverAChildlessCompositeThatHooksNoKeys() {
        new Composite(shell, SWT.NONE).setLayoutData(new RowData(80, 40));
        Text after = new Text(shell, SWT.BORDER);

        tabFromBefore();

        assertThat(display.getFocusControl()).isSameAs(after);
    }

    @Test
    void tabFocusesACanvasThatHooksKeysBeforeItsChildren() {
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setLayout(new RowLayout());
        canvas.addListener(SWT.KeyDown, e -> { });
        new Text(canvas, SWT.BORDER);
        new Text(shell, SWT.BORDER);

        tabFromBefore();

        assertThat(display.getFocusControl()).isSameAs(canvas);
    }

    @Test
    void tabFocusesAChildlessCompositeThatHooksKeys() {
        Composite composite = new Composite(shell, SWT.NONE);
        composite.setLayoutData(new RowData(80, 40));
        composite.addListener(SWT.KeyDown, e -> { });
        new Text(shell, SWT.BORDER);

        tabFromBefore();

        assertThat(display.getFocusControl()).isSameAs(composite);
    }

    @Test
    void tabFocusesACTabFolderSelectsItsFirstTabAndLeavesItWithTheNextTab() {
        org.eclipse.swt.custom.CTabFolder folder = new org.eclipse.swt.custom.CTabFolder(shell, SWT.BORDER);
        folder.setLayoutData(new RowData(200, 80));
        for (String name : new String[] {"One", "Two", "Three"})
            new org.eclipse.swt.custom.CTabItem(folder, SWT.NONE).setText(name);
        Text after = new Text(shell, SWT.BORDER);

        tabFromBefore();

        assertThat(display.getFocusControl()).isSameAs(folder);
        assertThat(folder.getSelectionIndex()).isZero();

        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();
        assertThat(display.getFocusControl()).isSameAs(after);
    }

    @Test
    void aFlatToolBarIsOneTabStopHoweverManyItemsItHas() {
        ToolBar bar = new ToolBar(shell, SWT.FLAT);
        for (String name : new String[] {"One", "Two", "Three"})
            new ToolItem(bar, SWT.PUSH).setText(name);
        Text after = new Text(shell, SWT.BORDER);

        tabFromBefore();
        assertThat(display.getFocusControl()).isSameAs(bar);

        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();
        assertThat(display.getFocusControl()).isSameAs(after);
    }

    @Test
    void aToolBarThatIsNotFlatIsOneTabStopToo() {
        ToolBar bar = new ToolBar(shell, SWT.BORDER);
        new ToolItem(bar, SWT.PUSH).setText("One");
        new ToolItem(bar, SWT.PUSH).setText("Two");
        Text after = new Text(shell, SWT.BORDER);

        tabFromBefore();
        assertThat(display.getFocusControl()).isSameAs(bar);

        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();
        assertThat(display.getFocusControl()).isSameAs(after);
    }

    @Test
    void tabPassesOverAToolBarWithNoItems() {
        new ToolBar(shell, SWT.FLAT).setLayoutData(new RowData(80, 30));
        Text after = new Text(shell, SWT.BORDER);

        tabFromBefore();

        assertThat(display.getFocusControl()).isSameAs(after);
    }

    private void tabFromBefore() {
        shell.setSize(300, 300);
        shell.open();
        flutter.settle();
        double[] rect = flutter.renderedRect(before);
        flutter.input().click(rect[0] + rect[2] / 2, rect[1] + rect[3] / 2);
        flutter.settle();
        assertThat(display.getFocusControl()).isSameAs(before);

        flutter.input().press(UserInput.Key.TAB);
        flutter.settle();
    }
}
