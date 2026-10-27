package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/** What {@link DartTreeItem} does when it is driven directly rather than through {@link TreeItem}. */
@Tag("native-unit")
class TreeItemImplementationNativeTest {

    private Tree tree;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        tree = new Tree(DartMocks.dartShell(), SWT.CHECK);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static DartTreeItem impl(TreeItem item) {
        return (DartTreeItem) item.getImpl();
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartTreeItem impl = new DartTreeItem(tree, SWT.NONE, null);

        TreeItem api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        assertThat(tree.getItem(0)).isSameAs(api);
    }

    @Test
    @DisplayName("an item of SWT's own class passes the subclass check")
    void ownClassPassesTheSubclassCheck() {
        TreeItem item = new TreeItem(tree, SWT.NONE);

        assertThatCode(item::checkSubclass).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("the implementation reports what the item holds")
    void implementationReportsWhatItHolds() {
        new TreeColumn(tree, SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        TreeItem child = new TreeItem(item, SWT.NONE);
        Image image = new Image(tree.getDisplay(), 16, 16);
        Color red = new Color(tree.getDisplay(), 255, 0, 0);
        Font font = new Font(tree.getDisplay(), "Arial", 12, SWT.BOLD);

        item.setText(new String[] {"a", "b"});
        item.setImage(1, image);
        item.setChecked(true);
        item.setGrayed(true);
        item.setExpanded(true);
        item.setForeground(red);
        item.setBackground(red);
        item.setForeground(1, red);
        item.setBackground(1, red);
        item.setFont(font);
        item.setFont(1, font);

        DartTreeItem impl = impl(item);
        assertThat(impl._parent()).isSameAs(tree);
        assertThat(impl._parentItem()).isNull();
        assertThat(impl(child)._parentItem()).isSameAs(item);
        assertThat(impl._items()[0]).isSameAs(child);
        assertThat(impl._itemCount()).isEqualTo(1);
        assertThat(impl._strings()).containsExactly("a", "b");
        assertThat(impl._images()[1]).isSameAs(image);
        assertThat(impl._checked()).isTrue();
        assertThat(impl._grayed()).isTrue();
        assertThat(impl._cached()).isTrue();
        assertThat(impl._expanded()).isTrue();
        assertThat(impl._foreground()).isEqualTo(red);
        assertThat(impl._background()).isEqualTo(red);
        assertThat(impl._cellForeground()[1]).isEqualTo(red);
        assertThat(impl._cellBackground()[1]).isEqualTo(red);
        assertThat(impl._font()).isEqualTo(font);
        assertThat(impl.getExplicitFont()).isEqualTo(font);
        assertThat(impl._cellFont()[1]).isEqualTo(font);
        assertThat(impl._width()).isEqualTo(-1);
    }

    @Test
    @DisplayName("images set on the value replace the item's, the first one as its image")
    void imagesSetOnTheValueReplaceTheItems() {
        TreeItem item = new TreeItem(tree, SWT.NONE);
        Image first = new Image(tree.getDisplay(), 16, 16);
        Image second = new Image(tree.getDisplay(), 16, 16);

        impl(item).setImages(new Image[] {first, second});

        assertThat(item.getImage()).isSameAs(first);
        assertThat(impl(item)._images()).containsExactly(first, second);
    }
}
