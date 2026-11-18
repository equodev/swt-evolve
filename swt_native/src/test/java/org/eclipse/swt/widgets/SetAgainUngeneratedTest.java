package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.custom.TreeCursor;
import org.eclipse.swt.custom.ViewForm;
import org.junit.jupiter.api.Test;

import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * The set-again sweep of the generated serialize tests, for the widgets that have none: a setter given
 * the value its getter returns marks nothing, and every wire property is changed by some setter or
 * listed with what changes it instead.
 */
class SetAgainUngeneratedTest extends SerializeTestBase {

    @Test
    void should_name_nothing_set_again_Shell() {
        assertSetAgainNamesNothing(style -> new Shell(shell(), style), this::setAll, unwritable(),
                SWT.NONE, SWT.SHELL_TRIM, SWT.DIALOG_TRIM, SWT.ON_TOP, SWT.TOOL, SWT.NO_TRIM, SWT.RESIZE,
                SWT.APPLICATION_MODAL, SWT.SHEET);
    }

    @Test
    void should_name_nothing_set_again_ScrollBar() {
        assertSetAgainNamesNothing(style -> new Canvas(shell(), SWT.H_SCROLL | SWT.V_SCROLL).getVerticalBar(),
                this::setAll, unwritable(), SWT.NONE);
    }

    @Test
    void should_name_nothing_set_again_Tracker() {
        assertSetAgainNamesNothing(style -> new Tracker(shell(), style), this::setAll, unwritable(),
                SWT.NONE, SWT.RESIZE, SWT.UP, SWT.DOWN, SWT.LEFT, SWT.RIGHT);
    }

    @Test
    void should_name_nothing_set_again_IME() {
        assertSetAgainNamesNothing(style -> new IME(new Canvas(shell(), SWT.NONE), style), this::setAll,
                unwritable(), SWT.NONE);
    }

    @Test
    void should_name_nothing_set_again_CBanner() {
        assertSetAgainNamesNothing(style -> new CBanner(shell(), style), this::setAll, unwritable(), SWT.NONE, SWT.BORDER);
    }

    @Test
    void should_name_nothing_set_again_ViewForm() {
        assertSetAgainNamesNothing(style -> new ViewForm(shell(), style), this::setAll, unwritable(),
                SWT.NONE, SWT.BORDER, SWT.FLAT);
    }

    @Test
    void should_name_nothing_set_again_TableCursor() {
        assertSetAgainNamesNothing(style -> new TableCursor(new Table(shell(), SWT.NONE), style), this::setAll,
                unwritable(), SWT.NONE, SWT.BORDER);
    }

    @Test
    void should_name_nothing_set_again_TreeCursor() {
        assertSetAgainNamesNothing(style -> new TreeCursor(new Tree(shell(), SWT.NONE), style), this::setAll,
                unwritable(), SWT.NONE, SWT.BORDER);
    }

    @Test
    void should_name_nothing_set_again_Tray() {
        assertSetAgainNamesNothing(style -> new Tray(shell().getDisplay(), style), this::setAll, unwritable(), SWT.NONE);
    }

    @Test
    void should_name_nothing_set_again_TrayItem() {
        assertSetAgainNamesNothing(style -> new TrayItem(new Tray(shell().getDisplay(), SWT.NONE), style), this::setAll,
                unwritable(), SWT.NONE);
    }

    @Test
    void should_name_nothing_set_again_TaskBar() {
        assertSetAgainNamesNothing(style -> new TaskBar(shell().getDisplay(), style), this::setAll, unwritable(), SWT.NONE);
    }

    @Test
    void should_name_nothing_set_again_TaskItem() {
        assertSetAgainNamesNothing(style -> new TaskItem(new TaskBar(shell().getDisplay(), SWT.NONE), style),
                this::setAll, unwritable(), SWT.NONE);
    }
}
