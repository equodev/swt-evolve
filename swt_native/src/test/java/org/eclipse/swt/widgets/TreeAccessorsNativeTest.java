package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/** The state {@link DartTree} answers through its accessors; Cocoa's Tree has had all of these fields since 3.126. */
@Tag("native-unit")
class TreeAccessorsNativeTest extends SerializeTestBase {

    private static TreeItem[] roots(Tree tree, int count) {
        TreeItem[] items = new TreeItem[count];
        for (int i = 0; i < count; i++) {
            items[i] = new TreeItem(tree, SWT.NONE);
            items[i].setText("item " + i);
        }
        return items;
    }

    private static DartTree impl(Tree tree) {
        return (DartTree) tree.getImpl();
    }

    @Test
    @DisplayName("the implementation answers its state through its accessors")
    void accessorsAnswerTheState() {
        Tree tree = new Tree(shell(), SWT.MULTI);
        TreeItem[] items = roots(tree, 2);
        TreeColumn column = new TreeColumn(tree, SWT.NONE);
        tree.setSortColumn(column);
        tree.setSortDirection(SWT.UP);
        tree.setSelection(items[1]);
        tree.setInsertMark(items[0], true);
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        tree.setTopItem(items[1]);
        ImplTree impl = impl(tree);
        DartTree dart = impl(tree);

        assertThat(impl._items()).startsWith(items);
        assertThat(impl._itemCount()).isEqualTo(2);
        assertThat(impl._columns()).startsWith(column);
        assertThat(impl._columnCount()).isEqualTo(1);
        assertThat(impl._sortColumn()).isSameAs(column);
        assertThat(impl._sortDirection()).isEqualTo(SWT.UP);
        assertThat(impl._selectedRowIndex()).isEqualTo(-1);
        assertThat(impl._ignoreExpand()).isFalse();
        assertThat(impl._ignoreSelect()).isFalse();
        assertThat(impl._ignoreRedraw()).isFalse();
        assertThat(impl._reloadPending()).isFalse();
        assertThat(impl._drawExpansion()).isFalse();
        assertThat(impl._didSelect()).isFalse();
        assertThat(impl._preventSelect()).isFalse();
        assertThat(impl._dragDetected()).isFalse();
        assertThat(impl._imageBounds()).isNull();
        assertThat(impl._insertItem()).isSameAs(items[0]);
        assertThat(impl._insertBefore()).isTrue();
        assertThat(impl._shouldExpand()).isTrue();
        assertThat(impl._shouldScroll()).isTrue();
        assertThat(impl._keyDown()).isFalse();
        assertThat(impl.isTransparent()).isTrue();
        impl.updateCursorRects(true);
        assertThat(dart._columnOrder()).isEmpty();
        assertThat(dart._editors()).isEmpty();
        assertThat(dart.__headerBackground()).isNull();
        assertThat(dart.__headerForeground()).isNull();
        assertThat(dart._headerVisible()).isTrue();
        assertThat(dart._linesVisible()).isTrue();
        assertThat(dart._selection()).containsExactly(items[1]);
        assertThat(dart._topItem()).isSameAs(items[1]);
    }
}
