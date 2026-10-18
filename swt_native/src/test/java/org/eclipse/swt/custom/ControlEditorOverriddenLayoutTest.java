package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A ControlEditor subclass that places its editor over one cell overrides {@code layout()}. Upstream,
 * {@code setEditor}, the parent's mouse/key/resize listeners and TableEditor's deferred re-layout after
 * a column resize all call {@code layout()} virtually, so the override is what positions the editor.
 */
class ControlEditorOverriddenLayoutTest {

    private static final Rectangle CELL = new Rectangle(40, 20, 80, 18);

    private Display display;
    private Canvas grid;

    @BeforeAll static void useEquo() { Config.forceEquo(); }
    @AfterAll static void reset() { Config.defaultToEclipse(); }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
        Shell shell = new Shell(display);
        grid = new Canvas(shell, SWT.NONE);
        grid.setBounds(0, 0, 400, 300);
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("setEditor places the editor with the subclass's layout()")
    void setEditorUsesOverriddenLayout() {
        CellEditor editor = new CellEditor(grid);
        Text text = new Text(grid, SWT.NONE);

        editor.setEditor(text);

        assertThat(text.getBounds()).isEqualTo(CELL);
    }

    @Test
    @DisplayName("a mouse-up on the parent keeps the editor on the cell")
    void parentEventUsesOverriddenLayout() {
        CellEditor editor = new CellEditor(grid);
        Text text = new Text(grid, SWT.NONE);
        editor.setEditor(text);

        Event up = new Event();
        up.button = 1;
        grid.notifyListeners(SWT.MouseUp, up);

        assertThat(text.getBounds()).isEqualTo(CELL);
    }

    @Test
    @DisplayName("the deferred re-layout after a column resize keeps a TableEditor subclass's placement")
    void columnResizeTimerUsesOverriddenLayout() throws InterruptedException {
        Table table = new Table(grid.getShell(), SWT.NONE);
        table.setBounds(0, 0, 400, 300);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(100);
        TableItem item = new TableItem(table, SWT.NONE);
        TableEditor editor = new TableEditor(table) {
            @Override
            public void layout() {
                if (getEditor() != null && !getEditor().isDisposed())
                    getEditor().setBounds(CELL);
            }
        };
        Text text = new Text(table, SWT.NONE);
        editor.setEditor(text, item, 0);

        column.setWidth(150);
        long until = System.currentTimeMillis() + DartTableEditor.TIMEOUT + 500;
        while (System.currentTimeMillis() < until) {
            if (!display.readAndDispatch()) Thread.sleep(20);
        }

        assertThat(text.getBounds()).isEqualTo(CELL);
    }

    private static class CellEditor extends ControlEditor {
        CellEditor(Canvas grid) {
            super(grid);
            grabHorizontal = true;
            grabVertical = true;
        }

        @Override
        public void layout() {
            if (getEditor() != null && !getEditor().isDisposed())
                getEditor().setBounds(CELL);
        }
    }
}
