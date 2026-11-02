package dev.equo.swt.delivery;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireFrames;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartTable;
import org.eclipse.swt.widgets.DartTree;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.VTable;
import org.eclipse.swt.widgets.VTree;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Columns created or disposed after their Tree or Table was first delivered.
 *
 * <p>A JFace viewer that reconfigures its columns at runtime disposes them all and creates new
 * ones on a widget the client already holds. The column list travels inside the parent's value,
 * so unless the parent reports it changed, the client keeps the list it had: no columns at all,
 * while every row carries a text per column and owner-drawn cells are placed by columns the
 * client never laid out.
 */
@ExtendWith(Mocks.class)
class ColumnsAfterDeliveryTest {

    private static final int CLIENT = 1;

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
        Serializer.enterConnection(CLIENT);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a TreeColumn created after the Tree was delivered is sent with the Tree")
    void treeColumnAddedAfterDelivery() {
        Shell shell = Mocks.shell();
        Tree tree = new Tree(shell, SWT.NONE);
        DartTree impl = (DartTree) tree.getImpl();
        WireFrames.record(impl, CLIENT);

        TreeColumn name = new TreeColumn(tree, SWT.NONE);
        TreeColumn owner = new TreeColumn(tree, SWT.NONE);

        assertThat(impl.getValue().changedKeys())
                .as("the column list changed, so the Tree has to say so")
                .contains(VTree.COLUMNS);
        assertThat(names(WireFrames.frame(impl, CLIENT), name))
                .as("and the update carries both new columns")
                .isTrue();
        assertThat(names(WireFrames.frame(impl, CLIENT), owner)).isTrue();
    }

    @Test
    @DisplayName("a TreeColumn disposed after the Tree was delivered leaves the Tree's list")
    void treeColumnDisposedAfterDelivery() {
        Shell shell = Mocks.shell();
        Tree tree = new Tree(shell, SWT.NONE);
        DartTree impl = (DartTree) tree.getImpl();
        TreeColumn gone = new TreeColumn(tree, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        WireFrames.record(impl, CLIENT);

        gone.dispose();

        assertThat(impl.getValue().changedKeys()).contains(VTree.COLUMNS);
        assertThat(names(WireFrames.frame(impl, CLIENT), gone)).isFalse();
    }

    @Test
    @DisplayName("a TableColumn created after the Table was delivered is sent with the Table")
    void tableColumnAddedAfterDelivery() {
        Shell shell = Mocks.shell();
        Table table = new Table(shell, SWT.NONE);
        DartTable impl = (DartTable) table.getImpl();
        WireFrames.record(impl, CLIENT);

        TableColumn column = new TableColumn(table, SWT.NONE);

        assertThat(impl.getValue().changedKeys()).contains(VTable.COLUMNS);
        assertThat(names(WireFrames.frame(impl, CLIENT), column)).isTrue();
    }

    @Test
    @DisplayName("a TableColumn disposed after the Table was delivered leaves the Table's list")
    void tableColumnDisposedAfterDelivery() {
        Shell shell = Mocks.shell();
        Table table = new Table(shell, SWT.NONE);
        DartTable impl = (DartTable) table.getImpl();
        TableColumn gone = new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        WireFrames.record(impl, CLIENT);

        gone.dispose();

        assertThat(impl.getValue().changedKeys()).contains(VTable.COLUMNS);
        assertThat(names(WireFrames.frame(impl, CLIENT), gone)).isFalse();
    }

    /** Whether some record of {@code frame} names {@code widget}. */
    private static boolean names(java.util.List<WireFrames.Record> frame, org.eclipse.swt.widgets.Widget widget) {
        return frame.stream().anyMatch(r -> !dev.equo.swt.harness.WireValues.describing(r.state(), widget.hashCode()).isEmpty());
    }
}
