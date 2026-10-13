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

/** What {@link DartTableItem} does when it is driven directly rather than through {@link TableItem}. */
@Tag("native-unit")
class TableItemImplementationNativeTest {

    private Table table;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        table = new Table(DartMocks.dartShell(), SWT.CHECK);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static DartTableItem impl(TableItem item) {
        return (DartTableItem) item.getImpl();
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartTableItem impl = new DartTableItem(table, SWT.NONE, null);

        TableItem api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("an item of SWT's own class passes the subclass check")
    void ownClassPassesTheSubclassCheck() {
        TableItem item = new TableItem(table, SWT.NONE);

        assertThatCode(item::checkSubclass).doesNotThrowAnyException();
        assertThatCode(impl(item)::checkSubclass).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("the implementation reports what the item holds")
    void implementationReportsWhatItHolds() {
        new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);
        TableItem item = new TableItem(table, SWT.NONE);
        Image image = new Image(table.getDisplay(), 16, 16);
        Color red = new Color(table.getDisplay(), 255, 0, 0);
        Font font = new Font(table.getDisplay(), "Arial", 12, SWT.BOLD);

        item.setText(new String[] {"a", "b"});
        item.setImage(1, image);
        item.setChecked(true);
        item.setGrayed(true);
        item.setForeground(1, red);
        item.setBackground(1, red);
        item.setFont(font);
        item.setFont(1, font);

        DartTableItem impl = impl(item);
        assertThat(impl._parent()).isSameAs(table);
        assertThat(impl._strings()).containsExactly("a", "b");
        assertThat(impl._images()[1]).isSameAs(image);
        assertThat(impl._checked()).isTrue();
        assertThat(impl._grayed()).isTrue();
        assertThat(impl._cached()).isTrue();
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
        TableItem item = new TableItem(table, SWT.NONE);
        Image first = new Image(table.getDisplay(), 16, 16);
        Image second = new Image(table.getDisplay(), 16, 16);

        impl(item).setImages(new Image[] {first, second});

        assertThat(item.getImage()).isNotNull();
        assertThat(impl(item)._images()).hasSize(2);
        impl(item).setImages(new Image[0]);
        assertThat(impl(item)._images()).isEmpty();
        impl(item).setImages(null);
        assertThat(impl(item)._images()).isNull();
    }

    @Test
    @DisplayName("an item that is not virtual is named by its text, one that has not been asked for its data by a placeholder")
    void theNameOfAnItem() {
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText("hello");
        Table virtual = new Table(DartMocks.dartShell(), SWT.VIRTUAL);
        TableItem pending = new TableItem(virtual, SWT.NONE);

        assertThat(item.toString()).isEqualTo("TableItem {hello}");
        assertThat(pending.toString()).isEqualTo("TableItem {*virtual*}");
    }

    @Test
    @DisplayName("the one cell of a table without columns is as wide as its content on Cocoa; elsewhere it fills the table, and holds its content where the table has no room")
    void theCellOfAColumnlessTable() {
        Table plain = new Table(DartMocks.dartShell(), SWT.NONE);
        TableItem item = new TableItem(plain, SWT.NONE);
        item.setText("hello");

        assertThat(plain.getClientArea().width).isZero();
        assertThat(item.getBounds(0).width).isPositive();
        assertThat(item.getBounds().width).isEqualTo(item.getBounds(0).width);

        Image image = new Image(plain.getDisplay(), 16, 16);
        int withoutImage = item.getBounds(0).width;
        item.setImage(image);
        assertThat(item.getBounds(0).width).isGreaterThan(withoutImage);

        plain.setBounds(0, 0, 400, 300);
        assertThat(item.getBounds(0).width).as("stretched across the table, as on Win32 and GTK").isGreaterThan(300);
        assertThat(item.getBounds().width).isLessThan(item.getBounds(0).width);
    }
}
