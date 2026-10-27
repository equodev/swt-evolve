package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.size.TableSizes;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Font;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.swt.widgets.Mocks.shell;

/** What {@link DartTable} keeps for the render side and answers when it is driven directly rather than through {@link Table}. */
@Tag("native-unit")
class TableImplementationNativeTest extends SerializeTestBase {

    private static DartTable impl(Table table) {
        return (DartTable) table.getImpl();
    }

    private static boolean named(Table table, String key) {
        return impl(table).getValue().changedKeys().contains(key);
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartTable impl = new DartTable(shell(), SWT.MULTI, null);

        Table api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("the API's subclass check is the implementation's, which accepts SWT's own package")
    void theSubclassCheckIsTheImplementations() {
        DartTable impl = new DartTable(shell(), SWT.NONE, null);
        Table subclass = new Table(impl) {
        };
        impl.setApi(subclass);

        assertThatCode(subclass::checkSubclass).doesNotThrowAnyException();
        assertThatCode(impl::checkSubclass).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("every accessor of the implementation answers its field")
    void accessorsAnswerTheirFields() throws Exception {
        Table table = new Table(shell(), SWT.NONE);
        new TableColumn(table, SWT.NONE);
        DartTable impl = impl(table);
        int called = 0;
        for (Method method : DartTable.class.getDeclaredMethods()) {
            if (method.getName().startsWith("_") && method.getParameterCount() == 0
                    && !Modifier.isStatic(method.getModifiers()) && !method.isSynthetic()
                    && !method.getName().startsWith("_hook")) {
                method.setAccessible(true);
                method.invoke(impl);
                called++;
            }
        }
        assertThat(called).isGreaterThan(15);
        assertThat(impl._columnCount()).isEqualTo(1);
        assertThat(impl._columns()[0]).isSameAs(table.getColumn(0));
    }

    @Test
    @DisplayName("the row height Cocoa lets SWT set internally takes -1 and up")
    void setItemHeightChecksItsArgument() {
        DartTable impl = impl(new Table(shell(), SWT.NONE));
        int height = impl.getItemHeight();

        impl.setItemHeight(-1);
        impl.setItemHeight(30);

        assertThat(impl.getItemHeight()).isEqualTo(height);
        assertThatThrownBy(() -> impl.setItemHeight(-2)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("what Cocoa asks of its table view that Flutter has no use for answers its neutral value")
    void cocoaHooksAnswerNeutrally() {
        Table table = new Table(shell(), SWT.CHECK);
        DartTable impl = impl(table);

        assertThat(impl.isTransparent()).isTrue();
        assertThat(impl.dragDetect(0, 0, false, null)).isFalse();
        assertThat(impl.getCheckColumnWidth()).isZero();
        assertThatCode(() -> impl.reskinChildren(0)).doesNotThrowAnyException();
        new TableColumn(table, SWT.NONE);
        new TableItem(table, SWT.NONE);
        assertThatCode(() -> impl.reskinChildren(0)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("an editor is named for the client when it is added, disposed or loses its control")
    void editorsAreNamed() {
        Table table = new Table(shell(), SWT.NONE);
        new TableItem(table, SWT.NONE);
        TableEditor editor = new TableEditor(table);
        Text text = new Text(table, SWT.NONE);
        editor.setEditor(text, table.getItem(0), 0);
        assertThat(impl(table)._editors()).contains(editor);
        impl(table).getValue().clearDirty();
        impl(table)._addEditor(editor);
        assertThat(named(table, VTable.EDITORS)).isFalse();

        text.dispose();
        assertThat(named(table, VTable.EDITORS)).isTrue();

        impl(table).getValue().clearDirty();
        editor.dispose();
        assertThat(impl(table)._editors()).doesNotContain(editor);
        assertThat(named(table, VTable.EDITORS)).isTrue();

        impl(table).getValue().clearDirty();
        impl(table)._removeEditor(editor);
        assertThat(named(table, VTable.EDITORS)).isFalse();
    }

    @Test
    @DisplayName("the selection and the column order are named for the client when they change, and not when they stay")
    void selectionAndOrderAreNamed() {
        Table table = new Table(shell(), SWT.MULTI);
        new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        new TableItem(table, SWT.NONE);
        new TableItem(table, SWT.NONE);

        impl(table).getValue().clearDirty();
        table.deselectAll();
        assertThat(named(table, VTable.SELECTION)).isFalse();

        table.select(1);
        assertThat(named(table, VTable.SELECTION)).isTrue();

        impl(table).getValue().clearDirty();
        table.setColumnOrder(new int[] {1, 0});
        assertThat(named(table, VTable.COLUMN_ORDER)).isTrue();

        impl(table).getValue().clearDirty();
        table.deselectAll();
        assertThat(named(table, VTable.SELECTION)).isTrue();
    }

    private static final String LONG = "a row much wider than the others";

    private static int width(TableItem item) {
        return TableHelper.contentWidth(item);
    }

    @Test
    @DisplayName("a columnless row changed while another row is asked for its data is measured once that request returns")
    void aRowChangedInsideAnotherRowsSetDataIsMeasured() {
        try (Mocks.Delivery delivery = Mocks.deliverEvents(shell().getDisplay())) {
            Table table = new Table(shell(), SWT.VIRTUAL);
            TableItem[] second = new TableItem[1];
            boolean[] widenSecond = {false};
            table.addListener(SWT.SetData, e -> {
                ((TableItem) e.item).setText(e.index == 0 ? "a" : "b");
                if (e.index == 1)
                    second[0] = (TableItem) e.item;
                if (e.index == 0 && widenSecond[0])
                    second[0].setText(LONG);
            });
            table.setItemCount(2);

            widenSecond[0] = true;
            table.clear(0);
            table.getItem(0).getText();

            assertThat(impl(table).columnlessWidth).isEqualTo(width(second[0])).isGreaterThan(width(table.getItem(0)));
        }
    }

    @Test
    @DisplayName("a columnless table measured again while a row is asked for its data is measured once that request returns")
    void aTableMeasuredInsideSetDataIsMeasuredAfter() {
        try (Mocks.Delivery delivery = Mocks.deliverEvents(shell().getDisplay())) {
            Table table = new Table(shell(), SWT.VIRTUAL);
            boolean[] removeSecond = {false};
            table.addListener(SWT.SetData, e -> {
                ((TableItem) e.item).setText(e.index == 0 ? "a" : LONG);
                if (e.index == 0 && removeSecond[0])
                    table.remove(1);
            });
            table.setItemCount(2);
            assertThat(impl(table).columnlessWidth).isEqualTo(width(table.getItem(1)));

            removeSecond[0] = true;
            table.clear(0);
            table.getItem(0).getText();

            assertThat(table.getItemCount()).isEqualTo(1);
            assertThat(impl(table).columnlessWidth).isEqualTo(width(table.getItem(0)));
        }
    }

    @Test
    @DisplayName("a SetData listener that changes another row and disposes the table leaves nothing to measure")
    void aTableDisposedInsideSetDataIsNotMeasured() {
        try (Mocks.Delivery delivery = Mocks.deliverEvents(shell().getDisplay())) {
            Table table = new Table(shell(), SWT.VIRTUAL);
            TableItem[] second = new TableItem[1];
            boolean[] dispose = {false};
            table.addListener(SWT.SetData, e -> {
                if (e.index == 1)
                    second[0] = (TableItem) e.item;
                if (e.index == 0 && dispose[0]) {
                    second[0].setText(LONG);
                    table.dispose();
                }
            });
            table.setItemCount(2);
            TableItem first = table.getItem(0);

            dispose[0] = true;
            table.clear(0);

            assertThat(impl(table).checkData(first, 0)).isFalse();
            assertThat(table.isDisposed()).isTrue();
        }
    }

    @Test
    @DisplayName("a row its SetData listener disposes is neither measured nor drawn")
    void aRowDisposedInsideSetDataIsNotMeasured() {
        try (Mocks.Delivery delivery = Mocks.deliverEvents(shell().getDisplay())) {
            Table table = new Table(shell(), SWT.VIRTUAL);
            table.setItemCount(1);
            TableItem row = table.getItem(0);
            table.addListener(SWT.SetData, e -> e.item.dispose());

            table.clear(0);

            assertThat(impl(table).checkData(row, 0)).isFalse();
            assertThat(table.getItemCount()).isZero();
            assertThat(impl(table).columnlessWidth).isZero();
        }
    }

    @Test
    @DisplayName("a columnless table measured while remove(int, int) releases its rows skips the rows already released")
    void aTableMeasuredDuringARangeRemoveSkipsReleasedRows() {
        try (Mocks.Delivery delivery = Mocks.deliverEvents(shell().getDisplay())) {
            Table table = new Table(shell(), SWT.NONE);
            new TableItem(table, SWT.NONE).setText(LONG);
            TableItem second = new TableItem(table, SWT.NONE);
            second.setText("b");
            new TableItem(table, SWT.NONE).setText("c");
            boolean[] measured = {false};
            second.addListener(SWT.Dispose, e -> {
                table.setRedraw(false);
                table.setRedraw(true);
                measured[0] = true;
            });

            table.remove(0, 1);

            assertThat(measured[0]).isTrue();
            assertThat(table.getItemCount()).isEqualTo(1);
            assertThat(impl(table).columnlessWidth).isEqualTo(width(table.getItem(0)));
        }
    }

    @Test
    @DisplayName("a columnless table with redraw off measures its rows when redraw comes back on")
    void aColumnlessTableIsMeasuredWhenRedrawComesBack() {
        Table table = new Table(shell(), SWT.NONE);
        TableColumn column = new TableColumn(table, SWT.NONE);
        new TableItem(table, SWT.NONE).setText("a");
        new TableItem(table, SWT.NONE).setText("b");

        table.setRedraw(false);
        column.dispose();
        table.getItem(1).setText(LONG);
        assertThat(impl(table).columnlessWidth).isZero();

        table.setRedraw(true);
        assertThat(impl(table).columnlessWidth).isEqualTo(width(table.getItem(1)));
    }

    @Test
    @DisplayName("a table with columns leaves the columnless width alone until its last column goes")
    void aTableWithColumnsIsMeasuredWhenItsLastColumnGoes() {
        Table table = new Table(shell(), SWT.NONE);
        TableColumn column = new TableColumn(table, SWT.NONE);
        new TableItem(table, SWT.NONE).setText("a");
        new TableItem(table, SWT.NONE).setText(LONG);
        table.remove(0);
        assertThat(impl(table).columnlessWidth).isZero();

        column.dispose();
        assertThat(impl(table).columnlessWidth).isEqualTo(width(table.getItem(0)));
    }

    private interface Measured {
        int[] measure();
    }

    private static int[] withSwtFonts(boolean on, Measured measured) {
        ConfigFlags saved = Config.getConfigFlags();
        try {
            Config.setConfigFlags(ConfigFlags.use_swt_fonts(on));
            return measured.measure();
        } finally {
            Config.setConfigFlags(saved);
        }
    }

    private static int[] rowAndHeader(Font font) {
        Table table = new Table(shell(), SWT.NONE);
        table.setHeaderVisible(true);
        if (font != null) table.setFont(font);
        return new int[] {table.getItemHeight(), table.getHeaderHeight()};
    }

    @Test
    @DisplayName("with SWT fonts on, a table's own font sets its row and header heights; with them off the theme does")
    void aTablesOwnFontSetsItsRowAndHeaderHeightsOnlyWithSwtFonts() {
        Font big = new Font(shell().getDisplay(), "Arial", 40, SWT.NORMAL);
        int[] themed = withSwtFonts(true, () -> rowAndHeader(null));
        int[] own = withSwtFonts(true, () -> rowAndHeader(big));
        int[] ignored = withSwtFonts(false, () -> rowAndHeader(big));

        assertThat(own[0]).isGreaterThan(themed[0]);
        assertThat(own[1]).isGreaterThan(themed[1]);
        assertThat(ignored).containsExactly(themed);
    }

    @Test
    @DisplayName("with SWT fonts on, a row's own font or its table's sets its text width; with them off only the style is kept")
    void aRowsOwnFontSetsItsTextWidthOnlyWithSwtFonts() {
        Font big = new Font(shell().getDisplay(), "Arial", 40, SWT.NORMAL);
        Measured widths = () -> {
            Table table = new Table(shell(), SWT.NONE);
            TableItem themed = new TableItem(table, SWT.NONE);
            TableItem own = new TableItem(table, SWT.NONE);
            TableItem empty = new TableItem(table, SWT.NONE);
            themed.setText(LONG);
            own.setText(LONG);
            own.setFont(big);
            Table bigTable = new Table(shell(), SWT.NONE);
            bigTable.setFont(big);
            TableItem inherited = new TableItem(bigTable, SWT.NONE);
            inherited.setText(LONG);
            return new int[] {TableSizes.getTextWidth(themed), TableSizes.getTextWidth(own),
                TableSizes.getTextWidth(inherited), TableSizes.getTextWidth(empty)};
        };
        int[] on = withSwtFonts(true, widths);
        int[] off = withSwtFonts(false, widths);

        assertThat(on[1]).isGreaterThan(on[0]);
        assertThat(on[2]).isEqualTo(on[1]);
        assertThat(on[3]).isZero();
        assertThat(off).containsExactly(on[0], on[0], on[0], 0);
    }
}
