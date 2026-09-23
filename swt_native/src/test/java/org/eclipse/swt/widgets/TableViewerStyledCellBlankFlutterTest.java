package org.eclipse.swt.widgets;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("flutter-it")
class TableViewerStyledCellBlankFlutterTest {

    private Display display;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    private static final class TestCaseRow {
        final String id;
        final String description;

        TestCaseRow(String id, String description) {
            this.id = id;
            this.description = description;
        }
    }

    private static final class StyledColumn extends StyledCellLabelProvider {
        private final Function<TestCaseRow, String> text;

        StyledColumn(Function<TestCaseRow, String> text) {
            this.text = text;
        }

        @Override
        public void update(ViewerCell cell) {
            cell.setText(text.apply((TestCaseRow) cell.getElement()));
            super.update(cell);
        }

        @Override
        public String getToolTipText(Object element) {
            return text.apply((TestCaseRow) element);
        }
    }

    @Test
    void styledCellLabelProviderTable_paintsItsCellText_notJustTheTooltip() {
        Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());

        TableViewer viewer = new TableViewer(shell,
                SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
        viewer.getTable().setHeaderVisible(true);

        StyledColumn idProvider = new StyledColumn(r -> r.id);
        StyledColumn descProvider = new StyledColumn(r -> r.description);

        TableViewerColumn idColumn = new TableViewerColumn(viewer, SWT.LEFT);
        idColumn.getColumn().setText("Test Case ID");
        idColumn.getColumn().setWidth(200);
        idColumn.setLabelProvider(idProvider);

        TableViewerColumn descColumn = new TableViewerColumn(viewer, SWT.LEFT);
        descColumn.getColumn().setText("Description");
        descColumn.getColumn().setWidth(200);
        descColumn.setLabelProvider(descProvider);

        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.getTable().setToolTipText("");
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        List<TestCaseRow> input = Arrays.asList(
                new TestCaseRow("TC-1", "Login test"),
                new TestCaseRow("TC-2", "Checkout test"));
        viewer.setInput(input);

        Table table = viewer.getTable();
        DartTable dartTable = (DartTable) table.getImpl();
        for (int i = 0; i < table.getItemCount(); i++) {
            dartTable.checkData(dartTable._getItem(i), i);
        }

        assertThat(idProvider.getToolTipText(input.get(0)))
                .as("the data is present: the tooltip shows it")
                .isEqualTo("TC-1");

        String[] row0 = ((DartTableItem) table.getItem(0).getImpl()).getTexts();
        String[] row1 = ((DartTableItem) table.getItem(1).getImpl()).getTexts();

        assertThat(row0)
                .as("row 0 cells must render their owner-drawn text, not blank")
                .containsExactly("TC-1", "Login test");
        assertThat(row1)
                .as("row 1 cells must render their owner-drawn text, not blank")
                .containsExactly("TC-2", "Checkout test");
    }
}
