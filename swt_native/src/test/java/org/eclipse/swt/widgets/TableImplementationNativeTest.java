package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
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
}
