package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * {@code SWT.SetData} must name the row it is asking about.
 *
 * <p>An application filling a VIRTUAL {@link Table} or {@link Tree} is entitled to read
 * {@code event.index} rather than call {@code indexOf} itself — it is the only row identifier the
 * event carries besides the item, and owner-drawing toolkits key their whole row model off it. An
 * index left at its default therefore does not render one row wrong: every row is filled from row
 * 0's model object, so the whole control repeats the first row's label, image and styling.
 */
@Tag("flutter-it")
class VirtualSetDataIndexFlutterTest {

    private static final String[] LABELS = { "HEAD", "stable", "origin/" };

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
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a VIRTUAL table's SetData names each row, so rows filled from event.index differ")
    void tableSetDataCarriesTheRowIndex() {
        List<Integer> indices = new ArrayList<>();
        Shell shell = new Shell(display);
        Table table = new Table(shell, SWT.VIRTUAL | SWT.BORDER);
        table.addListener(SWT.SetData, e -> {
            indices.add(e.index);
            ((TableItem) e.item).setText(LABELS[e.index]);
        });

        table.setItemCount(LABELS.length);

        assertThat(indices)
                .as("every row must be asked for by its own index")
                .containsExactlyInAnyOrder(0, 1, 2);
        assertThat(texts(table))
                .as("a row filled from event.index carries its own label, not the first row's")
                .containsExactly(LABELS);
    }

    @Test
    @DisplayName("a VIRTUAL tree's SetData names each top-level row, so rows filled from event.index differ")
    void treeSetDataCarriesTheRowIndex() {
        List<Integer> indices = new ArrayList<>();
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.VIRTUAL | SWT.BORDER);
        tree.addListener(SWT.SetData, e -> {
            indices.add(e.index);
            ((TreeItem) e.item).setText(LABELS[e.index]);
        });

        tree.setItemCount(LABELS.length);

        assertThat(indices)
                .as("every top-level row must be asked for by its own index")
                .containsExactlyInAnyOrder(0, 1, 2);
        assertThat(texts(tree))
                .as("a row filled from event.index carries its own label, not the first row's")
                .containsExactly(LABELS);
    }

    @Test
    @DisplayName("a VIRTUAL tree indexes a child row within its parent, not within the tree")
    void treeSetDataIndexesChildrenWithinTheirParent() {
        List<Integer> childIndices = new ArrayList<>();
        Shell shell = new Shell(display);
        Tree tree = new Tree(shell, SWT.VIRTUAL | SWT.BORDER);
        tree.addListener(SWT.SetData, e -> {
            TreeItem item = (TreeItem) e.item;
            if (item.getParentItem() == null) {
                item.setText("root");
                item.setItemCount(LABELS.length);
            } else {
                childIndices.add(e.index);
                item.setText(LABELS[e.index]);
            }
        });

        tree.setItemCount(1);
        tree.getItem(0).setExpanded(true);

        assertThat(childIndices)
                .as("a child row is identified by its position under its parent")
                .containsExactlyInAnyOrder(0, 1, 2);
        assertThat(texts(tree.getItem(0)))
                .as("a child filled from event.index carries its own label, not the first child's")
                .containsExactly(LABELS);
    }

    private static String[] texts(Table table) {
        String[] result = new String[table.getItemCount()];
        for (int i = 0; i < result.length; i++) result[i] = table.getItem(i).getText();
        return result;
    }

    private static String[] texts(Tree tree) {
        String[] result = new String[tree.getItemCount()];
        for (int i = 0; i < result.length; i++) result[i] = tree.getItem(i).getText();
        return result;
    }

    private static String[] texts(TreeItem parent) {
        String[] result = new String[parent.getItemCount()];
        for (int i = 0; i < result.length; i++) result[i] = parent.getItem(i).getText();
        return result;
    }
}
