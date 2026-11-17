package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.RecordingDisplayBridge.drainEventLoop;

/**
 * A shell taking the focus back gives it only to a control that could take it natively. Natively the
 * restore goes through {@code setFocus}, which refuses a control that is hidden, as one in an
 * unselected tab is, or disabled.
 */
@Tag("native-unit")
class HiddenFocusRestoreNativeTest {

    private Display display;

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("closing a child shell gives the focus to the selected tab, not to the one the user left")
    void closingAChildShellSkipsAControlInAnUnselectedTab() {
        install();
        Shell main = new Shell(display, SWT.SHELL_TRIM);
        main.setBounds(0, 0, 400, 300);
        CTabFolder folder = new CTabFolder(main, SWT.NONE);
        folder.setBounds(0, 0, 400, 300);
        CTabItem first = new CTabItem(folder, SWT.NONE);
        Text inFirst = new Text(folder, SWT.BORDER);
        first.setControl(inFirst);
        CTabItem second = new CTabItem(folder, SWT.NONE);
        Text inSecond = new Text(folder, SWT.BORDER);
        second.setControl(inSecond);
        folder.setSelection(first);
        main.open();
        inFirst.setFocus();
        drainEventLoop(display);
        assertThat(display.getFocusControl()).as("the first tab's field took the focus").isSameAs(inFirst);

        // An application's floating panel over the first tab's view: opening it deactivates the main
        // shell, which saves the field as the focus to give back.
        Shell child = openChild(main);

        folder.setSelection(second);
        drainEventLoop(display);
        assertThat(inFirst.isVisible()).as("the first tab's field is hidden").isFalse();

        // The view is hidden, so the application closes its panel, which hands the activation back.
        child.dispose();
        drainEventLoop(display);

        assertThat(display.getFocusControl()).as("the focus goes to the selected tab's field").isSameAs(inSecond);
        assertThat(folder.getSelection()).as("the folder keeps the tab the user selected").isSameAs(second);
    }

    @Test
    @DisplayName("closing a child shell does not give the focus back to a control disabled meanwhile")
    void closingAChildShellSkipsAControlDisabledMeanwhile() {
        install();
        Shell main = new Shell(display, SWT.SHELL_TRIM);
        main.setBounds(0, 0, 400, 300);
        main.setLayout(new FillLayout(SWT.VERTICAL));
        Text saved = new Text(main, SWT.BORDER);
        Text other = new Text(main, SWT.BORDER);
        main.open();
        saved.setFocus();
        drainEventLoop(display);
        assertThat(display.getFocusControl()).as("the first field took the focus").isSameAs(saved);

        Shell child = openChild(main);
        saved.setEnabled(false);

        child.dispose();
        drainEventLoop(display);

        assertThat(display.getFocusControl()).as("the focus goes to the next field that can take it").isSameAs(other);
    }

    private Shell openChild(Shell parent) {
        Shell child = new Shell(parent, SWT.NONE);
        child.setBounds(10, 10, 100, 100);
        new Canvas(child, SWT.NONE).setBounds(0, 0, 100, 100);
        child.open();
        drainEventLoop(display);
        return child;
    }

    private void install() {
        RecordingDisplayBridge.install(RecordingDisplayBridge::new);
        display = Display.getCurrent();
    }
}
