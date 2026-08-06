package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.RecordingComm;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Column widths of a JFace {@link TableViewer} whose {@link Table} is laid out by a
 * {@link TableLayout} with weighted columns — the shape every Eclipse preference page with a table
 * uses, and the one that leaves a table unreadable when the widths are wrong.
 */
@Tag("flutter-it")
class TableColumnWidthFlutterTest {

    private Display display;
    private RecordingBridge bridge;

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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    private static final class Row {
        final String host, type, fingerprint;

        Row(String host, String type, String fingerprint) {
            this.host = host;
            this.type = type;
            this.fingerprint = fingerprint;
        }
    }

    private static final class RowLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int columnIndex) {
            Row r = (Row) element;
            switch (columnIndex) {
                case 0:
                    return r.host;
                case 1:
                    return r.type;
                case 2:
                    return r.fingerprint;
            }
            return null;
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    /**
     * A three-column TableViewer sized by a TableLayout with weights 30/20/70 — no column ever gets
     * an explicit setWidth, they are distributed from the Table's client area.
     */
    private Table buildWeightedColumnTable(Composite parent) {
        Composite group = new Composite(parent, SWT.NULL);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_BOTH));

        Label label = new Label(group, SWT.NONE);
        label.setText("Keys of known hosts:");
        GridData labelData = new GridData();
        labelData.horizontalSpan = 2;
        label.setLayoutData(labelData);

        TableViewer viewer = new TableViewer(group,
                SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
        Table table = viewer.getTable();
        new TableEditor(table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        GridData tableData = new GridData(GridData.FILL_BOTH);
        tableData.widthHint = 500;
        tableData.heightHint = 300;
        table.setLayoutData(tableData);

        new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        TableColumn[] columns = table.getColumns();
        columns[0].setText("Hostname");
        columns[1].setText("Type");
        columns[2].setText("Fingerprint");

        viewer.setLabelProvider(new RowLabelProvider());
        viewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                return inputElement == null ? null : (Object[]) inputElement;
            }

            @Override
            public void inputChanged(Viewer v, Object oldInput, Object newInput) {
            }

            @Override
            public void dispose() {
            }
        });

        TableLayout tableLayout = new TableLayout();
        tableLayout.addColumnData(new ColumnWeightData(30));
        tableLayout.addColumnData(new ColumnWeightData(20));
        tableLayout.addColumnData(new ColumnWeightData(70));
        table.setLayout(tableLayout);

        Composite buttons = new Composite(group, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        buttons.setLayout(new GridLayout());
        Button remove = new Button(buttons, SWT.PUSH);
        remove.setText("Remove");
        remove.setEnabled(false);

        viewer.setInput(new Object[] {
                new Row("github.com", "ssh-rsa", "d5:2c:63:d9"),
                new Row("github.com", "ecdsa-sha2-nistp256", "7b:99:81:1e") });
        return table;
    }

    /**
     * A recursive {@code layout(true, true)} reaches composites that have no size yet — a
     * CTabFolder page that has never been selected sits at 0x0. Laying such a composite out hands
     * its children junk sizes (the Table ends up a handful of pixels wide), and a Layout that only
     * ever runs once is then poisoned for good: JFace's TableLayout distributes the column widths
     * on its first pass with {@code clientArea.width > 1}, clamps every ColumnWeightData to its
     * 20px minimum, and never recomputes — leaving every cell too narrow to show its text once the
     * page is finally displayed at its real size.
     */
    @Test
    void weightedColumnsSurviveALayoutPassOnAZeroSizedParent() {
        Shell shell = new Shell(display);
        shell.setLayout(null);
        shell.setSize(800, 600);

        Composite neverSelectedTabPage = new Composite(shell, SWT.NONE);
        neverSelectedTabPage.setLayout(new FillLayout());
        neverSelectedTabPage.setBounds(0, 0, 0, 0);

        Table table = buildWeightedColumnTable(neverSelectedTabPage);
        shell.open();
        shell.layout(true, true);
        FlutterBridge.update().join();

        // The tab is selected now: the page finally gets a real size.
        neverSelectedTabPage.setBounds(0, 0, 700, 450);
        FlutterBridge.update().join();

        assertThat(table.getColumn(2).getWidth())
                .as("'Fingerprint' carries weight 70 of 120 — it must get the lion's share of the "
                        + "real width, not ColumnWeightData's 20px minimum")
                .isGreaterThan(200);
    }

    /**
     * Columns have no widget of their own on the Dart side — the Table renders them inline from its
     * own {@code columns} list, and nothing listens on a {@code TableColumn/<id>} channel. So a
     * column-only change (the widths TableLayout distributes, or a header re-label) has to re-send
     * the parent Table, or it never reaches the renderer.
     */
    @Test
    void columnOnlyMutationIsDeliveredOnTheTableChannel() {
        Shell shell = new Shell(display);
        shell.setLayout(new GridLayout());
        shell.setSize(800, 600);
        Table table = buildWeightedColumnTable(shell);
        shell.open();
        shell.layout(true, true);
        FlutterBridge.update().join();

        int before = bridge.comm.sent.size();
        table.getColumn(0).setText("Renamed");
        table.getColumn(0).setWidth(321);
        FlutterBridge.update().join();

        List<String> channels = new ArrayList<>();
        int i = 0;
        for (RecordingComm.Frame frame : bridge.comm.sent) {
            if (i++ >= before) channels.add(frame.event);
        }
        assertThat(channels)
                .as("the Table itself must be re-sent, not only the TableColumn: %s", channels)
                .anyMatch(c -> c.startsWith("Table/") || c.startsWith("Shell/") || c.startsWith("Composite/"));
    }
}
